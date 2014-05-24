package ligo.utils;

import com.google.common.collect.Maps;
import ligo.exceptions.IllegalLabelExtractionAttemptException;
import ligo.exceptions.IllegalReflectionOperation;
import ligo.meta.Entity;
import ligo.meta.Id;
import ligo.meta.Indexed;
import ligo.meta.Property;
import org.reflections.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Set;

/**
 * Helper static methods for entities
 */
public class EntityUtils {

  private static final Logger LOG = LoggerFactory.getLogger(EntityUtils.class);

  /**
   * Extracts keys and values of all the @Property annotated fields
   *
   * @param t   Instance of the entity
   * @param <T> Type of entity
   * @return Map of key, value of fields
   */
  public static <T> Map<String, Object> extractPersistableProperties(T t) {
    Set<Field> allProperties =
        ReflectionUtils.getAllFields(t.getClass(), ReflectionUtils.withAnnotation(Property.class));
    Map<String, Object> properties = null;

    for (Field property : allProperties) {
      property.setAccessible(true);
      try {
        if (properties == null) {
          properties = Maps.newHashMap();
        }
        properties.put(property.getName().toLowerCase(), property.get(t));
      } catch (IllegalAccessException e) {
        new IllegalReflectionOperation(e);
      }
    }

    return properties;
  }

  /**
   * Extracts the label property of @Entity annotation from a class. If label value is not present,
   * then the class's simple name is returned as the default label.
   *
   * @param klass Candidate Class
   * @return Label name
   * @throws IllegalLabelExtractionAttemptException if the klass does not have @Entity annotation
   */
  public static String extractNodeLabel(Class klass) throws IllegalLabelExtractionAttemptException {

    if (!klass.isAnnotationPresent(Entity.class)) {
      throw new IllegalLabelExtractionAttemptException("class does not have @Entity annotation");
    }

    final String nodeLabel = ((Entity) klass.getAnnotation(Entity.class)).label().toLowerCase();
    if (nodeLabel == null || nodeLabel.isEmpty()) {
      return klass.getSimpleName().toLowerCase();
    } else {
      return nodeLabel;
    }
  }

  /**
   * Extracts the Long id from the given entity class
   *
   * @param entity Entity with a @Id Long field
   * @param <T>    Class of entity
   * @return id
   */
  public static <T> Long extractId(T entity) {
    Set<Field> allIdFields =
        ReflectionUtils.getAllFields(entity.getClass(), ReflectionUtils.withAnnotation(Id.class));
    if (allIdFields == null || allIdFields.size() != 1) {
      throw new IllegalReflectionOperation("@Id should be used exactly once for a class : "
          + entity.getClass());
    }
    Field idField = allIdFields.iterator().next();
    idField.setAccessible(true);
    try {
      return ((Long) idField.get(entity));
    } catch (IllegalAccessException e) {
      throw new IllegalReflectionOperation("@Id supports only Long");
    }
  }

  /**
   * Extract indexable fields for a given Class
   *
   * @param klass Class
   * @return set of fields
   */
  public static Set<Field> extractIndexable(Class<?> klass) {
    return ReflectionUtils.getAllFields(klass, ReflectionUtils.withAnnotation(Indexed.class));
  }
}
