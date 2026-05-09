# DrinkDiary Search Tab Use Case and Red Case

## 1. 문서 목적

이 문서는 DrinkDiary Android 앱에 신규 `검색` 탭을 추가하기 위한 요구사항, Use Case, Red Case, 구현 고려사항을 정의한다.

실제 코드 구현 전 기준 문서로 사용하며, 화면 구조와 데이터 조회 정책이 기존 `Dashboard`, `Collection` 탭과 일관되도록 한다.

## 2. 기능 범위

### 포함 범위

- 홈/목록 이외에 `검색` 탭을 추가한다.
- 검색 탭은 사용자가 입력한 단어를 기준으로 저장된 주류 기록을 검색한다.
- 검색 대상 필드는 아래 3개이다.
  - 이름
  - 장소
  - 테이스팅 노트
- 검색어는 2글자 이상일 때만 유효하다.
- 검색 결과는 검색 창 하단에 리스트로 표시한다.
- 검색 결과 항목 선택 시 기존 기록 상세 화면으로 이동한다.

## 3. 주요 화면 변경

### Top Level Navigation

기존 최상위 탭:

```text
Dashboard
Collection
```

변경 후 최상위 탭:

```text
Dashboard
Collection
Search
```

검색 탭은 Dashboard, Collection과 같은 top-level destination이다.

권장 정책:

- Compact width: Bottom Navigation에 `검색` 항목 추가
- Expanded width: Navigation Rail에 `검색` 항목 추가
- Detail, Editor 화면에서는 기존처럼 bottom navigation 또는 navigation rail을 숨김

## 4. Search Screen 기본 구성

검색 화면은 아래 구조를 가진다.

```text
SearchScreen
  -> DDScreenScaffold(title = "검색", screenType = TopLevel, selectedTab = Search)
  -> Search text field
  -> Search state content
     -> Idle / InvalidQuery / Loading / Empty / Success / Error
  -> Result list
```

검색 창은 화면 상단에 배치하고, 검색 결과 리스트는 검색 창 하단에 표시한다.

## 5. 검색 입력 규칙

| 항목 | 규칙 |
| --- | --- |
| 최소 길이 | 공백을 제거한 검색어 기준 2글자 이상 |
| 공백 처리 | 앞뒤 공백은 제거한다 |
| 빈 문자열 | 검색하지 않는다 |
| 1글자 검색어 | 검색하지 않고 안내 상태를 표시한다 |
| 대소문자 | 영문 검색 시 대소문자를 구분하지 않는 것을 권장한다 |
| 한글 | 입력된 문자열 포함 여부를 기준으로 검색한다 |
| 내부 공백 | 사용자가 입력한 그대로 유지한다 |
| 특수 검색 문자 | `%`, `_` 같은 SQLite LIKE wildcard 문자는 일반 문자로 검색되도록 escape 처리를 권장한다 |

입력값 예시:

| 입력값 | 처리 |
| --- | --- |
| `""` | Idle 상태 |
| `" "` | Idle 상태 |
| `"맥"` | InvalidQuery 상태 |
| `"맥주"` | 검색 실행 |
| `" CU "` | `"CU"`로 검색 실행 |

검색 실행 방식:

- 검색어가 2글자 이상이면 입력 변경에 따라 자동으로 검색한다.
- 빠른 연속 입력으로 인한 불필요한 조회를 줄이기 위해 ViewModel에서 `debounce` 적용을 권장한다.
- 동일한 검색어 반복 입력은 `distinctUntilChanged`로 중복 조회를 피한다.
- 검색창에 값이 있을 때 clear 버튼을 표시한다.
- clear 버튼을 누르면 검색어를 비우고 `Idle` 상태로 돌아간다.

## 6. 검색 대상

검색은 저장된 `DrinkRecord`의 아래 필드에서 수행한다.

| 필드 | Null 허용 | 검색 방식 |
| --- | --- | --- |
| name | No | 검색어 포함 |
| place | Yes | 값이 있을 때 검색어 포함 |
| tastingNote | Yes | 값이 있을 때 검색어 포함 |

검색어가 세 필드 중 하나라도 포함되면 결과에 포함한다.

예:

```text
query = "CU"

name contains "CU"
OR place contains "CU"
OR tastingNote contains "CU"
```

## 7. 결과 표시 규칙

검색 결과는 검색 창 하단에 리스트로 표시한다.

