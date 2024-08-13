# Uber Distribute System
This repository contains the code for the Uber Distributed System. The system offers is a distributed ride sharing service that allows users to either offer rides or request rides between a group of cities. The system is scalable and fault tolerant.

The servers are implemented in Java and the communication between them is done using the gRPC framework.
Each server runs in a Docker container. Few of the algorithms used between the servers are leader elections, failure detection and atomic broadcast. The cities are represented as points in a 2D plane.

## Run
To run the system, you need to have Docker installed. You can run the system by following these steps:
1. Clone the repository
2. Setup your configuration in the file `build-scripts/input.txt`: the first line is the number of servers in the system, and the rest of the lines are of the form <name_of_city>,<x_coordinate>,<y_coordinate>.
3. Create a network bridge called `uber-network` using subnet address `172.18.0.0/16`. Run all the containers on this bridge, and a ZooKeeper container on this bridge as well. To do this, run the following command:
```bash
python3 build-scripts/uberInit.py
```
A comfortable UI will be displayed once the script is run. You can use this UI to offer rides, request rides and view the status of rides.

### Using the UI
To publish a ride go to the `Publish Ride` tab and insert the info about the ride. After that, choose whether you want to send the request to a random server or not, if not choose your desired server in the list under the `Random server` box. When finished click on `Publish` and you should see the response of the server on the right panel.

To reserve a ride, go to the `Reserve Ride` tab and type the info of your reservation, the path is a comma separated field with the names of the cities in your path in the right order of the path. After that again decide to which server you want to send the request and click on `Reserve`.

To get a snapshot choose the server you want to request it from and click on the `snapshot` button. To clear the right panel, click on `Clear log` beneath it.


