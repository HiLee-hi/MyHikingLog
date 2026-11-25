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
| 2-1 | 산 찾기: 100대 명산 검색, 지역·난이도·높이 필터, 상세 정보 | ✅ |
| 2-2 | 산 찾기: 산림청 산정보 API로 전체 산 검색, 사진 | ✅ |
| 3 | 완등 기록: 날짜·코스·소요 시간·날씨·동행·별점·한줄평 | ✅ |
| 4 | 도장판: 100대 명산 도토리 도장 | |
| 5 | 지도 뷰: 네이버 지도에 다녀온 산 표시, 산 찾기 필터 결과만 핀 표시 | |
| 6 | 통계와 캘린더 | |
| 7 | 사진 첨부와 촬영 | |

## 실행 준비

1. [Supabase](https://supabase.com)에서 프로젝트를 만들고 Authentication에서 이메일 로그인 켜기.
   Google 로그인은 Redirect URL에 `myhiking://login-callback` 추가.
   SQL Editor에서 [`supabase/summit_records.sql`](supabase/summit_records.sql) 실행 (완등 기록 테이블, RLS).
2. `android/local.properties.template`을 `android/local.properties`로 복사 후 값 채우기.
   `FOREST_API_KEY`(산 검색·사진): [공공데이터포털 산림청 산정보 서비스](https://www.data.go.kr/data/15058662/openapi.do) 활용신청 후 **일반 인증키(Decoding)**.
3. Android Studio로 `android/` 폴더 열기.

- 키가 비어 있으면 앱은 뜨지만 로그인 안 됨. `FOREST_API_KEY`가 없으면 100대 명산만 검색, 사진 없음.
- 키를 바꾸면 `./gradlew clean` 후 빌드 (BuildConfig에 이전 값이 남음).

## 산 데이터

100대 명산은 앱에 번들, 오프라인으로 읽음.

- 출처: [산림청 국립자연휴양림관리소 숲나들e 숲길 100대명산 정보](https://www.data.go.kr/data/15112801/fileData.do) (공공데이터포털, 이용허락범위 제한 없음)
- 원본 CSV: `data/source/top100_mountains_20250421.csv`
  (내려받은 파일 이름: `산림청 국립자연휴양림관리소_숲나들e 숲길 100대명산 정보_20250421.csv`)
- 앱용 JSON: `android/app/src/main/assets/top100_mountains.json`

포털은 새 버전이 올라오면 예전 파일을 내림 → JSON 재생성용으로 원본도 저장소에 둠.
새 CSV는 `data/source/top100_mountains_<날짜>.csv`로 넣고 변환 스크립트 재실행.

```sh
python3 tools/convert_top100.py \
  data/source/top100_mountains_20250421.csv \
  android/app/src/main/assets/top100_mountains.json
```

- HTML로 된 본문은 텍스트로 풀고, 산행 코스 표는 코스별 경로·소요 시간으로 분리.
- 변환 후 `./gradlew :app:testDebugUnitTest` → `Top100ParserTest`가 100곳 들어갔는지 확인.

공식 난이도는 29곳뿐. 나머지는 추천 코스 시간으로 계산해서 "예상 난이도"로 표시 (3시간 미만 초급, 5시간 미만 중급, 그 이상 상급).

### 산림청 산정보 API (100대 명산 밖의 산)

검색어가 있으면 [산림청 산정보 서비스](https://www.data.go.kr/data/15058662/openapi.do)(국내 산 4,700여 곳, XML)에서 다른 산도 검색.

- 산 목록 `mntInfoOpenAPI2`(`searchWrd`), 사진 `mntInfoImgOpenAPI2`(`mntiListNo`). 사진 파일 경로: `https://www.forest.go.kr/images/data/down/mountain/`.
- 100대 명산과 같은 산(이름 앞부분이 같고 시·도가 겹침)은 API 결과 대신 번들 데이터로 표시. "백운대" 검색 → 북한산.
- 같은 이름 산이 여러 지역에 있음(갈미봉 11곳) → 목록에 시·군까지 표시.
- API 산은 좌표·난이도·코스 없음, 높이 0인 곳도 있음 → 난이도·높이 옵션 선택 시 제외, 5단계 지도 핀 불가(좌표 출처 필요).
- 사진은 250×178 → 상세 화면에 작은 사진 줄로. 100대 명산 사진도 같은 API에서 찾음.
- 결과가 많으면(한 글자 검색 등) 앞 100곳만 받음.

### 텍스트 정리 (CSV, API 공통)

문장은 원문 그대로, 형식만 `data/SourceText.kt`에서 똑같이 정리.

- 문단·목록 줄바꿈 (CSV는 변환 스크립트가 공백 여러 칸을 줄바꿈으로 복원, API는 원래 줄바꿈)
- 마침표 뒤 띄어쓰기, 전각 기호·`ㆍ`·`->` 통일
- CSV 개요 앞 `[제목]`은 상세 화면 이름 아래에 따로 표시
- 코스 이름 띄어쓰기(`기타코스1` → `기타 코스 1`), 코스 경로 `-` → `→`
- `ForestMountainParserTest`는 실제 응답을 저장한 `android/app/src/test/resources/forest/`를 읽음.

## 완등 기록

Supabase `summit_records` 테이블에만 저장. 로그인하면 앱이 전체를 한 번 받아 메모리에 두고, 저장·삭제할 때마다 반영.

- 인터넷이 없으면 저장 안 됨 (오프라인 저장은 나중에 Room으로).
  그때 바로 쓸 수 있게 id(UUID)는 앱에서 만들고, `updated_at`, `deleted_at`(삭제 표시) 컬럼을 미리 둠.
- 산 이름·높이·시·도는 기록에 같이 저장. API 산은 나중에 id로 다시 찾을 수 없어서.
- 100대 명산은 추천 코스를 칩으로 골라 코스·소요 시간을 채울 수 있음.
- 기록 카드는 코스 없이 한줄평 한 줄만 표시. 전체 내용은 카드를 눌러 기록 화면에서.
- 산 찾기 화면에서 API 산 상세를 연 채 앱이 종료됐다가 돌아오면 기록 화면에 "기록을 찾을 수 없어요"가 뜸 (검색 결과가 메모리에만 있음).

