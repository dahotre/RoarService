package ligo.utils;

import ligo.exceptions.IllegalReflectionOperation;
import org.neo4j.graphdb.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Converts Node/Relations to Objects
 */
public class Beanify {

  private static final Logger LOG = LoggerFactory.getLogger(Beanify.class);

  //Ensure that no one instantiates this util class
  private Beanify() {
  }

  /**
   * Converts given Node into an object of Class klass
   *
   * @param node Neo4j Node
   * @param klass Class
   * @return populated instance of type klass
   * @exception ligo.exceptions.IllegalReflectionOperation
   */
  public static <T> T get(Node node, Class<T> klass) {

    if (node == null)
      return null;
    T instance = null;
    try {
      instance = klass.getConstructor().newInstance();
    } catch (InvocationTargetException | IllegalAccessException | NoSuchMethodException |
        InstantiationException e) {
      throw new IllegalReflectionOperation(e);
    }

    Map<String, Object> properties = new HashMap<>();
    for (String key : node.getPropertyKeys()) {
      properties.put(key, node.getProperty(key));
    }
    properties.put("id", node.getId());

    populate(instance, properties);
    return instance;
  }

  /**
   * populates the instance with the given properties. All the property names are lowercase. So all
   * the camelCase properties in the instance will be changed to lowercase for population.
   *
   * @param instance instance to be populated
   * @param properties properties
   * @exception ligo.exceptions.IllegalReflectionOperation
   */
  public static <T> void populate(T instance, Map<String, Object> properties)
      throws IllegalReflectionOperation {

    for (Method method : instance.getClass().getMethods()) {
      final String methodName = method.getName();
      if (!EntityUtils.DEFAULT_METHODS.contains(method) // is not Object method
          && methodName.startsWith("set")) {  //is a setter

        final String propertyName = methodName.substring("set".length()).toLowerCase();
        if (properties.containsKey(propertyName)) {
          try {
            method.invoke(instance, properties.get(propertyName));
          } catch (InvocationTargetException | IllegalAccessException e) {
            throw new IllegalReflectionOperation(e);
          }
        }
      }
    }
  }
}
