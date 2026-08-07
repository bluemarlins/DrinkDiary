# Play Store 리스팅 그래픽 에셋 (Phase 3 산출물)

## 1. 문서 목적

`app/store-listing/`에 있는 Play Store 등록용 그래픽 자산의 생성 경위와 재생성 방법을 기록한다. 최종 스토어 등록(Phase 5)에서 그대로 쓰거나 갱신해서 쓴다.

## 2. 산출물

```text
app/store-listing/
  feature-graphic.png        # 1024x500, Play Store 상단 프로모션 배너
  screenshots/
    01-dashboard.png          # 대시보드 (기록 3개, 재구매/비선호 섹션 포함)
    02-collection.png         # 컬렉션 목록 + 필터
    03-record-detail.png      # 기록 상세 (세부 평가 포함)
    04-record-editor.png      # 기록 등록 폼 (빈 상태)
```

## 3. Feature Graphic

- 담당: `agy` (`gemini-3.6-flash-high`, 이미지 생성)
- 사양: 1024x500 PNG, Cellar Green(`#2F6F4E`) 배경, 앱 아이콘과 동일한 와인잔+하트체크 글리프, 좌측 여백 확보(Play Store가 아이콘/타이틀을 오버레이하는 영역 고려)
- 앱 아이콘(`app/docs/design/app-icon.md`)과 동일한 브랜드 언어를 사용해 일관성 유지

## 4. 스크린샷

- 담당: Claude — 에뮬레이터에서 `adb shell input` 자동화로 샘플 기록 3건(와인/위스키/맥주, 각기 다른 컬렉션 상태)을 직접 입력한 뒤 실제 화면을 캡처
- **AdMob 테스트 배너는 스크린샷에서 제거함** — Dashboard/Collection 원본 캡처에는 "Test Ad" 배너가 하단에 함께 찍혔으나, 스토어 등록용으로 부적절하므로 Pillow로 광고 영역만 잘라내고 하단 내비게이션을 그 자리에 재배치하는 후처리를 거쳤다 (광고 배너 y 좌표 구간을 픽셀 스캔으로 탐지 후 크롭·재합성)
- Record Detail/Editor 화면은애초에 배너가 없는 라우트라 후처리 불필요

## 5. 재생성 시 참고

- 실제 프로덕션 스크린샷은 최종 앱 이름(현재 `strings.xml`의 `app_name`은 placeholder인 `DrinkDiary`, ASO 후보는 `app/docs/research/persona-aso.md` 참고)이 확정된 뒤, 실제 AdMob 계정 연동 완료 후 다시 촬영하는 것을 권장한다.
- 샘플 데이터는 에뮬레이터 로컬 DB에만 존재하며 저장소에는 포함되지 않는다.
