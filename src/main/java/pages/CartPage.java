package pages;

import com.microsoft.playwright.Locator;

import core.BasePage;
import core.FindBy;
import io.qameta.allure.Step;
import components.Header;
import lombok.Getter;
import models.ShipInfo;
import core.ComponentObject;
import pages.component.Input;
import core.PageFactory;

@Getter
public class CartPage extends BasePage {

    @ComponentObject
    private Header header;

    @FindBy(locator = "//div[@class='cart_list']//div[@class='inventory_item_name']")
    private Locator items;

    @FindBy(locator = "//button[@data-test='chekout']")
    private Locator checkoutButton;

    @FindBy(locator = "//input[@data-test='firstName']")
    private Input firstNameInput;

    @FindBy(locator = "//input[@data-test='lastName']")
    private Input lastNameInput;

    @FindBy(locator = "//input[@data-test='postalCode']")
    private Input postalCodeInput;

    @FindBy(locator = "//input[@data-test='continue']")
    private Locator continueButton;

    @FindBy(locator = "//input[@data-test='finish']")
    private Locator finishButton;

    @FindBy(locator = "//h2[@data-test='complete-header']")
    private Locator completeHeader;

    @Step("Get item name")
    public Locator getItems() {
        return items;
    }

    @Step("Click on checkout button")
    public CartPage clickOnCheckout() {
        checkoutButton.click();
        return this;
    }

    @Step("Fill ship information <shipInfo>")
    public CartPage fillInfo(ShipInfo shipInfo) {
        firstNameInput.fill(shipInfo.getFirstName());
        lastNameInput.fill(shipInfo.getLastName());
        postalCodeInput.fill(shipInfo.getZip());
        return this;
    }

    @Step("Click on continue button")
    public CartPage clickOnContinue() {
        continueButton.click();
        return this;
    }

    @Step("Click on finish button")
    public CartPage clickOnFinish() {
        finishButton.click();
        return this;
    }

    @Step("Get complete header")
    public Locator getCompleteHeader() {
        return completeHeader;
    }

    @Step("Go back to products")
    public ProductPage continueShopping() {
        return PageFactory.create(ProductPage.class, page);
    }
}