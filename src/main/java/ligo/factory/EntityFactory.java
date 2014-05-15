package ligo.factory;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import ligo.exceptions.IllegalDBOperation;
import ligo.exceptions.IllegalLabelExtractionAttemptException;
import ligo.exceptions.IllegalReflectionOperation;
import ligo.meta.RelationType;
import ligo.utils.Beanify;
import ligo.utils.EntityUtils;
import org.neo4j.graphdb.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Set;

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
   * @param key   key
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
   * @param id    long id
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
   * @param key   key
   * @param value value
   */
  public <T> void delete(final Class<T> klass, final String key, final String value) {
    try (Transaction tx = db.beginTx();
         ResourceIterator<Node> iterator =
             db.findNodesByLabelAndProperty(label(EntityUtils.extractNodeLabel(klass)), key, value).iterator()) {
      while (iterator.hasNext()) {
        final Node node = iterator.next();
        for (Relationship relationship : node.getRelationships()) {
          relationship.delete();
        }
        node.delete();
      }
      iterator.close();
      tx.success();
    }
  }

  /**
   * Delete node
   *
   * @param klass Node's label has to match klass label
   * @param id    Node's id has to match id
   */
  public <T> void delete(final Class<T> klass, final long id) {
    try (Transaction tx = db.beginTx()) {
      final Node nodeById = db.getNodeById(id);
      if (nodeById.hasLabel(label(EntityUtils.extractNodeLabel(klass)))) {
        for (Relationship relationship : nodeById.getRelationships()) {
          relationship.delete();
        }
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
      if ((id = (Long) properties.get("id")) != null) {
        existingNode = db.getNodeById(id);
      }
      tx.success();
    }

    // If submitted node id already exists, this will act as an update
    if (existingNode != null) {
      return t;
    }

    Node newNode = null;
    //Next TX to create new node
    try (Transaction tx = db.beginTx()) {
      newNode = db.createNode(label(labelName));

      for (Map.Entry<String, Object> prop : properties.entrySet()) {
        newNode.setProperty(prop.getKey(), prop.getValue());
      }
      tx.success();
      return Beanify.get(newNode, (Class<T>) t.getClass());
    }

  }

  /**
   * Fetch relatives of the given node that belong to the given RelationType and Direction
   *
   * @param entity       Given node entity
   * @param relationType RelationType
   * @param direction    Should be either OUTGOING, INCOMING, or BOTH. OUTGOING is the most common.
   * @param <T>          Type of the given entity node
   * @param <V>          Type of Relative
   * @return Set of relatives
   */
  public <T, V> Set<V> getRelatives(final T entity, final RelationType relationType,
                                    final Direction direction) {
    if (entity == null) {
      throw new IllegalReflectionOperation("Cannot get relatives from null object");
    }

    try (Transaction tx = db.beginTx()) {
      final Long id = EntityUtils.extractId(entity);
      final Node node = db.getNodeById(id);

      final Iterable<Relationship> relationships = node.getRelationships(relationType, direction);
      final Set<V> relatives = Sets.newHashSet(
          Iterables.transform(relationships, new Function<Relationship, V>() {
            @Override
            public V apply(@Nullable Relationship relationship) {
              return Beanify.get(relationship.getOtherNode(node),
                  (Class<V>) relationType.getOtherNodeType(entity.getClass()));
            }
          })
      );

      tx.success();

      return relatives;
    }

  }

  /**
   * Adds relatives to the given entity node. The relative nodes are created if they don't exist.
   *
   * @param entity       Given node entity
   * @param relationType RelationType
   * @param relatives    Relatives to be added
   * @param <T>          Type of the given entity node
   * @param <V>          Type of Relative
   */
  public <T, V> void addRelatives(final T entity, final RelationType relationType,
                                  final V... relatives) {
    if (entity == null) {
      throw new IllegalReflectionOperation("Cannot get relatives from null object");
    }

    if (relatives == null || relatives[0] == null) {
      throw new IllegalDBOperation("Cannot add null relatives");
    }

    try (Transaction tx = db.beginTx()) {
      final Long id = EntityUtils.extractId(entity);
      final Node node = db.getNodeById(id);

      for (V relative : relatives) {
        Node relativeNode = null;
        try {
          final Long relativesId = EntityUtils.extractId(relative);
          relativeNode = db.getNodeById(relativesId);
        } catch (IllegalReflectionOperation e) {
          LOG.error("Skipping relative {} due to problem in extracting its Long id", e);
        } catch (NotFoundException nfe) {
          LOG.debug("Node not found");
        }

        if (relativeNode == null) {
          createUnique(relative);
        }
        node.createRelationshipTo(relativeNode, relationType);
      }

      tx.success();
    }

  }

}
