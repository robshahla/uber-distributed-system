import os
import subprocess
import sys
import time
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


# gets number of servers and cities coordinates from the stdin and return an int and a list of dictionaries
def getConfigs():
    servers_number = int(input("Enter servers number:\n"))
    print(servers_number)
    print("Enter city names and coordinates:")

    cities = []
    for line in sys.stdin.readlines():
        city_info = line.split(",")
        city = {"city_name": city_info[0],
                "X": city_info[1],
                "Y": city_info[2]}
        cities.append(city)

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


def run_servers(generator, zookeeper_container_name: str, servers_number: int):
    base_name = NetworkConfig().server_base_name
    docker_image_name = 'server:v1'
    os.system(f'cd ./servers/ && docker image build --no-cache -t {docker_image_name} .')
    for number in range(1, servers_number+1):
        server_name = base_name + '-' + str(number)
        os.system(f'docker rm -f {server_name}')
        time.sleep(2)
        command = [
            'docker', 'run ',
            '--network', NetworkConfig().subnet_name,
            '--ip', next(generator),
            '--link', zookeeper_container_name + ':' + server_name,
            '-d',
            '--name', server_name,
            '-t', f'\"{docker_image_name}\"',
            './config.json', server_name
        ]
        # subprocess.run(" ".join(command))
        os.system(" ".join(command))


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

    # running servers
    run_servers(generator, zookeeper_container_name, servers_number)
    print(next(generator))
    print(next(generator))


if __name__ == '__main__':
    main()
