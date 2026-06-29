package core;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static io.qameta.allure.Allure.step;

@Slf4j
@Getter
public abstract class LocatorWrapper {
    protected final Page page;
    protected final Locator locator;
    protected final String elementTitle;

    public LocatorWrapper(Page page, Locator locator) {
        this.page = page;
        this.locator = locator;
        this.elementTitle = getClass().getSimpleName();
    }

    public LocatorWrapper(Page page, Locator locator, String elementTitle) {
        this.page = page;
        this.locator = locator;
        this.elementTitle = elementTitle;
    }

    public void fill(String value) {
        String message = "Пользователь заполняет [%s] знчением: <%s>".formatted(elementTitle, value);
        log.info(message);
        step(message, () -> locator.fill(value));
    }

    public void click() {
        String message = "Пользователь нажимает на [%s]".formatted(elementTitle);
        log.info(message);
        step(message, () -> locator.click());
    }


    public Locator nth(int index) {
        String message = "Извлекается [%s] с индексом <%s>".formatted(elementTitle, index);
        log.info(message);
        return step(message, () -> locator.nth(index));
    }

    public void checkIsVisible() {
        String message = "Проверяется видимость [%s]".formatted(elementTitle);
        log.info(message);
        step(message, () -> assertThat(locator).isVisible());
    }}
