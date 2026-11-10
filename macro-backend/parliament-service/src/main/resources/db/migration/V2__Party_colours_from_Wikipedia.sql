-- From sv.wikipedia.org/wiki/Mall:Partifärg. One statement over every row, also those keeping their colour, because
-- /parliament/parties answers in the table's order and an update of only some rows would move them to the end.
UPDATE riksdag_party
SET color = CASE code
                WHEN 'S' THEN '#EE2020'
                WHEN 'M' THEN '#1B49DD'
                WHEN 'C' THEN '#009933'
                WHEN 'V' THEN '#AF0000'
                WHEN 'KD' THEN '#231977'
                WHEN 'L' THEN '#6BB7EC'
                WHEN 'MP' THEN '#83CF39'
                WHEN 'FP' THEN '#6BB7EC'
                WHEN 'NYD' THEN '#FFFF00'
                WHEN 'SD' THEN '#DDDD00'
                ELSE color
    END;
