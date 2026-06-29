FROM ubuntu:noble

ARG DEBIAN_FRONTEND=noninteractive
ARG TZ=America/Los_Angeles

ENV LANG=C.UTF-8
ENV LC_ALL=C.UTF-8

# === Установка JDK, Maven, Node.js ===

RUN apt-get update && \
    apt-get install -y --no-install-recommends \
    openjdk-17-jdk \
    maven \
    nodejs \
    npm \
    && rm -rf /var/lib/apt/lists/*

# Установка Playwright
RUN mkdir /tmp/playwright && cd /tmp/playwright \
    && npm init -y \
    && npm install playwright

# Установка chrome и зависисмостей
RUN cd /tmp/playwright && npx playwright install chrome --with-deps

# Путь к браузерам Playwright
ENV PLAYWRIGHT_BROWSERS_PATH=/opt/google/chrome/chrome

# Очистка временных файлов
RUN rm -rf /tmp/playwright

WORKDIR /app

# Копируем и кэшируем зависимости Maven
COPY pom.xml .
RUN mvn dependency:go-offline

# Копируем исходники тестов
COPY src ./src

# Создаём папку для Allure-результатов
RUN mkdir -p /app/target/allure-results

VOLUME /app/target/allure-results

ENTRYPOINT ["mvn", "test"]