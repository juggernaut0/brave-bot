#!/bin/env bash
docker run --name db --rm -v "$PWD/init_db.sql:/docker-entrypoint-initdb.d/init_db.sql:ro" -d -p 5432:5432 postgres
echo "Postgres started... sleeping..."
sleep 10
./gradlew :dbmigrate:run
