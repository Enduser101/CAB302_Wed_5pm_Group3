# EcoTwin — Technical Build Plan

**Companion to:** `PROJECT-PLAN.md` (assessment/checkpoint tracker)
**Purpose:** what we actually build, how it's structured, and in what order.
**Last updated:** _fill in_

---

## 1. Tech stack (lock this in Week 5 — don't revisit)

| Layer | Choice | Why |
|---|---|---|
| Language | **Java 21** (Amazon Corretto) | Unit requirement |
| UI | **JavaFX** + FXML + CSS | Unit requirement; FXML keeps view separate from controller (helps the OO mark) |
| Database | **SQLite** via **JDBC** | Zero-install, file-based, fits "local storage / no cloud" scope. Ships as one `.db` file |
| Build | **Gradle** (Kotlin or Groovy DSL) | Easiest JavaFX plugin + GitHub Actions integration |
| Tests | **JUnit 5** + **Mockito** | Mockito lets us mock the DAO layer so score-engine tests don't touch the DB |
| Docs | **Javadoc** | Required at Week 13 |
| CI | **GitHub Actions** | Required at Week 11 |
| Password hashing | **BCrypt** (jBCrypt) | Never store plaintext passwords — cheap mark, common miss |

**Decision rule:** if a library isn't on this list, it needs a team discussion + a note in `/docs/design/decisions.md`. Every "why we chose X" note is reusable in the video walkthrough (the rubric asks you to *justify technical choices*).

---

## 2. Architecture — MVC + DAO

This is the shape the Week 9 rubric explicitly rewards (MVC, controllers, DAO pattern, separation of concerns).

```
   JavaFX View (FXML + CSS)
            ↕
      Controller  ......... handles UI events only, no business logic
            ↕
       Service  ........... business logic: scoring, recommendations, simulation
            ↕
         DAO  ............. all SQL lives here, nothing else touches JDBC
            ↕
        SQLite
            
       Model  ............. plain data objects, used at every layer
```

**The one rule that protects your OO mark:** a Controller must never contain SQL, and a Service must never import anything from `javafx.*`. If those two hold, your separation of concerns is demonstrable in 30 seconds on video.

### Package structure

```
src/main/java/com/ecotwin/
├── App.java                      launches JavaFX, wires dependencies
├── model/                        User, Household, EnergyEntry, WasteEntry,
│                                 WaterEntry, TransportEntry, Score, Recommendation, Scenario
├── dao/                          interfaces: UserDao, HouseholdDao, EntryDao<T>, ScenarioDao
│   └── sqlite/                   SqliteUserDao, SqliteHouseholdDao, ... (implementations)
├── service/                      AuthService, ScoreService, RecommendationService,
│                                 SimulationService, HouseholdService
├── controller/                   LoginController, DashboardController, EnergyController, ...
├── util/                         DatabaseConnection, Validators, SessionContext
└── scoring/                      DomainScorer interface + EnergyScorer, WasteScorer,
                                  WaterScorer, TransportScorer

src/main/resources/com/ecotwin/
├── view/                         login.fxml, dashboard.fxml, energy.fxml, ...
├── css/                          app.css
└── schema.sql                    table definitions

src/test/java/com/ecotwin/        mirrors the main structure
```

---

## 3. Database schema (v1)

Keep it in `src/main/resources/schema.sql` and run it on first launch if tables don't exist.

```sql
CREATE TABLE users (
    id            INTEGER PRIMARY KEY AUTOINCREMENT,
    username      TEXT NOT NULL UNIQUE,
    password_hash TEXT NOT NULL,
    created_at    TEXT NOT NULL
);

CREATE TABLE households (
    id             INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id        INTEGER NOT NULL REFERENCES users(id),
    name           TEXT NOT NULL,
    occupants      INTEGER NOT NULL,
    dwelling_type  TEXT NOT NULL,       -- house / apartment / townhouse
    postcode       TEXT,
    created_at     TEXT NOT NULL
);

-- One table per domain keeps scoring simple and the DAO generic
CREATE TABLE energy_entries (
    id           INTEGER PRIMARY KEY AUTOINCREMENT,
    household_id INTEGER NOT NULL REFERENCES households(id),
    period       TEXT NOT NULL,          -- 'YYYY-MM'
    kwh          REAL NOT NULL,
    has_solar    INTEGER NOT NULL DEFAULT 0,
    notes        TEXT
);

CREATE TABLE water_entries (
    id           INTEGER PRIMARY KEY AUTOINCREMENT,
    household_id INTEGER NOT NULL REFERENCES households(id),
    period       TEXT NOT NULL,
    litres       REAL NOT NULL,
    notes        TEXT
);

CREATE TABLE waste_entries (
    id             INTEGER PRIMARY KEY AUTOINCREMENT,
    household_id   INTEGER NOT NULL REFERENCES households(id),
    period         TEXT NOT NULL,
    general_kg     REAL NOT NULL,
    recycled_kg    REAL NOT NULL,
    compost_kg     REAL NOT NULL
);

CREATE TABLE transport_entries (
    id             INTEGER PRIMARY KEY AUTOINCREMENT,
    household_id   INTEGER NOT NULL REFERENCES households(id),
    period         TEXT NOT NULL,
    mode           TEXT NOT NULL,        -- car / public / bike / walk / ev
    km             REAL NOT NULL
);

CREATE TABLE scenarios (
    id             INTEGER PRIMARY KEY AUTOINCREMENT,
    household_id   INTEGER NOT NULL REFERENCES households(id),
    name           TEXT NOT NULL,
    changes_json   TEXT NOT NULL,        -- serialised hypothetical changes
    baseline_score REAL NOT NULL,
    projected_score REAL NOT NULL,
    created_at     TEXT NOT NULL
);
```

