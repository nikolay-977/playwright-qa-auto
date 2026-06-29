package pages.component;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import core.LocatorWrapper;

public class Block extends LocatorWrapper {

    public Block(Page page, Locator locator) {
        super(page, locator);
    }

    public Block(Page page, Locator locator, String elementTitle) {
        super(page, locator, elementTitle);
    }
}