# --------- ETAPE DE BUILD ---------
FROM eclipse-temurin:25-jdk AS build
WORKDIR /app
COPY . .
# Rend le wrapper maven executable
RUN chmod +x mvnw
# Compile le projet en zappant les tests (déjà passés sur Gitlab/Github)
RUN ./mvnw clean package -DskipTests

# --------- ETAPE D'EXECUTION ---------
FROM eclipse-temurin:25-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
