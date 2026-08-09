# DrinkDiary Database Design

## 1. 문서 목적

이 문서는 `usecase.md`를 기준으로 DrinkDiary의 로컬 데이터베이스 구조와 조회 제약을 정의한다.
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
| tastingNote | String | Yes | 테이스팅 메모(자유 서술) |
| tastingTags | String | No | 테이스팅 태그 키 목록, 구분자 결합 문자열 — 아래 3.1절 |
| rating | Double | No | 별점 0.5~5.0, **0.1 단위** — 아래 3.3절 |
| abv | Double | Yes | 도수(%). NULL은 "미입력"이며 주종 기본값으로 해석 — 아래 3.2절 |
| volumeMl | Int | Yes | 용량(ml). NULL 해석은 `abv`와 동일 |
| collectionStatus | String | No | `Normal`, `Repurchase`, `NotForMe` 중 하나 |
| recordedAtMillis | Long | No | 기록 기준 시간 |
| createdAtMillis | Long | No | 생성 시간 |
| updatedAtMillis | Long | No | 마지막 수정 시간 |

### 3.1 테이스팅 태그 저장 포맷

태그는 별도 테이블이 아니라 `drink_records`의 컬럼 하나에 구분자로 결합해 저장한다. 기존 단일 테이블 구조와 UseCase에서 Kotlin으로 집계하는 관례(6절 참고)를 유지하기 위한 선택이며, 개인 기록 앱 규모(수천 건)에서는 메모리 집계로 충분하다.

**포맷**: 파이프(`|`)로 결합하되 **앞뒤에도 붙인다**. 예: `|citrus|oak|dry|`. 태그가 없으면 빈 문자열(`''`)이며 `'||'`가 아니다.

앞뒤 파이프가 있어야 태그 필터를 `LIKE '%|oak|%'`로 걸었을 때 `oaky` 같은 더 긴 키에 잘못 매칭되지 않는다. 자유 입력 태그에 사용자가 `|`를 넣으면 행 전체의 태그 목록이 깨지므로, 변환 지점인 `DrinkRecordMapper.toTagStorageValue()`에서 제거한다.

**저장되는 값은 한국어 라벨이 아니라 안정적인 ASCII 키다**(`citrus`이지 `시트러스`가 아니다). 라벨은 `domain/model/TastingTag.kt`의 카탈로그가 UI 계층에서만 매핑하므로, 칩 문구를 다듬어도 DB와 기존 기록에 영향이 없다. 반대로 **키를 바꾸면 그 키로 저장된 모든 기록이 고아가 되므로 키는 변경하지 않는다**.

여러 주종에 공통으로 나오는 어휘는 같은 키를 공유한다(와인·위스키·맥주의 시트러스가 모두 `citrus`). 주종을 가로지르는 취향 프로파일 집계가 여기에 의존한다.

### 3.2 도수·용량의 NULL 의미

`abv`와 `volumeMl`의 NULL은 "값 없음"이 아니라 **"사용자가 입력하지 않음 → 주종 기본값으로 추정"**을 뜻한다. 에디터는 주종 기본값을 화면에 보여주되 사용자가 건드리지 않으면 저장하지 않는다. 읽을 때는 `DrinkRecord.effectiveAbv` / `effectiveVolumeMl`이 `DrinkType.defaultAbv()` / `defaultVolumeMl()`로 대체하고, `isIntakeEstimated`가 추정 여부를 알려준다.

플래그 컬럼을 따로 두지 않아 컬럼이 하나 줄고, 대신 **기본값을 나중에 바꾸면 미입력 기록의 추정치가 함께 바뀐다**. 추정치이므로 의도된 동작으로 본다.

### 3.3 별점 단위 (0.5 → 0.1)

별점은 0.5~5.0을 **0.1 단위**로 받는다. 컬럼은 처음부터 `REAL`이라 **마이그레이션이 필요 없었고**, 기존 0.5 단위 값은 0.1의 배수라 그대로 유효하다. 바뀐 것은 `domain/model/DrinkRating.kt`의 검증 규칙뿐이다.

검증에서 `(rating * 10).rem(1.0) == 0.0` 같은 형태는 쓰지 않는다. 10을 곱하면 이진 부동소수 오차가 되살아나(`4.3 * 10`은 `43.000000000000007`) 슬라이더가 정상적으로 만든 값을 거부한다. `roundToStep()`으로 반올림한 값과 허용 오차로 비교한다.

