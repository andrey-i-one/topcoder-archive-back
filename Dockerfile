FROM harbor.stageogip.ru/hub/bellsoft/liberica-openjdk-debian:22 AS build

ARG NEXUS_USER
ARG NEXUS_PASS

WORKDIR /app
RUN apt-get update
RUN apt-get install -y maven
COPY . .

RUN mvn clean package -DskipTests

FROM harbor.stageogip.ru/hub/bellsoft/liberica-openjdk-debian:22

ENV ENVIRONMENT=""

RUN apt-get update
RUN apt-get -y install bash
RUN apt-get -y install time
RUN apt-get install -y cgroup-tools
RUN apt-get -y install g++
RUN ulimit -S -m 268435456
RUN apt-get update && apt-get -y install gcc mono-mcs && rm -rf /var/lib/apt/lists/*
# RUN cgcreate -a root -g memory:topcoder256mb
# RUN echo '268435456' > /sys/fs/cgroup/memory/topcoder256mb/memory.limit_in_bytes

WORKDIR /app
COPY --from=build /app/target/*.jar topcoder-archiver-back.jar

EXPOSE 8080

ENTRYPOINT ["java","-Xmx512m","-Dspring.profiles.active=${ENVIRONMENT}","-jar","topcoder-archiver-back.jar"]