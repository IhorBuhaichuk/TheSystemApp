# Room Change Playbook

Use for database schema, entity, DAO, migration, seed data, backup/import, or repository persistence changes.

## Read First

1. `GRAPH_REPORT.md`
2. `app/src/main/java/com/ihor/thesystem/data/local/room/database/AppDatabase.kt`
3. Related entity and DAO files.
4. Related repository interface in `domain/repository`.
5. Related implementation in `app/data/repository_impl`.

## Required Checks

- Entity change, DAO query change, database version, migration, schema, and mapper updates must be treated as one unit.
- Keep Room implementation in `:app`; domain models and repository contracts stay in `:domain`.
- Preserve backup policy: silent Android backup excludes Room DB; explicit JSON export/import is the user-facing backup path.
- Update `GRAPH_REPORT.md` if the database shape, DAO surface, or repository map changes.

## Verification

- Run `scripts/check-room.cmd`.
- For broad persistence changes, also run `scripts/check-tests.cmd`.
- If migration behavior changed, prefer adding or updating a focused Room guard test.
