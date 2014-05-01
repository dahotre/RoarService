package ligo.utils;

import ligo.meta.Transient;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper static methods for entities
 */
public class EntityUtils {

  private static final List<Method> DEFAULT_METHODS = Arrays.asList(Object.class.getMethods());

  public static boolean isTransient(Method m) {
    if (m == null || m.getAnnotations() == null) {
      return false;
    }

    for (Annotation annotation : m.getAnnotations()) {
      if (annotation.annotationType().equals(Transient.class)) {
        return true;
      }
    }

    return false;
  }

  public static <T> Map<String, Object> extractPersistableProperties(T t) {

    Map<String, Object> properties = new HashMap<>(5);
    for (Method method : t.getClass().getMethods()) {
      String methodName = method.getName();

      if ( !DEFAULT_METHODS.contains(method) &&  (methodName.startsWith("get") || methodName.startsWith("is")) && !isTransient(method) ) {
        try {
          final Object value = method.invoke(t);
          properties.put(methodName.substring(3), value);
        } catch (IllegalAccessException | InvocationTargetException e) {
          e.printStackTrace();
        }
      }
    }
    return properties;
  }
}
