# 돌아봄

**뒤를 맡겨 주세요. 당신은 앞만 보고 갈 수 있도록.**

운전 중 뒤를 돌아볼 수 없는 부모와 반려인을 대신해, 뒷좌석의 아이와 반려동물 상태를 보고 듣고 알려주는 AI 동승 케어 앱입니다.

Pleos Connect(Android Automotive 기반) 차량용 앱이며, 현대자동차그룹 차량용 앱 개발 공모전 출품작입니다.

## 문서

- [기획서](docs/기획서.md): 서비스 개요, 차량용 앱과 핸드폰 앱(뒷좌석 카메라 + 가족 알림)의 역할과 연동, 화면 구성, 디자인.
- [개발 계획](docs/개발계획.md): 메인 기능 요약, 1차 산출물 기준 개발 정도, 개발 방향, 2차까지 할 일과 일정.
- 시연 영상: [`docs/돌아봄_시연.mp4`](docs/돌아봄_시연.mp4). 지금 동작하는 차량 앱 화면과 2차에 만들 화면(디자인)을 41초 동안 보여 줍니다.

## 화면

| 화면 | 하는 일 |
|---|---|
| 1. 동승자 선택 | 오늘 뒷좌석에 타는 동승자(아이 또는 반려동물)와 부르는 이름을 정합니다. |
| 2. 주행 중 뒷좌석 상태 | 지금 상태를 한 문장으로 크게 보여 주고, 차량 신호와 기록을 함께 보여 줍니다. |
| 3. 알림과 쉼터 제안 | 살펴봐야 할 상황이면 한 문장 알림과 대응 제안을 띄웁니다. 하차 후 방치는 3단계로 올라갑니다. |

## 지금 되는 것과 아직 가짜인 것

| 기능 | 상태 |
|---|---|
| 상태 판단: 편안함, 잠듦, 지루함·불안, 멀미, 더위, 안전띠 풀림, 무반응, 하차 후 방치 | 구현. 단위 테스트 38개 |
| 한 줄 음성 안내 | 구현. Android 기본 음성 합성 사용. 음성 엔진이 없는 기기에서는 화면 기록에만 남습니다. |
| 하차 후 방치 3단계 (운전자 알림 → 60초 뒤 가족 알림 → 180초 뒤 119 신고 안내) | 단계 판단과 화면 구현. 가족 알림과 신고는 실제로 보내지 않고 문구만 보여 줍니다. |
| 뒷문 열림 상태 | `pleos` 빌드는 Pleos Vehicle SDK에서 읽습니다. `mock` 빌드는 가짜 데이터입니다. |
| 주차 여부, 운전자 탑승, 실내 온도 | 가짜 데이터. 화면의 시연 패널로 바꿉니다. |
| 뒷좌석 영상·소리 인식 | 가짜 데이터. 화면의 시연 패널로 바꿉니다. |
| 경유지 추가 | 요청만 기록합니다. 내비게이션(NaviHelper SDK) 연동 전이고, 장소 이름과 거리는 예시입니다. |

## 실행 방법

필요한 것: Android Studio(JDK 17 포함), Android SDK Platform 35.

1. 저장소를 받아 Android Studio에서 엽니다.
2. Build Variants에서 빌드를 고릅니다.
   - `mockDebug`: 가짜 차량 데이터만 씁니다. Pleos SDK 없이 API 28 이상 에뮬레이터나 기기에서 실행됩니다.
   - `pleosDebug`: Pleos Vehicle SDK로 뒷문 상태를 읽습니다. Pleos Connect 에뮬레이터가 필요합니다.
3. 실행합니다.

명령줄에서는 다음과 같이 합니다.

```bash
./gradlew installMockDebug        # 가짜 데이터 빌드 설치
./gradlew installPleosDebug       # Pleos Connect 에뮬레이터에 설치
./gradlew testMockDebugUnitTest   # 단위 테스트
./gradlew assemblePleosRelease    # 제출용 release APK (app/build/outputs/apk/pleos/release/)
```

### 시연 순서

