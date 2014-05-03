package ligo.factory;

import ligo.config.DBConfig;
import ligo.exceptions.IllegalLabelExtractionAttemptException;
import ligo.utils.Beanify;
import ligo.utils.EntityUtils;
import org.neo4j.graphdb.*;

import java.lang.reflect.Constructor;
import java.util.Map;

/**
 * Basic CRUD for a given Entity
 */
public abstract class EntityFactory<T> {
  public static final String cAT = "cAt";
  public static final String uAT = "uAt";

  protected GraphDatabaseService db;

  private Constructor<? extends T> constructor;

  private EntityFactory() {}

  public EntityFactory(Class<? extends T> impl) {
    try {
      this.constructor = impl.getConstructor();
    } catch (NoSuchMethodException e) {
      e.printStackTrace();
    }
    this.db = new DBConfig().getDb();
  }

  public EntityFactory(Class<? extends T> impl, GraphDatabaseService db) {
    try {
      this.constructor = impl.getConstructor();
    } catch (NoSuchMethodException e) {
      e.printStackTrace();
    }
    this.db = db;
  }

  public T find(final String key, final String value, Class<T> klass)
      throws IllegalLabelExtractionAttemptException {
    String labelName = EntityUtils.extractNodeLabel(klass);
    try (Transaction tx = db.beginTx();
        ResourceIterator<Node> nodes =
            db.findNodesByLabelAndProperty(DynamicLabel.label(labelName), key, value).iterator()) {

      if (nodes != null && nodes.hasNext()) {
        Node node = nodes.next();
        return new Beanify<T>().get(node, klass);
      }
      tx.success();
    }
    return null;
  }

  public void delete(final Label label, final String key, final String value) {
    try (Transaction tx = db.beginTx();
        ResourceIterator<Node> iterator =
            db.findNodesByLabelAndProperty(label, key, value).iterator()) {
      while (iterator.hasNext()) {
        final Node node = iterator.next();
        node.delete();
      }
      iterator.close();
      tx.success();
    }
  }

  /**
   * If an object with the same Id exists, it will be returned. Else the given instance will be
   * persisted.
   * 
   * @param t
   * @return
   * @throws IllegalLabelExtractionAttemptException if given class is not @Entity annotated
   */
  public T createUnique(final T t) throws IllegalLabelExtractionAttemptException {
    Map<String, Object> properties = EntityUtils.extractPersistableProperties(t);
    String labelName = EntityUtils.extractNodeLabel(t.getClass());

    try (Transaction tx = db.beginTx()) {
      final Object idObject = properties.get("id");
      final boolean isIdBlank = idObject == null || ((String) idObject).trim().isEmpty();

      final long id = isIdBlank ? EntityUtils.generateId() : (Long) idObject;

      Node existingNode = null;
      if (!isIdBlank) {
        existingNode = db.getNodeById(id);
      }

      if (existingNode != null) {
        return new Beanify<T>().get(existingNode, (Class<T>) t.getClass());
      } else {
        Node tNode = db.createNode(DynamicLabel.label(labelName));

        properties.put("id", id);
        for (Map.Entry<String, Object> prop : properties.entrySet()) {
          tNode.setProperty(prop.getKey(), prop.getValue());
        }
      }

      tx.success();
    }
    return t;
  }
}
