#!/bin/bash
docker run --name postgres-db -p 15432:5432 -e POSTGRES_PASSWORD=pgp -d postgres
