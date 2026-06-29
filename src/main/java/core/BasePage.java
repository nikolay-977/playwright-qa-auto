package core;

import com.microsoft.playwright.Page;
import lombok.extern.slf4j.Slf4j;
import pages.component.Block;
import pages.component.Button;
import pages.component.Input;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public abstract class BasePage {

    protected Page page;

    // Кэш: заголовок -> (тип элемента -> элемент)
    private final Map<String, Map<Class<? extends LocatorWrapper>, LocatorWrapper>> elements = new HashMap<>();

    public void init(Page page) {
        this.page = page;
        PageFactory.initElements(this, page);
        buildCache();
    }

    private void buildCache() {
        for (Field field : getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(ElementTitle.class)
                    && LocatorWrapper.class.isAssignableFrom(field.getType())) {
                ElementTitle annotation = field.getAnnotation(ElementTitle.class);
                String title = annotation.value();
                if (title == null || title.trim().isEmpty()) {
                    continue;
                }
                title = title.trim();
                field.setAccessible(true);
                try {
                    LocatorWrapper element = (LocatorWrapper) field.get(this);
                    if (element == null) {
                        continue;
                    }
                    Class<? extends LocatorWrapper> elementType = (Class<? extends LocatorWrapper>) element.getClass();

                    // Получаем или создаём карту для данного заголовка
                    Map<Class<? extends LocatorWrapper>, LocatorWrapper> typeMap =
                            elements.computeIfAbsent(title, k -> new HashMap<>());

                    // Проверка дубликата для того же типа
                    if (typeMap.containsKey(elementType)) {
                        throw new IllegalStateException(
                                String.format("Duplicate @ElementTitle '%s' of type %s in page %s. Field: %s",
                                        title, elementType.getSimpleName(), getClass().getName(), field.getName())
                        );
                    }

                    typeMap.put(elementType, element);
                    log.debug("Cached element '{}' of type {} for field {}",
                            title, elementType.getSimpleName(), field.getName());
                } catch (IllegalAccessException e) {
                    log.error("Failed to access field: {}", field.getName(), e);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends LocatorWrapper> T get(String title, Class<T> type) {
        Map<Class<? extends LocatorWrapper>, LocatorWrapper> typeMap = elements.get(title);
        if (typeMap == null) {
            throw new RuntimeException("Element with title '" + title + "' not found in page " + getClass().getSimpleName());
        }
        LocatorWrapper element = typeMap.get(type);
        if (element == null) {
            throw new RuntimeException("Element with title '" + title + "' and type " + type.getSimpleName() +
                    " not found in page " + getClass().getSimpleName());
        }
        return (T) element;
    }

    public Input getInput(String name) {
        return get(name, Input.class);
    }

    public Button getButton(String name) {
        return get(name, Button.class);
    }

    public Block getBlock(String name) {
        return get(name, Block.class);
    }
}