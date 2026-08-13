# DrinkDiary Database Design

> [!WARNING]
> **보관 문서 — 살아있는 명세가 아니다.** 구 MVP의 DB 스키마이며, 현재 저장소의 코드를 설명하지만
> 재정의된 제품 방향과 충돌한다(맥주 포함, 5축 슬라이더 입력 등).
> 현행 명세는 `../../specs/`를 따른다 — 제품은 `specs/planner/prd.md`,
> 구조는 `specs/developer/software-architecture.md`.
> 재작성 시 참고용으로만 남긴다. **여기에 새 내용을 추가하지 않는다.**


## 1. 문서 목적

이 문서는 `../planner/usecase.md`를 기준으로 DrinkDiary의 로컬 데이터베이스 구조와 조회 제약을 정의한다.
현재 앱은 서버 동기화 없이 로컬 우선으로 동작하므로 Room 기반 SQLite 저장소를 사용한다.

## 2. 데이터베이스 선택

Room을 사용한다.

선택 이유:

- 주류 기록은 구조화된 데이터이다.
- 주류 종류, 컬렉션 상태, 기간 기준 필터링이 필요하다.
- 대시보드에서 기간별 집계가 필요하다.
- Kotlin Flow와 연동해 UI 상태를 반응형으로 갱신하기 쉽다.

## 3. 주요 Entity

### DrinkRecordEntity

| 컬럼 | 타입 | Null 허용 | 설명 |
| --- | --- | --- | --- |
| id | Long | No | Primary Key, auto generate |
| type | String | No | `Wine`, `Whiskey`, `Beer` 중 하나 |
| name | String | No | 주류 이름 |
| imageUri | String | Yes | 사진 URI |
| price | Long | Yes | 구매 또는 소비 가격 |
| place | String | Yes | 마신 장소 또는 구매 장소 |
| tastingNote | String | Yes | 테이스팅 메모 |
| rating | Int | No | 별점 |
| collectionStatus | String | No | `Normal`, `Repurchase`, `NotForMe` 중 하나 |
| recordedAtMillis | Long | No | 기록 기준 시간 |
| createdAtMillis | Long | No | 생성 시간 |
| updatedAtMillis | Long | No | 마지막 수정 시간 |

## 4. Kotlin Entity 예시

```kotlin
@Entity(
    tableName = "drink_records",
    indices = [
        Index(value = ["recordedAtMillis"]),
        Index(value = ["type"]),
        Index(value = ["collectionStatus"]),
        Index(value = ["type", "collectionStatus"]),
        Index(value = ["recordedAtMillis", "collectionStatus"])
    ]
)
data class DrinkRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val type: String,
    val name: String,
    val imageUri: String?,
    val price: Long?,
    val place: String?,
    val tastingNote: String?,
    val rating: Int,
    val collectionStatus: String,
    val recordedAtMillis: Long,
    val createdAtMillis: Long,
    val updatedAtMillis: Long
)
```

Enum은 DB에 문자열로 저장하는 것을 권장한다.
이유: 값의 의미를 DB에서 확인하기 쉽고, enum 순서 변경에 영향을 받지 않는다.

## 5. 필드 제약

| 항목 | 제약 |
| --- | --- |
| type | 와인, 위스키, 맥주만 허용 |
| name | 공백만 있는 값 저장 금지 |
| rating | 1~5 범위 권장 |
| collectionStatus | 일반 기록, 재구매 후보, 비선호만 허용 |
| price | 선택값, 입력 시 0 이상 |
| imageUri | 선택값, URI 문자열만 저장 |
| recordedAtMillis | 기간별 조회 기준값으로 필수 |

Room 자체 제약만으로 모든 앱 규칙을 보장하지 않고 UseCase에서 함께 검증한다.
이유: 사용자 입력 오류를 DB 예외가 아니라 UI에서 친화적으로 처리하기 위함이다.

## 6. DAO 설계

```kotlin
@Dao
interface DrinkRecordDao {
    @Query(
        """
        SELECT * FROM drink_records
        WHERE (:type IS NULL OR type = :type)
          AND (:collectionStatus IS NULL OR collectionStatus = :collectionStatus)
        ORDER BY recordedAtMillis DESC
        """
    )
    fun observeRecords(
        type: String?,
        collectionStatus: String?
    ): Flow<List<DrinkRecordEntity>>

    @Query("SELECT * FROM drink_records WHERE id = :id")
    fun observeRecord(id: Long): Flow<DrinkRecordEntity?>

    @Query(
        """
        SELECT * FROM drink_records
        WHERE recordedAtMillis BETWEEN :startMillis AND :endMillis
        ORDER BY recordedAtMillis DESC
        """
    )
    fun observeRecordsByPeriod(
        startMillis: Long,
        endMillis: Long
    ): Flow<List<DrinkRecordEntity>>

    @Upsert
    suspend fun upsert(record: DrinkRecordEntity): Long

    @Query("DELETE FROM drink_records WHERE id = :id")
    suspend fun deleteById(id: Long): Int
}
```

