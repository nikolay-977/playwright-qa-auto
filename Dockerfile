# Используем Ubuntu 22.04 LTS
FROM ubuntu:22.04

ENV DEBIAN_FRONTEND=noninteractive

# Устанавливаем системные зависимости
RUN apt-get update && apt-get install -y \
    openjdk-17-jdk \
    maven \
    curl \
    wget \
    gnupg \
    unzip \
    xvfb \
    libnss3 \
    libatk-bridge2.0-0 \
    libdrm2 \
    libxkbcommon0 \
    libgbm1 \
    libasound2 \
    libxshmfence1 \
    libxcomposite1 \
    libxdamage1 \
    libxrandr2 \
    libpango-1.0-0 \
    libcairo2 \
    libatspi2.0-0 \
    libgtk-3-0 \
    fonts-liberation \
    libappindicator3-1 \
    libu2f-udev \
    libvulkan1 \
    xdg-utils \
    && rm -rf /var/lib/apt/lists/*

# Устанавливаем Google Chrome 148
RUN wget -q -O - https://dl-ssl.google.com/linux/linux_signing_key.pub | apt-key add - && \
    echo "deb [arch=amd64] http://dl.google.com/linux/chrome/deb/ stable main" >> /etc/apt/sources.list.d/google-chrome.list && \
    apt-get update && \
    apt-get install -y google-chrome-stable=148.0.7778.178-1 && \
    rm -rf /var/lib/apt/lists/*

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