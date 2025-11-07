FROM maven:3-eclipse-temurin-25-alpine AS builder
ADD src /usr/src/app/src
ADD pom.xml /usr/src/app
WORKDIR /usr/src/app
RUN mvn clean install

FROM eclipse-temurin:25-jre-alpine
LABEL org.opencontainers.image.source=https://github.com/SENERGY-Platform/event-operator-all
ENV NAME=event-all
COPY --from=builder /usr/src/app/target/${NAME}-jar-with-dependencies.jar /opt/operator.jar
ADD https://github.com/jmxtrans/jmxtrans-agent/releases/download/jmxtrans-agent-1.2.6/jmxtrans-agent-1.2.6.jar opt/jmxtrans-agent.jar
CMD ["java","-jar","/opt/operator.jar"]
