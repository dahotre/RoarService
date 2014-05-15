package ligo.meta;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;

/**
 * Annotation for all entities, i.e., nodes and relations
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(TYPE)
public @interface Entity {
  public EntityType type() default EntityType.NODE;
  public String label() default "";
}