> Note the requirements doc lists *"user accounts"* under Out of Scope but *"User authentication"* as a functional requirement, and Week 13 **mandates** an auth system. **Auth is in scope** — fix that contradiction in the requirements doc. What's genuinely out of scope is *cloud* accounts.

---

## 4. The scoring engine (the heart of the app)

This is your most testable, most demo-able component — build it with strict TDD, it's where your Red-Green-Refactor evidence comes from.

```java
public interface DomainScorer {
    DomainScore score(Household household, List<? extends Entry> entries);
    String domainName();
}
```

Four implementations (`EnergyScorer`, `WaterScorer`, `WasteScorer`, `TransportScorer`), each returning a **0–100 sub-score**. `ScoreService` holds a `List<DomainScorer>` and produces the weighted overall score plus a per-domain breakdown.

**Why this shape wins marks:** it's the **Strategy pattern**, it's an interface (OO principle: abstraction + polymorphism), adding a 5th domain requires zero changes to `ScoreService` (open/closed), and each scorer is trivially unit-testable with no DB.

**Scoring approach:** normalise the household's per-person usage against an Australian benchmark, then convert to 0–100. Keep benchmarks in a single constants class or a config file so they're easy to point at on video and easy to change. Remember scope: **"scientifically validated emissions modelling" is explicitly out of scope** — say so plainly in the UI and the video. A clearly-labelled indicative model is correct here; overclaiming accuracy is a risk.

**Recommendations:** `RecommendationService` inspects the domain breakdown and returns tailored suggestions for the weakest domains, ranked by estimated impact. Rules can be simple and data-driven (a table of `condition → suggestion → estimated point gain`).

