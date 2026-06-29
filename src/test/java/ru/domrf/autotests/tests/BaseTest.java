package ru.domrf.autotests.tests;

import com.google.common.collect.ImmutableMap;
import com.microsoft.playwright.*;
import io.qameta.allure.Allure;
import io.qameta.allure.testng.AllureTestNg;
import org.testng.ITestResult;
import org.testng.annotations.*;
import api.NetworkInterceptor;
import pages.LoginPage;
import utils.BrowserManager;
import core.PageFactory;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.lang.reflect.Method;

import static com.github.automatedowl.tools.AllureEnvironmentWriter.allureEnvironmentWriter;
import static config.ConfigurationManager.config;

@Listeners({AllureTestNg.class, ConfigFailureListener.class})
public class BaseTest {

    protected NetworkInterceptor networkInterceptor;
    protected static Playwright playwright;
    protected static Browser browser;
    protected BrowserContext browserContext;
    protected Page page;
    protected LoginPage loginPage;

    private String currentTestName;
    private static final ThreadLocal<BaseTest> currentInstance = new ThreadLocal<>();

    public static BaseTest getCurrentInstance() {
        return currentInstance.get();
    }

    public String getCurrentTestName() {
        return currentTestName;
    }

    @BeforeClass(alwaysRun = true)
    public static void initBrowser() {
        playwright = Playwright.create();
        browser = BrowserManager.getBrowser(playwright);
        allureEnvironmentWriter(
                ImmutableMap.<String, String>builder()
                        .put("Platform", System.getProperty("os.name"))
                        .put("Version", System.getProperty("os.version"))
                        .put("Browser", config().browser())
                        .put("Context URL", config().baseUrl())
                        .put("Trace Enabled", String.valueOf(config().trace()))
                        .put("Video Enabled", String.valueOf(config().video()))
                        .put("Playwright Version", "1.58.0")
                        .build(),
                config().allureResultsDir() + "/");
    }

    @AfterClass(alwaysRun = true)
    public static void close() {
        if (browser != null) browser.close();
        if (playwright != null) playwright.close();
    }

    @BeforeMethod(alwaysRun = true)
    public void createContext(Method method) {
        currentInstance.set(this);
        currentTestName = method.getName().replaceAll("[^a-zA-Z0-9]", "_");

        Browser.NewContextOptions contextOptions = new Browser.NewContextOptions();
        if (config().video()) {
            contextOptions.setRecordVideoDir(Paths.get(config().baseTestVideoPath()));
        }
        browserContext = browser.newContext(contextOptions);
        page = browserContext.newPage();

        networkInterceptor = new NetworkInterceptor(page);

        browserContext.tracing().start(new Tracing.StartOptions()
                .setScreenshots(true)
                .setSnapshots(true)
                .setSources(true));
        System.out.println("🎯 Трассировка начата для теста: " + currentTestName);

        PageFactory.setCurrentPage(page);
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown(ITestResult result) {
        // Остановка трассировки (если она ещё активна)
        if (browserContext != null) {
            try {
                browserContext.tracing().stop();
            } catch (Exception e) {
                // Трассировка уже остановлена в listener'е – игнорируем
            }
            browserContext.close();
        }

        // Сохраняем видео только если тест упал и включена опция video
        if (config().video() && page != null && page.video() != null && !result.isSuccess()) {
            try {
                Path videoPath = page.video().path();
                if (videoPath != null && Files.exists(videoPath)) {
                    try (InputStream is = Files.newInputStream(videoPath)) {
                        Allure.addAttachment("Video on test: " + result.getMethod().getMethodName(),
                                "video/webm", is, "webm");
                    }
                }
            } catch (Exception e) {
                System.err.println("Failed to attach video: " + e.getMessage());
            }
        }
        currentInstance.remove();
    }
}