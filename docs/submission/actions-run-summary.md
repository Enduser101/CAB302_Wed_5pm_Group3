# GitHub Actions run history

Exported 25/10/2026 from the "Build and Test" workflow (`.github/workflows/build.yml`). The full list of runs is in `docs/submission/actions-run-history.csv`; times are Brisbane time.

## Summary

| | |
|---|---|
| Total runs | 188 |
| Passed | 184 (98%) |
| Failed | 4 |
| First run | 19/09/2026 |
| Latest run | 25/10/2026 |
| Runs in the final sprint (from 05/10) | 159 |
| Triggered by a push | 103 |
| Triggered by a pull request | 85 |

## Runs per week

| Week starting | Runs | Passed | Failed | Branches built |
|---|---|---|---|---|
| 14/09 | 19 | 17 | 2 | 8 |
| 21/09 | 8 | 7 | 1 | 5 |
| 28/09 | 2 | 2 | 0 | 2 |
| 05/10 | 41 | 41 | 0 | 14 |
| 12/10 | 42 | 41 | 1 | 10 |
| 19/10 | 76 | 76 | 0 | 23 |

## Runs by branch type

| Branch | Runs |
|---|---|
| `feature/*` | 91 |
| `dev` | 47 |
| `docs/*` | 15 |
| `fix/*` | 5 |
| `chore/*` | 5 |
| `lifeisthing-patch-2` | 4 |
| `lifeisthing-patch-1` | 4 |
| `main` | 3 |
| `US-21-domain-scores` | 2 |
| `refactor/*` | 2 |
| `lifeisthing-patch-3` | 2 |
| `lifeisthing-patch-4` | 2 |
| `James-patch` | 2 |
| `documentation/*` | 1 |
| `housekeeping` | 1 |
| `revert-95-feature/*` | 1 |
| `logs-upload` | 1 |

## Failed runs

| Date | Branch | Commit / PR | What happened |
|---|---|---|---|
| 20/09 12:33 | `feature/us-03-04-session` | Merge branch 'dev' into feature/us-03-04-session | PR #83 failed CI, so it could not merge. It was closed and the work was redone on `feature/us-03-guest` (PR #84), which passed and merged the same day. |
| 20/09 12:35 | `feature/us-03-04-session` | Feature/us 03 04 session | PR #83 failed CI, so it could not merge. It was closed and the work was redone on `feature/us-03-guest` (PR #84), which passed and merged the same day. |
| 22/09 22:08 | `fix/guest-mode-bugs-85` | Fix/guest mode bugs 85 | Build failed on the first push; fixed 3 minutes later on the same branch, which passed before PR #91 merged. |
| 14/10 17:41 | `feature/us-23-score-chart` | US-23: tests for household score history | Expected: the failing tests for US-23 were pushed first (TDD red). The implementation commit 13 minutes later passed. |

`dev` and `main` are protected: the `build` check must pass and the branch must be up to date before a pull request can merge, and admins cannot bypass this.
