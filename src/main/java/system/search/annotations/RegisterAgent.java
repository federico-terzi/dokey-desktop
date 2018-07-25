package system.search.annotations;

import system.search.results.Result;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface RegisterAgent {
    /**
     * Agent Priority, Higher value means higher priority
     * @return
     */
    int priority();

    /**
     * Java class of the Result produced by this agent
     * @return
     */
    Class<? extends Result> resultClass();
}
