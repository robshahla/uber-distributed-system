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

    def buildNetworkBridge(self):
        os.system('docker network rm ' + self.subnet_name)
        time.sleep(2)
        command = [
            'docker', 'network',
            'create',
            '--subnet', self.subnet_address,
            self.subnet_name,
        ]
        os.system(" ".join(command))


class Server:
    def __init__(self, number: int, ip_address: str, shards: list):
        base_server_name = NetworkConfig().server_base_name
        self.name = base_server_name + '-' + str(number)
        self.ip_address = ip_address
        self.outside_address = '0.0.0.0:' + str(int(NetworkConfig().rest_port) + number)
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
        self.shard = ""

    def setShard(self, shard: str):
        self.shard = shard
        return self

    def getConfig(self):
        return {
            'city-name': self.name,
            'X': self.x_co,
            'Y': self.y_co,
            'shard': self.shard
        }


# gets number of servers and cities coordinates from the stdin and return an int and a list of dictionaries
def getConfigs():
    servers_number = int(input("Enter servers number:\n"))
    print(servers_number)
    print("Enter city names and coordinates:")

    cities = []
    for line in sys.stdin.readlines():
        city_info = line.replace('\n', '').split(",")
        cities.append(City(city_info[0], city_info[1], city_info[2]))

    return servers_number, cities


def runZookeeper(container_name: str, address: str):
    os.system('docker rm -f ' + container_name)
    time.sleep(3)
    command = [
        'docker', 'run',
        '--network', NetworkConfig().subnet_name,
        '--ip', address,
        '--name', container_name,
        '--restart', 'always',
        '-d', 'zookeeper'
    ]
    os.system(" ".join(command))



def runCleaner(container_name: str, generator, zookeeper_container_name: str, zookeeper_connection: str, shards: list):
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


def runServers(servers_config: list, zookeeper_container_name: str, servers_number: int):
    docker_image_name = 'server:v3'
    os.system(f'cd ./servers/ && docker image build --no-cache -t {docker_image_name} .')

    # remove existing containers
    for server in servers_config:
        server_name = server.name
        os.system(f'docker rm -f {server_name}')

    time.sleep(2)

    # create the containers
    for server in servers_config:
        command = [
            'docker', 'run ',
            '--network', NetworkConfig().subnet_name,
            '--ip', server.ip_address,
            '--link', zookeeper_container_name + ':' + server.name,
            '-p', server.outside_address + ':' + '8080',
            '-d',
            '--name', server.name,
            '-t', f'\"{docker_image_name}\"',
            './config.json', server.name
        ]
        os.system(" ".join(command))


# return a list that contains all of the cities with their shars assigned, and a list of all the shards
# in our system
def insertShardForCity(cities, servers_number):
    assert servers_number > 0

    shards_number = (len(cities) // 3) + 1  # 3 to 4 cities in each shard
    if len(cities) >= servers_number:
        shards_number = servers_number

    shards = ['shard-' + str(shard_number) for shard_number in range(1, shards_number + 1)]
    cities_info = [cities[city].setShard(shards[city % shards_number]) for city in range(len(cities))]

    return cities_info, shards


# return a list that looks like: [['shard-1'], ['shard-1', 'shard-2'], ['shard-2']]
def getShardsPartition(shards, servers_number: int):
    quorum_size = servers_number // 2 + 1
    responsibility_shards = [[] for _ in range(servers_number)]
    for shard_number in range(len(shards)):
        for server_number in range(shard_number, shard_number + quorum_size):
            responsibility_shards[server_number % servers_number].append(shards[shard_number])

    return responsibility_shards


def createConfigFile(zookeeper_connection, cities_info, servers_config):
    configs = {
        'zk-address': zookeeper_connection,
        'cities': [city.getConfig() for city in cities_info],
        'servers': [server.getConfig() for server in servers_config]
    }
    with open('./servers/config.json', 'w') as config_file:
        json.dump(configs, config_file, indent=2)


def createServersAdressesFile(servers_config: list):
    with open('./servers-addresses.txt', 'w') as file:
        for server in servers_config:
            file.write(server.name + ',' + server.outside_address + '\n')


def copyNeededFiles():
    os.system('cp ../server/build/libs/server-1.0-SNAPSHOT.jar ./servers')
    os.system('cp ../client/build/libs/client-1.0.jar .')


def runGUI():
    os.system('java -jar client-1.0.jar ./servers-addresses.txt')


def main():
    copyNeededFiles()

    servers_number, cities = getConfigs()
    network_config = NetworkConfig()

    # building network bridge for all the containers
    network_config.buildNetworkBridge()

    generator = network_config.generateIpAddress()

    # running zookeeper container
    zookeeper_container_name = 'some-zookeeper'
    zookeeper_address = next(generator)
    # runZookeeper(zookeeper_container_name, zookeeper_address)

    # building the shards and allocating each city in a shard
    cities_info, shards = insertShardForCity(cities, servers_number)

    # running cleaner
    cleaner_container_name = 'cleaner'
    zookeeper_connection = ':'.join([zookeeper_address, network_config.zookeeper_port])
    runCleaner(cleaner_container_name, generator, zookeeper_container_name, zookeeper_connection, shards)

    # configuring the assignment of shards to servers
    responsibility_shards = getShardsPartition(shards, servers_number)

    servers_config = [Server(number=number, ip_address=next(generator), shards=responsibility_shards[number - 1])
                      for number in range(1, servers_number + 1)]

    # creating config.json file
    createConfigFile(zookeeper_connection, cities_info, servers_config)

    createServersAdressesFile(servers_config)

    # running servers
    runServers(servers_config, zookeeper_container_name, servers_number)

    # running GUI
    runGUI()


if __name__ == '__main__':
    main()
