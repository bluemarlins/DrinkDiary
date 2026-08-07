---
name: agy-research
description: Delegate a research, fact-finding, competitive/product analysis, or image-generation task to the agy CLI (Gemini models) instead of doing it directly or guessing from training knowledge. Use for market/competitor research, ASO keyword research, policy/legal fact-finding, product-planning validation, or generating icon/graphic image assets for this project.
---

# agy-research

`agy` is a multi-model CLI (not the `gemini` npm package) with real web search and image-generation tools, pointed at Gemini models. Use it whenever a task calls for external facts, current information, or generated imagery — don't answer from memory and don't try to generate images yourself.

## Invocation pattern

```
cd "<repo root>" && agy -p "<prompt>" --model <model> --dangerously-skip-permissions --print-timeout 8m > <absolute-output-path> 2>&1
```

For quick things without file output, drop the redirect and just read stdout.

## Model selection

| Task type | Model |
|---|---|
| Research (web search, competitive analysis, policy fact-finding) | `gemini-3.1-pro-high` |
| Image generation (icons, graphics, screenshots critique) | `gemini-3.6-flash-high` |
| Mechanical/no-reasoning tasks (see `agy-commit`) | `gemini-3.5-flash-medium` |

Always pass `--model` explicitly — `agy` defaults to a different model family otherwise.

## Gotchas (learned by trial and error this session)

- **Trailing punctuation breaks commands**: don't end a prompt sentence with a period immediately after an instruction that names a literal command/value — `agy` has appended the period into a git ref or similar and broken the command. Phrase instructions so nothing trails the literal value, or put the value on its own line.
- **cwd is not reliably inherited**: `agy`'s shell tool does not consistently pick up the invoking process's working directory. Prompts that shell out (e.g. for `agy-commit`) must not rely on a prior `cd` — give it a self-contained, absolute command.
- **Always require absolute output paths** for any file `agy` writes (research docs, generated images) — relative paths have landed in `agy`'s own scratch directory instead of the intended location.
- **Background long calls**: research/image-gen calls can take 1-8 minutes. Run them with `run_in_background: true` (Bash tool) rather than blocking, especially when running several in parallel.
- **First-run image-generation prompts can fail or degrade** (e.g. asking for a "transparent background" variant of something already generated once returned a flat, detail-less silhouette instead of the expected result). If a generated asset looks wrong, prefer post-processing a known-good prior output (e.g. flood-fill background removal via Pillow) over re-prompting repeatedly.

## Output routing — where research results should be saved

Match the task to the department folder under `app/docs/`:

| Task | Save to |
|---|---|
| Competitor/market analysis, ad monetization benchmarks, personas, ASO keywords | `app/docs/research/` |
| Product/use-case validation, feature gap analysis | `app/docs/product/` (or `research/` if it's raw findings feeding a product decision) |
| Legal/policy fact-finding (Play Console rules, tax treatment, etc.) | `app/docs/research/` with a clear "not legal/tax advice" disclaimer at the top |
| ASO copy, store listing strategy | `app/docs/marketing/` |
| Icon/graphic image assets | `app/store-listing/` (final assets) — document the generation pipeline in `app/docs/design/` or `app/docs/marketing/` |

Every research doc produced this way should open with a short **문서 정보** block noting the tool/model used, the date, and an explicit caveat that AI-researched facts (especially legal/tax/market-size claims) need verification before being treated as ground truth — see existing docs under `app/docs/research/` for the established format.

## After research lands

Update `app/docs/service-launch-roadmap.md`'s relevant Phase section with a short "핵심 발견사항" summary so findings actually change subsequent decisions, not just sit in a doc nobody rereads.
