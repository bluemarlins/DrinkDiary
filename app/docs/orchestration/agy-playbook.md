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

`agy agents`가 비어 있으므로 별도 에이전트 프로필에 의존하지 않고, 태스크 유형별로 아래 프롬프트
템플릿 + 모델 + 플래그 조합을 그때그때 구성한다.

## 공통 규칙

- 모든 프롬프트 서두에 `app/docs/orchestration/harness.md`를 반드시 참조하도록 명시한다.
- 편집 범위는 항상 `--add-dir`로 최소 서브트리에 한정한다(리포지토리 루트 전체를 열어주지 않는다).
- 비대화형 모드는 승인 프롬프트에 응답할 수 없으므로 편집이 필요한 태스크는
  `--dangerously-skip-permissions`가 불가피하다. **대신 결과 diff는 병합 전 Claude가 반드시
  검수·테스트한다** — 신뢰는 사전 승인이 아니라 사후 검수로 확보한다.
- 태스크 완료 후에는 `harness.md`의 Definition of Done을 그대로 적용한다.

## 태스크 유형별 템플릿

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

Claude는 반환된 텍스트를 파싱해 `app/docs/product-plan.md`의 "경쟁/벤치마킹 조사" 절에 정리
반영한다. 파일 수정이 없는 태스크이므로 리스크가 낮고, 파이프라인 검증용 첫 호출로 적합하다.

### 2. 디자인 시스템 범위 내 UI 고도화

```bash
agy -p "$(cat <<'EOF'
[컨텍스트] app/docs/orchestration/harness.md, app/docs/design-system.md 규칙을 반드시 따른다.
[작업 범위] <구체적 화면/컴포넌트 경로>
[요청] <구체적 UI 개선 내용>. 기존 DD* 공용 컴포넌트를 재사용하고, 새 하드코딩 색상/치수를
추가하지 마라. 이 태스크와 무관한 파일은 건드리지 마라.
EOF
)" --model gemini-3.6-flash-high --mode accept-edits \
   --add-dir app/src/main/java/com/bluemarlin/drinkdiary/ui/<feature> \
   --output-format json --dangerously-skip-permissions
```

### 3. 반복적 코드 작성 (UseCase/Mapper/DAO 보일러플레이트)

```bash
agy -p "$(cat <<'EOF'
[컨텍스트] app/docs/orchestration/harness.md의 아키텍처 규칙(UI->ViewModel->UseCase->Repository->DAO)
을 반드시 따른다.
[요청] <구체적 UseCase/Mapper/DAO 작업 내용 + 대응 단위 테스트 작성 요청>
EOF
)" --model gemini-3.1-pro-high --mode accept-edits \
   --add-dir app/src/main/java/com/bluemarlin/drinkdiary/domain \
   --add-dir app/src/main/java/com/bluemarlin/drinkdiary/data \
   --add-dir app/src/test/java/com/bluemarlin/drinkdiary \
   --output-format json --dangerously-skip-permissions
```

### 4. 후속 지시 (같은 태스크 이어가기)

```bash
agy -p "<수정 지시>" -c --output-format json
```

또는 `--conversation <id>`로 특정 세션을 명시 재개한다.

## Claude가 위임하지 않는 것

- 비즈니스 모델/수익화 정책 결정, PRD 작성 → Claude가 직접.
- 아키텍처 결정(예: Billing 연동 방식, 마이그레이션 전략) → Claude가 직접.
- 코드 리뷰, 테스트 설계·검수, 커밋/push 판단 → Claude가 직접.
- `agy`의 산출물은 항상 Claude의 리뷰를 거친 뒤에만 커밋된다.
