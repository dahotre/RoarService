package ligo.config;

import com.google.common.base.Predicate;
import com.google.common.collect.Sets;
import ligo.meta.Entity;
import ligo.meta.Indexed;
import ligo.utils.EntityUtils;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.factory.GraphDatabaseSettings;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexManager;
import org.neo4j.graphdb.schema.IndexDefinition;
import org.neo4j.helpers.collection.MapUtil;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

/**
 * Configures Neo start/shut
 */
public class DBConfig {

  private static final Logger LOG = LoggerFactory.getLogger(DBConfig.class);

  public static boolean isDbOn = false;
  private static String dbPath;
  private final String DB_PROP_FILE = "db.properties";
  private Properties dbProperties = new Properties();
  private GraphDatabaseService db;

  public DBConfig() {
    try {
      dbProperties.load(DBConfig.class.getClassLoader().getResourceAsStream(DB_PROP_FILE));
      dbPath = dbProperties.getProperty("dbPath");
    } catch (IOException e) {
      LOG.error("Problem loading DB properties. Check db.properties on classpath", e);
      e.printStackTrace();
    }
    start();
  }

  public DBConfig(String dbPath) {
    this.dbPath = dbPath;
    start();
  }

  public void start() {
    db =
        new GraphDatabaseFactory().newEmbeddedDatabaseBuilder(dbPath)
            .setConfig(GraphDatabaseSettings.allow_store_upgrade, "true")
            .setConfig(GraphDatabaseSettings.node_auto_indexing, "true").newGraphDatabase();
    initIndexes(db);
    isDbOn = true;
    registerShutdownHook(db);
  }

  private void registerShutdownHook(final GraphDatabaseService db) {
    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override
      public void run() {
        LOG.info("DB going down. Bye!");
        db.shutdown();
      }
    });
  }

  public GraphDatabaseService getDb() {
    if (!isDbOn) {
      start();
    }
    return db;
  }

  private void initIndexes(final GraphDatabaseService db) {
    final String[] modelPackages = ((String) dbProperties.get("modelPackages")).split(",");
    Set<Class<?>> entities = new HashSet<>();
    for (String modelPackage : modelPackages) {
      Reflections reflections = new Reflections(modelPackage);
      entities.addAll(reflections.getTypesAnnotatedWith(Entity.class));
    }
    LOG.info("DB has {} entities total.", entities.size());

    try (Transaction tx = db.beginTx()) {
      final Set<String> nodeIndexNames = Sets.newHashSet(db.index().nodeIndexNames());

      for (Class<?> entity : entities) {
        Set<Method> indexedMethods = Sets.filter(Sets.newHashSet(entity.getMethods()),
            new Predicate<Method>() {
              @Override
              public boolean apply(@Nullable Method method) {
                return method.isAnnotationPresent(Indexed.class);
              }
            });

        final Label label = DynamicLabel.label(EntityUtils.extractNodeLabel(entity));
        final Iterable<IndexDefinition> schemaIndexes = db.schema().getIndexes(label);
        for (Method indexedMethod : indexedMethods) {
          final Indexed indexedAnnotation = indexedMethod.getAnnotation(Indexed.class);

          INDEX_SWITCH:
          switch (indexedAnnotation.type()) {
            case EXACT:

              String indexableProperty =
                  indexedMethod.getName().toLowerCase()
                      .substring(indexedMethod.getName().startsWith("get") ? 3 : 2);

              for (IndexDefinition schemaIndex : schemaIndexes) {
                if (schemaIndex.getPropertyKeys().iterator().hasNext()
                    && schemaIndex.getPropertyKeys().iterator().next().equals(indexableProperty)) {

                  break INDEX_SWITCH;
                }
              }

              db.schema().indexFor(label).on(indexableProperty).create();
              LOG.info("Created index for {} on {}", label, indexableProperty);
              break;

            case FULL_TEXT:
              if (nodeIndexNames.contains(indexedAnnotation.name())) {
                continue;
              } else {
                final Index<Node> nodeIndex = db.index().forNodes(indexedAnnotation.name(),
                    MapUtil.stringMap(IndexManager.PROVIDER, "lucene", "type", "fulltext"));
                //TODO Make this variable accessible for further search
                LOG.info("Init index {}", indexedAnnotation.name());
              }
              break;
          }
        }

      }
      tx.success();
    }
  }

}
