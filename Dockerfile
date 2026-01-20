FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY . .

RUN chmod +x mvnw

RUN ./mvnw clean package -DskipTests

EXPOSE 8056

CMD ["java", "-jar", "target/ATM-Project-1.0.jar"]

