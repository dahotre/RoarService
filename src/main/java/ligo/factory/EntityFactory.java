package ligo.factory;

import ligo.config.DBConfig;
import ligo.exceptions.IllegalLabelExtractionAttemptException;
import ligo.utils.Beanify;
import ligo.utils.EntityUtils;
import org.neo4j.graphdb.*;

import java.util.Map;

/**
 * Basic CRUD for a given Entity
 */
public abstract class EntityFactory<T> {

  protected GraphDatabaseService db;

  private EntityFactory() {}

  public EntityFactory(Class<? extends T> impl) {
    this.db = new DBConfig().getDb();
  }

  public EntityFactory(Class<? extends T> impl, GraphDatabaseService db) {
    this.db = db;
  }

  /**
   * Find by property key-value for the given klass
   *
   * @param key
   * @param value
   * @param klass
   * @return
   * @throws IllegalLabelExtractionAttemptException
   */
  protected T find(final String key, final Object value, Class<T> klass) {
    String labelName = EntityUtils.extractNodeLabel(klass);
    T t = null;
    try (Transaction tx = db.beginTx();
        ResourceIterator<Node> nodes =
            db.findNodesByLabelAndProperty(DynamicLabel.label(labelName), key, value).iterator()) {

      if (nodes != null) {
        t = new Beanify<T>().get(nodes.next(), klass);
      }
      tx.success();
    }
    return t;
  }

  /**
   * Find a node of given class, by it's id.
   *
   * @param id
   * @param klass
   * @return
   */
  protected T find(final Long id, Class<T> klass) {
    try (Transaction tx = db.beginTx()) {
      final Node nodeById = db.getNodeById(id);
      tx.success();
      return new Beanify<T>().get(nodeById, klass);
    }
  }

  /**
   * Delete node of given class, on basis of the given property key-value
   *
   * @param klass
   * @param key
   * @param value
   * @throws IllegalLabelExtractionAttemptException
   */
  protected void delete(final Class<T> klass, final String key, final String value) {
    try (Transaction tx = db.beginTx();
        ResourceIterator<Node> iterator =
            db.findNodesByLabelAndProperty(DynamicLabel.label(EntityUtils.extractNodeLabel(klass)), key, value).iterator()) {
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
  protected T createUnique(final T t) {
    Map<String, Object> properties = EntityUtils.extractPersistableProperties(t);
    String labelName = EntityUtils.extractNodeLabel(t.getClass());

    Node existingNode = null;

    // 1st TX to find existing
    try (Transaction tx = db.beginTx()) {
      Long id;
      if ( (id = (Long) properties.get("id")) != null) {
        existingNode = db.getNodeById((Long) properties.get(id));
      }
      tx.success();
    }

    // If submitted node id already exists, this will act as an update
    if (existingNode != null) {
      return t;
    }

    //Next TX to create new node
    try(Transaction tx = db.beginTx()) {
      Node newNode = db.createNode(DynamicLabel.label(labelName));

      for (Map.Entry<String, Object> prop : properties.entrySet()) {
        newNode.setProperty(prop.getKey(), prop.getValue());
      }
      tx.success();
      return new Beanify<T>().get(newNode, (Class<T>) t.getClass());
    }

  }
}
