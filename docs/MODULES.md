# setmon 앱 모듈 구조와 화면 이동(route) 규칙

> 작성: A (프론트) · 2026-11-04 · 바꿀 일이 생기면 이 문서도 같이 고쳐주세요.

## 1. 모듈 목록과 담당

| 모듈 | 하는 일 | 담당 |
|---|---|---|
| `app` | 앱 시작점(MainActivity, Hilt Application), **모든 화면을 잇는 NavHost** | A |
| `core:designsystem` | 색·글자·공통 컴포넌트, `SetmonTheme` | A |
| `core:model` | 앱 전체가 쓰는 데이터 클래스 | A (C의 API 모양을 따름) |
| `core:network` | Retrofit·OkHttp, 토큰 인터셉터 (2주차) | A |
| `core:data` | Repository (화면은 여기만 본다), Mock ↔ 실서버 전환 | A |
| `feature:auth` | S-01 로그인, S-02 회원가입 | A |
| `feature:calendar` | S-03 메인 달력 | A |
| `feature:day` | S-05 하루 상세 | A |
| `feature:friend` | S-07 친구 | A |
| `feature:mypage` | S-12 마이페이지 | A |
| **`feature:capture`** | **S-04 촬영 (녹화·썸네일·업로드 포함)** | **B** |
| `build-logic` | 모든 모듈의 공통 Gradle 설정 | A |

## 2. 의존성 규칙 (중요)

```
app ──▶ 모든 feature, core
feature:* ──▶ core:designsystem, core:model, core:data   (자동 연결됨)
core:data ──▶ core:network, core:model
core:network ──▶ core:model
```

- **feature끼리는 서로 의존하지 않는다.** `feature:capture`에서 `feature:calendar`를 import하면 안 된다.
  다른 화면으로 가야 하면 콜백(`onUploadComplete`, `onBack` 등)을 호출하고, 실제 이동은 `app`의 `SetmonNavHost`가 한다.
- 새 라이브러리는 **`gradle/libs.versions.toml`에 먼저 추가**하고, 모듈의 `build.gradle.kts`에서 `libs.xxx`로 쓴다. 버전을 모듈 파일에 직접 적지 않는다.
- feature 모듈의 `build.gradle.kts`에는 `id("setmon.android.feature")` 하나만 있으면 Compose·Hilt·Navigation·core 연결이 다 된다.

## 3. route 규칙

type-safe 방식(`@Serializable` 클래스)으로 만든다. 계획서의 문자열 규칙과 1:1로 대응한다.

| 화면 | 계획서 route | 코드 | 인자 |
|---|---|---|---|
| S-01 로그인 | `login` | `LoginRoute` | – |
| S-02 회원가입 | `signup` | `SignupRoute` | – |
| S-03 메인 달력 | `calendar/{userId}?readOnly=` | `CalendarRoute(userId, readOnly)` | `userId: Long`, `readOnly: Boolean = false` |
| S-04 촬영 | `capture` | `CaptureRoute` | – |
| S-05 하루 상세 | `day/{userId}/{date}?readOnly=` | `DayRoute(userId, date, readOnly)` | `date: String` (`"yyyy-MM-dd"`) |
| S-07 친구 | `friend` | `FriendRoute` | – |
| S-12 마이페이지 | `mypage` | `MyPageRoute` | – |

- 친구 달력은 **같은 S-03·S-05를 친구 userId + `readOnly = true`로 연다.** 읽기 전용이면 촬영·삭제·지금 만들기 버튼을 숨긴다.
- 날짜는 `LocalDate.toString()` 결과(`"2026-11-04"`)를 그대로 쓴다.

## 4. feature 모듈이 밖으로 공개하는 것

각 feature는 `navigation/XxxNavigation.kt` 한 파일에 아래 세 가지만 `public`으로 둔다. 화면 Composable은 `internal`.

1. route 클래스 (`@Serializable`)
2. `NavController.navigateToXxx(...)` — 이 화면으로 가는 함수
3. `NavGraphBuilder.xxxScreen(콜백들)` — NavHost에 화면을 등록하는 함수

## 5. ★ A ↔ B 계약: 촬영 화면

`feature/capture/src/main/java/com/setmon/feature/capture/navigation/CaptureNavigation.kt`

```kotlin
fun NavGraphBuilder.captureScreen(
    onUploadComplete: () -> Unit, // 업로드 성공 시 B가 호출 → A가 달력으로 돌아가 새로고침
    onBack: () -> Unit,           // 촬영을 그만두고 나갈 때 B가 호출
)
```

- B는 `CaptureScreen.kt` 내부와 `feature:capture` 안의 파일을 자유롭게 만든다 (ViewModel, ClipRecorder, 업로드 모듈 등).
- **이 함수의 이름과 파라미터를 바꿔야 하면 A와 먼저 합의한다.** 바뀌면 `app`의 `SetmonNavHost`도 같이 고쳐야 한다.
- 촬영 알림 탭 → 촬영 화면 바로 열기(3주차)는 A가 `app`에서 `navigateToCapture()`로 처리한다.
- CameraX 등 녹화 라이브러리는 B가 `libs.versions.toml`에 추가하고 `feature/capture/build.gradle.kts`의 `dependencies`에 넣는다.

## 6. 처음 받아서 실행하는 법

1. `git clone` 후 Android Studio로 폴더 열기 (**경로에 한글·공백이 없어야 한다**, 예: `D:\dev\FE_dev`)
2. A에게 받은 `google-services.json`을 `app/` 폴더에 넣기 (git에 올라가지 않는 파일. 지금은 없어도 빌드되지만, Firebase를 붙인 뒤부터는 꼭 필요)
3. Gradle sync → 에뮬레이터나 폰에서 ▶ 실행
4. 로그인(Mock) → 달력 → 촬영 버튼으로 촬영 화면이 열리면 정상

### Mock 로그인·가입 (서버 연결 전, `core:data` FakeAuthRepository)

- 형식만 맞으면 성공한다: 이메일 형식, 비밀번호 8자 이상, 닉네임 2~10자
- 일부러 실패를 보고 싶을 때
  - 가입 이메일 `taken@setmon.app` → "이미 가입된 이메일이에요"
  - 로그인 비밀번호 `wrongpassword` → "이메일 또는 비밀번호가 맞지 않아요"
  - 이메일 `offline@setmon.app` → 네트워크 오류
- 실서버로 바꿀 때는 `core/data/.../di/DataModule.kt` 의 `@Binds` 한 줄만 바꾼다

### FCM 토큰 확인

`app/google-services.json` 을 넣고 실행 → Logcat 에서 `SetmonFCM` 으로 검색하면 `FCM token = ...` 이 찍힌다.
파일이 없으면 "Firebase 미설정" 경고만 찍히고 앱은 그대로 동작한다.

## 7. 버전 정보

- AGP 9.3.3 · Gradle 9.5.0 · Kotlin 2.2.10 (AGP 9 built-in Kotlin, `kotlin-android` 플러그인 없음)
- compileSdk / targetSdk 37 · minSdk 26 · Java 11
- Hilt 2.59.2 · KSP 2.3.6 (AGP 9의 공식 최소 버전 이상)
- Navigation Compose 2.10.2
