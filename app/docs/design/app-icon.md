# 앱 아이콘 (Phase 3)

## 1. 문서 목적

이 문서는 DrinkDiary 앱 아이콘을 재제작한 과정과, 재생성 방법을 기록한다.

## 2. 배경

기존 아이콘(와인·위스키·맥주 잔 3개 나열)은 `agy` 검토 결과 다음 문제가 있었다:
- Vivino, casky 등 경쟁 앱과 시각적 차별성 부족
- 작은 런처 아이콘 크기에서 반짝이 디테일까지 겹쳐 가독성 저하
- "나만의 술 다이어리" 감성보다 일반 술집 메뉴판 느낌

또한 코드 감사 결과 실제 적응형 아이콘(`mipmap-anydpi-v26/ic_launcher.xml`)의 `foreground` 레이어(`mipmap-*/ic_launcher_foreground.webp`)가 투명 글리프가 아니라 배경까지 통째로 구운 불투명 이미지였고, `res/drawable/ic_launcher_background.xml`은 Android Studio 기본 템플릿의 로봇 그리드 배경(`#3DDC84`)이 그대로 남아 전혀 사용되지 않는 상태였다(불투명 foreground가 항상 덮어버림). `res/drawable/ic_launcher_foreground.xml`(기본 로봇 벡터)도 아무 곳에서도 참조되지 않는 죽은 코드였다.

## 3. 새 디자인

- 담당: `agy`(이미지 생성) + Claude(에셋 파이프라인/통합)
- 컨셉: 와인잔 + 재구매를 상징하는 하트-체크 마크를 잔 안에 통합
- 색상: 앱 테마 팔레트 그대로 사용 — 배경 Cellar Green `#2F6F4E`(`Color.kt`의 `CellarGreen40`), 글리프 Malt Gold/Rose 계열

## 4. 적응형 아이콘 구조 정상화

| 파일 | 이전 | 이후 |
|---|---|---|
| `res/drawable/ic_launcher_background.xml` | 기본 템플릿 로봇 그리드(`#3DDC84`), 실질적으로 안 보임 | 단색 Cellar Green(`#2F6F4E`) |
| `res/mipmap-*/ic_launcher_foreground.webp` | 배경까지 구운 불투명 이미지 | **투명 배경의 글리프만** — 진짜 적응형 아이콘 전경 레이어로 동작 |
| `res/drawable/ic_launcher_foreground.xml` | 미사용 기본 로봇 벡터 | 삭제 |
| `res/mipmap-*/ic_launcher.webp`, `ic_launcher_round.webp` | 이전 3잔 디자인 | Cellar Green 배경 + 새 글리프, 각각 rounded-square/circle 마스크 적용 |
| `app/src/main/ic_launcher-playstore.png` | 이전 3잔 디자인, 512x512 | 새 글리프, full-bleed(사전 라운딩 없음) 512x512 |

## 5. 재생성 방법

1. `agy`로 마스터 글리프(투명 배경, 512x512, 안전 영역 내 중앙 정렬)를 생성한다. 최초 생성 시도는 배경이 완전히 다른 저품질 결과가 나온 적이 있어, 필요하면 기존 배경 있는 버전에서 flood-fill로 배경을 제거하는 방식이 더 안정적이었다(`icon-concept-1.png` → 흰색/그린 배경을 `PIL.ImageDraw.floodfill`로 투명화 → 경계 잔여물 크롭).
2. Python(Pillow)으로 아래를 생성한다:
   - `flat_master` = Cellar Green 512x512 배경에 글리프 합성 (Play Store 아이콘, legacy 아이콘의 원본)
   - 밀도별 `ic_launcher_foreground.webp` = 글리프만 리사이즈 (108/162/216/324/432px)
   - 밀도별 `ic_launcher.webp` = `flat_master` 리사이즈 후 rounded-rect 마스크 (48/72/96/144/192px)
   - 밀도별 `ic_launcher_round.webp` = `flat_master` 리사이즈 후 circle 마스크
   - `ic_launcher-playstore.png` = `flat_master` 512x512 그대로(마스킹 없음)
3. `./gradlew :app:assembleDebug` 및 실기기/에뮬레이터 설치로 런처 아이콘 렌더링 확인.

## 6. 검증 상태 (2026-08-06)

- 빌드/유닛테스트 통과
- 에뮬레이터 홈 화면에서 원형 마스크로 실제 렌더링 확인 (런처가 자체 circle 마스크 적용, 초록 배경 위 글리프가 작은 크기에서도 또렷하게 보임)
