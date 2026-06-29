package core;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import lombok.extern.slf4j.Slf4j;
import pages.component.Block;
import pages.component.Button;
import pages.component.Input;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class PageFactory {

    private static final ThreadLocal<Page> currentPage = new ThreadLocal<>();
    private static final Map<String, Class<? extends BasePage>> pageCache = new ConcurrentHashMap<>();

    static {
        scanPagesPackage();
    }

    public static void setCurrentPage(Page page) {
        currentPage.set(page);
    }

    public static void clearCurrentPage() {
        currentPage.remove();
    }

    @SuppressWarnings("unchecked")
    public static <T extends BasePage> T getPage(String name) {
        Page page = currentPage.get();
        if (page == null) {
            throw new IllegalStateException("Current Page is not set. Call PageFactory.setCurrentPage(page) before getPage()");
        }
        Class<? extends BasePage> pageClass = pageCache.get(name);
        if (pageClass == null) {
            throw new RuntimeException("Page with name '" + name + "' not found. Check @PageObject annotation.");
        }
        try {
            T instance = (T) pageClass.getDeclaredConstructor().newInstance();
            instance.init(page);
            return instance;
        } catch (Exception e) {
            log.error("Failed to create page instance for name: {}", name, e);
            throw new RuntimeException("Failed to create page instance", e);
        }
    }

    public static <T> T create(Class<T> pageClass, Page page) {
        try {
            T instance = pageClass.getDeclaredConstructor().newInstance();
            if (instance instanceof BasePage) {
                ((BasePage) instance).init(page);
            } else {
                throw new RuntimeException("Page class must extend BasePage");
            }
            return instance;
        } catch (Exception e) {
            log.error("Failed to create page instance: {}", pageClass.getName(), e);
            throw new RuntimeException("Failed to create page instance", e);
        }
    }

    // -------- Инициализация полей --------

    public static void initElements(Object pageObject, Page page) {
        initElements(pageObject, page, null);
    }

    public static void initElements(Object pageObject, Page page, Locator rootLocator) {
        Field[] fields = pageObject.getClass().getDeclaredFields();

        // Сначала компоненты (их можно обрабатывать аналогично или отдельно)
        for (Field field : fields) {
            if (field.isAnnotationPresent(ComponentObject.class)) {
                processComponent(field, pageObject, page);
            }
        }

        // Поля без parent
        for (Field field : fields) {
            if (field.isAnnotationPresent(FindBy.class)) {
                FindBy findBy = field.getAnnotation(FindBy.class);
                if (findBy.parentLocator().isEmpty()) {
                    processFindBy(field, pageObject, page, rootLocator);
                }
            }
        }

        // Поля с parent
        for (Field field : fields) {
            if (field.isAnnotationPresent(FindBy.class)) {
                FindBy findBy = field.getAnnotation(FindBy.class);
                if (!findBy.parentLocator().isEmpty()) {
                    processFindBy(field, pageObject, page, rootLocator);
                }
            }
        }
    }

    private static void processComponent(Field field, Object pageObject, Page page) {
        try {
            field.setAccessible(true);
            Object component = field.getType().getDeclaredConstructor(Page.class).newInstance(page);
            initElements(component, page);
            field.set(pageObject, component);
            log.debug("Initialized component field: {}", field.getName());
        } catch (Exception e) {
            log.error("Failed to initialize component: {}", field.getName(), e);
            throw new RuntimeException("Failed to initialize component: " + field.getName(), e);
        }
    }

    private static void processFindBy(Field field, Object pageObject, Page page, Locator rootLocator) {
        FindBy findBy = field.getAnnotation(FindBy.class);
        String selector = findBy.locator();
        String parentFieldName = findBy.parentLocator();

        try {
            field.setAccessible(true);
            Locator targetLocator;
            String parentElementTitle = null;
            String parentSimpleName = null;

            if (!parentFieldName.isEmpty()) {
                Field parentField = pageObject.getClass().getDeclaredField(parentFieldName);
                parentField.setAccessible(true);
                Object parentValue = parentField.get(pageObject);
                Locator parentLocator = extractLocator(parentValue);
                if (parentLocator == null) {
                    throw new RuntimeException("Parent field '" + parentFieldName + "' does not provide a Locator");
                }
                targetLocator = parentLocator.locator(selector);

                parentSimpleName = parentField.getType().getSimpleName();
                if (parentField.isAnnotationPresent(ElementTitle.class)) {
                    ElementTitle parentEt = parentField.getAnnotation(ElementTitle.class);
                    parentElementTitle = parentEt.value();
                    if (parentElementTitle != null && parentElementTitle.trim().isEmpty()) {
                        parentElementTitle = null;
                    }
                }
            } else if (rootLocator != null) {
                targetLocator = rootLocator.locator(selector);
            } else {
                targetLocator = page.locator(selector);
            }

            // Заголовок текущего элемента
            String currentElementTitle = null;
            if (field.isAnnotationPresent(ElementTitle.class)) {
                ElementTitle et = field.getAnnotation(ElementTitle.class);
                currentElementTitle = et.value();
                if (currentElementTitle != null && !currentElementTitle.trim().isEmpty()) {
                    currentElementTitle = currentElementTitle.trim();
                } else {
                    currentElementTitle = null;
                }
            }

            // Составной заголовок для логирования
            String finalElementTitle = null;
            if (currentElementTitle != null) {
                if (parentElementTitle != null && parentSimpleName != null) {
                    String childSimpleName = field.getType().getSimpleName();
                    finalElementTitle = parentSimpleName + ":" + parentElementTitle + "][" + childSimpleName + ":" + currentElementTitle;
                } else {
                    finalElementTitle = field.getType().getSimpleName() + ":" + currentElementTitle;
                }
            }

            Class<?> fieldType = field.getType();
            Object element;

            // Обработка Locator
            if (fieldType == Locator.class) {
                element = targetLocator;
            }
            // Если тип является наследником LocatorWrapper, создаём через рефлексию
            else if (LocatorWrapper.class.isAssignableFrom(fieldType)) {
                element = createLocatorWrapperInstance((Class<? extends LocatorWrapper>) fieldType, page, targetLocator, finalElementTitle);
            }
            else {
                log.warn("Unsupported field type: {} for field: {}", fieldType, field.getName());
                return;
            }

            field.set(pageObject, element);

        } catch (Exception e) {
            log.error("Failed to process @FindBy for field: {}", field.getName(), e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Создаёт экземпляр наследника LocatorWrapper через рефлексию.
     * Сначала ищет конструктор (Page, Locator, String), если нет — (Page, Locator).
     */
    @SuppressWarnings("unchecked")
    private static <T extends LocatorWrapper> T createLocatorWrapperInstance(Class<T> clazz, Page page, Locator locator, String elementTitle) {
        try {
            // Пытаемся найти конструктор с тремя параметрами (Page, Locator, String)
            Constructor<T> threeArgConstructor = clazz.getDeclaredConstructor(Page.class, Locator.class, String.class);
            threeArgConstructor.setAccessible(true);
            return threeArgConstructor.newInstance(page, locator, elementTitle);
        } catch (NoSuchMethodException e) {
            // Нет трёхпараметрического — ищем двухпараметрический (Page, Locator)
            try {
                Constructor<T> twoArgConstructor = clazz.getDeclaredConstructor(Page.class, Locator.class);
                twoArgConstructor.setAccessible(true);
                return twoArgConstructor.newInstance(page, locator);
            } catch (NoSuchMethodException ex) {
                throw new RuntimeException("Class " + clazz.getName() + " must have constructor (Page, Locator) or (Page, Locator, String)", ex);
            } catch (Exception ex) {
                throw new RuntimeException("Failed to instantiate " + clazz.getName(), ex);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to instantiate " + clazz.getName(), e);
        }
    }

    private static Locator extractLocator(Object fieldValue) {
        if (fieldValue instanceof Locator) return (Locator) fieldValue;
        if (fieldValue instanceof Input) return ((Input) fieldValue).getLocator();
        if (fieldValue instanceof Button) return ((Button) fieldValue).getLocator();
        if (fieldValue instanceof Block) return ((Block) fieldValue).getLocator();
        return null;
    }

    // -------- Сканирование пакета (без изменений) --------

    private static void scanPagesPackage() {
        long start = System.currentTimeMillis();
        log.info("Scanning pages package...");
        String packageName = "pages";
        String packagePath = packageName.replace('.', '/');
        try {
            Enumeration<URL> resources = Thread.currentThread().getContextClassLoader().getResources(packagePath);
            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                String filePath = URLDecoder.decode(resource.getFile(), StandardCharsets.UTF_8.name());
                File dir = new File(filePath);
                if (dir.isDirectory()) {
                    for (File file : Objects.requireNonNull(dir.listFiles())) {
                        if (file.getName().endsWith(".class")) {
                            String className = packageName + "." + file.getName().replace(".class", "");
                            try {
                                Class<?> clazz = Class.forName(className);
                                if (clazz.isAnnotationPresent(PageObject.class) && BasePage.class.isAssignableFrom(clazz)) {
                                    PageObject pageObject = clazz.getAnnotation(PageObject.class);
                                    String pageName = pageObject.title();
                                    if (pageName == null || pageName.trim().isEmpty()) {
                                        throw new IllegalStateException(
                                                String.format("Page class %s has @PageObject with empty or missing title", clazz.getName())
                                        );
                                    }
                                    if (pageCache.containsKey(pageName)) {
                                        throw new IllegalStateException(
                                                String.format("Duplicate page name '%s' in classes %s and %s",
                                                        pageName, pageCache.get(pageName).getName(), clazz.getName())
                                        );
                                    }
                                    @SuppressWarnings("unchecked")
                                    Class<? extends BasePage> pageClass = (Class<? extends BasePage>) clazz;
                                    pageCache.put(pageName, pageClass);
                                    log.debug("Registered page: {} -> {}", pageName, className);
                                }
                            } catch (ClassNotFoundException e) {
                                log.warn("Cannot load class: {}", className, e);
                            }
                        }
                    }
                }
            }
            long duration = System.currentTimeMillis() - start;
            log.info("Scanned pages package, found {} pages. Duration: {} ms", pageCache.size(), duration);
        } catch (IOException e) {
            long duration = System.currentTimeMillis() - start;
            log.error("Failed to scan pages package after {} ms", duration, e);
            throw new RuntimeException("Failed to scan pages package", e);
        }
    }
}