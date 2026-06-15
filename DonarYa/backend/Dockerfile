FROM gradle:8.13-jdk21 AS build
WORKDIR /app
COPY . .
RUN gradle :backend:installDist --no-daemon

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/backend/build/install/backend .
EXPOSE 8080
CMD ["bin/backend"]
