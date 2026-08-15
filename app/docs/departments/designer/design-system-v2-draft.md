# DrinkDiary (테이스트 아카이브) Design System v2 초안

> **문서 상태**: Draft (디자이너 부서 실무 산출물)  
> **대상 기준**: 2026-08 재정의된 6화면 체제 (`record`, `profile`, `collection`, `detail`, `settings`, `navigation`) 및 3~5탭 입력·81유형 모델  
> **승격 계획**: 사용자 검토 및 서명 후 `app/docs/specs/designer/design-system.md`로 승격

---

## 1. 개정 배경 및 방향

기존 디자인 시스템(`specs/designer/design-system.md`)은 재정의 전 구 4화면(대시보드·컬렉션·상세·편집기)과 슬라이더 기반 입력 시절에 작성되어 현재의 코드 구조 및 PRD 요구사항과 어긋나 있습니다.

### 핵심 개정 방향
1. **F2 (3~5탭 입력) 중심 UI 컴포넌트 표준화**: 슬라이더 폐기, 3선택(`Low`/`Mid`/`High`) Probe 및 선택 태그 칩 공식화.
2. **F3 (취향 프로필) 문장 중심 시각 위계**: 차트가 아닌 문장(`DDTasteSentence`)과 81유형 배지를 주인공으로 구성.
3. **F5 (매장 3초 판단) 가독성 극대화**: 와인/위스키 뱃지 및 재구매 후보 여부의 즉각적 시각 인지.
4. **F4 (9:16 인스타 공유 카드)** 템플릿 컴포넌트 신규 규격화.
5. **화면별 인라인 구현 최소화**: `ui/component/` 하위에 재사용 가능한 `DD*` 컴포넌트 계층 정립.

---

## 2. 디자인 파운데이션 (Tokens)

### 2.1 색상 토큰 (Color Palette)
종이(Paper)와 술의 질감(Wine, Malt)을 반영한 웜톤(Warm tone) 팔레트.

| 토큰명 | Light 테마 Hex | Dark 테마 Hex | 용도 |
| :--- | :--- | :--- | :--- |
| `Paper` | `#FFF8F2` | `#15110E` | 앱 전체 배경 기본색 |
| `Surface` | `#FFFFFF` | `#221C17` | 카드, 바텀시트, 다이얼로그 배경 |
| `SurfaceSunk` | `#F6EDE4` | `#1B1612` | 칩 미선택 상태, 배경 속 인셋 영역 |
| `Ink` | `#241E19` | `#F2E9E0` | 본문 기본 텍스트 (Primary Text) |
| `InkSoft` | `#6B5F56` | `#B3A498` | 보조 텍스트, 캡션 (Secondary Text) |
| `InkFaint` | `#9C8F84` | `#7D7066` | 비활성 텍스트, 플레이스홀더 |
| `Line` | `#E7DACC` | `#3A302A` | 기본 구분선, 연한 테두리 |
| `LineStrong` | `#D3C2B0` | `#4E4139` | 카드/인풋 테두리 강한 구분 |
| `Primary (Green)` | `#2F6F4E` | `#6FBF93` | 주 액션 버튼, 브랜드 강조 |
| `PrimaryContainer` | `#E4EFE8` | `#1E3229` | 액션 보조 배경, 선택된 칩 배경 |
| `Wine` | `#93425E` | `#DB90AC` | 와인 주종 뱃지 및 테마 포인트 |
| `WineContainer` | `#F6E6EC` | `#38222B` | 와인 뱃지/배경용 연한 톤 |
| `Malt (Whisky)` | `#9C6722` | `#DFA75B` | 위스키 주종 뱃지 및 테마 포인트 |
| `MaltContainer` | `#F8ECDA` | `#38290F` | 위스키 뱃지/배경용 연한 톤 |
| `Destructive` | `#BA1A1A` | `#FFB4AB` | 삭제, 위험 액션 |

### 2.2 타이포그래피 (Typography)
- **한글**: 시스템 산세리프 (Pretendard / Noto Sans KR / Apple SD Gothic Neo)
- **영문/강조**: Serif (Georgia)를 취향 유형 코드(예: `SFRE`, `XFRX`) 및 브랜드 헤드라인에 적용

| 스타일 | 서체 / Weight | 크기 / 행간 | 주요 사용처 |
| :--- | :--- | :--- | :--- |
| `DisplayTasteCode` | Serif Bold | 32sp / 40sp | 81가지 취향 유형 코드 |
| `HeadlineSentence` | Sans SemiBold | 20sp / 28sp | 취향 요약 핵심 문장 |
| `TitleLarge` | Sans Bold | 18sp / 24sp | 상단 앱바 타이틀, 화면 주요 섹션 헤더 |
| `TitleMedium` | Sans SemiBold | 16sp / 22sp | 카드 타이틀, 질문 문항 텍스트 |
| `BodyLarge` | Sans Regular | 15sp / 22sp | 폼 필드 입력값, 본문 |
| `BodyMedium` | Sans Regular | 13sp / 18sp | 카드 설명, 보조 텍스트, 태그 설명 |
| `LabelLarge` | Sans SemiBold | 14sp / 20sp | 주요 버튼 레이블 |
| `LabelSmall` | Sans Medium | 11sp / 14sp | 주종 뱃지, 태그 칩 레이블 |

