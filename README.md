# 돌아봄

운전 중 뒤를 돌아볼 수 없는 부모와 반려인을 대신해, 뒷좌석의 아이와 반려동물 상태를 보고 듣고 알려주는 AI 동승 케어 앱입니다.

Pleos Connect(Android Automotive 기반) 차량용 앱이며, 현대자동차그룹 차량용 앱 개발 공모전 출품작입니다.

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

제출용 release APK는 [`apk/`](apk/) 폴더에 `팀명_돌아봄_release.apk` 형식의 이름으로 올립니다.

## 검증 상태 (2026-11-14 기준)

- 확인함: 상태 판단, 음성 안내 시점, 방치 타이머, 시연 상황을 다루는 `core` 패키지 단위 테스트 38개 통과.
- 아직 확인하지 않음: Android 빌드와 에뮬레이터 실행. Pleos Connect 에뮬레이터에서 `pleos` 빌드를 돌려 뒷문 신호가 들어오는지도 확인 전입니다.
- 뒷문 영역 이름은 `ROW_2_LEFT`, `ROW_2_RIGHT`로 가정했습니다. 에뮬레이터가 지원하지 않는 이름이면 건너뛰고 화면의 출처 줄에 실패 이유를 보여 줍니다.

## 코드 구조

```
app/src/main/java/com/dolabom/app/
  core/   상태 판단, 음성 안내 시점, 방치 타이머. Android에 의존하지 않는 순수 Kotlin
  data/   차량 신호, 뒷좌석 인식, 내비게이션, 음성 안내의 인터페이스와 가짜 구현
  ui/     화면 3개와 ViewModel (Jetpack Compose)
app/src/mock/    가짜 차량 데이터만 쓰는 빌드
app/src/pleos/   Pleos Vehicle SDK를 쓰는 빌드
app/src/test/    core 단위 테스트
```

뒷좌석 인식(`RearSeatSensor`), 차량 신호(`VehicleDataSource`), 내비게이션(`Navigator`)은 인터페이스로 분리해 두어서, 실제 구현을 붙일 때 화면과 판단 로직은 그대로 둡니다.

## 프라이버시 원칙

지금 빌드는 카메라와 마이크를 쓰지 않습니다. 영상·소리 인식을 붙일 때도 차 안에서만 처리하고, 저장하거나 차 밖으로 보내지 않습니다.