초기 구현에서는 기간별 대시보드 집계를 UseCase에서 계산한다.
기록 수가 증가하면 `COUNT`, `AVG`, `GROUP BY` 기반 DAO 쿼리를 추가한다.

## 7. 주요 조회 시나리오

### 유스케이스별 DB 처리

| Use Case | DB 처리 | 제약 |
| --- | --- | --- |
| UC-01 주류 기록 등록 | `upsert` 또는 `insert`로 신규 row 저장 | 필수값 검증 후 저장, 사진은 URI만 저장 |
| UC-02 주류 컬렉션 조회 | 전체 목록 최신순 조회 | 삭제된 기록은 조회되지 않음 |
| UC-03 주류 기록 수정 | 동일 `id` row 업데이트 | 대상 기록이 없으면 실패 처리 |
| UC-04 주류 기록 삭제 | `id` 기준 삭제 | 삭제 후 목록과 대시보드 Flow가 갱신되어야 함 |
| UC-05 기간별 대시보드 조회 | `recordedAtMillis` 기간 조건 조회 | 기간 계산은 앱 계층에서 수행 |
| UC-06 주류 종류별 기록 확인 | `type` 조건 조회 | 와인, 위스키, 맥주 외 값은 저장하지 않음 |
| UC-07 재구매 후보 확인 | `collectionStatus = Repurchase` 조건 조회 | 별점과 독립적으로 분류 |
| UC-08 비선호 술 확인 | `collectionStatus = NotForMe` 조건 조회 | 별점과 독립적으로 분류 |

### 컬렉션 전체 조회

```text
type = null
collectionStatus = null
ORDER BY recordedAtMillis DESC
```

UC-02를 지원한다.

### 주류 종류별 조회

```text
type = Wine | Whiskey | Beer
collectionStatus = null
```

UC-06을 지원한다.

### 재구매 후보 조회

```text
type = null 또는 특정 주류 종류
collectionStatus = Repurchase
```

UC-07을 지원한다.

### 비선호 술 조회

```text
type = null 또는 특정 주류 종류
collectionStatus = NotForMe
```

UC-08을 지원한다.

### 기간별 대시보드 조회

```text
recordedAtMillis BETWEEN startMillis AND endMillis
```

UC-05를 지원한다.
조회된 목록에서 기록 수, 평균 별점, 종류별 수, 재구매 후보 수, 비선호 수를 계산한다.

## 8. 인덱스 전략

| 인덱스 | 목적 |
| --- | --- |
| recordedAtMillis | 최신순 목록과 기간별 대시보드 조회 최적화 |
| type | 주류 종류별 필터 조회 최적화 |
| collectionStatus | 재구매 후보와 비선호 필터 조회 최적화 |
| type + collectionStatus | 복합 필터 조회 최적화 |
| recordedAtMillis + collectionStatus | 기간 내 재구매/비선호 조회 최적화 |

## 9. 마이그레이션 고려사항

초기 버전은 `version = 1`로 시작한다.
다음 변경은 DB 마이그레이션이 필요하다.

- 사진을 여러 장으로 확장하는 경우 별도 `drink_record_images` 테이블 추가
- 별점 범위를 변경하거나 소수점 별점을 지원하는 경우 `rating` 타입 또는 해석 변경
- 컬렉션 상태를 3개 이상으로 확장하는 경우 enum 매핑과 필터 UI 변경
- 장소를 지도 기반 위치로 확장하는 경우 위도, 경도 컬럼 추가

스키마 변경 시 기존 사용자의 기록이 손실되지 않도록 Room Migration을 작성해야 한다.

## 10. 사진 저장 정책

DB에는 이미지 바이너리를 저장하지 않고 `imageUri`만 저장한다.
이유: 대용량 비트맵을 DB에 직접 저장하면 DB 크기와 메모리 사용량이 커지고 목록 스크롤 성능에 영향을 줄 수 있다.

사진 파일 접근 권한이 사라지거나 URI가 유효하지 않으면 UI는 이미지 영역에 대체 상태를 표시하고 기록 자체는 유지한다.