### 2.3 간격 (Spacing) 및 형태 (Shapes)
- **간격 토큰**: `4dp`(xxs), `8dp`(xs), `12dp`(sm), `16dp`(md - 기본 마진), `20dp`(lg), `24dp`(xl), `32dp`(xxl)
- **라운드 토큰**:
  - `ShapeSmall` (6dp): 태그 칩, 뱃지
  - `ShapeMedium` (12dp): 버튼, 텍스트 필드, 작은 카드
  - `ShapeLarge` (18dp): 메인 카드, 다이얼로그, 바텀시트 상단

---

## 3. 공통 컴포넌트 카탈로그 (`DD*`)

### 3.1 Action Components
| 컴포넌트 | 파라미터 / 역할 | 비고 |
| :--- | :--- | :--- |
| `DDPrimaryButton` | `text`, `onClick`, `modifier`, `enabled`, `icon` | 전체 너비 또는 하단 고정 주 저장/등록 버튼 |
| `DDSecondaryButton`| `text`, `onClick`, `modifier`, `enabled` | 이전 단계, 취소 등 보조 액션 |
| `DDDestructiveButton` | `text`, `onClick`, `modifier` | 기록 삭제 등 비가역적 액션 |
| `DDIconButton` | `icon`, `contentDescription`, `onClick` | 상단 툴바 뒤로가기, 닫기, 설정 등 |

### 3.2 Input & Probe Components (F2)
| 컴포넌트 | 역할 및 인터랙션 |
| :--- | :--- |
| `DDProbeQuestion` | 1개 감각 축 질문 문항 + 3개 선택지 버튼(`Low`/`Mid`/`High`). 탭 1회 즉시 선택 |
| `DDProbeProgress` | 4문항 중 현재 진행 단계 표시 바 (예: 2/4) |
| `DDTagChipGroup` | 선택 태그(와인 색, 위스키 분류, 피트 등) 다중/단일 선택 가로 칩 목록 |
| `DDRatingInput` | 5점 척도 만족도 선택 (1~5점) |
| `DDToggleRow` | 재구매 의향 ("다시 마실래요") 원터치 스위치/토글 |
| `DDTextField` | 술 이름, 장소, 가격 등 간결한 입력 필드 (Clear 버튼 포함) |

### 3.3 Display & Insight Components (F3 & F5)
| 컴포넌트 | 역할 및 비고 |
| :--- | :--- |
| `DDTasteSentenceCard` | **[F3 핵심]** 문장 형태의 취향 요약 표시 카드 (배경 톤 + 인용구 스타일) |
| `DDTasteTypeBadge` | 81유형 코드(예: `SFRE`)와 레이블을 품격 있게 표현하는 컴포넌트 |
| `DDProfileProgressCard` | 임계치 도달 전("N잔 더 마시면...") 남은 진행도 안내 카드 |
| `DDDrinkBadge` | 와인(버건디)/위스키(골드) 및 세부 분류를 한눈에 보여주는 도메인 뱃지 |
| `DDRepurchaseBadge` | 매장에서 3초 만에 선호도를 확인하는 "재구매 추천" 뱃지 |
| `DDDrinkRecordCard` | 컬렉션 목록의 직관적인 기록 카드 (이미지 썸네일, 이름, 주종, 만족도, 날짜) |

### 3.4 Sharing Component (F4)
| 컴포넌트 | 역할 |
| :--- | :--- |
| `DDShareCard` | 인스타그램 스토리용 9:16 비율의 로컬 비트맵 렌더링용 Compose 레이아웃 |

---

## 4. 화면별 컴포넌트 매핑 (New 6-Screens)

1. **기록 플로우 (`ui/record`)**
   - 1단계: 주종/기본 분류 선택 (`DrinkPicker`, `DDDrinkBadge`)
   - 2단계: 4축 감각 입력 (`ProbeSequenceScreen`, `DDProbeProgress`, `DDProbeQuestion`)
   - 3단계: 상세 정보 & 선택 태그 (`RecordDetailStep`, `DDRatingInput`, `DDToggleRow`, `DDTagChipGroup`)
   - 완료: 첫 기록 태그 승격 프롬프트 (`TagPreferencePrompt`)
2. **취향 프로필 (`ui/profile/ProfileScreen`)**
   - 상단: `DDTasteTypeBadge` + `DDTasteSentenceCard`
   - 중단: 감각 축별 선호 상태 리스트 (`TraitStatusRow`)
   - 하단: 라벨/태그 기반 인사이트 ("셰리 4.7점", "스모키함 4.5점")
   - 미달 상태: `DDProfileProgressCard`
3. **컬렉션 & 검색 (`ui/collection/CollectionScreen`)**
   - 상단: 검색창 + 주종/재구매 필터 칩
   - 목록: `LazyColumn` + `DDDrinkRecordCard`
4. **기록 상세 (`ui/collection/RecordDetailScreen`)**
   - 대표 이미지(`DDUriImage`) + 주종/재구매 뱃지 + 평점 + 감각 축 응답 + 메모
   - 하단 액션: 수정(`EditRecordScreen`), 삭제(`DDDestructiveButton`)
5. **설정 (`ui/settings/SettingsScreen`)**
   - 기록 시 물어볼 태그 스위치 관리
