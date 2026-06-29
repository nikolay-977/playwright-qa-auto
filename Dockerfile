# Используем Ubuntu 22.04 LTS в качестве базового образа
FROM ubuntu:22.04

# Отключаем интерактивный режим установки пакетов
ENV DEBIAN_FRONTEND=noninteractive

# Устанавливаем системные зависимости
RUN apt-get update && apt-get install -y \
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

# Устанавливаем Java 17 (OpenJDK)
RUN apt-get update && apt-get install -y openjdk-17-jdk && \
    rm -rf /var/lib/apt/lists/*

# Устанавливаем Maven 3.9.x
RUN wget https://dlcdn.apache.org/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.tar.gz -P /tmp && \
    tar -xzf /tmp/apache-maven-3.9.6-bin.tar.gz -C /opt && \
    ln -s /opt/apache-maven-3.9.6 /opt/maven && \
    rm /tmp/apache-maven-3.9.6-bin.tar.gz

# Настраиваем переменные окружения для Maven
ENV MAVEN_HOME=/opt/maven
ENV PATH=$MAVEN_HOME/bin:$PATH

# Устанавливаем Google Chrome версии 148
RUN wget -q -O - https://dl-ssl.google.com/linux/linux_signing_key.pub | apt-key add - && \
    echo "deb [arch=amd64] http://dl.google.com/linux/chrome/deb/ stable main" >> /etc/apt/sources.list.d/google-chrome.list && \
    apt-get update && \
    apt-get install -y google-chrome-stable=148.0.7778.178-1 && \
    rm -rf /var/lib/apt/lists/*

# Проверяем версию Chrome
RUN google-chrome --version

# Создаем пользователя для безопасного запуска браузера
RUN useradd -m -s /bin/bash pwuser

# Устанавливаем рабочую директорию
WORKDIR /app

# Копируем pom.xml для кэширования зависимостей Maven
COPY pom.xml .
RUN mvn dependency:go-offline

# Копируем исходники тестов
COPY src ./src

# Создаём папку для Allure-результатов
RUN mkdir -p /app/target/allure-results && \
    chown -R pwuser:pwuser /app

# Переключаемся на пользователя pwuser
USER pwuser

# Указываем путь к Chrome
ENV PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD=1
ENV PLAYWRIGHT_CHROME_EXECUTABLE_PATH=/usr/bin/google-chrome-stable

# Монтируем том для Allure-результатов
VOLUME /app/target/allure-results

# Точка входа для запуска тестов
ENTRYPOINT ["mvn", "test"]