FROM eclipse-temurin:17-jdk-jammy AS build
WORKDIR /build

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
COPY src/ src/
RUN sh mvnw -B -DskipTests package

FROM eclipse-temurin:17-jre-jammy
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && useradd --system --uid 10001 --user-group app

WORKDIR /app
RUN mkdir -p /app/uploads/products && chown -R app:app /app
COPY --from=build --chown=app:app /build/target/telegram-miniapp-shop-*.jar /app/app.jar

USER app
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