1. 동승자를 고르고 "지켜보기 시작"을 누릅니다.
2. 시연 패널에서 "멀미"를 누르면 알림 화면이 뜹니다. "들렀다 가자"를 누르면 경유지 요청이 기록됩니다.
3. "하차 후 방치"를 누르고 "1분 건너뛰기"를 누를 때마다 단계가 올라갑니다. 60초에 2단계, 180초에 3단계입니다.
4. "온도 +1℃"로 실내 온도를 28℃ 이상으로 올리면 방치 단계가 시간과 상관없이 올라갑니다. 32℃ 이상이면 바로 3단계입니다.

## 테스트 환경과 필요한 설정

| 항목 | 값 |
|---|---|
| Pleos Connect 에뮬레이터 | v2.1.0 (Android 14, API 34, Automotive) |
| 화면 규격 | 2560 × 1440 px, 160 dpi, 가로 |
| 에뮬레이터 설정 | RAM 4GB, Cold boot |
| 앱 SDK 버전 | minSdk 28, targetSdk 35, compileSdk 35 |
| Pleos Vehicle SDK | `ai.pleos.playground:Vehicle:2.3.2` |
| SDK 저장소 | `https://nexus-playground.pleos.ai/repository/maven-releases/` |
| 권한 | `pleos.car.permission.CAR_DOORS` (`pleos` 빌드만) |

Pleos Connect 에뮬레이터 시스템 이미지는 Android Studio의 SDK Manager → SDK Update Sites에 아래 주소를 추가해 받습니다(Apple Silicon 기준).

```
https://nexus-playground.pleos.ai/repository/raw-releases/connect-avd/2.x/crp-arm64-sys-img.xml
```

화면은 가로 1280dp를 기준으로 그리고, 더 넓은 차량 화면에서는 전체를 최대 2배까지 키웁니다. 2560 × 1440 화면에서는 2배로 보입니다.

## APK 위치

제출용 release APK는 [`apk/돌아봄랩_돌아봄_release.apk`](apk/돌아봄랩_돌아봄_release.apk)입니다. `./gradlew assemblePleosRelease`로 만든 `pleos` 빌드이며, 디버그 키로 서명되어 있습니다.

## 검증 상태 (2026-11-15 기준)

빌드 환경: JDK 17, Gradle 8.10.2, AGP 8.8.2, Android SDK Platform 35 / Build-Tools 35.0.0 (macOS, Apple Silicon).

