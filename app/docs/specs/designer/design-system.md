# DrinkDiary (테이스트 아카이브) Design System

> [!NOTE]
> **확정 명세 (2026-08-16 승격).**  
> 2026-08 제품 재정의에 맞추어 6화면 체제(`record`, `profile`, `collection`, `detail`, `settings`, `navigation`), 3~5탭 입력(F2), 81유형 모델(F3), 반응형 브레이크포인트, 모션, Figma 3계층 토큰 및 순수 디자인 Do & Don't 원칙을 반영하여 전면 개정함.  
> 근거: `../../departments/designer/design-system-ux-research.md`, `design-system-showcase.html`

---

## 1. 개요 및 설계 철학

테이스트 아카이브(DrinkDiary) 디자인 시스템은 **"한 잔의 기록이, 당신의 취향을 만듭니다"**라는 제품 가치에 맞추어, 아날로그 양장본 테이스팅 저널의 깊이감과 모바일에서의 최고 가독성·터치 조작성을 제공하는 UI 프레임워크입니다.

### 핵심 설계 원칙
1. **에디토리얼 무드 (Editorial & Warm)**: 차가운 블루톤을 배제하고, 종이 질감(`Paper`)과 주류 웜톤(`Wine`, `Malt`), Serif(코드)+Sans(UI) 페어링으로 품격 있는 저널 느낌 전달.
2. **반응형 & 적응형 (Adaptive Layout)**: `Compact`, `Medium`, `Expanded` 3단계 브레이크포인트와 가로/세로 화면 전환에 완전 대응.
3. **공간적 깊이감과 모션 (Spatial Motion)**: `Depth In/Out` (Z-Axis Shared Axis), `Selection Mode` 일괄 작업 바, 탭 전환 모핑.
4. **접근성 및 터치 보장 (Accessibility First)**: 모든 인터랙티브 컴포넌트의 최소 48×48dp 터치 영역 및 WCAG 2.1 AAA/AA 명도 대비 보장.

---

## 2. 순수 시각 디자인 원칙 (Pure Design Do & Don't)

| 영역 | ✅ DO (권장 규격) | ❌ DON'T (금지 규격) |
| :--- | :--- | :--- |
| **1. 색상 배합 (Color Harmony)** | • **한 화면 내 액센트 컬러 최대 2개로 제한**<br>• **60-30-10 법칙** 준수 (배경 60%, 중립 서피스 30%, 액센트 10%) | • 한 뷰포트에 3가지 이상의 다채색/원색 혼용 금지 (시각적 소음 유발)<br>• 코드 내 임의 Hex 하드코딩 금지 |
| **2. 테마 & 시인성 (Theme & Contrast)** | • **WCAG 2.1 본문 7.0:1 (AAA), 보조 4.5:1 (AA) 대비 보장**<br>• 완전 블랙 대신 깊이감 있는 웜 다크(`#15110E`) 사용 | • Dark 모드에서 채도 100% 원색/네온 텍스트 사용 금지 (눈부심/Halation 유발)<br>• 라이트 테마 단순 색상 반전(Invert) 금지 |
| **3. 타이포그래피 (Typography)** | • **앱 전체 서체 최대 2종(Serif + Sans)으로 제한**<br>• 본문 폰트 대비 140~150% 행간 및 자간 보정 | • 한 화면에 3종 이상의 이종 서체(필기체, 장식체 등) 혼용 금지<br>• 자간(Tracking) 보정 없는 거대 텍스트 남발 금지 |
| **4. 에셋 무결성 (Iconography)** | • **모든 아이콘은 24×24dp 그리드, 2dp 선 굵기, 동일 코너 반경으로 자체 제작/규격화**<br>• 해상도 독립적인 Vector XML 사용 | • 웹/외부에서 개별 다운로드한 이종 스타일 아이콘(선형+채움형 혼합, 선 굵기 불일치) 혼용 금지<br>• 저해상도 래스터 이미지(PNG/JPG) 사용 금지 |
| **5. 터치 타깃 (Touch & Grid)** | • **모든 인터랙티브 요소는 최소 48×48dp 터치 영역 보장**<br>• 4dp 배수 그리드 토큰(`4`, `8`, `12`, `16`, `20`, `24`, `32dp`)만 사용 | • 36dp 미만 작은 터치 영역 배치 금지 (오터치 유발)<br>• 임의의 비표준 dp 여백(`7dp`, `13dp` 등) 흩뿌림 금지 |
| **6. 표면 장식 (Decoration & Depth)** | • **1px의 단정한 외곽선(Stroke)과 부드러운 다단계 섀도우로 은은한 계층 표현** | • 3중 이상 과도하게 중첩된 카드(Over-Nested Cards) 금지<br>• 빛나는 네온 테두리 및 키워드 그라데이션 남발 금지 |

