-- Members are upserted on Riksdagen's stable person id on every scrape
DELETE FROM swedish_members_of_parliament;

ALTER TABLE swedish_members_of_parliament
    ADD COLUMN intressent_id VARCHAR(64) NOT NULL,
    ADD CONSTRAINT swedish_members_of_parliament_intressent_id_key UNIQUE (intressent_id);
