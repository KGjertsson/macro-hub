#!/bin/bash
docker run --name my-postgres -p 5432:5432 -e POSTGRES_PASSWORD=mypassword -d postgres
