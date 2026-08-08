---
name: sync-design-docs
description: Sync the design/product docs after adding or changing a DD* Compose component, then commit. Use at the end of any UI component change — new component, changed parameters, or changed visual behavior — so design-system.md and the roadmap stay the source of truth instead of drifting behind the code.
---

# sync-design-docs

The docs under `app/docs/` define the actual product rules for this project, not background reading. A component change that isn't reflected there means the next session reads a stale spec and re-derives (or contradicts) a decision that was already made. Run this every time, before committing.

## 1. Update `app/docs/design/design-system.md`

Three places, in this order:

**a. The component table** in the right section — §7 Selection & Filter / §8 Display / §9 Feedback / §10 Domain. Add a row for a new component, or edit the 역할 column if behavior changed. Keep the 사용 위치 column accurate; it's how the next reader finds call sites.

**b. The 제약 조건 bullets** under that same table. This is the highest-value part and the easiest to skip. Record:
- Non-obvious parameter contracts (e.g. a state-hoisted `expanded`/`onToggle` pair, an opt-in nullable parameter that's `null` by default so existing call sites don't change).
- **Failures you actually hit**, phrased as a rule with the reason. Not hypothetical advice — this file's value comes from every bullet being something that genuinely broke once. Examples already in there: passing an empty series to the chart crashes it, so guard it; stacking fixed-height items above a weighted scroll area starves the scroll area, so use one `LazyColumn`.
- Verified third-party API shapes, noting they were verified against the artifact rather than assumed (see the `verify-external-api` skill).

**c. §11 화면별 사용 컴포넌트** — the per-screen table. If a component moved position, was added to a screen, or a screen's composition order changed meaningfully, update that screen's rows. Row labels can carry ordering/state hints (e.g. "캘린더(접힘 기본, 최하단)").

If the change breaks a rule that §14 (AI 에이전트 작업 금지 규칙) should have prevented — or reveals a new class of mistake — add a row to that table too, with the concrete failure in the 이유 column.

## 2. Log the round in `app/docs/service-launch-roadmap.md`

Append an entry in §4's running log, following the established shape:

```
**<한 줄 제목> (YYYY-MM-DD)** — <왜 이 작업을 했는지: 사용자 피드백이나 계기>.

- <변경 사항 항목별로>
- <겪은 버그가 있으면 원인과 해결까지>
- <검증 방법과 결과>
```

Write what was actually done and what actually broke, including things that were **deliberately left out of scope** and why — a reader six rounds later needs to know an omission was a decision, not an oversight. Convert relative dates to absolute.

## 3. Commit via `agy-commit`

`git add` the specific files (never `-A`), then hand the commit and push to the `agy-commit` skill. Do not run `git commit` or `git push` directly in this repo — that delegation is the whole point of that skill. Afterwards verify the remote actually moved:

```
git fetch origin <branch> && git log origin/<branch> --oneline -2
```

## Scope note

This is for design-system-level changes. Docs owned by other departments follow their own routing — see the table in the `agy-research` skill for where research/legal/marketing output belongs. If a component change also changes a **product rule** (what the app does, not how it looks), `app/docs/product/usecase.md` needs updating too, and that's a bigger decision worth surfacing to the user rather than folding silently into a UI commit.
