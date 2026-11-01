-- regional-statistics has its own service and database, and the monolith hasn't read these copies since V33.
-- Referencing tables first and no CASCADE, so a dependency nobody knew about fails this migration instead of being
-- dropped with them.
DROP TABLE scb_area_value;
DROP TABLE scb_availability;
DROP TABLE scb_indicator;
DROP TABLE scb_table_state;
