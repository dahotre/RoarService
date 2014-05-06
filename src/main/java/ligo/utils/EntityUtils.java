package ligo.utils;

import ligo.exceptions.IllegalLabelExtractionAttemptException;
import ligo.meta.Entity;
import ligo.meta.Index;
import ligo.meta.Transient;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Helper static methods for entities
 */
public class EntityUtils {

  private static final List<Method> DEFAULT_METHODS = Arrays.asList(Object.class.getMethods());
  private static AtomicLong atomicLong = new AtomicLong(System.currentTimeMillis());

  public static boolean isTransient(Method m) {
    return hasAnnotation(m, Transient.class);
  }

  public static <T> Map<String, Object> extractPersistableProperties(T t) {

    Map<String, Object> properties = new HashMap<>(5);
    for (Method method : t.getClass().getMethods()) {
      String methodName = method.getName();

      if (!DEFAULT_METHODS.contains(method)
          && (methodName.startsWith("get") || methodName.startsWith("is")) && !isTransient(method)) {
        try {
          final Object value = method.invoke(t);
          if (value != null) {
            int beginIndex = methodName.startsWith("get") ? 3 : 2;
            properties.put(methodName.substring(beginIndex).toLowerCase(), value);
          }
        } catch (IllegalAccessException | InvocationTargetException e) {
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
   * @param klass
   * @return
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
   * Generates long ID on basis of UUID
   * 
   * @return
   */
  public static long generateId() {
    return System.currentTimeMillis() + atomicLong.incrementAndGet();
  }

  /**
   * populates the instance with the given properties. All the property names are lowercase. So all
   * the camelCase properties in the instance will be changed to lowercase for population.
   * 
   * @param instance
   * @param properties
   * @param <T>
   */
  public static <T> void populate(T instance, Map<String, Object> properties) {
    for (Method method : instance.getClass().getMethods()) {
      final String methodName = method.getName();
      if (!DEFAULT_METHODS.contains(method) && methodName.startsWith("set")) {
        try {
          final String propertyName = methodName.substring("set".length()).toLowerCase();
          if (properties.containsKey(propertyName)) {
            method.invoke(instance, properties.get(propertyName));
          }
        } catch (IllegalAccessException | InvocationTargetException e) {
          e.printStackTrace();
        }
      }
    }

  }

  public static <T> Set<String> extractIndexableProperties(T t) {
    Set<String> indexableProperties = new HashSet<>();
    for (Method method : t.getClass().getMethods()) {
      final String methodName = method.getName();
      if (!DEFAULT_METHODS.contains(method) && (methodName.startsWith("get") || methodName.startsWith("is")) && isIndexable(method) ) {
        indexableProperties.add(methodName.toLowerCase().substring(methodName.startsWith("get") ?
            3 :
            2));
      }
    }

    return indexableProperties;
  }

  private static boolean isIndexable(final Method m) {
    return hasAnnotation(m, Index.class);
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
}
