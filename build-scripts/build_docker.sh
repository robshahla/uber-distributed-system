#!/bin/bash
cp ../server/build/libs/server-1.0-SNAPSHOT.jar .
docker network create --subnet=172.18.0.0/16 uber-network

echo cleaning zookeeper
cd ./cleaner/
docker image build -t cleaner:v1 .
docker rm -f cleaner
docker run --network uber-network --ip 172.18.0.4 --link some-zookeeper:cleaner -d --name cleaner -t "cleaner:v1" \
--zookeeper_host 172.18.0.2:2181 --shards shard-1 shard-2 shard-3

#sleep 5
#cd ../servers/
#echo building servers...
#
#echo building servers image...
#docker image build -t server:v2 .
#
#echo removing server-1 container...
#docker rm -f server-1
#
#echo running docker containers from the above image...
#echo running server-1
#docker run  --network uber-network --ip 172.18.1.5 --link some-zookeeper:server-1 -d --name server-1 -t "server:v2" ./config.json server-1
#sleep 3
#
#echo removing server-2 container...
#docker rm -f server-2
#
#echo running server-2
#docker run  --network uber-network --ip 172.18.1.6 --link some-zookeeper:server-2 -d --name server-2 -t "server:v2" ./config.json server-2
#sleep 3
#
#echo removing server-3 container...
#docker rm -f server-3
#
#echo running server-3
#docker run  --network uber-network --ip 172.18.1.7 --link some-zookeeper:server-3 -d --name server-3 -t "server:v2" ./config.json server-3
#
#sleep 2
#docker ps
# docker run -it --rm --network uber-network --ip 172.18.0.7 --link some-zookeeper:zookeeper zookeeper zkCli.sh -server zookeeper

#docker run --network uber-network --ip 172.18.0.4 -it ubuntu bash
#apt-get update
#apt-get install curl

#docker run -it --rm --network uber-network --ip 172.18.0.7 --link some-zookeeper:zookeeper zookeeper zkCli.sh -server zookeeper