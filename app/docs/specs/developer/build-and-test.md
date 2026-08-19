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

## 3. 화면 동작 검증 — 실기기 우선, 없으면 에뮬레이터

**연결된 실기기가 있으면 실기기를 쓴다. 없으면 에뮬레이터로 내려간다** *(2026-08-19 사용자 확정)*.

구 규칙은 정반대였다 — "에뮬레이터에서 한다, 실기기를 쓰지 않는다". 근거는 실제 사고였다:
실기기는 사용자가 일상적으로 쓰는 폰이라 adb로 조작하면 그 기기를 점유하게 되고, **하드웨어
BACK으로 앱을 벗어나 사용자의 다른 화면이 스크린샷에 찍힌 적이 있다(2026-08-14).**

**기본값만 뒤집혔고 그 위험은 그대로다.** 실기기를 쓸 때 아래 셋을 지킨다.

- **하드웨어 BACK을 쓰지 않는다.** 화면 이동은 앱 안의 뒤로 버튼과 탭만 쓴다.
- **앱이 포커스를 잃으면 캡처를 멈춘다.** `dumpsys window displays`의 `mCurrentFocus`가
  `com.bluemarlin.drinkdiary`가 아니면 그 화면은 사용자의 것이지 우리 것이 아니다.
- **`pm clear`·계정·시스템 설정을 건드리지 않는다.** 지우면 사용자의 실제 기록이 사라진다.

```powershell
# 1) 붙어 있는 기기 확인 — 실기기가 있으면 그 시리얼을 쓴다
adb devices -l

# 2) 실기기 경로 (예: SM-F971N)
adb -s R3KL406ERJM install -r app\build\outputs\apk\debug\app-debug.apk

# 3) 실기기가 없을 때만 에뮬레이터
& "$env:LOCALAPPDATA\Android\Sdk\emulator\emulator.exe" -list-avds
& "$env:LOCALAPPDATA\Android\Sdk\emulator\emulator.exe" -avd Medium_Phone_API_36.1 -no-snapshot-load
adb -s emulator-5554 install -r app\build\outputs\apk\debug\app-debug.apk
```

**시리얼을 항상 `-s`로 지정한다.** 둘 다 붙어 있으면 `adb`는 어느 쪽인지 묻지 않고 실패하거나,
더 나쁘게는 의도하지 않은 쪽에 설치한다.

### 폴더블(SM-F971N)에서만 걸리는 것 *(2026-08-19 확인)*

- **`screencap`에 `-d <displayId>`를 준다.** 디스플레이가 둘이라 생략하면 경고 문구가 PNG 앞에
  섞여 **파일이 통째로 깨진다**(이미지가 아니라 텍스트로 읽힌다). id는
  `adb -s <serial> shell dumpsys SurfaceFlinger --display-id`로 얻는다.
- **`monkey`로 앱을 열지 않는다.** 실행 뒤 랜덤 이벤트를 하나 더 보내서 엉뚱한 탭이 선택된 채로
  열린다 — 초기 화면을 확인하려던 검증이 그대로 오판이 된다.
  `am start -n com.bluemarlin.drinkdiary/.MainActivity`를 쓴다.
- 폭이 약 555dp(1248px ÷ density 360)라 **Compact 구간**이다. 하단 바가 나오는 것이 정상이고,
  `NavigationRail`을 보려면 Medium(600dp) 이상이 필요하다.

**AVD 로케일은 기본이 en-US다.** 타깃이 국내 사용자이므로 앱 단위로 한국어를 지정한다
(에뮬레이터에 root가 없어 시스템 로케일은 못 바꾼다).

```powershell
adb -s emulator-5554 shell cmd locale set-app-locales com.bluemarlin.drinkdiary --locales ko-KR
```

### 조작·확인 시 주의

- **adb는 PowerShell에서 호출한다.** Git Bash는 `/sdcard/...`를 Windows 경로로 바꿔버린다.
- **화면 전환은 앱 안의 "뒤로"를 쓴다.** 하드웨어 BACK은 앱 밖으로 나간다.
- **좌표는 `uiautomator dump`로 얻는다.** 스크린샷은 축소돼 전달되므로 눈대중 좌표는 어긋난다.
- **DB를 뽑을 때 `-wal`을 같이 가져온다.** Room은 데이터를 WAL에 남겨서, `.db`만 뽑으면
  **빈 DB로 읽힌다** — "아무것도 저장 안 됐다"로 오판하기 쉽다.
- **파일은 `cmd /c "... > file"`로 받는다.** PowerShell의 `>`는 바이너리를 깨뜨린다.

실기기 설치가 필요한 경우에만 루트 `build.gradle.kts`의 `installDebugApk`를 쓴다.

## 4. 테스트 환경 전제

- Room이나 Android 프레임워크 클래스에 닿는 유닛 테스트는 Robolectric을 쓴다(예: `DrinkRecordDaoTest`).
  이 때문에 `app/build.gradle.kts`에 `testOptions.unitTests.isIncludeAndroidResources = true`가
  설정돼 있다 — **제거하지 말 것.**
- Robolectric은 `@Config(sdk = [35])`로 고정한다. JDK 17 환경에서 SDK 36이 아직 지원되지 않아
  생기는 문제를 우회하기 위함이다.

## 5. 실행 시 주의

- Gradle 빌드가 느린 편이다. 명령을 파이프(`| tail` 등)로 넘기면 **종료 코드가 파이프 마지막
  명령의 것으로 바뀌어 실패를 성공으로 오인**할 수 있다. 결과 판정이 필요하면 출력은 파일로
  리다이렉트하고 종료 코드를 따로 확인한다.
- ktlint 위반은 대부분 `ktlintFormat`으로 자동 수정된다. 자동 수정이 불가능한 항목
  (주로 `max-line-length`)만 손으로 고친다.
