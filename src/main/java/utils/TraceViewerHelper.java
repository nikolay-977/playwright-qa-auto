package utils;

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Tracing;
import io.qameta.allure.Attachment;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@UtilityClass
public class TraceViewerHelper {

    private static final DateTimeFormatter formatter =
            DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    private static final String TRACE_DIR = "target/traces/";

    @SneakyThrows
    @Attachment(value = "Trace: {testName}", type = "application/zip")
    public static byte[] saveTrace(BrowserContext context, String testName) {
        if (context == null) {
            return new byte[0];
        }

        String timestamp = LocalDateTime.now().format(formatter);
        String safeTestName = testName.replaceAll("[^a-zA-Z0-9]", "_");
        Path tracePath = Paths.get(TRACE_DIR + safeTestName + "_" + timestamp + "_trace.zip");

        // Создаем директорию
        Files.createDirectories(tracePath.getParent());

        // Останавливаем и сохраняем трассу
        context.tracing().stop(new Tracing.StopOptions()
                .setPath(tracePath));

        if (Files.exists(tracePath)) {
            return Files.readAllBytes(tracePath);
        }
        return new byte[0];
    }

    /**
     * Начинает группировку действий в трассе
     */
    public static void startGroup(BrowserContext context, String groupName) {
        if (context != null) {
            context.tracing().group(groupName);
            System.out.println("📑 Trace group started: " + groupName);
        }
    }

    /**
     * Завершает группировку
     */
    public static void endGroup(BrowserContext context) {
        if (context != null) {
            context.tracing().groupEnd();
        }
    }

    /**
     * Создает команду для открытия трассы
     */
    public static String getOpenTraceCommand(Path tracePath) {
        return String.format(
                "mvn exec:java -D exec.mainClass=com.microsoft.playwright.CLI -D exec.args=\"show-trace %s\"",
                tracePath.toString()
        );
    }
}