---

## 3. 디자인 파운데이션 (Tokens)

### 3.1 색상 토큰 (Color Palette)

| 토큰명 | Light 테마 Hex | Dark 테마 Hex | 시맨틱 용도 |
| :--- | :--- | :--- | :--- |
| `Paper` | `#FFF8F2` | `#15110E` | 앱 전체 배경 기본색 (양장본 종이 톤) |
| `Surface` | `#FFFFFF` | `#221C17` | 카드, 바텀시트, 다이얼로그 배경 |
| `SurfaceSunk` | `#F6EDE4` | `#1B1612` | 칩 미선택 상태, 배경 속 인셋 영역 |
| `Ink` | `#241E19` | `#F2E9E0` | 본문 기본 텍스트 (Primary Text) |
| `InkSoft` | `#6B5F56` | `#B3A498` | 보조 텍스트, 캡션 (Secondary Text) |
| `InkFaint` | `#9C8F84` | `#7D7066` | 비활성 텍스트, 플레이스홀더 |
| `Line` | `#E7DACC` | `#3A302A` | 기본 1px 구분선 |
| `LineStrong` | `#D3C2B0` | `#4E4139` | 카드/인풋 테두리 강한 구분 |
| `Primary` | `#2F6F4E` | `#6FBF93` | 주 액션 버튼, 브랜드 강조 |
| `PrimaryContainer` | `#E4EFE8` | `#1E3229` | 액션 보조 배경, 선택된 칩 배경 |
| `Wine` | `#93425E` | `#DB90AC` | 와인 주종 뱃지 및 테마 포인트 |
| `WineContainer` | `#F6E6EC` | `#38222B` | 와인 뱃지/배경용 연한 톤 |
| `Malt` | `#8A5A1B` | `#DFA75B` | 위스키 주종 뱃지 및 테마 포인트 |
| `MaltContainer` | `#F8ECDA` | `#38290F` | 위스키 뱃지/배경용 연한 톤 |
| `Destructive` | `#BA1A1A` | `#FFB4AB` | 삭제, 위험 액션 |

> [!NOTE]
> **`Malt` Light 정정 (2026-08-16)**: `#9C6722` → `#8A5A1B`. 원래 값은 `MaltContainer`(`#F8ECDA`) 위에서
> **4.11:1로 2절이 요구하는 보조 4.5:1(AA)에 미달**했다 — 주종 뱃지 레이블(`LabelSmall` 11sp)이
> 이 조합을 쓴다. AA를 넘기는 데 필요한 최소 변경만 했다(**5.06:1**). Dark는 6.58:1로 이미 통과라
> 그대로 둔다. 근거: `../../departments/designer/design-system-audit-2026-08.md` 3절.
>
> **명도 대비는 이제 테스트가 지킨다** — `app/src/test/.../ui/theme/DesignTokenTest.kt`가 이 표의
> hex와 대비 비율을 검사하므로, 표를 고치면 테스트도 함께 고쳐야 한다.

### 3.2 타이포그래피 (Typography Hierarchy)

