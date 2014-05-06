package ligo.utils;

import org.neo4j.graphdb.Node;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

/**
 * Converts Node/Relations to Objects
 */
public class Beanify<T> {

  /**
   * Converts given map of properties into an object of Class klass
   *
   * @param map
   * @param klass
   * @return
   */
  public T get(Map<String, Object> map, Class<T> klass) {
    if (map == null) return null;

    T newInstance = null;
    try {
      newInstance = klass.getConstructor().newInstance();
      EntityUtils.populate(newInstance, map);
    } catch (IllegalAccessException | InvocationTargetException | InstantiationException | NoSuchMethodException e) {
      e.printStackTrace();
    }
    return newInstance;
  }

  /**
   * Converts given Node into an object of Class klass
   *
   * @param node
   * @param klass
   * @return
   */
  public T get(Node node, Class<T> klass) {
    if (node == null) return null;

    Map<String, Object> properties = new HashMap<>();
    for (String key : node.getPropertyKeys()) {
      properties.put(key, node.getProperty(key));
    }
    properties.put("id", node.getId());
    return get(properties, klass);
  }
}
