package pages;

import com.microsoft.playwright.Locator;

import core.*;
import io.qameta.allure.Step;
import lombok.Getter;
import pages.component.Block;
import pages.component.Button;
import pages.component.Input;

import static config.ConfigurationManager.config;

@PageObject(title = "Авторизация")
@Getter
public class LoginPage extends BasePage {

    @ElementTitle(value = "Логин")
    @FindBy(locator = "#login_button_container")
    private Block loginBlock;

    @ElementTitle(value = "Логин")
    @FindBy(locator = "#user-name", parentLocator = "loginBlock")
    private Input usernameInput;

    @ElementTitle(value = "Пароль")
    @FindBy(locator = "#password")
    private Input passwordInput;

    @ElementTitle(value = "Войти")
    @FindBy(locator = "//input[@id='login-button']")
    private Button loginButton;

    @FindBy(locator = "//h3[@data-test='error']")
    private Locator errorMessage;

    @Step("Navigate to the login page")
    public void open() {
        page.navigate(config().baseUrl());
    }

}