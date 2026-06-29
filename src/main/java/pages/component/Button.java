package pages.component;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import core.LocatorWrapper;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
public class Button extends LocatorWrapper {

    public Button(Page page, Locator locator) {
        super(page, locator);
    }

    public Button(Page page, Locator locator, String elementTitle) {
        super(page, locator, elementTitle);
    }
}