import os
import subprocess
import sys
import time
import json
import cleaner.ZKInit as zkInit


# TODO: make this configurable from ENV VAR, using kofiko
class NetworkConfig:
    def __init__(self):
        self.server_base_name = "server"
        self.subnet_name = "uber-network"
        self.subnet_address = "172.18.0.0/16"
        self.initial_address = "172.18.1.0"
        self.zookeeper_port = "2181"
        self.grpc_port = "8000"
        self.rest_port = "8080"

    # generate ip addresses for containers on demand
    def generateIpAddress(self):
        counter = 2
        while True:
            yield ".".join([self.initial_address.rsplit(".", 1)[0], str(counter)])
            counter += 1


class Server:
    def __init__(self, number: int, ip_address: str, shards: list):
        base_server_name = NetworkConfig().server_base_name
        self.name = base_server_name + '-' + str(number + 1)
        self.ip_address = ip_address
        self.shards = shards

    def getConfig(self):
        return {
            'server-name': self.name,
            'rest-address': self.ip_address + ':' + NetworkConfig().rest_port,
            'grpc-address': self.ip_address + ':' + NetworkConfig().grpc_port,
            'shards': self.shards
        }


class City:
    def __init__(self, name: str, x_co: str, y_co: str):
        self.name = name
        self.x_co = x_co
        self.y_co = y_co


# gets number of servers and cities coordinates from the stdin and return an int and a list of dictionaries
def getConfigs():
    servers_number = int(input("Enter servers number:\n"))
    print(servers_number)
    print("Enter city names and coordinates:")

    cities = []
    for line in sys.stdin.readlines():
        city_info = line.split(",")
        cities.append(City(city_info[0], city_info[1], city_info[2]))

    return servers_number, cities


def run_zookeeper(container_name: str, address: str):
    os.system('docker rm -f ' + container_name)
    time.sleep(3)
    subprocess.run(['docker', 'rm', '-f', container_name])
    command = [
        'docker', 'run',
        '--network', NetworkConfig().subnet_name,
        '--ip', address,
        '--name', container_name,
        '--restart', 'always',
        '-d', 'zookeeper'
    ]
    os.system(" ".join(command))
    subprocess.run(command)


def run_cleaner(container_name: str, generator, zookeeper_container_name: str, zookeeper_connection: str, shards: list):
    docker_image_name = 'cleaner:v1'
    os.system(f'cd ./cleaner/ && docker image build -t {docker_image_name} .')
    os.system('docker rm -f ' + container_name)
    time.sleep(3)
    command = [
        'docker', 'run',
        '--network', NetworkConfig().subnet_name,
        '--ip', next(generator),
        '--link', zookeeper_container_name + ':' + container_name,
        '-d',
        '--name', container_name,
        '-t', f'\"{docker_image_name}\"',
        '--zookeeper_host', zookeeper_connection,
        '--shards', " ".join(shards)
    ]
    os.system(" ".join(command))


def run_servers(servers_config: list, zookeeper_container_name: str, servers_number: int):
    docker_image_name = 'server:v3'
    os.system(f'cd ./servers/ && docker image build --no-cache -t {docker_image_name} .')

    # remove existing containers
    for server in servers_config:
        server_name = server.name
        os.system(f'docker rm -f {server_name}')

    time.sleep(2)

    # create the containers
    for server in servers_config:
        server_name = server.name
        server_ip = server.ip_address
        command = [
            'docker', 'run ',
            '--network', NetworkConfig().subnet_name,
            '--ip', server_ip,
            '--link', zookeeper_container_name + ':' + server_name,
            '-d',
            '--name', server_name,
            '-t', f'\"{docker_image_name}\"',
            './config.json', server_name
        ]
        os.system(" ".join(command))


def insertShardForCity(cities):
    cities_info = []
    cities_info.append(cities[0].addShard('shard-1'))
    cities_info.append(cities[0].addShard('shard-1'))
    cities_info.append(cities[0].addShard('shard-2'))

    return cities_info, ['shard-1', 'shard-2']


def getShardsPartition(shards, servers_number):
    return []


def createConfigFile(zookeeper_connection, cities_info, servers_config):
    configs = {}
    configs['zk-address'] = zookeeper_connection
    configs['cities'] = cities_info
    configs['servers'] = [server.getConfig() for server in servers_config]
    with open('./servers/config-test.json', 'w') as config_file:
        json.dump(configs, config_file)



def main():
    os.system('cp ../server/build/libs/server-1.0-SNAPSHOT.jar ./servers')
    # servers_number, cities = getConfigs()
    # shards = getShards(servers_number, cities)
    servers_number = 3
    shards = ['shard-1', 'shard-2']
    network_config = NetworkConfig()
    generator = network_config.generateIpAddress()

    # running zookeeper container
    zookeeper_container_name = 'some-zookeeper'
    zookeeper_address = next(generator)
    # run_zookeeper(zookeeper_container_name, zookeeper_address)

    # running cleaner
    cleaner_container_name = 'cleaner'
    zookeeper_connection = ':'.join([zookeeper_address, network_config.zookeeper_port])
    run_cleaner(cleaner_container_name,  generator, zookeeper_container_name, zookeeper_connection, shards)

    # building tuples of (server_name, server_ip_address, responsible_shards)
    # cities_info, shards = insertShardForCity(cities)
    # responsibility_shards = getShardsPartition(shards, servers_number)
    responsibility_shards = [['shard-1'], ['shard-1', 'shard-2'], ['shard-2']]

    servers_config = [Server(number=number, ip_address=next(generator), shards=responsibility_shards[number])
                      for number in range(servers_number)]

    # creating config.json file
    # createConfigFile(zookeeper_connection, cities_info, servers_config)


    # running servers
    run_servers(servers_config, zookeeper_container_name, servers_number)
    print(next(generator))
    print(next(generator))


if __name__ == '__main__':
    main()
