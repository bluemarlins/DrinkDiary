# agy CLI Playbook

`agy`는 로컬에 설치된 Google Antigravity CLI(v1.1.9, `C:\Users\wooga\AppData\Local\agy\bin\agy.exe`)로,
Claude Code와 유사하게 비대화형 `-p/--print` 모드로 단발성 프롬프트를 실행하고 파일 편집까지
수행할 수 있다. 이 문서는 Claude가 `agy`를 서브에이전트로 호출할 때 쓰는 표준 명령 템플릿을
정의한다. 실행은 항상 Claude(오케스트레이터)가 Bash 도구로 직접 수행한다.

## 확인된 환경

```powershell
agy --version        # 1.1.9
agy models           # gemini-3.6-flash-{high,medium,low}, gemini-3.5-flash-{high,medium,low},
                      # gemini-3.1-pro-{high,low}, claude-sonnet-4-6, claude-opus-4-6-thinking,
                      # gpt-oss-120b-medium
agy agents           # (현재 비어 있음 — 사전 정의된 에이전트 프로필 없음)
```

위 `agy models` 목록은 **CLI가 노출하는 전체 목록일 뿐 사용 허가 목록이 아니다.** 이 저장소가
실제로 쓰는 것은 `harness.md` §6-2의 4개 로스터뿐이다.

`agy agents`가 비어 있으므로 별도 에이전트 프로필에 의존하지 않고, 태스크 유형별로 아래 프롬프트
템플릿 + 모델 + 플래그 조합을 그때그때 구성한다.

## 공통 규칙

- 모든 프롬프트 서두에 `app/docs/orchestration/harness.md`를 반드시 참조하도록 명시한다.
- 편집 범위는 항상 `--add-dir`로 최소 서브트리에 한정한다(리포지토리 루트 전체를 열어주지 않는다).
- 비대화형 모드는 승인 프롬프트에 응답할 수 없으므로 편집이 필요한 태스크는
  `--dangerously-skip-permissions`가 불가피하다. **대신 결과 diff는 병합 전 Claude가 반드시
  검수·테스트한다** — 신뢰는 사전 승인이 아니라 사후 검수로 확보한다.
- 태스크 완료 후에는 `harness.md`의 Definition of Done을 그대로 적용한다.
- 프롬프트에 **"프로젝트에 없는 라이브러리를 새로 도입하지 마라"**를 명시한다. 명시하지 않으면
  MockK 같은 미설치 의존성을 임의로 끌어와 컴파일이 깨진다(2026-08-14 실제 사례).
- **`--add-dir`를 줘도 파일이 저장소에 안 써질 수 있다.** agy 자신의 스크래치
  (`~/.gemini/antigravity-cli/scratch/<프로젝트명>/`)에 쓰는 경우가 반복 확인됐다. 호출 후
  `git status`가 비어 있다고 실패로 단정하지 말고 스크래치 경로를 반드시 확인한다.
- **테스트 코드는 위임 결과를 그대로 쓰지 않는다.** 검증은 Claude 전담이다
  (`persona-registry.artifact.md` 5번). 구현은 받되 테스트는 직접 쓴다.

## 태스크 유형별 템플릿

**로스터 제약**: `--model`에는 `harness.md` §6-2의 4개 모델만 쓴다 —
`gemini-3.5-flash-medium`, `gemini-3.6-flash-high`, `gemini-3.1-pro-high`, `claude-sonnet-4-6`.
`agy models`가 보여주는 나머지 모델은 이 저장소에서 사용하지 않는다.

아래 각 템플릿의 `--model` 값은 **그 태스크 유형에서 가장 흔한 경우의 기본값**이다. 실제 태스크의
난이도/성격이 전형적인 경우와 다르면(예: "반복 코드"인데 로직이 복잡함) §6-2 로스터 표의 역할
기준으로 다시 판단해 모델을 교체한다 — 템플릿 번호가 아니라 §6-2가 최종 근거다. 게이트를 2회 연속
통과하지 못하면 §6-2의 승급 경로를 따른다.

### 1. 벤치마킹/경쟁 앱 조사 (파일 수정 없음)

