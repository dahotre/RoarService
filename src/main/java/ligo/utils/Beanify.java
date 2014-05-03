package ligo.utils;

import org.neo4j.graphdb.Node;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

/**
 * Converts Node/Relations to Objects
 */
public class Beanify<T> {

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

  public T get(Node n, Class<T> klass) {
    if (n == null) return null;

    Map<String, Object> properties = new HashMap<>();
    for (String key : n.getPropertyKeys()) {
      properties.put(key, n.getProperty(key));
    }
    return get(properties, klass);
  }
}
