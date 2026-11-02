-- parliament has its own service and database, and the monolith hasn't read these copies since V37.
-- riksdag_election_result references riksdag_election and riksdag_party, so it goes first. No CASCADE or IF EXISTS, so
-- a dependency nobody knew about, or a table that isn't there, fails this migration.
DROP TABLE riksdag_election_result;
DROP TABLE riksdag_election;
DROP TABLE riksdag_party;
DROP TABLE riksdag_organ;
DROP TABLE swedish_member_activity;
DROP TABLE swedish_members_of_parliament;

-- Only swedish_members_of_parliament's trigger used it (V19). parliament-service's baseline has its own.
DROP FUNCTION update_updated_column();
