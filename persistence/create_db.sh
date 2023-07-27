#!/bin/bash
psql -h localhost -p 15432 -U postgres -W
CREATE DATABASE macroanalyzer;
CREATE USER macrouser WITH PASSWORD 'macropassword';
ALTER USER macrouser WITH SUPERUSER;