권장 UI:

- 기존 `DDDrinkRecordListItem` 재사용
- 기록 이름, 주류 종류, 별점, 컬렉션 상태, 기록 일시 표시
- 결과는 최신 기록 순으로 표시
- 결과 항목 선택 시 `RecordDetailScreen(recordId)`로 이동

결과 개수가 많을 수 있으므로 `LazyColumn`을 사용한다.

## 8. UI State

### SearchUiState

| 상태 | 설명 |
| --- | --- |
| Idle | 검색어가 비어 있어 아직 검색하지 않은 상태 |
| InvalidQuery | 검색어가 1글자라 검색할 수 없는 상태 |
| Loading | 검색 결과 조회 중 |
| Empty | 유효한 검색어로 조회했지만 결과가 없음 |
| Success | 검색 결과가 있음 |
| Error | 검색 중 오류 발생 |

권장 모델:

```kotlin
sealed interface SearchUiState {
    data object Idle : SearchUiState
    data class InvalidQuery(val query: String) : SearchUiState
    data object Loading : SearchUiState
    data class Empty(val query: String) : SearchUiState
    data class Success(val query: String, val records: List<DrinkRecord>) : SearchUiState
    data class Error(val message: String) : SearchUiState
}
```

`InvalidQuery`가 query를 포함하는 이유:

- UI가 현재 입력값 기준으로 안내 메시지를 안정적으로 표시할 수 있다.
- 검색어가 빠르게 바뀌는 중에도 오래된 invalid 상태와 현재 입력값이 섞이지 않는다.

## 9. Use Case

### UC-S01. 검색 탭 진입

| 항목 | 내용 |
| --- | --- |
| 목적 | 사용자가 저장된 주류 기록을 검색할 수 있는 화면에 진입한다. |
| 액터 | 사용자 |
| 사전 조건 | 앱이 실행되어 있어야 한다. |
| 사후 조건 | 검색 화면이 표시된다. |

#### 기본 흐름

1. 사용자가 하단 내비게이션 또는 Navigation Rail에서 `검색`을 선택한다.
2. 앱은 `SearchScreen`을 표시한다.
3. 검색어가 비어 있으면 검색 입력 안내 상태를 표시한다.

### UC-S02. 유효한 검색어로 기록 검색

| 항목 | 내용 |
| --- | --- |
| 목적 | 사용자가 입력한 단어가 포함된 기록을 찾는다. |
| 액터 | 사용자 |
| 사전 조건 | 저장된 기록이 있거나 빈 결과를 표시할 수 있어야 한다. |
| 사후 조건 | 검색 결과 리스트 또는 빈 상태가 표시된다. |

#### 기본 흐름

1. 사용자가 검색 창에 2글자 이상의 단어를 입력한다.
2. 앱은 앞뒤 공백을 제거한 검색어를 만든다.
3. 앱은 이름, 장소, 테이스팅 노트에 검색어가 포함된 기록을 조회한다.
4. 앱은 검색 결과를 최신 기록 순 리스트로 표시한다.
5. 사용자가 결과 항목을 선택한다.
6. 앱은 선택한 기록의 상세 화면으로 이동한다.
7. 상세 화면에서 뒤로가기를 선택하면 검색어와 검색 결과 상태를 유지한 검색 화면으로 돌아온다.

### UC-S03. 검색어 수정

| 항목 | 내용 |
| --- | --- |
| 목적 | 사용자가 검색어를 변경하면 결과를 갱신한다. |
| 액터 | 사용자 |
| 사전 조건 | 검색 화면이 표시되어 있어야 한다. |
| 사후 조건 | 현재 검색어 기준 상태가 표시된다. |

#### 기본 흐름

1. 사용자가 검색어를 수정한다.
2. 앱은 수정된 검색어를 기준으로 유효성을 다시 판단한다.
3. 검색어가 2글자 이상이면 결과를 갱신한다.
4. 검색어가 비어 있거나 1글자이면 검색 결과를 표시하지 않는다.

### UC-S04. 검색어 초기화

| 항목 | 내용 |
| --- | --- |
| 목적 | 사용자가 현재 검색어와 결과를 빠르게 초기화한다. |
| 액터 | 사용자 |
| 사전 조건 | 검색 창에 입력값이 있어야 한다. |
| 사후 조건 | 검색어가 비워지고 Idle 상태가 표시된다. |

