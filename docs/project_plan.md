# CAB302 — EcoTwin Team Project Plan & Tracker

**Unit:** CAB302 Software Development (2026 SEM-2, QUT)
**Project:** EcoTwin — household sustainability profiler (JavaFX + local DB)
**Theme:** Technology for Sustainable Futures
**Last updated:** _fill in_ · **Owner of this doc:** _fill in_

> **How to use this doc:** Keep it in the repo at `/docs/PROJECT-PLAN.md` and edit it every week. Tick boxes with `[x]`. The "Owner" column is the accountability lever — every task has one name. Anything with **[TEAM]** means the whole team loses marks if it's not done, so schedule those first in each sprint.

---

## 0. Read this first — where we are right now

We are in **Week 5**. That means **Checkpoint 1 (Milestone: Inception & Setup) is due this practical.** Priority this week is: team formed, requirements drafted (done — EcoTwin doc exists), repo created, and everyone's individual work log started. See [Section 6](#6-checkpoint-milestone-checklists-assessment-1).

---

## 1. Project snapshot

**What EcoTwin does:** Builds a profile of a household's resource use, gives a sustainability score, explains which areas contribute most, recommends improvements, and lets users simulate a change to see the score impact before committing in real life.

**Target user:** Australian households (owners/renters), non-technical, on a computer, willing to spend a few minutes entering data.

**Core domains tracked:** Transport · Waste · Water · Energy

**In scope:** household profile data entry + storage · overall + per-domain score · tailored recommendations · scenario simulation with score delta · multiple household profiles + saved scenarios to local storage.

**Out of scope (say "no" to these):** cloud storage/sync · network features · utility-provider integration · mobile/web versions · scientifically validated emissions modelling.

**Theme hook (must be explicit in assignments):** EcoTwin directly serves *Technology for Sustainable Futures* — it turns opaque environmental impact into an understandable, actionable score for ordinary households.

---

## 2. Team roster & roles

| Name          | Student # | Primary role / area | Backup for |
|---------------|-----------|---|---|
| Tinom Bature  | _fill in_ | _fill in_ | |
| Frankie Galea | 11496703  | _fill in_ | |
| James Renwick | 12564800  | | |
| _fill in_     | _fill in_ | | |
| _fill in_     | _fill in_ | | |
| _fill in_     | _fill in_ | | |

**Suggested role split** (adapt to team size — everyone still commits code, this is just *primary* ownership):
- **Scrum lead / PM tool owner** — runs standups, keeps the board honest, exports the PM tool for submission.
- **Repo & CI owner** — branch protection, PR rules, GitHub Actions, build script.
- **Auth + persistence owner** — login/signup + JDBC database layer.
- **Domain features** — split the 4 trackers (Energy / Waste / Water / Transport) across members.
- **Score engine + dashboard owner** — scoring logic + JavaFX dashboard/visualisation.
- **UX / testing lead** — wireframes, usability checks, keeps TDD discipline across the team.

---

## 3. Assessment overview — the big picture

| # | Assessment | Weight | Type | Key dates |
|---|---|---|---|---|
| 1 | Project Checkpoints | **10%** | Individual + Group | Checkpoints Wk **5, 7, 9, 11** (2.5 pts each) |
| 2 | Project Progress & Performance | **50%** | Individual + Group | Sub 1: **Fri Wk 9 EOD** · Sub 2: **Fri Wk 13 EOD** |
| 3 | Final Presentation | **40%** | Individual + Group | **Week 13**, in class (video recorded) |

**Grace period / extensions:**
- Assessment 1 & 2 → **48-hour late period + extensions apply.**
- Assessment 3 (presentation) → **NOT eligible.** No safety net — it happens live in Week 13.

> **⚠️ Point discrepancy to confirm with tutor:** The Assessment 2 summary table says each submission is worth **25/50**, but the Week 13 page header says **"Worth points: 30/50"**. Clarify the exact split before Week 9 so nobody miscalculates effort.

---

## 4. Semester timeline (phases + deadlines)

Week 5 lands on the week of **18 Aug 2026** (from the requirements draft). Confirm every Friday date against the official QUT calendar — **watch for the mid-semester break**, which can shift Weeks 9–13.

