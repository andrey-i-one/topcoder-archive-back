FROM harbor.stageogip.ru/hub/bellsoft/liberica-openjdk-alpine-musl:22.0.2-11 AS build

ARG NEXUS_USER
ARG NEXUS_PASS

WORKDIR /app
RUN apk add --no-cache maven
COPY . .

RUN mvn clean package -DskipTests

FROM harbor.stageogip.ru/hub/bellsoft/liberica-openjdk-alpine-musl:22.0.2-11

ENV ENVIRONMENT=""

WORKDIR /app
COPY --from=build /app/target/*.jar topcoder-archiver-back.jar

EXPOSE 8080

ENTRYPOINT ["java","-Xmx512m","-Dspring.profiles.active=${ENVIRONMENT}","-jar","topcoder-archiver-back.jar"]