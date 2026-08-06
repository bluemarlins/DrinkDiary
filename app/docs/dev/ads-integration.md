# AdMob 광고 통합 (Phase 2)

## 1. 문서 목적

이 문서는 `com.bluemarlin.drinkdiary.ads` 패키지에 구현된 Google AdMob 통합 구조와, Phase 4(릴리즈 준비)에서 반드시 처리해야 할 TODO를 정리한다. 배치 근거는 `app/docs/research/ad-monetization.md`를 따른다.

## 2. 의존성

- `com.google.android.gms:play-services-ads:25.4.0`
- `com.google.android.ump:user-messaging-platform:4.0.0` (GDPR/UMP 동의 처리)
- `AndroidManifest.xml`에 `INTERNET`, `ACCESS_NETWORK_STATE` 권한과 `com.google.android.gms.ads.APPLICATION_ID` meta-data 추가

## 3. 현재 사용 중인 ID — **전부 Google 공식 테스트 ID**

| 용도 | ID | 비고 |
|---|---|---|
| AdMob App ID | `ca-app-pub-3940256099942544~3347511713` | AndroidManifest.xml |
| 배너(Anchored Adaptive) | `ca-app-pub-3940256099942544/9214589741` | `AdConfig.BANNER_AD_UNIT_ID` |
| 전면(Interstitial) | `ca-app-pub-3940256099942544/1033173712` | `AdConfig.INTERSTITIAL_AD_UNIT_ID` |

**⚠️ Phase 4/릴리즈 전 필수 작업**: `spicyrabbit` AdMob 계정을 개설하고 실제 앱/광고 단위를 생성한 뒤, 위 3개 ID를 `AndroidManifest.xml`과 `AdConfig.kt`에서 교체해야 한다. 테스트 ID로 릴리즈 빌드를 배포하면 수익이 발생하지 않는다.

## 4. 구조

- `ads/AdConfig.kt` — 광고 단위 ID, 전면광고 노출 빈도(`INTERSTITIAL_SAVE_FREQUENCY = 4`) 상수
- `ads/ConsentManager.kt` — UMP 동의 흐름 래핑. `MainActivity.onCreate`에서 호출, EEA/UK 사용자에게만 동의 폼 표시, 그 외 지역은 자동 스킵 후 `MobileAds.initialize` 호출
- `ads/InterstitialAdManager.kt` — `SharedPreferences` 기반 저장 횟수 카운터로 빈도 제한. `AppContainer`에 싱글턴으로 보관
- `ads/DDBannerAdView.kt` — Compose `AndroidView` 래퍼, Anchored Adaptive Banner

## 5. 배치

| 위치 | 광고 | 트리거 |
|---|---|---|
| Dashboard, Collection 화면 하단(바텀 내비 위) | 배너 | `DDScreenScaffold(showBannerAd = true)` — `DDScreenScaffold.kt`의 `bottomBar` 슬롯에서 렌더링 |
| RecordEditor 저장 완료 → 상세 화면 전환 시점 | 전면 | `RecordEditorScreen.kt`의 `RecordEditorEvent.Saved` 처리부에서 `InterstitialAdManager.maybeShowAfterSave` 호출, 4회 저장당 1회 노출 |

작성 폼(Editor) 자체에는 배너를 넣지 않았다 — 경쟁앱 리서치에서 나온 "저장 버튼 근처 광고로 인한 오터치/흐름 방해" 불만을 피하기 위함 (`app/docs/research/competitor-analysis.md` 참고).

## 6. 검증 상태 (2026-08-06)

- `./gradlew :app:assembleDebug`, `:app:testDebugUnitTest` 통과
- 에뮬레이터 실행 결과 Dashboard 하단에 실제 "Test Ad" 배너 렌더링 확인 (스크린샷 확인됨)
- logcat에 `Ads: This request is sent from a test device.` 확인 — SDK 초기화/요청 정상
- 전면 광고는 4회 저장 빈도 제한 특성상 이번 세션에서 시각적으로는 미확인 — 로직상 프레임워크 콜백(onAdDismissedFullScreenContent 등)에 의해 `onProceed`가 정확히 1회 호출되도록 구성됨

## 7. 남은 작업 (백로그)

- 네이티브 광고(리스트 삽입형), 보상형 광고는 이번 Phase 2 범위에서 제외 — 연구 문서의 "설정/부가기능" 배치 대상 기능(테마, 내보내기 등)이 아직 없으므로 보류
- "광고 제거 1회성 인앱결제(IAP)"는 별도 의사결정 필요 (`service-launch-roadmap.md` Phase 1 참고)
