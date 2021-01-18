#!/bin/bash
curl -X POST -H "Content-Type: application/json" \
-d '{"first_name": "paz", "last_name": "pz", "phone": "0500000000", "start_position": "A", "end_position": "B", "departure_time": "15/02/2021", "vacancies": "4", "pd": "5"}' \
http://172.18.1.4:8080/publishRide

curl -X POST -H "Content-Type: application/json" \
-d '{"first_name": "paz", "last_name": "pz", "phone": "0500000000", "start_position": "A", "end_position": "B", "departure_time": "15/02/2021", "vacancies": "4", "pd": "5"}' \
http://172.18.0.6:8080/publishRide

curl -X POST -H "Content-Type: application/json" \
-d '{"first_name": "Rob", "last_name": "pz", "phone": "0500000000", "start_position": "C", "end_position": "B", "departure_time": "15/02/2021", "vacancies": "4", "pd": "2"}' \
http://172.18.0.4:8080/publishRide

curl -X POST -H "Content-Type: application/json" \
-d '{"first_name": "pzz", "last_name": "pz", "phone": "0500000000", "start_position": "C", "end_position": "A", "departure_time": "15/02/2021", "vacancies": "4", "pd": "5"}' \
http://172.18.1.6:8080/publishRide

curl -X POST -H "Content-Type: application/json" \
-d '{"first_name": "scott", "last_name": "sh", "path": "A,B", "departure_time": "15/02/2021"}' \
http://172.18.1.4:8080/reserveRide

curl 'http://172.18.1.4:8080/snapshot'



#test case: We publish a ride1 from A to B with vacancies=4 and a ride2 from C to B with vacancies=4. A ride from A to B
#agrees to pick someone from C.
#s1 is leader for A, B and s2 is leader for C. Reserve ride2 until it's full and then try to reserve another ride from
#C to B, asking s1. s1 will ask s2 if he has a ride, s2 doesn't have, he wants to return a nullRide, but he couldn't
#because he tried to build a protobuf from a null ride, and we don't support that (we get null reference when trying
#to build it, and thus s2 dies. s1 sees that s2 died so he continues asking other servers.
#we don't want s2 to die, a solution is to not return a nullRide, s1 anyways initiates the ride he's looking for as nullRide
#so it is enough to send a onComplete to s1, and then he would know that no ride was found.
curl -X POST -H "Content-Type: application/json" -d '{"first_name": "paz", "last_name": "pz", "phone": "0500000000", "start_position": "A", "end_position": "B", "departure_time": "15/02/2021", "vacancies": "4", "pd": "5"}' http://172.18.1.4:8080/publishRide
curl -X POST -H "Content-Type: application/json" -d '{"first_name": "Rob", "last_name": "pz", "phone": "0500000000", "start_position": "C", "end_position": "B", "departure_time": "15/02/2021", "vacancies": "4", "pd": "5"}' http://172.18.1.4:8080/publishRide
curl -X POST -H "Content-Type: application/json" -d '{"first_name": "scott", "last_name": "sh", "path": "A,B", "departure_time": "15/02/2021"}' http://172.18.1.4:8080/reserveRide
curl -X POST -H "Content-Type: application/json" -d '{"first_name": "kevin", "last_name": "sh", "path": "A,B", "departure_time": "15/02/2021"}' http://172.18.1.5:8080/reserveRide
curl -X POST -H "Content-Type: application/json" -d '{"first_name": "jim", "last_name": "sh", "path": "C,B", "departure_time": "15/02/2021"}' http://172.18.1.5:8080/reserveRide
curl -X POST -H "Content-Type: application/json" -d '{"first_name": "scarn", "last_name": "sh", "path": "C,B", "departure_time": "15/02/2021"}' http://172.18.1.5:8080/reserveRide
curl -X POST -H "Content-Type: application/json" -d '{"first_name": "creed", "last_name": "sh", "path": "C,B", "departure_time": "15/02/2021"}' http://172.18.1.5:8080/reserveRide
curl -X POST -H "Content-Type: application/json" -d '{"first_name": "pam", "last_name": "sh", "path": "C,B", "departure_time": "15/02/2021"}' http://172.18.1.4:8080/reserveRide

curl -X POST -H "Content-Type: application/json" -d '{"first_name": "oscar", "last_name": "sh", "path": "C,B", "departure_time": "15/02/2021"}' http://172.18.1.5:8080/reserveRide

curl -X POST -H "Content-Type: application/json" -d '{"first_name": "dwight", "last_name": "sh", "path": "C,B", "departure_time": "15/02/2021"}' http://172.18.1.6:8080/reserveRide
#TODO: If a server got an exception we want it to die and no to be stuck, in order for the system to continue working.

#TODO: when reserving rides, if we ask the leader directly to reserve a ride that he is leader on then we get a response
#contains who reserved this ride before, however if don't ask the leader then we don't get this info because
#we don't add this info when building the protobuf for the ride. (When asking the leader we don't build a protobuf.
#Do we want to return this info or not? should return the same for both cases