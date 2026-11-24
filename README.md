# MyHikingLog

<img src="docs/brand/app-icon-acorn-1024.png" width="96" alt="도토리한알 앱 아이콘">

앱 이름: **도토리한알** (영어 기기: Acorn Log). 팝콘한알(MyMovieLog) 시리즈 두 번째 앱.

나만의 등산 기록 앱.
산 찾기 → 완등 기록 → 100대 명산 도토리 도장판 → 지도·통계로 확인.

| 앱 | 언어 / UI | 폴더 |
| --- | --- | --- |
| Android | Kotlin + Jetpack Compose | [`android/`](android/) |
| iOS | Swift + SwiftUI (Android 이후 이식 예정) | — |

앱 아이콘: 흰 바탕 위 실사 도토리 한 알 (Figma AI 생성, 팝콘한알과 같은 방식). 원본·다른 시안은 `docs/brand/`.

디자인: "도토리 숲" (크림 바탕, 이끼 초록 채움, 도토리 갈색 글자, 제목 글꼴 Jua, 라이트 테마).

## 진행 단계

| 단계 | 내용 | 상태 |
| --- | --- | --- |
| 1 | 기반: 프로젝트, 로그인, 디자인 시스템, 탭(홈·산 찾기·도장판·통계·프로필) | ✅ |
| 2 | 산 찾기: 공공데이터 산 목록 검색, 상세 정보 | |
| 3 | 완등 기록: 날짜·코스·소요 시간·날씨·동행·별점·한줄평 | |
| 4 | 도장판: 100대 명산 도토리 도장 | |
| 5 | 지도 뷰: 네이버 지도에 다녀온 산 표시 | |
| 6 | 통계와 캘린더 | |
| 7 | 사진 첨부와 촬영 | |

## 실행 준비

1. [Supabase](https://supabase.com)에서 프로젝트를 만들고 Authentication에서 이메일 로그인 켜기.
   Google 로그인은 Redirect URL에 `myhiking://login-callback` 추가.
2. `android/local.properties.template`을 `android/local.properties`로 복사 후 값 채우기.
3. Android Studio로 `android/` 폴더 열기.

- 키가 비어 있으면 앱은 뜨지만 로그인 안 됨.
