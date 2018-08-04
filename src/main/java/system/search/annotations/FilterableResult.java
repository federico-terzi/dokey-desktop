package system.search.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface FilterableResult {
    /**
     * The name of the filter expressed as the id of the resource bundle.
     * @return
     */
    String filterName();
}
