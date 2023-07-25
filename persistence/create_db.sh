#!/bin/bash
docker exec -it <mycontainer> bash

psql -d postgres -U postgres

CREATE DATABASE macroanalyzer;

CREATE TABLE your_table_name (
    column1 data_type1 constraints,
    column2 data_type2 constraints,
    -- Add more columns and constraints as needed.
);

CREATE USER macrouser WITH PASSWORD 'macroPassword';
GRANT ALL PRIVILEGES ON macroanalyzer TO macrouser;
