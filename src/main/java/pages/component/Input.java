package pages.component;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import core.LocatorWrapper;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
public class Input extends LocatorWrapper {

    public Input(Page page, Locator locator) {
        super(page, locator);
    }

    public Input(Page page, Locator locator, String elementTitle) {
        super(page, locator, elementTitle);
    }
}