# topcoder-archive-back

## Create network between docker conainers
docker network create topcoder-net

## Run postgres docker container
docker run --name topcoderdb -e POSTGRES_PASSWORD=topcoder872 -p 5432:5432 -d postgres

docker cp dump-postgres-202507251748.sql topcoderdb:/backups
docker exec topcoderdb pg_restore -U postgres -d postgres /backups

## Connect to network
docker network connect topcoder-net topcoderdb

## Run topcoder-api container
docker image build . -t topcoder-container
docker run --privileged --name topcoder-back --network=topcoder-net -p 8084:8084 -d topcoder-container