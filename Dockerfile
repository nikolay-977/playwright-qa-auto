# Используем Ubuntu 22.04 LTS
FROM ubuntu:22.04

ENV DEBIAN_FRONTEND=noninteractive

# Устанавливаем системные зависимости
RUN apt-get update && apt-get install -y \
    openjdk-17-jdk \
    maven \
    google-chrome-stable \
    && rm -rf /var/lib/apt/lists/*

# Проверяем версии
RUN java -version && mvn -version && google-chrome --version

# Создаем пользователя
RUN useradd -m -s /bin/bash pwuser

WORKDIR /app

# Копируем pom.xml и кэшируем зависимости
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Копируем исходники
COPY src ./src

# Создаем папку для Allure-результатов
RUN mkdir -p /app/target/allure-results && \
    chown -R pwuser:pwuser /app

USER pwuser

# Настройки Playwright для использования системного Chrome
ENV PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD=1
ENV PLAYWRIGHT_CHROME_EXECUTABLE_PATH=/usr/bin/google-chrome-stable

VOLUME /app/target/allure-results

ENTRYPOINT ["mvn", "test"]