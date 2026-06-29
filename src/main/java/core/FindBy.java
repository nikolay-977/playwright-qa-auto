package core;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface FindBy {
    String locator() default "";
    String parentLocator() default ""; // имя поля, являющегося родительским локатором
}
