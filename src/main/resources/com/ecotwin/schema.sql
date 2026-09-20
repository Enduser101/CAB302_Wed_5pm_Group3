-- EcoTwin canonical schema (v4)
-- History: v1-v3 were reconciled and prototyped in /scratch (never committed to this repo) against
-- three conflicting drafts (docs/build_plan.md, ecotwin_draft_database_schema.txt, web_arch.txt) and the
-- approved user stories on the GitHub Projects board. See docs/design/schema/SCHEMA.md for the full
-- table-by-table rationale and open questions.
-- v4 change: households.postcode -> households.state (TEXT), to match the approved high-fidelity
-- "Set up your household" screen (docs/Ecotwin High Fidelity/...), which collects a state dropdown,
-- not a postcode.

CREATE TABLE IF NOT EXISTS users (
    id            INTEGER PRIMARY KEY AUTOINCREMENT,
    username      TEXT NOT NULL UNIQUE,
    email         TEXT,
    password_hash TEXT NOT NULL,
    display_name  TEXT,
    created_at    TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS households (
    id             INTEGER PRIMARY KEY AUTOINCREMENT,
    name           TEXT NOT NULL,
    join_code      TEXT NOT NULL UNIQUE,
    occupants      INTEGER NOT NULL,
    dwelling_type  TEXT,                 -- house / apartment / townhouse
    state          TEXT,                 -- e.g. QLD, NSW - matches the approved UI (not postcode)
    created_at     TEXT NOT NULL
);

-- Association entity, not a plain junction table: it carries role/joined_at/left_at.
-- US-08 (create -> creator is admin), US-09 (join by code), US-10 (leave, keep history),
-- US-11 (view current members, admin identifiable, departed hidden).
CREATE TABLE IF NOT EXISTS household_memberships (
    id           INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id      INTEGER NOT NULL REFERENCES users(id),
    household_id INTEGER NOT NULL REFERENCES households(id),
    role         TEXT NOT NULL,          -- 'ADMIN' / 'MEMBER'
    joined_at    TEXT NOT NULL,
    left_at      TEXT                    -- NULL = currently active member
);

-- One table per domain keeps DomainScorer implementations simple and DAO-generic (build_plan.md S4/S7).
-- Append-only log: the Resources "pool" screen adds a new row per entry (many rows per period expected).
CREATE TABLE IF NOT EXISTS energy_entries (
    id                   INTEGER PRIMARY KEY AUTOINCREMENT,
    household_id         INTEGER NOT NULL REFERENCES households(id),
    period               TEXT NOT NULL,   -- 'YYYY-MM'
    electricity_kwh      REAL NOT NULL,
    solar_generation_kwh REAL,            -- nullable: no solar = NULL, not 0
    notes                TEXT,
    updated_by_user_id   INTEGER REFERENCES users(id),
    updated_at           TEXT
);

CREATE TABLE IF NOT EXISTS water_entries (
    id                 INTEGER PRIMARY KEY AUTOINCREMENT,
    household_id       INTEGER NOT NULL REFERENCES households(id),
    period             TEXT NOT NULL,
    litres             REAL NOT NULL,
    notes              TEXT,
    updated_by_user_id INTEGER REFERENCES users(id),
    updated_at         TEXT
);

CREATE TABLE IF NOT EXISTS waste_entries (
    id                 INTEGER PRIMARY KEY AUTOINCREMENT,
    household_id       INTEGER NOT NULL REFERENCES households(id),
    period             TEXT NOT NULL,
    general_kg         REAL NOT NULL,
    recycled_kg        REAL NOT NULL,
    compost_kg         REAL NOT NULL,
    updated_by_user_id INTEGER REFERENCES users(id),
    updated_at         TEXT
);

-- A household's vehicle list (mockup: "Vehicle 1/2", fuel type + km/week, add/remove).
CREATE TABLE IF NOT EXISTS vehicles (
    id            INTEGER PRIMARY KEY AUTOINCREMENT,
    household_id  INTEGER NOT NULL REFERENCES households(id),
    label         TEXT NOT NULL,          -- 'Vehicle 1', 'Vehicle 2', ...
    fuel_type     TEXT NOT NULL,          -- petrol / diesel / hybrid / ev
    km_per_week   REAL NOT NULL
);

-- Household-level transport scalars that aren't per-vehicle.
CREATE TABLE IF NOT EXISTS transport_entries (
    id                                 INTEGER PRIMARY KEY AUTOINCREMENT,
    household_id                       INTEGER NOT NULL REFERENCES households(id),
    period                             TEXT NOT NULL,
    public_transport_trips_per_week    REAL NOT NULL DEFAULT 0,
    flights_per_year                   REAL NOT NULL DEFAULT 0,
    updated_by_user_id                 INTEGER REFERENCES users(id),
    updated_at                         TEXT,
    UNIQUE (household_id, period)
);

-- US-27 (simulate without mutating real data), US-28 (compare scores), US-29/US-30 (save/view named scenarios).
CREATE TABLE IF NOT EXISTS scenarios (
    id                  INTEGER PRIMARY KEY AUTOINCREMENT,
    household_id        INTEGER NOT NULL REFERENCES households(id),
    created_by_user_id  INTEGER REFERENCES users(id),
    name                TEXT NOT NULL,
    domain              TEXT NOT NULL,     -- which domain tab the scenario was built from
    changes_json        TEXT NOT NULL,     -- serialised hypothetical changes
    baseline_score      REAL NOT NULL,
    projected_score     REAL NOT NULL,
    created_at          TEXT NOT NULL
);

-- Powers the Activity History screen (Household -> Activity History).
CREATE TABLE IF NOT EXISTS activity_log (
    id            INTEGER PRIMARY KEY AUTOINCREMENT,
    household_id  INTEGER NOT NULL REFERENCES households(id),
    actor_user_id INTEGER REFERENCES users(id), -- nullable: system-generated entries
    message       TEXT NOT NULL,
    created_at    TEXT NOT NULL
);
