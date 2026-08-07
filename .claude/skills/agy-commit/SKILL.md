---
name: agy-commit
description: Commit already-staged changes and push to the current branch via the agy CLI instead of running git commit/push directly. Use whenever Claude has staged changes in this repository and they're ready to commit — this is the standard way commits happen in this project, not a fallback.
---

# agy-commit

Committing and pushing are mechanical, low-reasoning steps — delegate them to `agy` to conserve Claude usage, once Claude has already staged the right files with `git add`. Claude should never run `git commit` or `git push` directly in this repo; that's the whole point of this skill.

## Division of labor

- **Claude**: decides what changed, runs `git status`/`git diff` to review, `git add` the right files, drafts the commit message.
- **agy**: executes the actual `git commit` and `git push` commands.

## Invocation pattern

```
cd "<repo root>" && agy -p 'Using your shell tool, run these two commands in order in the <repo name> git repository, exactly as given, and report the exact output of each:

COMMAND 1:
git commit -m "<full commit message, including Co-Authored-By trailer>"

COMMAND 2:
git push origin <branch>

Do not run any other git commands (no reset, no force-push, no rebase, no amend).' --model gemini-3.5-flash-medium --dangerously-skip-permissions
```

Use `gemini-3.5-flash-medium` — this is a mechanical task, no reasoning needed, and it keeps the delegation cheap.

## Gotchas

- Put the full, final commit message (including the `Co-Authored-By: Claude Sonnet 5 <noreply@anthropic.com>` trailer) directly in the prompt text — don't ask `agy` to compose it.
- No trailing punctuation immediately after a literal command/value in the prompt (see `agy-research` skill) — it can get appended to the git command and break it (e.g. `git push origin <branch> .` failing with `invalid refspec '.'`).
- Explicitly forbid other git commands in the prompt every time (`no reset, no force-push, no rebase, no amend`) — don't rely on it being implied.
- After the push, verify with `git fetch origin <branch> && git log origin/<branch> --oneline -2` to confirm the remote actually advanced — don't just trust agy's reported output.
