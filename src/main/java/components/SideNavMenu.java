package components;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

import io.qameta.allure.Step;
import core.FindBy;
import core.PageFactory;

public class SideNavMenu {

    private final Page page;

    @FindBy(locator = "#logout_sidebar_link")
    private Locator logoutLink;

    @FindBy(locator = "#about_sidebar_link")
    private Locator aboutLink;

    @FindBy(locator = "#inventory_sidebar_link")
    private Locator allItemsLink;

    @FindBy(locator = "#reset_sidebar_link")
    private Locator resetAppLink;

    public SideNavMenu(Page page) {
        this.page = page;
        PageFactory.initElements(this, page);
    }

    @Step("Click on logout button")
    public void clickOnLogout() {
        logoutLink.click();
    }

    @Step("Click on about button")
    public void clickOnAbout() {
        aboutLink.click();
    }

    @Step("Click on all items link")
    public void clickOnAllItems() {
        allItemsLink.click();
    }

    @Step("Click on reset app link")
    public void clickOnResetApp() {
        resetAppLink.click();
    }
}