- 확인함: `./gradlew assembleMockDebug` 성공. 코드 수정 없이 빌드됩니다.
- 확인함: `./gradlew testMockDebugUnitTest`에서 `core` 패키지 단위 테스트 38개 통과 (AnnouncementPolicy 7, CareEngine 14, DemoScenarios 4, Korean 4, TripMonitor 9).
- 확인함: `./gradlew assemblePleosRelease` 성공. Pleos Vehicle SDK `2.3.2`를 공식 저장소에서 받아 컴파일했습니다. 결과물이 `apk/돌아봄랩_돌아봄_release.apk`이며, 디자인 반영 뒤 다시 빌드한 것입니다.
- 확인함: `mock` 빌드를 휴대폰 에뮬레이터(Pixel 10 Pro XL, Android 17 / API 37, arm64, 1344 × 2992 px, 480 dpi, 세로)에 `./gradlew installMockDebug`로 설치해 실행했습니다. 동승자 선택 → "지켜보기 시작" → 주행 중 뒷좌석 상태 → 시연 패널 "멀미" → 알림 화면 → "들렀다 가자" → 경유지 요청 기록 → "주행 종료" → 반려동물 재선택까지 화면이 모두 떴고, 그동안 `adb logcat`에 앱 크래시(FATAL EXCEPTION)와 ANR이 없었습니다.
- 확인함: 블랙 + 파랑 디자인을 반영한 뒤, 같은 에뮬레이터의 화면을 1280 × 720 · 160dpi(차량 화면 기준 배율)로 바꿔 동승자 선택 → 주행 중 → 멀미 알림 → 경유지 기록 → 하차 후 방치 3단계까지 다시 확인했습니다. 크래시 없음. 시연 영상의 "실제 화면" 장면이 이때 찍은 것입니다.
- 휴대폰 세로 화면에서는 한 줄로 쌓인 배치로 바뀌며, 일부 요소가 작게 보입니다. 기준 화면은 가로 1280dp 이상입니다.
- 확인함: 제출 파일 `apk/돌아봄랩_돌아봄_release.apk`가 릴리즈 빌드임을 확인했습니다. 매니페스트에 `debuggable` 없음, 설치 후 패키지 플래그에 DEBUGGABLE 없음, versionName `0.1.0`(mock 접미사 없음), 디버그 전용 ui-tooling 미포함. 서명은 README에 적은 대로 디버그 키입니다.
- 확인함: 그 APK 파일을 `adb install`로 휴대폰 에뮬레이터(Android 17, 세로)에 설치해 실행했습니다. Pleos 차량 서비스가 없는 기기라 뒷문 구독은 "need Permission"으로 실패하지만 앱은 그 사유를 화면 출처 줄에 보여 주고 계속 동작하며, 동승자 선택 → 주행 중 → 멀미 알림까지 크래시·ANR 없이 진행됐습니다.
- 확인함: 실제 휴대폰(Galaxy Z Flip5 SM-F731N, Android 14 / API 34, 1080 × 2640, 480dpi)에 제출 APK를 `adb install`로 설치해 실행했습니다. 설치 성공, 패키지 플래그에 DEBUGGABLE 없음, 앱 시작 0.2초. 동승자 선택 → 주행 중 → 멀미 알림 → "들렀다 가자" → 경유지 요청 기록까지 진행됐고 `adb logcat`에 크래시·ANR이 없었습니다. Pleos 차량 서비스가 없는 폰이라 뒷문 구독은 "need Permission"으로 실패하며, 앱은 그 사유를 화면에 보여 주고 계속 동작합니다.
- 휴대폰 세로 화면에서는 상단 바의 "음성 안내 켬" 글자가 좁게 접힙니다. 기준 화면(가로 1280dp 이상)에서는 한 줄입니다.
- 아직 확인하지 않음: Pleos Connect 에뮬레이터(2560 × 1440)에서 `pleos` 빌드 실행과 뒷문 신호 수신. `pleos` 빌드는 컴파일과 APK 생성까지만 확인했습니다.
- 뒷문 영역 이름은 `ROW_2_LEFT`, `ROW_2_RIGHT`로 가정했습니다. 에뮬레이터가 지원하지 않는 이름이면 건너뛰고 화면의 출처 줄에 실패 이유를 보여 줍니다.

## 디자인

블랙(#0A0A0B) 바탕에 무채색 면과 1dp 선만 쓰고, 색은 파랑(#2E7CF6) 하나로 "지금 눌러야 할 것"만 가리킵니다. 상태는 작은 점(편안 초록·알림 호박·경고 주황·긴급 빨강)과 글자로 구분합니다. 차량 화면은 왼쪽 세로 레일과 얇은 상단 바를 공통으로 씁니다. 값은 `ui/Theme.kt`의 `Ink`에 있고, 좌석 도식·상태 링·지도는 `ui/Widgets.kt`가 Canvas로 그립니다.

## 코드 구조

```
app/src/main/java/com/dolabom/app/
  core/   상태 판단, 음성 안내 시점, 방치 타이머. Android에 의존하지 않는 순수 Kotlin
  data/   차량 신호, 뒷좌석 인식, 내비게이션, 음성 안내의 인터페이스와 가짜 구현
  ui/     화면 3개와 ViewModel, 공통 크롬(Chrome.kt), 도식 위젯(Widgets.kt) (Jetpack Compose)
app/src/mock/    가짜 차량 데이터만 쓰는 빌드
app/src/pleos/   Pleos Vehicle SDK를 쓰는 빌드
app/src/test/    core 단위 테스트
```

뒷좌석 인식(`RearSeatSensor`), 차량 신호(`VehicleDataSource`), 내비게이션(`Navigator`)은 인터페이스로 분리해 두어서, 실제 구현을 붙일 때 화면과 판단 로직은 그대로 둡니다.

## 프라이버시 원칙

지금 빌드는 카메라와 마이크를 쓰지 않습니다. 영상·소리 인식을 붙일 때도 차 안에서만 처리하고, 저장하거나 차 밖으로 보내지 않습니다.
