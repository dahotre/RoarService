package me.roar.config;

import me.roar.model.factory.LionFactory;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.schema.IndexDefinition;
import org.neo4j.graphdb.schema.Schema;

/**
 * Configures Neo start/shut
 */
public class DBConfig {

  public static boolean isDbOn = false;

  private GraphDatabaseService db;

  private String DB_PATH = "resources/db";

  public DBConfig() {
    start();
  }

  public DBConfig(String dbPath) {
    this.DB_PATH = dbPath;
    start();
  }

  public void start() {
    db = new GraphDatabaseFactory().newEmbeddedDatabaseBuilder(DB_PATH).newGraphDatabase();
    isDbOn = true;
    registerShutdownHook(db);
    createIndexes();
  }

  private void createIndexes() {
    try (Transaction tx = db.beginTx()) {
      final Schema schema = db.schema();
      final Iterable<IndexDefinition> lionIndexes = schema.getIndexes(LionFactory.LION_LABEL);
      for (IndexDefinition lionIndex : lionIndexes) {
        for (String key : lionIndex.getPropertyKeys()) {
          if (key.equals(LionFactory.NAME)) {
            lionIndex.drop();
            break;
          }
        }
      }
      schema.indexFor(LionFactory.LION_LABEL).on(LionFactory.NAME).create();
      tx.success();
    }
  }

  private void registerShutdownHook(final GraphDatabaseService db) {
    Runtime.getRuntime().addShutdownHook(new Thread(){
      @Override
      public void run() {
        System.out.println("DB going down. Bye!");
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

}
