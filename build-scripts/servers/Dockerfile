FROM openjdk:latest

WORKDIR /usr/server/
COPY server-1.0-SNAPSHOT.jar .
COPY config.json .
ENTRYPOINT ["java", "-jar", "server-1.0-SNAPSHOT.jar"]
CMD ["./comfig.json", "server"]