**Simulation:** `SimulationService` takes a baseline `Household` + a set of hypothetical changes, builds a *modified copy* (don't mutate the original — good encapsulation to show off), re-runs `ScoreService`, and returns the delta. This is the feature that makes EcoTwin distinctive — make it the centrepiece of your demo.

---

## 5. Screens to build

| Screen | Contents | Priority |
|---|---|---|
| Login / Sign-up | username, password, validation, error states | **Must** |
| Household profile | create/edit household, occupants, dwelling type; switch between profiles | **Must** |
| Dashboard | overall score, per-domain breakdown (bar/gauge), top recommendations | **Must** |
| Energy entry | list + add/edit/delete entries | **Must** |
| Water entry | list + add/edit/delete | **Must** |
| Waste entry | list + add/edit/delete | **Must** |
| Transport entry | list + add/edit/delete | **Must** |
| Recommendations | full ranked list with explanations | **Should** |
| Scenario simulator | toggle hypothetical changes, live score delta, save scenario | **Should** |
| Saved scenarios | list/compare saved scenarios | **Could** |
| Trends over time | score by period, line chart | **Could** |

**Must-have = everything required to be complete by Week 13.** Be honest and conservative when you tag priorities in your user stories — you're marked against *your own* Must-Haves.

---

## 6. Build order (sprint by sprint)

### Sprint 0 — Week 5 (foundations)
- [ ] Repo created, `.gitignore` (Java + IntelliJ + `*.db`), branch protection on `main`
- [ ] Gradle project builds and launches a blank JavaFX window
- [ ] `DatabaseConnection` util + `schema.sql` runs on startup
- [ ] One trivial JUnit test passing (proves the test harness works)
- [ ] Everyone has pushed at least one commit ← *checkpoint evidence*

### Sprint 1 — Weeks 6–7 (planning + skeleton)
- [ ] User stories written with priority + acceptance criteria (**keep the drafts**)
- [ ] Wireframes for all Must-have screens (**keep preliminary + final**)
- [ ] `model/` classes written (plain POJOs, easy parallel work)
- [ ] DAO **interfaces** defined (unblocks everyone: services can be built against interfaces before SQL exists)
- [ ] Auth: `UserDao` + `AuthService` + login/signup screen, **TDD'd**
- [ ] Release plan + Sprint 2 plan ← *Checkpoint 2 evidence*

### Sprint 2 — Weeks 8–9 (functional prototype → **Submission 1**)
- [ ] Household profile CRUD end-to-end (view → controller → service → DAO → DB)
- [ ] All four domain entry screens (split one per person — clean parallel work)
- [ ] `DomainScorer` implementations, **written test-first**
- [ ] Dashboard showing overall + breakdown
- [ ] Feature branches + PRs for every item, reviewed
- [ ] Freeze features Wednesday of Week 9 → record video Thursday → submit Friday

### Sprint 3 — Weeks 10–11 (enhance + CI)
- [ ] Recommendations engine + screen
- [ ] Scenario simulation + save/load
- [ ] Build script finalised; **GitHub Actions** running build + tests on every push ← *Checkpoint 4*
- [ ] First deliberate refactoring pass — introduce design patterns and **commit them separately** so the before/after is visible

### Sprint 4 — Weeks 12–13 (polish → **Submission 2** + presentation)
- [ ] Javadoc on all public classes/methods + Actions generating Javadoc pages
- [ ] Usability testing with ≥3 real people outside the team; log findings and fixes in `/docs/feedback/`
- [ ] Accessibility pass: contrast, font sizes, keyboard navigation, clear error messages
- [ ] Test coverage report
- [ ] Sprint retro written up
- [ ] **Feature freeze end of Week 12** — Week 13 is presentation only, no new features

---

## 7. Design patterns — where each one naturally fits

Don't bolt patterns on for the sake of it; the Week 13 rubric wants them *introduced through refactoring*, with commit evidence.

| Pattern | Where it fits in EcoTwin |
|---|---|
| **DAO** | The whole `dao/` package — isolates all JDBC |
| **Strategy** | `DomainScorer` implementations |
| **Singleton** | `DatabaseConnection`, `SessionContext` (current logged-in user) |
| **Factory** | Creating the right `Scorer` / DAO implementation for a domain |
| **Dependency Injection** | Constructor-inject DAOs into services, services into controllers — this is what makes Mockito testing possible |
| **Builder** | Constructing `Household` / `Scenario` objects with many optional fields |
| **Observer** | JavaFX properties — dashboard auto-updates when the simulator changes a value |

**Do this:** implement something the obvious way first, commit it, then refactor to the pattern in a *separate* commit with a message like `refactor: extract DomainScorer strategy to decouple ScoreService`. That commit pair is exactly the evidence the marker wants.

---

## 8. Testing strategy

- **Test-first for all business logic** — scorers, recommendations, simulation, validation. These need no DB and no UI, so there's no excuse.
- **Behaviour-focused names:** `heavyCarUsageLowersTransportScore()`, not `testGetScore()`. The rubric explicitly calls out "not just getters/setters".
- **DAO tests** run against an in-memory or temp SQLite file, torn down after each test.
- **Services tested with mocked DAOs** (Mockito) — this is why DI matters.
- **GUI:** don't chase full UI test automation. Keep controllers thin so there's little to test, and say on video that GUI/DB were isolated via mocks — the rubric awards acknowledging this.
- **Red-Green-Refactor evidence:** commit the failing test, then the fix, as two commits. Do this deliberately for at least 3–4 features so you can point at the commit groups on video.

---

## 9. Git workflow

- `main` — always green, protected, no direct pushes
- `develop` — integration branch (shows "integration branch" use, called out in the Week 13 rubric)
- `feature/<story-id>-short-name` — one per user story
- Every merge via **PR with at least one reviewer** (peer code review is assessed at Week 13)
- Commit message format: `type: message (#story-id)` — e.g. `feat: add water entry form (#US-07)`

**Everyone commits regularly.** A member with 4 commits the night before Week 9 loses marks individually *and* weakens the team's "regular, consistent contribution" evidence.

---

## 10. Biggest risks (and the fix)

| Risk | Fix |
|---|---|
| Scoring model becomes a rabbit hole | Timebox it. Indicative model, clearly labelled, benchmarks in one config file |
| One person becomes the only one who understands the DB | Pair on the first DAO; everyone writes at least one |
| Merge conflicts in FXML | One screen = one owner. Never two people in one FXML file |
| Scope creep (trends, charts, extras) | Must-haves only until Week 11. Could-haves are bonus |
| Silent non-contributor | Weekly commit-count check at standup; raise it early with the tutor, not in Week 12 |
| Everything lands the night before a submission | Feature freeze 2 days before every deadline, no exceptions |
