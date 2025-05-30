FROM eclipse-temurin:17-jdk AS build

WORKDIR /app

COPY .mvn/ .mvn
COPY mvnw pom.xml ./

RUN chmod +x mvnw && ./mvnw dependency:go-offline

COPY src ./src
RUN ./mvnw clean package -DskipTests

FROM eclipse-temurin:17-jdk AS runtime

WORKDIR /app

COPY --from=build /app/target/*.jar shorten-url.jar

CMD ["java", "-jar", "shorten-url.jar"]