#### 기본 흐름

1. 사용자가 검색 창의 clear 버튼을 선택한다.
2. 앱은 검색어를 빈 문자열로 변경한다.
3. 앱은 검색 결과 리스트를 숨긴다.
4. 앱은 검색 입력 안내 상태를 표시한다.

## 10. Red Case

### RC-S01. 검색어가 비어 있음

| 항목 | 내용 |
| --- | --- |
| 상황 | 사용자가 검색 탭에 진입했거나 검색어를 모두 지움 |
| 기대 동작 | DB 검색을 실행하지 않는다. 입력 안내 상태를 표시한다. |
| 사용자 메시지 예시 | `검색어를 입력해 주세요.` |

### RC-S02. 검색어가 1글자임

| 항목 | 내용 |
| --- | --- |
| 상황 | 사용자가 공백 제거 기준 1글자만 입력 |
| 기대 동작 | DB 검색을 실행하지 않는다. 검색어가 2글자 이상이어야 함을 안내한다. |
| 사용자 메시지 예시 | `검색어는 2글자 이상 입력해 주세요.` |

### RC-S03. 앞뒤 공백만 있는 입력

| 항목 | 내용 |
| --- | --- |
| 상황 | 사용자가 `"   "`처럼 공백만 입력 |
| 기대 동작 | trim 후 빈 검색어로 처리한다. DB 검색을 실행하지 않는다. |
| 사용자 메시지 예시 | `검색어를 입력해 주세요.` |

### RC-S04. 검색 결과 없음

| 항목 | 내용 |
| --- | --- |
| 상황 | 유효한 검색어로 조회했지만 일치 기록이 없음 |
| 기대 동작 | 빈 리스트 대신 Empty 상태를 표시한다. |
| 사용자 메시지 예시 | `검색 결과가 없습니다.` |

### RC-S05. 저장된 기록이 없음

| 항목 | 내용 |
| --- | --- |
| 상황 | 앱에 저장된 기록이 없는 상태에서 검색 |
| 기대 동작 | 검색 결과 없음 상태를 표시한다. 필요 시 기록 추가 동선을 제공한다. |
| 사용자 메시지 예시 | `아직 검색할 기록이 없습니다.` |

### RC-S06. 장소 또는 테이스팅 노트가 null

| 항목 | 내용 |
| --- | --- |
| 상황 | 일부 기록의 optional 필드가 null |
| 기대 동작 | null 필드는 검색 대상에서 제외하고, 다른 필드 검색은 정상 수행한다. |
| 구현 주의 | null 값을 빈 문자열로 안전하게 처리한다. |

### RC-S07. 검색 중 DB 오류 발생

| 항목 | 내용 |
| --- | --- |
| 상황 | Room 조회 중 예외 발생 |
| 기대 동작 | 앱이 종료되지 않고 Error 상태를 표시한다. |
| 사용자 메시지 예시 | `검색하지 못했습니다. 다시 시도해 주세요.` |

### RC-S08. 빠른 입력 변경

| 항목 | 내용 |
| --- | --- |
| 상황 | 사용자가 검색어를 빠르게 연속 입력 |
| 기대 동작 | 최신 검색어 기준 결과만 표시한다. |
| 구현 주의 | ViewModel에서 `debounce`, `distinctUntilChanged`, `flatMapLatest` 사용을 검토한다. |

### RC-S09. 검색 결과 항목이 삭제됨

| 항목 | 내용 |
| --- | --- |
| 상황 | 검색 결과에 있던 기록이 다른 흐름에서 삭제됨 |
| 기대 동작 | Flow 기반 조회 결과에서 자동으로 제외된다. 상세 진입 시 없으면 기존 NotFound 처리를 사용한다. |

### RC-S10. 검색어에 LIKE wildcard 문자가 포함됨

| 항목 | 내용 |
| --- | --- |
| 상황 | 사용자가 `%`, `_` 같은 SQLite LIKE wildcard 문자를 입력 |
| 기대 동작 | wildcard 패턴 검색이 아니라 일반 문자 검색으로 처리한다. |
| 구현 주의 | DAO 호출 전 검색어를 escape 하거나, DAO 쿼리에서 `ESCAPE` 절 사용을 검토한다. |

### RC-S11. 상세 화면 이동 후 뒤로가기

