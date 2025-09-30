# Etapa 1: build da aplicação usando Gradle
FROM gradle:8.5-jdk17 AS build
WORKDIR /app

# Copia arquivos Gradle e de configuração
COPY build.gradle settings.gradle ./

COPY gradle ./gradle

# Copia o código-fonte
COPY src ./src

# Gera o .jar
RUN gradle clean bootJar

# Etapa 2: imagem final com o JAR
FROM eclipse-temurin:17-jdk
WORKDIR /app

# Copia o jar gerado na etapa anterior
COPY --from=build /app/build/libs/*.jar app.jar

# Define a porta exposta
EXPOSE 8080

# Executa a aplicação
ENTRYPOINT ["java", "-jar", "app.jar"]
