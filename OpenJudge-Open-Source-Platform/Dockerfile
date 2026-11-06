FROM maven:3.9.11-eclipse-temurin-25 AS builder
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:25-jre AS runner
WORKDIR /app
COPY --from=builder /app/target/OpenJudge-Open-Source-Platform-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 4000
ENTRYPOINT ["java", "-Xmx400m", "--enable-native-access=ALL-UNNAMED", "-jar", "app.jar"]