슬라이더는 연속 실수를 주므로 **저장·검증 전에 `roundToStep()`으로 소수점 1자리로 스냅**한다. 그러지 않으면 `4.300000000000001`이 DB에 들어가고 이후 모든 비교가 같은 허용 오차를 떠안게 된다.

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
| rating | 0.5~5.0, 0.1 단위. 0.0은 '미평가'라 저장 불가 |
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

## 9. 마이그레이션 이력과 고려사항

### 이력

| 버전 | 변경 내용 |
| --- | --- |
| 1 | 초기 스키마 |
| 2 | 소수점 별점 지원(`rating`을 정수에서 `Double`로), 세부 평가 4개 컬럼(`detailRating1..4`) 추가. 기존 행은 네 값을 모두 `rating`으로 채움 |
| 3 | `tastingTags`·`abv`·`volumeMl` 추가, `detailRating1..4` 제거 |

### v2 → v3 상세

세부 평가 별점을 테이스팅 태그로 대체하면서 `detailRating1..4`를 **버렸다**. 숫자를 맛 표현으로 옮길 정직한 규칙이 없고("산도 3.5"는 새콤함도 시큼함도 아니다), v2 마이그레이션이 네 값을 모두 대표 별점의 복사본으로 채웠으며 상세 화면도 대표 별점과 다를 때만 표시했으므로 대부분의 행에서 실제 정보가 없었다. 이 시점에 앱은 미출시 상태였다.

`MIGRATION_1_2`와 달리 테이블을 재생성하지 않고 `ALTER TABLE ... ADD/DROP COLUMN`을 쓴다. `minSdk = 35`가 SQLite 3.44를 보장해 `DROP COLUMN`(3.35+)을 쓸 수 있고, 삭제 대상 컬럼이 어느 인덱스에도 속하지 않아 제약에 걸리지 않으며, 기존 인덱스 5개를 다시 만들 필요도 없다.

`NOT NULL` 컬럼을 기존 행이 있는 테이블에 추가하려면 SQLite가 `DEFAULT`를 요구하므로 `tastingTags`는 `DEFAULT ''`로 추가한다. 엔티티가 `@ColumnInfo(defaultValue=...)`를 선언하지 않으면 Room의 스키마 검증은 기본값을 비교하지 않으므로 불일치로 처리되지 않는다.

### 앞으로 마이그레이션이 필요한 변경

- 사진을 여러 장으로 확장하는 경우 별도 `drink_record_images` 테이블 추가
- 컬렉션 상태를 3개 이상으로 확장하는 경우 enum 매핑과 필터 UI 변경
- 장소를 지도 기반 위치로 확장하는 경우 위도, 경도 컬럼 추가
- 태그 집계가 메모리로 감당되지 않을 만큼 기록이 늘면 `drink_record_tags` 관계형 테이블로 전환

스키마 변경 시 기존 사용자의 기록이 손실되지 않도록 Room Migration을 작성해야 한다.

### 마이그레이션 검증 방법

앱을 지우고 새로 설치하면 마이그레이션이 **실행되지 않는다**(새 DB가 최신 버전으로 생성될 뿐이다). 반드시 이전 버전 데이터가 있는 상태에 덮어 설치해서 확인한다.

```
adb install -r app/build/outputs/apk/debug/app-debug.apk   # 지우지 말 것
adb shell am force-stop com.bluemarlin.drinkdiary && adb shell am start -n com.bluemarlin.drinkdiary/.MainActivity
adb logcat -b crash -d          # 마이그레이션 실패는 여기서 IllegalStateException으로 드러난다
adb shell "run-as com.bluemarlin.drinkdiary od -An -tx1 -j60 -N4 <db경로>"   # user_version 확인
```

DB 파일을 꺼내 볼 때는 **`-wal`과 `-shm`까지 함께 복사**해야 한다. WAL 모드라 방금 저장한 행은 아직 본 파일에 없고, `.db`만 복사하면 최신 기록이 사라진 것처럼 보인다.

## 10. 사진 저장 정책

DB에는 이미지 바이너리를 저장하지 않고 `imageUri`만 저장한다.
이유: 대용량 비트맵을 DB에 직접 저장하면 DB 크기와 메모리 사용량이 커지고 목록 스크롤 성능에 영향을 줄 수 있다.

사진 파일 접근 권한이 사라지거나 URI가 유효하지 않으면 UI는 이미지 영역에 대체 상태를 표시하고 기록 자체는 유지한다.
