package ru.domrf.autotests.tests;

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Tracing;
import io.qameta.allure.Allure;
import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestResult;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static config.ConfigurationManager.config;

public class ConfigFailureListener implements IInvokedMethodListener {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    @Override
    public void afterInvocation(IInvokedMethod method, ITestResult testResult) {
        // Не обрабатываем успешные или пропущенные методы
        if (testResult.getThrowable() == null) return;
        if (testResult.getStatus() == ITestResult.SKIP) return;

        BaseTest baseTest = BaseTest.getCurrentInstance();
        if (baseTest == null) {
            System.err.println("BaseTest instance is null – cannot capture failure artifacts");
            return;
        }

        Page page = baseTest.page;
        BrowserContext browserContext = baseTest.browserContext;
        String currentTestName = baseTest.getCurrentTestName();
        String methodName = method.getTestMethod().getMethodName();
        String type = method.isConfigurationMethod() ? "config" : "test";
        Throwable error = testResult.getThrowable();

        // 1. Лог ошибки (текст + стектрейс)
        attachErrorLog(error, type, methodName);

        // 2. Скриншот – только если страница существует и не закрыта
        attachScreenshot(page, type, methodName);

        // 3. Трассировка – только если включена и контекст активен
        if (config().trace() && browserContext != null) {
            saveTrace(browserContext, currentTestName, type, methodName);
        }

        // 4. Видео – в listener'е не сохраняем, так как контекст ещё не закрыт.
        //    Видео корректно сохраняется только в @AfterMethod (в BaseTest).
        //    При желании можно добавить флаг "ошибка" и сохранить видео в BaseTest.tearDown().
    }

    private void attachErrorLog(Throwable error, String type, String methodName) {
        try {
            String errorLog = error.toString() + "\n" + stackTraceToString(error);
            Allure.getLifecycle().addAttachment(
                    "Error log on " + type + ":" + methodName,
                    "text/plain",
                    "txt",
                    new ByteArrayInputStream(errorLog.getBytes(StandardCharsets.UTF_8))
            );
        } catch (Exception e) {
            System.err.println("Failed to attach error log: " + e.getMessage());
        }
    }

    private String stackTraceToString(Throwable error) {
        StringBuilder sb = new StringBuilder();
        for (StackTraceElement element : error.getStackTrace()) {
            sb.append(element).append("\n");
        }
        return sb.toString();
    }

    private void attachScreenshot(Page page, String type, String methodName) {
        if (page == null) return;
        try {
            // Проверяем, не закрыта ли страница
            if (!page.isClosed()) {
                byte[] screenshot = page.screenshot();
                if (screenshot != null && screenshot.length > 0) {
                    Allure.getLifecycle().addAttachment(
                            "Screenshot on " + type + ":" + methodName,
                            "image/png", "png",
                            new ByteArrayInputStream(screenshot)
                    );
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to capture screenshot: " + e.getMessage());
        }
    }

    private void saveTrace(BrowserContext browserContext, String testName, String type, String methodName) {
        try {
            String timestamp = LocalDateTime.now().format(formatter);
            Path tracePath = Paths.get("target/traces/" + testName + "_" + timestamp + "_trace.zip");
            Files.createDirectories(tracePath.getParent());

            // Останавливаем трассировку и сохраняем в файл
            browserContext.tracing().stop(new Tracing.StopOptions().setPath(tracePath));

            if (Files.exists(tracePath)) {
                try (InputStream is = Files.newInputStream(tracePath)) {
                    Allure.addAttachment("Trace on " + type + ":" + methodName,
                            "application/zip", is, "zip");
                }
            }
        } catch (Exception e) {
            // Игнорируем ошибку "Tracing has been stopped" – она означает,
            // что трассировку уже остановили (например, в предыдущем failure)
            if (!e.getMessage().contains("Tracing has been stopped")) {
                System.err.println("Failed to save trace: " + e.getMessage());
            }
            try {
                // Пытаемся остановить трассировку без сохранения, чтобы избежать утечки
                browserContext.tracing().stop();
            } catch (Exception ignored) {}
        }
    }
}