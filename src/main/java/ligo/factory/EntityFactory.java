package ligo.factory;

import ligo.exceptions.IllegalLabelExtractionAttemptException;
import ligo.utils.Beanify;
import ligo.utils.EntityUtils;
import org.neo4j.graphdb.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static org.neo4j.graphdb.DynamicLabel.label;

/**
 * Basic CRUD for a given Entity
 */
public abstract class EntityFactory {

  private static final Logger LOG = LoggerFactory.getLogger(EntityFactory.class);

  protected GraphDatabaseService db;

  public EntityFactory(GraphDatabaseService db) {
    this.db = db;
  }

  /**
   * Find by property key-value for the given klass
   *
   * @param key key
   * @param value value to be searched. Although this is Object, anything other than
   *              primitives, String, or Array will result in problems.
   * @param klass Class of expected object
   * @return Object found in DB
   * @throws IllegalLabelExtractionAttemptException
   */
  protected <T> T find(final String key, final Object value, Class<T> klass) {
    String labelName = EntityUtils.extractNodeLabel(klass);
    T t = null;
    try (Transaction tx = db.beginTx();
        ResourceIterator<Node> nodes =
            db.findNodesByLabelAndProperty(label(labelName), key, value).iterator()) {

      if (nodes != null) {
        t = Beanify.get(nodes.next(), klass);
      }
      tx.success();
    }
    return t;
  }

  /**
   * Find a node of given class, by it's id.
   *
   * @param id long id
   * @param klass class
   * @return Object found in DB
   */
  protected <T> T find(final Long id, Class<T> klass) {
    T t = null;
    try (Transaction tx = db.beginTx()) {
      try {
        Node nodeById = db.getNodeById(id);
        t = Beanify.get(nodeById, klass);
      } catch (NotFoundException e) {
        LOG.debug("Node {} not found for class {}", id, klass);
      }
      tx.success();
    }
    return t;
  }

  /**
   * Delete node of given class, on basis of the given property key-value
   *
   * @param klass Class to which the object belongs
   * @param key key
   * @param value value
   */
  protected <T> void delete(final Class<T> klass, final String key, final String value) {
    try (Transaction tx = db.beginTx();
         ResourceIterator<Node> iterator =
             db.findNodesByLabelAndProperty(label(EntityUtils.extractNodeLabel(klass)), key, value).iterator()) {
      while (iterator.hasNext()) {
        final Node node = iterator.next();
        node.delete();
      }
      iterator.close();
      tx.success();
    }
  }

  /**
   * Delete node
   * @param klass Node's label has to match klass label
   * @param id Node's id has to match id
   */
  protected <T> void delete(final Class<T> klass, final long id) {
    try (Transaction tx = db.beginTx()) {
      final Node nodeById = db.getNodeById(id);
      if (nodeById.hasLabel(label(EntityUtils.extractNodeLabel(klass)))) {
        nodeById.delete();
      } else {
        LOG.warn("Nothing to delete.");
      }
      tx.success();
    }
  }

  /**
   * If an object with the same Id exists, it will be returned. Else the given instance will be
   * persisted.
   *
   * @param t Instance of Class to be created
   * @return Created instance of class T
   */
  protected <T> T createUnique(final T t) {
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
      Node newNode = db.createNode(label(labelName));

      for (Map.Entry<String, Object> prop : properties.entrySet()) {
        newNode.setProperty(prop.getKey(), prop.getValue());
      }
      tx.success();
      return Beanify.get(newNode, (Class<T>) t.getClass());
    }

  }
}
