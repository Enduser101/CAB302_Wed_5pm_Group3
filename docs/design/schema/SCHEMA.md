# EcoTwin Database Schema (canonical, v4)

**Source of truth:** [`src/main/resources/com/ecotwin/schema.sql`](../../../src/main/resources/com/ecotwin/schema.sql) — this document explains it, it doesn't replace it. `DatabaseConnection` (`src/main/java/com/ecotwin/util/DatabaseConnection.java`) applies that file to `ecotwin.db` automatically on first run of the app or any test that touches it.

Read this before adding a table, a column, or a DAO. The goal is that two people (or two AI agents) working on different user stories at the same time don't collide on the same schema file without realising why a decision was made a certain way.

## Why this looks the way it does

Three earlier, mutually inconsistent schema drafts existed in this project before this version (`docs/build_plan.md` §3, the old `docs/design/schema/db_schema.txt`, and an even earlier `web_arch.txt` package sketch). They disagreed on household ownership, scenario fields, and naming. Those disagreements were resolved by checking each option against the **actual approved user stories** on the GitHub Projects board (Epics E1–E9, `docs/user-stories-epics-1-8.md`) rather than picking arbitrarily, then validated by building a real, running SQLite database against it. `db_schema.txt` is now superseded — don't edit it, this file and `schema.sql` are canonical.

## Tables

| Table | Powers | Notes |
|---|---|---|
| `users` | US-01 Register, US-02 Log in, US-05/06 Account | `password_hash` only — never a plaintext password column. `email` is optional (guests/older rows may not have one). |
| `households` | US-08 Create household | `join_code` is unique, 6 characters, generated from an ambiguity-free alphabet (no `0/O/1/I` — see `HouseholdService.JOIN_CODE_ALPHABET`). `state` (not `postcode` — see "Decisions" below). |
| `household_memberships` | US-08/09/10/11 | **Association entity, not a plain join table** — it carries `role` (`ADMIN`/`MEMBER`), `joined_at`, `left_at`. `left_at IS NULL` means "currently active"; rows are never deleted, so leaving/removal (US-10, US-32) doesn't destroy history (US-12/13). |
| `energy_entries`, `water_entries`, `waste_entries` | US-15/16/17/19, Epic 6 scoring | Append-only logs (the "resource pool" UI adds a new row per entry rather than upserting one row per period) — scoring reads the sum of a period's rows. |
| `vehicles` | US-18 Transport | A household's vehicle list (label, fuel type, km/week) — matches the mockup's "Vehicle 1/2 + Add a vehicle" UI, not a row-per-mode log. |
| `transport_entries` | US-18 Transport | Household-level scalars that aren't per-vehicle (public transport trips/week, flights/year). Still upsert-per-period (`UNIQUE(household_id, period)`) since these aren't a list. |
| `scenarios` | US-27–31 | Stores a serialised set of hypothetical changes plus both the baseline and projected score, so comparison (US-28) never has to re-derive the baseline. |
| `activity_log` | US-12/13/14, and written as a side effect of US-08/09 (create/join) | Free-text `message` + actor + timestamp. Simple by design — no structured diff column, because Epic 4's stories only need a human-readable trail, not machine-replayable history. |

## Decisions worth knowing before you touch this file

1. **Household ownership is multi-user, not single-owner.** An earlier draft (`build_plan.md`) modelled `households.user_id`. That's wrong for Epic E3 (US-08/09/10/11 all assume a household has *members*, not one owner) — hence the `household_memberships` association entity instead.
2. **`state`, not `postcode`.** Earlier drafts used `postcode`. The **approved high-fidelity "Set up your household" screen** (`docs/Ecotwin High Fidelity/CAB302 Ecotwin High Fidelity.pdf`) collects a state dropdown (ACT/NSW/NT/QLD/SA/TAS/VIC/WA), not a postcode field. Schema follows the approved UI, not the older draft doc.
3. **Transport is a vehicle list, not one row per mode.** The mockup's Resources → Transport tab manages a list of vehicles (add/remove, fuel type, km/week) rather than logging `car_km`/`public_transport_km`/... as scalar columns. `vehicles` models that directly.
4. **`activity_log` is intentionally unstructured.** No `old_value`/`new_value` columns — every acceptance criterion in Epic 4 only asks for a readable trail ("Frankie updated transport: Vehicle 2 distance 90 → 120 km/week"), which a formatted `message` string satisfies without a rigid schema every future entry type has to fit into.

## Still open — raise with the team before calling this "final" (issue #79)

- Can a user be an active member of **more than one household** at once? `household_memberships` doesn't stop it (no unique constraint on `(user_id) WHERE left_at IS NULL`). Neither US-09 nor US-10 says either way. If the team wants "exactly one active household," that constraint needs adding — right now it's permissive by omission, not by decision.
- Should leaving (US-10) or removal (US-32) be blocked if the acting user is the household's **sole admin**? Not stated in either story's acceptance criteria. Whoever picks up US-10/32 needs an actual team answer here, not an assumption baked into the DAO layer.

## Conventions for extending this schema without stepping on someone else's story

- **Add, don't restructure.** A new column should default to nullable or have a `DEFAULT`, so it doesn't break another branch's `INSERT` statements written against the current shape.
- **New tables go at the end of `schema.sql`**, each with a one-line comment saying which user story/epic it's for — makes conflicts in this file trivial to resolve (they land in different regions of the file).
- **Every table uses `CREATE TABLE IF NOT EXISTS`** — `DatabaseConnection` re-applies the whole file on every app start, so this must stay idempotent.
- If your change touches a table another epic already depends on (e.g. adding a required column to `households`), say so in your PR description and tag the person owning that epic — don't assume it's safe just because your own tests pass.
