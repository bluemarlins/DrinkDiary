# DrinkDiary 빌드 · 테스트 실행

## 1. 문서 목적

이 문서는 DrinkDiary의 빌드·테스트·정적 분석 명령과 그 전제 조건을 정의한다. 어떤 에이전트가
작업하든 동일한 명령으로 검증하도록 하기 위한 개발부 소유 명세다.

품질 게이트를 **언제 통과해야 하는지**(Definition of Done)는 `app/docs/orchestration/harness.md`
4절에 있다. 이 문서는 **무엇을 어떻게 실행하는지**만 다룬다.

## 2. 명령 (저장소 루트, Gradle 래퍼, PowerShell)

```powershell
.\gradlew.bat :app:assembleDebug        # 디버그 APK 빌드
.\gradlew.bat :app:testDebugUnitTest    # JVM 유닛 테스트 (app/src/test)
.\gradlew.bat :app:connectedDebugAndroidTest  # 계측 테스트 (app/src/androidTest), 기기/에뮬레이터 필요
.\gradlew.bat :app:lint                 # Android Lint
.\gradlew.bat :app:ktlintCheck          # ktlint 스타일 검사
.\gradlew.bat :app:ktlintFormat         # ktlint 위반 자동 수정
```

단일 테스트 클래스/메서드는 `--tests`로 지정한다:

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests "com.bluemarlin.drinkdiary.domain.usecase.SaveDrinkRecordUseCaseTest"
.\gradlew.bat :app:testDebugUnitTest --tests "com.bluemarlin.drinkdiary.domain.usecase.SaveDrinkRecordUseCaseTest.validation fails when rating is out of range"
```

실기기 설치는 루트 `build.gradle.kts`의 `installDebugApk` 태스크를 쓴다. 연결 여부는
`adb devices`로 먼저 확인한다.

## 3. 테스트 환경 전제

- Room이나 Android 프레임워크 클래스에 닿는 유닛 테스트는 Robolectric을 쓴다(예: `DrinkRecordDaoTest`).
  이 때문에 `app/build.gradle.kts`에 `testOptions.unitTests.isIncludeAndroidResources = true`가
  설정돼 있다 — **제거하지 말 것.**
- Robolectric은 `@Config(sdk = [35])`로 고정한다. JDK 17 환경에서 SDK 36이 아직 지원되지 않아
  생기는 문제를 우회하기 위함이다.

## 4. 실행 시 주의

- Gradle 빌드가 느린 편이다. 명령을 파이프(`| tail` 등)로 넘기면 **종료 코드가 파이프 마지막
  명령의 것으로 바뀌어 실패를 성공으로 오인**할 수 있다. 결과 판정이 필요하면 출력은 파일로
  리다이렉트하고 종료 코드를 따로 확인한다.
- ktlint 위반은 대부분 `ktlintFormat`으로 자동 수정된다. 자동 수정이 불가능한 항목
  (주로 `max-line-length`)만 손으로 고친다.