| Weeks | Phase | Focus | What's due |
|---|---|---|---|
| 1–4 | 1 · Getting Ready | IDE, Git, OOP refresher, JavaFX, Java Readiness quizzes | (setup) |
| **5** | 2 · Agile begins | Inception & setup | **Checkpoint 1** |
| 6 | 2 | Planning & design work | — |
| **7** | 2 | User stories, PM tool, sprint/release plan, UI design | **Checkpoint 2** |
| 8 | 2 | Build the prototype | — |
| **9** | 3 · Advanced + support | Functional prototype, TDD, VC workflow | **Checkpoint 3** + **A2 Submission 1 (Fri EOD)** |
| 10 | 3 | Enhance, refactor, patterns | — |
| **11** | 3 | Enhanced prototype, build script, CI server | **Checkpoint 4** |
| 12 | 3 | Presentation prep, Javadoc, UX validation | — |
| **13** | 4 · Conclusion | Final demo + present | **A2 Submission 2 (Fri EOD)** + **A3 Presentation** |

---

## 5. Individual setup checklist — EVERYONE does this (Phase 1)

Each team member copies this block into their own work log and ticks it off. This is the foundation of Checkpoint 1 and the "excellent" bonus.

- [ ] IntelliJ IDEA installed
- [ ] **Amazon Corretto JDK 21** installed and set as project SDK
- [ ] Git installed and configured (`user.name`, `user.email`)
- [ ] Cloned the team repo, can **commit AND push** successfully (test with a trivial commit)
- [ ] JavaFX runs (ships with IntelliJ — build and run the starter app)
- [ ] **All Java Readiness quizzes completed** ← required for *excellent* progress at Checkpoint 1
- [ ] Individual work log started (see [Section 10](#10-individual-work-log--8-hrsweek-outside-class))

---

## 6. Checkpoint milestone checklists (Assessment 1)

Checkpoints are **15–20 min tutor meetings, marked individually** (2.5 pts each). Come with work logs open and be able to show the exact artefact for each item. **[TEAM]** items must exist or the whole team can lose marks — do these first in each sprint.

### ✅ Checkpoint 1 — Week 5 · Inception & Setup
_Satisfactory (2 pts):_
- [ ] **[TEAM]** Team formed
- [ ] **[TEAM]** Project requirements documented (EcoTwin doc — tidy up the typos/gaps first)
- [ ] **[TEAM]** Project repository created, everyone has access
- [ ] Each member has meaningful entries in their individual work log

_Excellent (2.5 pts):_
- [ ] All of the above **+ every member has completed all Java Readiness quizzes**

### ✅ Checkpoint 2 — Week 7 · Planning & Design
_Satisfactory:_
- [ ] **[TEAM]** User stories written (with priority + acceptance criteria)
- [ ] **[TEAM]** PM tool set up and actively used (Trello / GitHub Projects / Jira)
- [ ] **[TEAM]** Release plan + first sprint plan
- [ ] **[TEAM]** UI design at low/medium fidelity (sketches or wireframes)

_Excellent:_
- [ ] All of the above **+ evidence of iteration** (draft vs final user stories, preliminary vs final UI sketches — keep both versions!)

### ✅ Checkpoint 3 — Week 9 · Implementation & QA
_Satisfactory:_
- [ ] **[TEAM]** Functional prototype (JavaFX UI + DB persistence)
- [ ] **[TEAM]** Test-Driven Development in evidence (behaviour-focused tests)
- [ ] **[TEAM]** Version control workflow (branches, PRs, meaningful commits from everyone)

_Excellent:_
- [ ] All of the above **+ able to demo progress live to tutor** (in class or Zoom)

### ✅ Checkpoint 4 — Week 11 · Continuous Integration
_Satisfactory:_
- [ ] **[TEAM]** Enhanced prototype
- [ ] **[TEAM]** Build script
- [ ] **[TEAM]** Automated build server (GitHub Actions)

_Excellent:_
- [ ] All of the above **+ evidence of refactoring and OO design patterns** to show the tutor

> **If you miss a checkpoint practical** (public holiday or unavoidable): email the tutor a **3–5 min Zoom cloud recording** within **48 hours** of your scheduled tutorial, screen-sharing your individual progress. Subject line format: `CAB302 Checkpoint XX - Tutorial Day and Time - Student Name - Group Name`.

---

## 7. Assessment 2 — submission checklists (50%)

Both submissions need the **same four files**, built the same way. **Markers only mark what's in the Canvas zip — they will NOT follow GitHub links.** If it's not in the zip and shown in the video, it doesn't get marked.

### The submission package (both Week 9 and Week 13)
- [ ] **Zip of the full git repo** (all source, docs, supporting files — everyone has committed & pushed)
- [ ] **Git commit summary file** — run and save output of:
  ```
  git shortlog --summary --numbered --all --no-merges
  ```
- [ ] **Git log file** — run and save output of:
  ```
  git log --pretty=format:'%h was %an (%ae), %at, %ar, message: %s'
  ```
- [ ] **PM tool export** (from Trello/GitHub Projects/etc.) saved to a file
- [ ] **Video walkthrough** (10 min MAX) — see contents below
- [ ] Submitted to the **correct** Canvas upload link (Sub 1 vs Sub 2 are different links)

**Video–zip consistency rule ("no hunting"):** for every artefact you show in the video, say its exact path (e.g. `/docs/planning/sprint-1-plan.xlsx`) so the marker can find it in the zip in seconds. Keep folder/file names stable and human-readable.

### 🎥 Submission 1 — Week 9 (Preliminary Prototype)
Video must convincingly demonstrate all four criteria, each shown in video **and** matched in the zip:
- [ ] **Agile Planning** — user stories (priority + acceptance criteria), release + sprint plans (with revisions), regular PM tool use, agile practices (retros, estimation)
- [ ] **Object-Oriented Design** — clean JavaFX prototype UI · how requirements shaped design · OO principles (encapsulation, interfaces, inheritance, separation of concerns) · architectural patterns (MVC, controllers, DAO) · brief class/structure diagram
- [ ] **Version Control Workflow** — commit history from all members · branches + PRs · commits align to user stories · good commit messages
- [ ] **Test-Driven Development** — behaviour-focused tests (not just getters/setters) · Red→Green→Refactor evidence via commit groups · run tests live · note any mocks for DB/GUI

_Recommended video order:_ intro/purpose → user stories → planning artefacts → UI prototypes → live demo (JavaFX + DB) → PM tool → VC workflow → Java code walkthrough (UI, persistence/DAO, OO design) → test suite (purpose, Red-Green-Refactor, run tests).

### 🎥 Submission 2 — Week 13 (Final Prototype + Demo)
Week 9 criteria are **not re-marked** — give a fast refresher, then emphasise what's new since Week 9. Must now show:
- [ ] **Final prototype delivers minimum requirements:** JavaFX windows for main functions · sign-up/sign-in auth (GUI + models) · persistence to store/retrieve/update user data · one+ windows doing the actual useful work · **all "Must-Have" user stories implemented**
- [ ] **Advanced OO design patterns** — Dependency Injection, Builder, Singleton, Factory, Observer (show *how* they were introduced via refactoring commits)
- [ ] **CI / automated build** — build script in GitHub · **GitHub Actions** running build + tests + Javadoc · evidence of *sustained* use (not one-off)
- [ ] **Documentation** — Javadoc comments + generated Javadoc pages
- [ ] **Feedback-driven improvement** — who you sought feedback from & how it was actioned · code quality (e.g. test coverage) · UI/usability assessment · reflective team practices (sprint retros) · show iterative improvement across process, product, teamwork

_Recommended video order (timing is a guide):_ ~1 min refresher (purpose, stories, old UI) → ~1 min final demo (fast on old, emphasise new + week-9-feedback improvements) → ~3 min code (OO patterns + refactoring evidence + justify choices) → ~2 min CI (run Actions + tests + Javadoc, show sustained use) → ~3 min feedback-driven improvement (feedback sources, coverage, usability, retros).

---

## 8. Assessment 3 — Final Presentation (40%)

- [ ] Group presentation covering technical choices + project management approach, reflecting on strengths/limits of your practices
- [ ] Use the **provided slide template** (Week 12 covers "Creating Professional Presentations")
- [ ] Agree timing + who speaks to what (even distribution — everyone presents)
- [ ] Run **practice run-throughs** before the day (Phase 3 outcome)
- [ ] **No submission needed** — it's recorded in class. **No late period.** Everyone normally gets the same mark unless no-show or documented underperformance.
- [ ] Keep your own record of the verbal feedback given after presenting

---

## 9. Task allocation — who owns what

Fill this in each sprint. One owner per row. **[TEAM]** = checkpoint-critical, schedule first.

| Feature / task | Owner | Sprint | Status | Linked user story |
|---|---|---|---|-|
| **[TEAM]** Repo + branch protection + PR rules | | 0 | | |
| **[TEAM]** PM tool set up | | 0 | | |
| User stories (all) | | 1 | | |
| Release plan + Sprint 1 plan | | 1 | | |
| UI wireframes (low/med fi) | | 1 | | |
| Auth — sign up / sign in (GUI + model) | | | | US-01, US-02 |
| Account management — view details, change password, log out | | | | US-05, US-06, US-07 |
| Persistence layer — JDBC + DAO | | | | |
| Household creation, joining, leaving | | | | US-08, US-09, US-10, US-11 |
| Activity history / audit log | | | | US-12, US-13, US-14 |
| Household profile CRUD | | | | |
| Energy tracker | | | | US-15 |
| Waste tracker | | | | US-17 |
| Water tracker | | | | US-16 |
| Transport tracker | | | | US-18 |
| Edit existing resource data | | | | US-19 |
| Score engine (overall + per-domain) | | | | US-20, US-21, US-22, US-23 |
| Dashboard / visualisation | | | | |
| Recommendations logic | | | | US-24, US-25, US-26 |
| Scenario simulation + score delta | | | | US-27, US-28, US-29, US-30, US-31 |
| Household administration — remove member, correct data, rename, transfer rights, delete household | | | | US-32, US-33, US-34, US-35, US-36 |
| **[TEAM]** Build script | | | | |
| **[TEAM]** GitHub Actions CI | | | | |
| Javadoc comments + generated pages | | | | |
| Test suite (per feature) | (each feature owner) | | | |

---

## 10. Individual work log — 8 hrs/week outside class

CAB302 expects **12 hrs/week**: 4 in class + **8 on the project outside class**. Each member keeps their **own** log (a Google Doc works well). Log every task with **time spent + evidence**. Checkpoints are marked individually off this log.

**What counts (with evidence):**
- **Meetings / Scrum ceremonies** → minutes, sprint board screenshots, summaries
- **Programming** → git commit history, screenshots, working build
- **Design / UX** → sketches, Figma files, wireframes
- **Research** → summary docs, demo/spike projects

**Rules of thumb:** time claimed must match the output shown; keep work appropriate to the current stage (don't polish visuals before requirements are locked); class/practical work only counts if it directly feeds a task you actually own.

**Log entry template:**

| Date | Task | Time | Evidence (link/path) | Sprint |
|---|---|---|---|---|
| | | | | |

---

## 11. Definition of Done (quality gate for every task)

A task isn't "done" until:
- [ ] Code is on a feature branch, merged via PR (reviewed by ≥1 teammate)
- [ ] Behaviour-focused tests written and passing
- [ ] Commit messages are meaningful and reference the user story/task
- [ ] Javadoc added to public classes/methods (from Phase 3 on)
- [ ] CI build green on GitHub Actions (from Week 11 on)
- [ ] The artefact lives at a stable, human-readable path in the repo

---

## 12. Weekly accountability rhythm

Pick fixed slots and keep them — this is what actually holds people accountable between checkpoints.

- **Standup (async or 15 min, 2×/week):** what I did / what I'll do / blockers
- **Sprint planning (start of sprint):** pull [TEAM] items first, assign owners, set the board
- **Mid-sprint check:** is anything red? re-balance before it becomes a checkpoint problem
- **Sprint retro (end of sprint):** what went well / what to change — **write it down** (this is directly assessed in Week 13)
- **Pre-checkpoint dry run (day before Wk 5/7/9/11):** confirm every [TEAM] box is ticked and everyone can show their log

---

## 13. Repo structure (stable paths so markers never hunt)

```
/src/                     Java source (JavaFX UI, models, DAO, score engine)
/src/test/                Unit tests
/docs/
  PROJECT-PLAN.md         ← this file
  requirements.md         EcoTwin requirements
  planning/               user stories, release plan, sprint plans (keep drafts!)
  design/                 wireframes, UI sketches (preliminary + final), class diagrams
  retros/                 sprint retrospectives
  feedback/               feedback sought + how it was actioned
/.github/workflows/       GitHub Actions CI
build.gradle / pom.xml    build script
README.md
```

---

### Open items to confirm with the tutor
- [ ] Exact A2 point split (25/25 vs 25/30) — see Section 3
- [ ] Confirmed Friday calendar dates for Weeks 9 and 13 (around the mid-sem break)
- [ ] Which PM tool the whole team will standardise on
- [ ] Build tool: Gradle or Maven
