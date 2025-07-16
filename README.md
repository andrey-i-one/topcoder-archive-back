# topcoder-archive-back

## Run postgres docker container
docker run --name topcoderdb -e POSTGRES_PASSWORD=topcoder872 -p 5432:5432 -d postgres

## Restore data
docker cp ./topcoder.sql topcoderdb:/home/
docker exec topcoderdb pg_restore -U postgres -d postgres /home/topcoder.sql