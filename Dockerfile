FROM maven:3.8-eclipse-temurin-17 as builder
ADD src /usr/src/app/src
ADD pom.xml /usr/src/app
WORKDIR /usr/src/app
RUN mvn clean install

FROM ghcr.io/senergy-platform/analytics-operator-base:latest
ENV NAME event-all
LABEL org.opencontainers.image.source https://github.com/SENERGY-Platform/analytics-operator-addTimestamp
COPY --from=builder /usr/src/app/target/${NAME}-jar-with-dependencies.jar /opt/operator.jar