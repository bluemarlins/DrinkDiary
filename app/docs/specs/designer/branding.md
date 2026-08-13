# 브랜딩 — 테이스트 아카이브

> [!IMPORTANT]
> **부분 유효 문서 (2026-08-13 개정).** 제품이 재정의되면서 이 문서의 절반이 무효가 됐다.
>
> - **여전히 유효**: 앱 이름(1절), 브랜드 컬러·톤앤매너(3절), 아이콘(5절) — 모두 사용자 확정 완료
> - **무효·삭제됨**: 가치 제안, Play 스토어 설명, Free/프리미엄 기능 스펙
>   → 현행은 `../planner/prd.md`와 `../planner/mvp-scope.md`를 따른다
>
> **브랜딩 전면 개정이 예정돼 있다**(`../planner/problem-definition.md` 11절 4번). 타깃 연령 확정 후
> 착수하며, 그때 아래 내용도 재검토 대상이다.

## 1. 애플리케이션 이름 — **확정: 테이스트 아카이브 / Taste Archive**

1차 추천안("그날의 팔레트", "안온한 잔")은 "너무 감성적이고 기능이 직관적으로 안 드러난다"는
피드백으로 반려됐고, 기능 직관성을 최우선 기준으로 `agy`에 재브레인스토밍을 지시해(conversation_id
`c6d24b88-adc0-4e0c-8fca-9cf1ba2313b4`) 재큐레이션한 "테이스트 아카이브 / Taste Archive"가
**사용자 확정**되었다.

- "취향(Taste)을 기록·보관(Archive)한다"는 기능이 한국어·영어 양쪽에서 동일하게 직관적으로
  읽히고, 3절의 브랜드 키워드 "아카이브"와도 자연스럽게 연결된다.
- Android 앱 표시 이름(`app_name`)을 이 이름으로 반영 완료(`res/values/strings.xml`). 개발
  패키지명(`com.bluemarlin.drinkdiary`)과 문서상 프로젝트 코드명 "DrinkDiary"는 그대로 유지 —
  이미 게시 이력이 없는 초기 단계이므로 패키지명 변경의 실익이 없고, 개발자 내부용 식별자와
  사용자 대면 브랜드명을 분리해서 관리한다.
- **참고**: 상표/도메인/스토어 중복 여부는 agy도 실시간 조회 없이 추정한 것이므로, Play Console
  등록 전 실제 이름 중복 검색이 필요하다.
- 기각된 후보/전체 브레인스토밍 로그는 conversation_id `c6d24b88-adc0-4e0c-8fca-9cf1ba2313b4`
  (2차, 채택) 및 `fe5823a5-a196-4a7f-86ee-c66c5cd4caa8`(1차, 참고용)에서 재현 가능.

## 2. 브랜딩 방향

- **톤앤매너**: 조용하고 담백한 개인 아카이브. 소셜/게임화 요소 지양, 과장된 마케팅 톤 지양.
- **키워드**: 셀러(Cellar), 조용한 의식(Quiet ritual), 개인 아카이브, 정직한 팔레트.
- **컬러**: 이미 `ui/theme/Color.kt`/`Theme.kt`에 구현된 팔레트를 그대로 브랜드 컬러로 채택 —
  보틀그린(Primary, `#2F6F4E`), 몰트 앰버(Secondary, `#9C6722`), 와인베리(Tertiary, `#93425E`),
  따뜻한 종이색 배경(`#FFF8F2`). 세 주종을 상징하는 색이 이미 앱 전체 테마와 일치하므로 별도 브랜드
  컬러 신규 정의가 필요 없다.

## 3. 폐기된 절 (2026-08-13)

아래 내용은 재정의로 무효가 되어 삭제했다. 대체 문서를 따른다.

| 폐기된 절 | 무효 사유 | 대체 |
| --- | --- | --- |
| 가치 제안 | "타인의 시선 없는 공간"이 SNS 공유 전략과 충돌 | `../planner/prd.md` 1~2절 |
| Play 스토어 설명 | 맥주 포함, 구 기능 목록 기준 | 브랜딩 개정 시 재작성 |
| Free 기능 스펙 | 맥주 포함, 총 30개 한도 | `../planner/mvp-scope.md` 3절 |
| 프리미엄 기능 스펙 | 셀러 관리·가격 정책이 현행과 불일치 | `../planner/mvp-scope.md` 3절 F6, 과금 축 재검토 중 |

## 4. App Icon — **확정: 컨셉 2 (Archival Ledger & Triple Tab)**

`agy`(gemini-3.6-flash-high)가 제안한 3안(SVG 스케치, conversation_id
`e24a39f0-b513-420a-9457-63f870125116`) 중 컨셉 2(기록장 페이지 + 3색 인덱스 탭)가 사용자
확정되었다. 이후 `agy`의 `generate_image` 도구(실제 래스터 이미지 생성 가능, conversation_id
`654b81c6-e95a-4526-81b1-833e88cb18da`)로 실제 PNG 에셋을 생성해 앱에 반영 완료했다:

- `app/docs/specs/designer/assets/icon-drafts/taste_archive_icon_512.png` — Play 스토어 등록용
  512x512 불투명 아이콘 원본 보관
- `app/docs/specs/designer/assets/icon-drafts/taste_archive_icon_foreground.png` — adaptive icon
  foreground 원본(투명 배경, RGBA 검증 완료) 보관
- Android 리소스 반영: `res/drawable/ic_launcher_background.xml`을 브랜드 보틀그린(`#2F6F4E`)
  단색으로 교체, `res/mipmap-{m,h,x,xx,xxx}hdpi/ic_launcher_foreground.png`를 원본에서 밀도별로
  리사이즈해 교체(기존 Android 기본 로봇 아이콘 대체). `minSdk 35`이므로 API 26 미만 레거시 폴백은
  불필요해 건드리지 않음. `:app:assembleDebug` + `:app:lint`로 리소스 병합 검증 완료.
- **참고**: 이 PNG는 방향 확인 및 즉시 사용 가능한 초안이며, Play 스토어 정식 제출용 최종 고해상도
  에셋은 필요 시 전문 디자인 툴에서 다듬는 것을 권장한다.

## 5. 남은 확인 사항

- [x] 앱 이름 최종 확정 — **테이스트 아카이브 / Taste Archive**
- [x] 아이콘 컨셉 방향 확정 — **컨셉 2 (Archival Ledger & Triple Tab)**, 실제 앱 리소스에 반영 완료
- [ ] Phase 2(MVP 고도화 구현) 착수 승인
