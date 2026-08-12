---
name: promote-doc
description: Promote a department draft under app/docs/departments/ into a confirmed spec under app/docs/specs/, or move any documentation file, without breaking cross-references. Use when the user approves a draft, when a doc needs to change status or owner, or when any markdown file under app/docs is relocated.
---

# 문서 승격 / 이동

`app/docs/departments/<부서>/`의 초안을 `app/docs/specs/<부서>/`의 확정 명세로 올린다.
문서 이동 전반(부서 간 이동, 오분류 교정)에도 같은 절차를 쓴다.

경로 규칙은 `AGENTS.md`의 "Documentation layout" 절이 원본이다. 상태가 최상위 디렉터리를,
소유 부서가 하위 디렉터리를 정한다.

## 전제 — 승격 조건

**사용자 승인 없이는 승격하지 않는다.** 초안은 "Claude가 보기에 완성됐다"가 아니라
"사용자가 확정했다"일 때만 명세가 된다.

승격 시 상충하는 기존 명세가 있으면 **함께 개정하거나 폐기**한다. 같은 주제를 다루는 문서가 둘 남으면
어느 쪽이 진실인지 알 수 없게 된다 — 실제로 이 저장소에서 프리미엄 기능 스펙이 세 문서로 갈라져
서로 다른 내용을 주장한 적이 있다.

## 절차

### 1. 이동 전 — 참조 조사

```bash
# 대상 문서를 참조하는 모든 곳 (경로 형태 + 파일명만 있는 형태 둘 다)
git ls-files '*.md' | xargs grep -n "<파일명>"
```

두 종류를 구분해서 세어둔다.

- **경로 참조**: `app/docs/departments/planner/prd.md` — 이동 후 반드시 깨진다
- **파일명만**: `` `prd.md` `` — 같은 디렉터리 안이면 유효, 디렉터리를 넘으면 오해를 부른다

### 2. 이동

```bash
git mv app/docs/departments/<부서>/<파일>.md app/docs/specs/<부서>/<파일>.md
```

**반드시 `git mv`를 쓴다.** 복사 후 삭제하면 rename으로 인식되지 않아 히스토리가 끊긴다.
에셋(이미지 등)을 참조하는 문서라면 **에셋도 함께 옮긴다.**

### 3. 참조 갱신

1단계에서 찾은 곳을 모두 고친다. 디렉터리를 넘는 참조는 **상대 경로로 명시**한다
(예: `../planner/usecase.md`). 파일명만 적혀 있으면 나중에 읽는 사람이 어디 있는지 알 수 없다.

문서 자신이 갖고 있는 참조도 잊지 않는다 — 위치가 바뀌면 상대 경로의 기준점이 바뀐다.

### 4. 상태 배지 교체

초안 배지를 지우고 명세임을 표시한다.

```markdown
> [!NOTE]
> **상태: 초안 — 사용자 확정 전.** ...
```

승격 후에는 위 블록을 제거하고, 필요하면 확정일과 대체된 문서를 밝힌다.

### 5. 상충 문서 처리

승격된 문서가 기존 명세를 대체한다면 셋 중 하나를 택한다.

- **개정**: 기존 문서의 해당 절만 새 내용으로 교체
- **폐기**: `git rm` 후 참조를 새 문서로 돌림
- **보관**: `departments/`로 내리고 상단에 "보관 문서 — 살아있는 명세가 아니다" 명시
  (`departments/developer/development-todo.md`가 이 사례)

**아무것도 하지 않는 선택지는 없다.** 방치하면 문서가 서로 다른 주장을 하게 된다.

### 6. 기록과 검증

`app/docs/orchestration/task-log.md`에 승격 사실을 남긴다.

```bash
# 구 경로가 남아 있지 않은지 확인
git ls-files '*.md' | xargs grep -n "departments/<부서>/<파일>.md" || echo "잔존 없음"

# rename으로 인식됐는지 확인 (R 표시여야 함)
git status --short
```

## 체크리스트

- [ ] 사용자 승인을 받았다
- [ ] `git mv`를 썼고 `git status`에 `R`로 표시된다
- [ ] 경로 참조를 모두 갱신했다
- [ ] 디렉터리를 넘는 파일명 참조를 상대 경로로 바꿨다
- [ ] 문서 자신의 상대 경로 기준점을 다시 계산했다
- [ ] 초안 상태 배지를 제거했다
- [ ] 상충하는 기존 문서를 개정·폐기·보관 중 하나로 처리했다
- [ ] `task-log.md`에 기록했다
- [ ] 구 경로 잔존 0건을 확인했다