```bash
agy -p "$(cat <<'EOF'
[역할] 너는 모바일 앱 시장 조사 담당이다. 아래 조건에 맞는 벤치마킹 리포트를 markdown으로만
출력해라. 파일을 수정하지 마라.

[조사 대상] Google Play Store에 있는 개인 음주/취향 기록 앱 3~5개
[정리 항목] 앱 이름, 핵심 기능, 무료/프리미엄 기능 구분, 가격 정책, DrinkDiary 대비 차별점 제안
EOF
)" --model gemini-3.5-flash-medium --output-format json
```

Claude는 반환된 텍스트를 검수해 `app/docs/departments/researcher/`에 리포트로 남긴다. 검증 절차는
`delegate-research` 스킬을 따른다. 파일 수정이 없는 태스크이므로 리스크가 낮고, 파이프라인 검증용 첫 호출로 적합하다.

### 2. 디자인 시스템 범위 내 UI 고도화

```bash
agy -p "$(cat <<'EOF'
[컨텍스트] app/docs/orchestration/harness.md, app/docs/specs/designer/design-system.md 규칙을 반드시 따른다.
[작업 범위] <구체적 화면/컴포넌트 경로>
[요청] <구체적 UI 개선 내용>. 기존 DD* 공용 컴포넌트를 재사용하고, 새 하드코딩 색상/치수를
추가하지 마라. 이 태스크와 무관한 파일은 건드리지 마라.
EOF
)" --model gemini-3.6-flash-high --mode accept-edits \
   --add-dir app/src/main/java/com/bluemarlin/drinkdiary/ui/<feature> \
   --output-format json --dangerously-skip-permissions
```

### 3. 반복적 코드 작성 (UseCase/Mapper/DAO)

이 유형은 §6-2의 "반복/보일러플레이트" 축과 "추론 깊이" 축 중 **어느 쪽에 더 가까운지**를 먼저
판단한다. 둘을 하나의 모델로 뭉뚱그리지 않는다(과거 P2-2는 계산 로직이 복잡해 pro-high를 썼고,
P2-5는 단순 CRUD라 flash-medium으로 충분했다 — 겉보기엔 둘 다 "UseCase 구현"이었지만 실제 배정은
갈렸다).

#### 3-a. 기존 패턴을 그대로 복제하는 단순 CRUD/Mapper

```bash
agy -p "$(cat <<'EOF'
[컨텍스트] app/docs/orchestration/harness.md의 아키텍처 규칙(UI->ViewModel->UseCase->Repository->DAO)
을 반드시 따른다.
[요청] <구체적 UseCase/Mapper/DAO 작업 내용 + 대응 단위 테스트 작성 요청>
EOF
)" --model gemini-3.5-flash-medium --mode accept-edits \
   --add-dir app/src/main/java/com/bluemarlin/drinkdiary/domain \
   --add-dir app/src/main/java/com/bluemarlin/drinkdiary/data \
   --add-dir app/src/test/java/com/bluemarlin/drinkdiary \
   --output-format json --dangerously-skip-permissions
```

#### 3-b. 다단계 도메인 규칙/계산 로직이 새로 들어가는 UseCase

```bash
agy -p "$(cat <<'EOF'
[컨텍스트] app/docs/orchestration/harness.md의 아키텍처 규칙(UI->ViewModel->UseCase->Repository->DAO)
을 반드시 따른다.
[요청] <구체적 UseCase 작업 내용 — 계산식/분기 규칙을 프롬프트에 명시 + 대응 단위 테스트 작성 요청>
EOF
)" --model gemini-3.1-pro-high --mode accept-edits \
   --add-dir app/src/main/java/com/bluemarlin/drinkdiary/domain \
   --add-dir app/src/main/java/com/bluemarlin/drinkdiary/data \
   --add-dir app/src/test/java/com/bluemarlin/drinkdiary \
   --output-format json --dangerously-skip-permissions
```

두 경우 모두 게이트(ktlint/lint/test)를 2회 연속 실패하면 §6-2 승급 경로에 따라 3-a → 3-b 순서로,
3-b도 실패하면 `claude-sonnet-4-6`으로 전환한다. sonnet에서도 실패하면 위임을 중단하고 Claude가
직접 작성한다.

#### 3-c. 최종 승급 (Gemini 계열이 반복 실패한 경우)

