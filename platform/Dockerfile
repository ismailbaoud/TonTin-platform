FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

COPY ./target/platform-0.0.1-SNAPSHOT.war app.war

EXPOSE 9090

ENTRYPOINT [ "java", "-jar", "app.war" ]