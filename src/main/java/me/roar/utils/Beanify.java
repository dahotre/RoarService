package me.roar.utils;

import org.apache.commons.beanutils.BeanUtils;
import org.neo4j.graphdb.Node;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

/**
 * Converts Node/Relations to Objects
 */
public class Beanify<T> {
  private T t;

  private Beanify(){}

  public Beanify(T t) {
    this.t = t;
  }

  public T get(Map<String, Object> map) {
    if (map == null) return null;
    try {
      BeanUtils.populate(t, map);
    } catch (IllegalAccessException | InvocationTargetException e) {
      e.printStackTrace();
    }
    return t;
  }

  public T get(Node n) {
    if (n == null) return null;

    Map<String, Object> properties = new HashMap<>();
    for (String key : n.getPropertyKeys()) {
      properties.put(key, n.getProperty(key));
    }
    return get(properties);
  }
}
