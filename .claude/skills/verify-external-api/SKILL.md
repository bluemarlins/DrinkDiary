---
name: verify-external-api
description: Verify a third-party library's exact API (function signatures, parameter order, import paths) against the real artifact and official samples before writing code against it. Use whenever calling a library this project hasn't used that way before — especially charting/UI libraries whose APIs churn between major versions. Don't write the call from memory and let the compiler find out.
---

# verify-external-api

Model memory of a library API is unreliable for anything that has had a major version bump. This project has both failure modes on record with the same library (Vico): guessing the API produced a compile error and a wasted build cycle; verifying it first produced a first-try successful build of a non-trivial custom chart. The verification takes a few minutes and is cheaper than the retry loop.

Do this **before** writing the call, not after the compiler complains.

## 1. Find the real artifact in the Gradle cache

The dependency is already resolved and downloaded — the ground truth is on disk.

```
Get-ChildItem -Path "$env:USERPROFILE\.gradle\caches\modules-2\files-2.1\<group.id>" -Recurse -File | Select-Object FullName
```

Use PowerShell for this. A recursive `find` over `~/.gradle/caches` from Bash is slow enough to hit the tool timeout and get backgrounded — it has done so in this project. Scoping the path to the exact group ID makes it instant either way.

Note that Android libraries ship as `.aar`, and the KMP ones publish the platform artifact under a `-android` suffixed module (e.g. `compose-android/3.2.3/…/compose.aar`, not `compose/3.2.3/`). The non-suffixed directory often contains only `.pom`/`.module` metadata.

## 2. Read the signatures out of the bytecode

```
unzip -o -q <name>.aar -d aar_out          # .aar → contains classes.jar
unzip -o -q aar_out/classes.jar -d classes
javap -p classes/<package path>/<Name>Kt.class
```

Kotlin top-level functions land in a `<FileName>Kt` class, so a function declared in `Components.kt` is in `ComponentsKt.class`. This gives you **exact parameter order and types**, which is the thing most likely to be wrong from memory. Expect Kotlin's name mangling on functions with inline-class parameters (`rememberLineComponent-zXfTrVk`) — the suffix is irrelevant to how you call it from Kotlin.

What `javap` will *not* give you: default values, and parameter *names*. So it settles "what types, in what order" but not "what is this called" — that's the next step.

## 3. Read an official sample that actually calls it

Library docs describe; samples compile. Fetch the sample sources raw:

```
curl -s "https://api.github.com/repos/<owner>/<repo>/git/trees/master?recursive=1" -o "<scratchpad>/tree.json"
```

Then grep that JSON for the sample paths and pull the interesting files from `https://raw.githubusercontent.com/<owner>/<repo>/master/<path>`. Prefer the sample whose *output* most resembles what you're building — a "styled column chart" sample answers styling questions that the "basic chart" sample doesn't.

This is where you get the parameter names, the import paths, and the idiomatic nesting (e.g. that a column's fill goes through a `Fill(...)` wrapper that accepts either a `Color` or a `Brush`, reached via a `ColumnProvider.series(...)`).

## 4. Docs last, and don't trust a 200

Official doc sites are the least reliable link in the chain. In this project, the library's own guide returned 404s for URLs its own error page recommended, returned pages containing only the company name, and explicitly admitted it only shows call sites rather than signatures. Use docs to confirm an idiom you already saw in a sample, not to discover one.

## Gotchas

- **GitHub code search via the API is unauthenticated-401.** `api.github.com/search/code` requires auth; the web UI at `github.com/search?type=code` returns a "sign in to search code" page to WebFetch. Use the git-trees API (step 3) instead — it needs no auth.
- **Write scratch files to the scratchpad, not `/tmp`.** In this Windows/git-bash environment `curl -o /tmp/x.json` writes somewhere the Read/Grep tools can't then find (they resolve relative to the repo working directory), which looks like the download silently failing.
- **Don't downgrade the library to make an "unresolved reference" go away.** If the symbol isn't there, the call shape is wrong — go back to step 2. The version resolved in `libs.versions.toml` is the one to code against.

## After it compiles

Record the verified API shape in the relevant doc — for UI components that's the constraints section of `app/docs/design/design-system.md`, noting explicitly that it was verified against the artifact and samples rather than assumed. That way the next change to the same component doesn't repeat this whole procedure.
