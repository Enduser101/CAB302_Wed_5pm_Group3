# EcoTwin

CAB302 Agile Software Engineering, Wednesday 5pm, Group 3.

EcoTwin is a JavaFX desktop app for households to track their monthly energy, water, waste and
transport. It turns the readings into a sustainability score out of 100, recommends where to
improve, and lets you try "what if" scenarios before making a change. Members share a household
through a join code, and every change is recorded in the household's activity history.

## Running the app

Requires **JDK 21**. Maven is included through the wrapper, so it doesn't need installing.

```
./mvnw javafx:run          # macOS / Linux / Git Bash
mvnw.cmd javafx:run        # Windows Command Prompt / PowerShell
```

The database (`ecotwin.db`) is created in the project folder on first launch.

**Demo account:** log in as `Cab 302` with password `Testuser1`. It is created automatically
with six months of readings and a month of edits, so the dashboard, score chart and history
all have data. You can also register a new account, or continue as a guest.

## Tests, coverage and Javadoc

| Command | Output |
|---|---|
| `./mvnw test` | Runs the JUnit 5 / Mockito test suite and writes the coverage report to `target/site/jacoco/index.html` |
| `./mvnw javadoc:javadoc` | Generates the Javadoc pages in `target/reports/apidocs/index.html` |

GitHub Actions (`.github/workflows/build.yml`) runs the tests on every push and pull request,
and uploads the coverage report and Javadoc as build artifacts. `main` and `dev` are protected,
so nothing merges unless the build passes.

## Branching

`feature/*` branches → pull request into `dev` (integration branch) → `main` for releases.
New features are written test-first: a failing-test commit, then the implementation, then any
refactor as its own commit.

## Project structure

| Path | Contents |
|---|---|
| `src/main/java/com/ecotwin/` | Application code: `controller` (JavaFX screens), `service` (business rules), `dao` + `dao/sqlite` (database access), `model`, `util` |
| `src/main/resources/com/ecotwin/` | FXML screens, `css/app.css` and `schema.sql` |
| `src/test/java/com/ecotwin/` | Unit and DAO tests |
| `docs/requirements.md`, `docs/user-stories-epics-1-8.md` | Requirements and user stories |
| `docs/project_plan.md`, `docs/build_plan.md` | Planning documents |
| `docs/design-notes.md` | Design decisions and refactoring notes |
| `docs/design/` | Architecture, ER diagram, schema, UML, stylesheets and wireframes (low, medium and high fidelity) |
| `docs/pm-export/` | Project board exports |
| `Meeting notes/` | Team meeting notes and sprint plans |
| `personal_logs/` | Each team member's weekly log |

## Design patterns

- **DAO / Repository**: services depend on DAO interfaces in `dao/`. SQLite implementations live in `dao/sqlite/`, and tests use mocks.
- **Dependency injection**: `AppContext` creates every DAO and service once and passes them to the screens.
- **Singleton**: `DatabaseConnection` (one shared connection) and `SessionContext` (the signed-in user).
- **Strategy**: `IndicativeScoreCalculator` scores each area with its own `DomainScorer`.