- **Serif Family**: `FontFamily.Serif` (Georgia 계열)
- **Sans Family**: `FontFamily.SansSerif` (시스템 산세리프 / Pretendard / Noto Sans KR)

| 스타일 Role | 서체 / Weight | 크기 / 행간 | Tracking | 주요 사용처 |
| :--- | :--- | :--- | :--- | :--- |
| `DisplayTasteCode` | Serif Bold | 32sp / 40sp | +0.05em | 81가지 취향 유형 코드 (`SFRE`), 9:16 공유 카드 |
| `HeadlineSentence` | Sans SemiBold | 20sp / 28sp | -0.02em | 취향 요약 핵심 문장 |
| `TitleLarge` | Sans Bold | 18sp / 24sp | -0.01em | 상단 앱바 타이틀, 화면 주요 섹션 헤더 |
| `TitleMedium` | Sans SemiBold | 16sp / 22sp | 0.00em | 카드 타이틀, 질문 문항 텍스트 |
| `BodyLarge` | Sans Regular | 15sp / 22sp | 0.00em | 폼 필드 입력값, 본문 |
| `BodyMedium` | Sans Regular | 13sp / 18sp | +0.01em | 카드 설명, 보조 텍스트, 날짜/가격 |
| `LabelLarge` | Sans SemiBold | 14sp / 20sp | +0.01em | 주요 버튼 레이블 (`DDPrimaryButton`) |
| `LabelSmall` | Sans Medium | 11sp / 14sp | +0.03em | 주종 뱃지, 태그 칩 레이블 |

### 3.3 간격 및 형태 (Spacing & Shapes)
- **간격 토큰**: `4dp`(xxs), `8dp`(xs), `12dp`(sm), `16dp`(md - 표준 마진), `20dp`(lg), `24dp`(xl), `32dp`(xxl)
- **라운드 토큰**:
  - `ShapeSmall` (6dp): 태그 칩, 도메인 배지
  - `ShapeMedium` (12dp): 버튼, 텍스트 필드, 작은 카드
  - `ShapeLarge` (18dp): 메인 카드, 다이얼로그, 바텀시트 상단

---

## 4. 반응형 브레이크포인트 (Adaptive Breakpoints)

| WindowSizeClass | 기준 너비 (Width) | 마진 (Margin) | 네비게이션 구조 | 화면별 적응형 레이아웃 |
| :--- | :--- | :--- | :--- | :--- |
| **Compact** | `< 600dp` (일반 폰) | `16dp` | `BottomNavigationBar` | 1-Column 단일 스택 플로우 |
| **Medium** | `600dp ~ 839dp` (폴더블/가로) | `24dp` | `NavigationRail` (좌측) | 2-Column 분할 (좌 40% : 우 60%) |
| **Expanded** | `≥ 840dp` (태블릿/DeX) | `32dp` | `NavigationDrawer` (영구) | List-Detail / Supporting Pane (최대 콘텐츠폭 720dp 제한) |

---

## 5. 공통 컴포넌트 카탈로그 (`DD*`)

### 5.1 Action Components
| 컴포넌트 | 파라미터 / 역할 | 비고 |
| :--- | :--- | :--- |
| `DDPrimaryButton` | `text`, `onClick`, `modifier`, `enabled`, `icon` | 최소 48dp 높이, 긍정 액션 버튼 |
| `DDSecondaryButton`| `text`, `onClick`, `modifier`, `enabled` | 이전 단계, 취소 등 보조 액션 |
| `DDDestructiveButton` | `text`, `onClick`, `modifier` | 기록 삭제 등 위험 액션 |
| `DDIconButton` | `icon`, `contentDescription`, `onClick` | 상단 툴바 뒤로가기, 설정 등 (48×48dp 터치 영역) |

