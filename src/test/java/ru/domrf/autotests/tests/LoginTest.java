package ru.domrf.autotests.tests;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import io.qameta.allure.Step;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import pages.LoginPage;
import pages.ProductPage;
import core.PageFactory;
import pages.component.Button;
import pages.component.Input;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static config.ConfigurationManager.config;
import static org.testng.Assert.assertFalse;

public class LoginTest extends BaseTest {

    @BeforeMethod
    @Step("Предусловие")
    public void precondition() {
    }

    @Test
    public void testLoginWithCustomElements1() {
        LoginPage loginPage = PageFactory.getPage("Авторизация");
        ProductPage productPage = PageFactory.getPage("Продукты");
        // Логинимся
        loginPage.open();
        loginPage.get("Логин", Input.class).checkIsVisible();
        loginPage.get("Логин", Input.class).fill("standard_user");
        loginPage.get("Пароль", Input.class).fill("secret_sauce");
        loginPage.get("Войти", Button.class).click();
        // Берём первый товар из списка и добавляем в корзину
        productPage.get("В корзину", Button.class).nth(0).click();
        // Проверяем, что в корзине появился 1 товар
        assertThat(productPage.getHeader().getCartBadge()).hasText("1");
        assertFalse(false);

    }

    @Test
    public void testLoginWithCustomElements2() {
        LoginPage loginPage = PageFactory.getPage("Авторизация");
        ProductPage productPage = PageFactory.getPage("Продукты");
        // Логинимся
        loginPage.open();
        loginPage.getBlock("Логин").checkIsVisible();
        loginPage.getInput("Логин").fill("standard_user");
        loginPage.getInput("Пароль").fill("secret_sauce");
        loginPage.getButton("Войти").click();
        // Берём первый товар из списка и добавляем в корзину
        productPage.getButton("В корзину").nth(0).click();
        // Проверяем, что в корзине появился 1 товар
        assertThat(productPage.getHeader().getCartBadge()).hasText("1");
    }

    @Test
    public void testLoginWithCustomElements3() {
        LoginPage loginPage = PageFactory.create(LoginPage.class, page);
        ProductPage productPage = PageFactory.create(ProductPage.class, page);
        // Логинимся
        loginPage.open();
        loginPage.getUsernameInput().fill("standard_user");
        loginPage.getPasswordInput().fill("secret_sauce");
        loginPage.getLoginButton().click();
        // Берём первый товар из списка и добавляем в корзину
        productPage.getAddToCartButton().nth(0).click();
        // Проверяем, что в корзине появился 1 товар
        assertThat(productPage.getHeader().getCartBadge()).hasText("1");
    }

    @Test
    public void testLoginWithCustomElements4() {
        // Логинимся
        page.navigate(config().baseUrl());
        page.getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName("Username")).fill("standard_user");
        page.getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName("Password")).fill("secret_sauce");
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Login")).click();
        // Берём первый товар из списка и добавляем в корзину
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Add to cart")).nth(0).click();
        // Проверяем, что в корзине появился 1 товар
        assertThat(page.locator("[data-test=\"shopping-cart-link\"]")).hasText("1");
    }

    @AfterMethod
    @Step("Постусловие")
    public void postcondition() {
        assertFalse(false);
    }
}