import argparse
import os

from kazoo.client import KazooClient


def zkConnect(hosts) -> KazooClient:
    zk = KazooClient(hosts=hosts)
    zk.start()
    return zk


def cleanBuild(zookeeper: KazooClient, paths, root):
    zkClean(zookeeper, root)
    for path in paths:
        if path == "/" or path == "/zookeeper": continue
        try:
            print(f"Creating {path}...")
            zookeeper.create(path, makepath=True)
            print(f"{path} created successfully!")
        except:
            print(f"Could not create path: {path}")


def zkClean(zookeeper: KazooClient, root):
    try:
        print(f"Cleaning root {root}")
        if root == "/":
            childs = zookeeper.get_children(path=root)
            for child in childs:
                zkClean(zookeeper, child)
            return
        elif root == "/zookeeper":
            print("Directory zookeeper cant be removed!")
            return
        zookeeper.delete(root, recursive=True)
        print(f"root {root} cleaned")
    except:
        print("Path does not exist! building from scratch...")


if __name__ == "__main__":

    parser = argparse.ArgumentParser(description="Cleans & build zookeeper")
    parser.add_argument('--zookeeper_host', type=str, help="host address to connect to (for example --zookeeper-host localhost:2181")
    parser.add_argument('--shards', nargs="+", default=[], help="shards list (for example --shards shard-1 shard-2")
    args = parser.parse_args()
    shards_list = args.shards
    host = args.zookeeper_host
    ROOT = "/Uber"
    ELECTIONS = os.path.join(ROOT, "elections")
    ACTIVE = os.path.join(ROOT, "active")
    MESSAGES = os.path.join(ROOT, "mailbox")
    COUNTER = os.path.join(ROOT, "id-generator")

    PATHS = [ROOT, ELECTIONS, ACTIVE, MESSAGES, COUNTER]
    for shard in shards_list:
        PATHS.append(os.path.join(ELECTIONS, shard))
    zk = zkConnect(args.zookeeper_host)
    zkClean(zk, root="/")
    cleanBuild(zookeeper=zk, paths=PATHS, root=ROOT)
