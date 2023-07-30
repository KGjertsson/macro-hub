#!/bin/bash
psql -h localhost -p 15432 -U postgres -d postgres
CREATE DATABASE macroanalyzer;
CREATE USER macrouser WITH PASSWORD 'macropassword';
ALTER USER macrouser WITH SUPERUSER;
