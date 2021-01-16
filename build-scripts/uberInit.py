import os
import sys
import cleaner.ZKInit as zkInit


# gets number of servers and cities coordinates from the stdin and return an int and a list of dictionaries
def getConfigs():
    servers_number = int(input("Enter servers number:\n"))
    print(servers_number)
    print("Enter city names and coordinates:")
    for line in sys.stdin.readlines():
        print(line)
        city_info = line.split(",")
        city = {"city_name": city_info[0],
                "X": city_info[1],
                "Y": city_info[2]}


def main():
    servers_number, cities = getConfigs()



if __name__ == '__main__':
    main()
