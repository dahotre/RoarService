package ligo.meta;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Denotes that the field should be indexed.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Index {
}
