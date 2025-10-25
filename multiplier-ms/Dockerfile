#Builder Container to build jar file
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /app
COPY . .
RUN mvn -q -DskipTests package

#Runtime Container
FROM eclipse-temurin:21-jre
WORKDIR /app
ENV JAVA_OPTS=""
EXPOSE 8080
COPY --from=builder /app/target/*.jar /app/app.jar
ENTRYPOINT ["bash","-lc","java $JAVA_OPTS -jar /app/app.jar"]