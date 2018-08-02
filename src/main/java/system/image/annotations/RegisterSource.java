package system.image.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface RegisterSource {
    /**
     * The prefix of the image id that identifies a source
     */
    String scheme();

    /**
     * If true, when used in async mode, the image resolver will create another
     * thread to serve the request. Useful when obtaining the image is
     * a slow process ( for example when downloading the image from the web )
     */
    boolean useAnotherThread() default false;
}
