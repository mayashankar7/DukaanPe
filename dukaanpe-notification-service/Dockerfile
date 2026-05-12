FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /workspace
COPY pom.xml .
COPY src ./src
RUN mvn -B -ntp clean package -DskipTests && JAR_PATH=$(find target -maxdepth 1 -type f -name '*.jar' ! -name '*original*' | head -n 1) && cp "$JAR_PATH" /tmp/app.jar

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /tmp/app.jar /app/app.jar
EXPOSE 8091
ENTRYPOINT ["java", "-jar", "/app/app.jar"]

