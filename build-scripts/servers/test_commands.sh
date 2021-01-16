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