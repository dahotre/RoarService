package ligo.meta;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Annotation for all entities, i.e., nodes and relations
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Entity {
  public EntityType type() default EntityType.NODE;
  public String label() default "";
}
