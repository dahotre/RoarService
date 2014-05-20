package ligo.factory;

import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import ligo.config.DBConfig;
import ligo.exceptions.IllegalDBOperation;
import ligo.exceptions.IllegalLabelExtractionAttemptException;
import ligo.exceptions.IllegalReflectionOperation;
import ligo.meta.IndexType;
import ligo.meta.Indexed;
import ligo.meta.RelationType;
import ligo.utils.Beanify;
import ligo.utils.EntityUtils;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexHits;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.util.Collection;
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
  protected <T> Set<T> find(final String key, final Object value, Class<T> klass) {
    String labelName = EntityUtils.extractNodeLabel(klass);
    Set<T> tSet = null;
    try (Transaction tx = db.beginTx();
         ResourceIterator<Node> nodes =
             db.findNodesByLabelAndProperty(label(labelName), key, value).iterator()) {

      while (nodes != null && nodes.hasNext()) {
        if (tSet == null) {
          tSet = Sets.newHashSet();
        }
        tSet.add(Beanify.get(nodes.next(), klass));

      }
      tx.success();
    }
    return tSet;
  }


  /**
   * Searches for the given query in the specified index name
   *
   * @param indexName Index name to be leveraged
   * @param key Name of field to search
   * @param query Query string
   * @param klass Class of the expected result
   * @param <T> Class of the expected result
   * @return Set of objects that match the search criteria
   */
  protected <T> Set<T> search(final String indexName, final String key, final String query, Class<T> klass) {
    if (Strings.isNullOrEmpty(indexName) || Strings.isNullOrEmpty(query) || klass == null) {
      return null;
    }
    Set<T> tSet = null;

    try (Transaction tx = db.beginTx()) {
      final Index<Node> fullTextIndex = DBConfig.getFullTextIndex(indexName);
      final IndexHits<Node> hits = fullTextIndex.query(key, query);
      for (Node hit : hits) {
        if (tSet == null) {
          tSet = Sets.newHashSet();
        }
        tSet.add(Beanify.get(hit, klass));
      }
      tx.success();
    }
    return tSet;
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
    final Collection<Index<Node>> fullTextIndexes = DBConfig.getFullTextIndexes(klass);

    try (Transaction tx = db.beginTx();
         ResourceIterator<Node> iterator =
             db.findNodesByLabelAndProperty(label(EntityUtils.extractNodeLabel(klass)), key, value).iterator()) {
      while (iterator.hasNext()) {
        final Node node = iterator.next();
        for (Relationship relationship : node.getRelationships()) {
          relationship.delete();
        }
        for (Index<Node> fullTextIndex : fullTextIndexes) {
          fullTextIndex.remove(node);
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
    final Collection<Index<Node>> fullTextIndexes = DBConfig.getFullTextIndexes(klass);

    try (Transaction tx = db.beginTx()) {
      final Node nodeById = db.getNodeById(id);
      if (nodeById.hasLabel(label(EntityUtils.extractNodeLabel(klass)))) {
        for (Relationship relationship : nodeById.getRelationships()) {
          relationship.delete();
        }
        for (Index<Node> fullTextIndex : fullTextIndexes) {
          fullTextIndex.remove(nodeById);
        }
        nodeById.delete();
      } else {
        LOG.warn("Nothing to delete.");
      }
      tx.success();
    }
  }

  /**
   * If an object with the same Id exists, it's node representation will be returned.
   * Else the given instance will be persisted and the resultant node will be returned.
   *
   * @param t Entity to be persisted
   * @param <T> Type of given entity
   * @return Node representation of Object
   */
  protected <T> Node createUniqueNode(final T t) {
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

    // If submitted node id already exists, it will be returned
    if (existingNode != null) {
      return existingNode;
    }

    //Getting ready for node creation. First find full text indexes
    Map<String, String> keyToIndexNameMap = Maps.newHashMap();
    final Set<Method> indexable = EntityUtils.extractIndexable(t.getClass());
    if (indexable != null) {
      for (Method method : indexable) {
        final Indexed indexed = method.getAnnotation(Indexed.class);
        if (indexed.type().equals(IndexType.FULL_TEXT)) {
          int beginIndex = method.getName().startsWith("get") ? 3 : 2;
          keyToIndexNameMap.put(method.getName().substring(beginIndex).toLowerCase(), indexed.name());
        }
      }
    }

    Node newNode = null;
    //Next TX to create new node
    try (Transaction tx = db.beginTx()) {
      newNode = db.createNode(label(labelName));

      for (Map.Entry<String, Object> prop : properties.entrySet()) {
        newNode.setProperty(prop.getKey(), prop.getValue());
        if (keyToIndexNameMap.containsKey(prop.getKey())) {
          final Index<Node> fullTextIndex =
              DBConfig.getFullTextIndex(keyToIndexNameMap.get(prop.getKey()));
          fullTextIndex.add(newNode, prop.getKey(), prop.getValue());
        }
      }


      tx.success();
      return newNode;
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
    try (Transaction tx = db.beginTx()) {
      T persistedT = Beanify.get(createUniqueNode(t), (Class<T>) t.getClass());
      tx.success();
      return persistedT;
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

          if (relativesId == null) {
            relativeNode = createUniqueNode(relative);
          } else {
            relativeNode = db.getNodeById(relativesId);
          }
        } catch (IllegalReflectionOperation e) {
          LOG.error("Skipping relative {} due to problem in extracting its Long id", e);
        } catch (NotFoundException nfe) {
          LOG.debug("Node not found");
        }

        if (relativeNode == null) {
          throw new IllegalDBOperation(
              "The relative node is null. Here is the relative object : " + relative);
        }
        node.createRelationshipTo(relativeNode, relationType);
      }

      tx.success();
    }

  }

}