### 5.2 Input & Probe Components (F2)
| 컴포넌트 | 역할 및 인터랙션 |
| :--- | :--- |
| `DDProbeQuestion` | 1개 감각 축 질문 문항 + 3개 선택지 버튼(`Low`/`Mid`/`High`). 탭 1회 즉시 선택 (높이 52dp) |
| `DDProbeProgress` | 4문항 중 현재 진행 단계 표시 바 (부드러운 진행도 보간) |
| `DDTagChipGroup` | 선택 태그(와인 색, 위스키 분류, 피트 등) 칩 목록 |
| `DDRatingInput` | 5점 척도 만족도 선택 (터치 및 드래그) |
| `DDToggleRow` | 재구매 의향 ("다시 살래요") 원터치 스위치/토글 |
| `DDTextField` | 술 이름, 장소, 가격 등 단정한 텍스트 필드 |

### 5.3 Display & Insight Components (F3, F5, F4)
| 컴포넌트 | 역할 및 비고 |
| :--- | :--- |
| `DDTasteSentenceCard` | **[F3 핵심]** 문장 형태의 취향 요약 표시 카드 (배경 톤 + 인용구 스타일) |
| `DDTasteTypeBadge` | 81유형 코드(예: `SFRE`)와 레이블을 품격 있게 표현하는 컴포넌트 |
| `DDProfileProgressCard` | 임계치 도달 전("N잔 더 마시면...") 남은 진행도 안내 카드 |
| `DDDrinkBadge` | 와인(버건디)/위스키(골드) 및 세부 분류를 한눈에 보여주는 도메인 뱃지 |
| `DDRepurchaseBadge` | 매장에서 3초 만에 선호도를 확인하는 "재구매 추천" 뱃지 |
| `DDDrinkRecordCard` | 컬렉션 목록의 직관적인 기록 카드 (이미지 썸네일, 이름, 주종, 만족도, 날짜) |
| `DDShareCard` | 인스타그램 스토리용 9:16 비율의 로컬 비트맵 렌더링용 Compose 레이아웃 |

### 5.4 Feedback Components
| 컴포넌트 | 역할 |
| :--- | :--- |
| `DDLoadingContent` | 로딩 인디케이터 및 스켈레톤 UI |
| `DDEmptyContent` | 기록 없음 또는 필터 결과 없음 시 등록 유도 CTA |
| `DDConfirmDialog` | 삭제 확인 다이얼로그 (ShapeLarge 18dp) |
| `DDSnackbar` | 피드백 메시지 표시 |

---

## 6. 화면별 컴포넌트 매핑 규칙

1. **기록 플로우 (`ui/record`)**
   - 주종/기본 분류 선택 (`DrinkPicker`, `DDDrinkBadge`)
   - 4축 감각 입력 (`ProbeSequenceScreen`, `DDProbeProgress`, `DDProbeQuestion`)
   - 상세 정보 & 선택 태그 (`RecordDetailStep`, `DDRatingInput`, `DDToggleRow`, `DDTagChipGroup`)
   - 첫 기록 태그 승격 프롬프트 (`TagPreferencePrompt`)
2. **취향 프로필 (`ui/profile/ProfileScreen`)**
   - `DDTasteTypeBadge` + `DDTasteSentenceCard`
   - 감각 축별 선호 상태 리스트 (`TraitStatusRow`)
   - 라벨/태그 기반 인사이트 ("셰리 4.7점", "스모키함 4.5점")
   - 미달 상태 안내: `DDProfileProgressCard`
3. **컬렉션 & 검색 (`ui/collection/CollectionScreen`)**
   - 검색창 + 주종/재구매 필터 칩 (`LazyColumn` + `DDDrinkRecordCard`)
4. **기록 상세 (`ui/collection/RecordDetailScreen`)**
   - 대표 이미지(`DDUriImage`) + 주종/재구매 뱃지 + 평점 + 감각 축 응답 + 메모
   - 수정(`EditRecordScreen`), 삭제(`DDDestructiveButton`, `DDConfirmDialog`)
5. **설정 (`ui/settings/SettingsScreen`)**
   - 기록 시 물어볼 태그 스위치 관리
