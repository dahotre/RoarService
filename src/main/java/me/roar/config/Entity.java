package me.roar.config;

import me.roar.model.factory.EntityType;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Annotation for all entities, i.e., nodes and relations
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Entity {
  EntityType type() default EntityType.NODE;
}
