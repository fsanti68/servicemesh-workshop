FROM openjdk:8u131-jre-alpine

COPY target/lib/* /app/lib/
COPY target/sync-service-latest.jar /app/app.jar

ENTRYPOINT ["java", "-jar", "/app/app.jar"]