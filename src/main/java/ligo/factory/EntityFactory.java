package ligo.factory;

import ligo.config.DBConfig;
import ligo.exceptions.IllegalLabelExtractionAttemptException;
import ligo.utils.Beanify;
import ligo.utils.EntityUtils;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.schema.IndexDefinition;
import org.neo4j.graphdb.schema.Schema;

import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.Set;

/**
 * Basic CRUD for a given Entity
 */
public abstract class EntityFactory<T> {

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

  public void delete(final Class<T> klass, final String key, final String value) throws IllegalLabelExtractionAttemptException {
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
  public T createUnique(final T t) throws IllegalLabelExtractionAttemptException {
    Map<String, Object> properties = EntityUtils.extractPersistableProperties(t);
    String labelName = EntityUtils.extractNodeLabel(t.getClass());

    verifyOrBuildIndexes(t);

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

  private void verifyOrBuildIndexes(T t) throws IllegalLabelExtractionAttemptException {
    Set<String> indexableProperties = EntityUtils.extractIndexableProperties(t);
    if (indexableProperties == null || indexableProperties.isEmpty()) {
      return;
    }
    final Label label = DynamicLabel.label(EntityUtils.extractNodeLabel(t.getClass()));

    try (Transaction tx = db.beginTx()) {
      final Schema schema = db.schema();
      final Iterable<IndexDefinition> indexes = schema.getIndexes(label);
      for (IndexDefinition indexDefinition : indexes) {
        for (String key : indexDefinition.getPropertyKeys()) {
          if (indexableProperties.contains(key)) {
            indexableProperties.remove(key);
          }
        }
      }

      for (String indexableProperty : indexableProperties) {
        System.out.println("Creating index : " + indexableProperty);
        schema.indexFor(label).on(indexableProperty).create();
      }

      tx.success();
    }
  }
}
