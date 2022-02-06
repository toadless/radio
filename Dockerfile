FROM openjdk:15-jdk
WORKDIR /home/monkebot/
COPY build/libs/Radio-all.jar Radio.jar
ENTRYPOINT java -jar Radio.jar