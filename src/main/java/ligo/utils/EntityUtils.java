package ligo.utils;

import ligo.exceptions.IllegalLabelExtractionAttemptException;
import ligo.exceptions.IllegalReflectionOperation;
import ligo.meta.Entity;
import ligo.meta.Indexed;
import ligo.meta.Transient;
import org.reflections.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * Helper static methods for entities
 */
public class EntityUtils {

  public static final List<Method> DEFAULT_METHODS = Arrays.asList(Object.class.getMethods());
  private static final Logger LOG = LoggerFactory.getLogger(EntityUtils.class);
  private static final String ILLEGAL_GETID_MESSAGE =
      "Instance has either none or more than 1 methods with name getId AND with modifier PUBLIC AND return type Long.";

  public static boolean isTransient(Method m) {
    return hasAnnotation(m, Transient.class);
  }

  public static <T> Map<String, Object> extractPersistableProperties(T t) {

    Map<String, Object> properties = new HashMap<>(5);
    for (Method method : t.getClass().getMethods()) {
      String methodName = method.getName();

      if ((methodName.startsWith("get") || methodName.startsWith("is")) // is getXX or isYY
          && !DEFAULT_METHODS.contains(method)  //is not getClass or other Object methods
          && !isTransient(method)) {  //does not have @Transient

        try {
          final Object value = method.invoke(t);
          if (value != null) {
            int beginIndex = methodName.startsWith("get") ? 3 : 2;
            properties.put(methodName.substring(beginIndex).toLowerCase(), value);
          }
        } catch (IllegalAccessException | InvocationTargetException e) {
          LOG.error("Problem in extractPersistableProperties", e);
          e.printStackTrace();
        }

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

  public static boolean isIndexable(final Method m) {
    return hasAnnotation(m, Indexed.class);
  }

  private static boolean hasAnnotation(final Method m, final Class annotationClass) {

    if (m == null || m.getAnnotations() == null || annotationClass == null
        || !annotationClass.isAnnotation()) {
      return false;
    }

    for (Annotation annotation : m.getAnnotations()) {
      if (annotation.annotationType().equals(annotationClass)) {
        return true;
      }
    }

    return false;
  }

  /**
   * Extracts the Long id from the given entity class
   *
   * @param entity Entity with a public Long getId() method
   * @param <T>    Class of entity
   * @return id
   */
  public static <T> Long extractId(T entity) {
    final Set<Method> methodSet = ReflectionUtils.getAllMethods(entity.getClass(),
        ReflectionUtils.withName("getId"),
        ReflectionUtils.withModifier(Modifier.PUBLIC),
        ReflectionUtils.withReturnType(Long.class));

    if (methodSet == null || methodSet.size() != 1) {
      throw new IllegalReflectionOperation(ILLEGAL_GETID_MESSAGE);
    }

    final Method method = methodSet.iterator().next();

    if (method == null) {
      throw new IllegalReflectionOperation("getId() not found");
    }

    try {
      return (Long) method.invoke(entity);
    } catch (IllegalAccessException | InvocationTargetException | ClassCastException e) {
      throw new IllegalReflectionOperation("Cannot invoke getId on entity", e);
    }
  }
}
