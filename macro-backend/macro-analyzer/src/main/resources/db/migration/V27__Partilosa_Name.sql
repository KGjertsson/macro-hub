-- Members without a party are shown as "Partilösa" in the seat distribution
UPDATE riksdag_party
SET name = 'Partilösa'
WHERE code = '-';
