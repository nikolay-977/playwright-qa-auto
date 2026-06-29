FROM ubuntu:noble

ARG DEBIAN_FRONTEND=noninteractive
ARG TZ=America/Los_Angeles

ENV LANG=C.UTF-8
ENV LC_ALL=C.UTF-8

# === Установка JDK, Maven, Node.js ===

RUN apt-get update && \
    apt-get install -y --no-install-recommends \
    openjdk-21-jdk \
    wget \
    unzip \
    gnupg \
    ca-certificates \
    curl \
    && rm -rf /var/lib/apt/lists/*

# Установка Node.js 18 (требуется для Playwright)
RUN curl -fsSL https://deb.nodesource.com/setup_22.x | bash - \
    && apt-get install -y nodejs \
    && rm -rf /var/lib/apt/lists/*

# Установка Maven 3.9+
RUN VERSION=3.9.9 && \
    wget -O - https://archive.apache.org/dist/maven/maven-3/$VERSION/binaries/apache-maven-$VERSION-bin.tar.gz | tar zxfv - -C /opt/ && \
    ln -s /opt/apache-maven-$VERSION/bin/mvn /usr/local/bin/

# Установка Playwright и браузеров
RUN mkdir /tmp/playwright && cd /tmp/playwright \
    && npm init -y \
    && npm install playwright@1.61.0

# Установка браузеров
RUN cd /tmp/playwright && npx playwright install chrome --with-deps

# Путь к браузерам Playwright
ENV PLAYWRIGHT_BROWSERS_PATH=/root/.cache/ms-playwright

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