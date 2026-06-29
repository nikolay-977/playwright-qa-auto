package pages;

import com.microsoft.playwright.Locator;
import components.Header;
import components.SideNavMenu;

import core.*;
import lombok.Getter;
import pages.component.Block;
import pages.component.Button;

import java.util.List;

@Getter
@PageObject(title = "Продукты")
public class ProductPage extends BasePage {

    @ComponentObject
    private Header header;



    @FindBy(locator = ".inventory_list")
    private Block root ;

    @ElementTitle("Карточка продукта")
    @FindBy(locator = ".inventory_item")
    private Block inventoryItem ;

    @FindBy(locator = ".inventory_item_name", parentLocator = "inventoryItem")
    private Locator nameLabel;

    @FindBy(locator = ".inventory_item_price", parentLocator = "inventoryItem")
    private Locator priceLabel;

    @ElementTitle("В корзину")
    @FindBy(locator = "button[data-test*='add-to-cart']", parentLocator = "inventoryItem")
    private Button addToCartButton;

    @ComponentObject
    private SideNavMenu sideNavMenu;
}