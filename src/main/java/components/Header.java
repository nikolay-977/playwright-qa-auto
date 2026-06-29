package components;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

import io.qameta.allure.Step;
import lombok.Getter;
import core.FindBy;
import core.PageFactory;

@Getter
public class Header {

    private final Page page;

    @FindBy(locator = ".shopping_cart_link")
    private Locator cartIcon;

    @FindBy(locator = "#react-burger-menu-btn")
    private Locator hamburgerIcon;

    @FindBy(locator = ".shopping_cart_badge")
    private Locator cartBadge;

    public Header(Page page) {
        this.page = page;
        PageFactory.initElements(this, page);
    }

    @Step("Click on cart icon")
    public void clickOnCart() {
        cartIcon.click();
    }

    @Step("Click on hamburger menu icon")
    public void clickOnHamburgerIcon() {
        hamburgerIcon.click();
    }

    @Step("Get cart item count")
    public String getCartItemCount() {
        return cartBadge.textContent();
    }
}