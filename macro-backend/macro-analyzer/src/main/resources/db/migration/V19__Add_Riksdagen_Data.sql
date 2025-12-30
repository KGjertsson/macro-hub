CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

INSERT INTO series_configuration (
    global_id,
    name,
    display_name,
    scrape_url,
    category
)
VALUES
    (uuid_generate_v4(), 'MembersOfParliament', 'Riksdagsledamöter', 'https://data.riksdagen.se/personlista/?iid=&fnamn=&enamn=&f_ar=&kn=&parti=&valkrets=&rdlstatus=samtliga&org=&utformat=json&sort=sorteringsnamn&sortorder=asc&termlista=', 'Riksdagen')
;

CREATE TABLE IF NOT EXISTS swedish_members_of_parliament
(
    id           SERIAL PRIMARY KEY,
    global_id    uuid         NOT NULL,
    member_name  VARCHAR(100) NOT NULL,
    member_data  JSONB        NOT NULL,
    created      TIMESTAMP DEFAULT NOW(),
    updated      TIMESTAMP DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS swedish_members_of_parliament_member_name ON swedish_members_of_parliament (member_name);

CREATE OR REPLACE FUNCTION update_updated_column()
RETURNS TRIGGER AS
$$
BEGIN
    NEW.updated = NOW();
    RETURN NEW;
END;
$$ language 'plpgsql';

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_trigger WHERE tgname = 'update_swedish_members_of_parliament_updated') THEN
        CREATE TRIGGER update_swedish_members_of_parliament_updated
            BEFORE UPDATE
            ON swedish_members_of_parliament
            FOR EACH ROW
        EXECUTE PROCEDURE update_updated_column();
    END IF;
END $$;
