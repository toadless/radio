FROM openjdk:15-jdk
WORKDIR /home/radio/
COPY build/libs/Radio-all.jar Radio.jar
ENTRYPOINT java -jar Radio.jar