```bash
agy -p "$(cat <<'EOF'
[컨텍스트] app/docs/orchestration/harness.md의 아키텍처 규칙을 반드시 따른다.
[이전 시도] <어떤 모델로 무엇이 실패했는지 — 게이트 오류 메시지 포함>
[요청] <구체적 작업 내용 + 대응 단위 테스트 작성 요청>
EOF
)" --model claude-sonnet-4-6 --mode accept-edits \
   --add-dir <최소 서브트리> \
   --output-format json --dangerously-skip-permissions
```

이 템플릿을 쓴 결과는 성공/실패와 무관하게 `harness.md` §6-2 로스터 표의 `claude-sonnet-4-6` 행
"실사용 이력"에 기록한다(현재 비어 있음).

### 4. 이미지 생성 (아이콘/에셋 컨셉)

`agy`는 `generate_image`라는 실제 래스터 이미지 생성 도구를 갖고 있다(확인됨 — 512x512 PNG,
투명 배경(RGBA alpha=0) 생성 가능). **단, 실행 시 결과 파일을 `--add-dir`로 지정한 폴더가 아니라
agy 자신의 기본 스크래치 폴더(`C:\Users\wooga\.gemini\antigravity-cli\scratch`)에 쓰는 것을
확인했다.** 따라서 이미지 생성 태스크 후에는 항상 그 경로를 확인해 파일을 리포지토리의 의도한
위치로 직접 복사해야 한다.

```bash
agy -p "$(cat <<'EOF'
[역할] 너는 Android 앱 아이콘 디자이너다. generate_image 도구로 실제 PNG 이미지를 생성해라.
[요청] <구체적 아이콘/에셋 스펙 — 모티프, 색상(브랜드 팔레트 hex 명시), 크기, 배경 투명 여부>
EOF
)" --model gemini-3.6-flash-high --mode accept-edits --add-dir <스테이징 폴더> \
   --output-format json --dangerously-skip-permissions
# 실행 후 반드시 확인:
ls "$HOME/.gemini/antigravity-cli/scratch"
# 필요한 파일을 리포지토리 스테이징 폴더로 복사한 뒤 사용
```

생성된 이미지는 Claude가 PIL 등으로 알파 채널/해상도를 검증하고, Android 리소스로 편입할 때는
Claude가 직접 밀도별 리사이즈·XML 배선을 수행한다(정밀한 리소스 배치는 agy에 위임하지 않음).

### 5. 후속 지시 (같은 태스크 이어가기)

```bash
agy -p "<수정 지시>" -c --output-format json
```

또는 `--conversation <id>`로 특정 세션을 명시 재개한다.

### 6. 부서별 페르소나 호출 (Persona-specific Execution)

`app/docs/orchestration/persona-registry.artifact.md`에 정의된 페르소나를 호출할 때 사용하는 템플릿이다.

#### [Researcher] 시장 리서치 및 벤치마킹
```bash
agy -p "$(cat <<'EOF'
[Persona] 너는 '테이스트 아카이브'의 Market Researcher다. persona-registry.artifact.md의 정의를 따른다.
[요청] <구체적 리서치 요청>
[출력 형식] markdown 보고서 형식으로 출력하고, 관련 데이터는 표로 정리해라.
EOF
)" --model gemini-3.6-flash-high --output-format json
```

#### [Planner] 제품 전략 및 기획
```bash
agy -p "$(cat <<'EOF'
[Persona] 너는 '테이스트 아카이브'의 Product Planner다. persona-registry.artifact.md의 정의를 따른다.
[요청] <구체적 기획/전략 요청>
[참조] Researcher가 작성한 리서치 결과(app/docs/departments/researcher/market-analysis.md)
EOF
)" --model gemini-3.1-pro-high --mode accept-edits --add-dir app/docs/departments/planner --output-format json
```

## Claude가 위임하지 않는 것

- 비즈니스 모델/수익화 정책 결정, PRD 작성 → Claude가 직접.
- 아키텍처 결정(예: Billing 연동 방식, 마이그레이션 전략) → Claude가 직접.
- 코드 리뷰, 테스트 설계·검수, 커밋/push 판단 → Claude가 직접.
- `agy`의 산출물은 항상 Claude의 리뷰를 거친 뒤에만 커밋된다.
