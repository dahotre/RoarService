package ligo.meta;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Marks a getter as transient
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Transient {
  boolean value() default true;
}
