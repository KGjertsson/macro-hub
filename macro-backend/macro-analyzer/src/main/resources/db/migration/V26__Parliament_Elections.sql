-- Riksdag results per valkrets reuse the local election tables with election_type 'RD'
INSERT INTO series_configuration (
    global_id,
    name,
    display_name,
    scrape_url,
    category
)
VALUES
    (uuid_generate_v4(), 'ParliamentElections', 'Riksdagsval', 'https://resultat.val.se/resultatfiler/val2026', 'Valmyndigheten')
;
