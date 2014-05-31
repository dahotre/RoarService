package ligo.meta;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static ligo.meta.IndexType.EXACT;

/**
 * Denotes that the field should be indexed.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Indexed {
  public IndexType type() default EXACT;

  /**
   * Name is necessary only for FULL_TEXT indexes. For EXACT index, this name will be ignored.
   *
   * @return name for the index
   */
  public String name() default "";
}
