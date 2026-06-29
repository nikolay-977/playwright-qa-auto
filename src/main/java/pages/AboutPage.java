package pages;

import com.microsoft.playwright.Locator;

import core.BasePage;
import core.FindBy;
import io.qameta.allure.Step;

public class AboutPage extends BasePage {

    @FindBy(locator = ".title")
    private Locator title;

    @Step("Get title of the 'AboutPage' page")
    public Locator getTitle() {
        return title;
    }
}