| 항목 | 내용 |
| --- | --- |
| 상황 | 검색 결과 항목을 선택해 상세 화면으로 이동한 뒤 뒤로가기 |
| 기대 동작 | 검색 탭으로 돌아왔을 때 검색어와 결과 리스트를 유지한다. |
| 구현 주의 | SearchRoute의 ViewModel 인스턴스가 back stack에서 유지되도록 Navigation 구성을 확인한다. |

### RC-S12. 검색 중 탭 전환 후 복귀

| 항목 | 내용 |
| --- | --- |
| 상황 | 사용자가 검색어를 입력한 상태에서 Dashboard 또는 Collection 탭으로 이동 후 다시 검색 탭으로 복귀 |
| 기대 동작 | 가능하면 검색어와 결과 상태를 유지한다. 단, 앱 프로세스 종료 후 복원은 별도 저장 정책이 없으면 필수로 보지 않는다. |

### RC-S13. 검색 결과가 많음

| 항목 | 내용 |
| --- | --- |
| 상황 | 검색 결과가 많은 경우 |
| 기대 동작 | 스크롤 성능이 유지되어야 한다. |
| 구현 주의 | `LazyColumn`에 stable key로 record id를 사용한다. |

## 11. 데이터 계층 구현 고려사항

### DAO

Room DAO에 검색용 쿼리를 추가한다.

```kotlin
@Query(
    """
    SELECT * FROM drink_records
    WHERE name LIKE '%' || :query || '%'
       OR place LIKE '%' || :query || '%'
       OR tastingNote LIKE '%' || :query || '%'
    ORDER BY recordedAtMillis DESC
    """
)
fun observeSearchResults(query: String): Flow<List<DrinkRecordEntity>>
```

주의:

- `place`, `tastingNote`가 null이어도 SQLite `LIKE` 비교는 false로 처리된다.
- 영문 대소문자 처리는 대소문자 구분 없이 동작하도록 `LOWER(...)` 또는 collation 적용을 검토한다.
- `%`, `_` 같은 wildcard 문자는 일반 문자 검색이 되도록 escape 처리를 검토한다.
- MVP에서는 단순 `LIKE` 검색을 사용한다.

### Repository

Repository는 DAO 결과를 `DrinkRecord` 도메인 모델로 변환해 제공한다.

권장 API:

```kotlin
fun observeSearchResults(query: String): Flow<List<DrinkRecord>>
```

### UseCase

검색어 검증과 trim 처리는 UseCase 또는 ViewModel 중 한 곳에서 일관되게 처리한다.

권장 방향:

- ViewModel: 입력 상태, debounce, UI state 변환 담당
- UseCase: 최소 글자 수 검증과 Repository 호출 담당

상태 유지:

- SearchViewModel은 검색어와 검색 결과 상태를 보관한다.
- 검색 결과 상세 화면으로 이동했다가 뒤로 돌아오면 기존 검색 상태를 유지한다.
- 탭 전환 후 복귀 시에도 가능한 한 기존 검색 상태를 유지한다.

## 12. 테스트 후보

### Unit Test

- 빈 검색어는 Idle 또는 Invalid 상태로 처리되는지 확인
- 1글자 검색어는 Repository를 호출하지 않는지 확인
- 2글자 검색어는 Repository 검색을 호출하는지 확인
- 이름에 검색어가 포함된 기록이 반환되는지 확인
- 장소에 검색어가 포함된 기록이 반환되는지 확인
- 테이스팅 노트에 검색어가 포함된 기록이 반환되는지 확인
- 검색 결과가 최신순으로 정렬되는지 확인
- null 장소/테이스팅 노트가 있어도 오류가 발생하지 않는지 확인
- `%`, `_` 입력이 전체 결과를 의도치 않게 반환하지 않는지 확인
- 상세 화면 이동 후 뒤로 왔을 때 검색어와 결과가 유지되는지 확인

### UI 확인

- 검색 탭이 Bottom Navigation에 표시됨
- 넓은 화면에서 검색 탭이 Navigation Rail에 표시됨
- 검색어 입력창이 화면 상단에 표시됨
- 1글자 입력 시 결과 리스트가 표시되지 않음
- 2글자 이상 입력 시 결과 리스트가 검색창 하단에 표시됨
- 검색어 clear 버튼 선택 시 Idle 상태로 돌아감
- 결과 항목 선택 시 상세 화면으로 이동함
