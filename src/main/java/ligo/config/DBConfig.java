package ligo.config;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

import java.io.IOException;
import java.util.Properties;

/**
 * Configures Neo start/shut
 */
public class DBConfig {

  public static boolean isDbOn = false;
  private static String dbPath;

  private GraphDatabaseService db;

  private final String DB_PROP_FILE = "db.properties";

  public DBConfig() {
    Properties dbProperties = new Properties();
    try {
      dbProperties.load(DBConfig.class.getClassLoader().getResourceAsStream(DB_PROP_FILE));
      dbPath = dbProperties.getProperty("dbPath");
    } catch (IOException e) {
      e.printStackTrace();
    }
    start();
  }

  public DBConfig(String dbPath) {
    this.dbPath = dbPath;
    start();
  }

  public void start() {
    db = new GraphDatabaseFactory().newEmbeddedDatabaseBuilder(dbPath).newGraphDatabase();
    isDbOn = true;
    registerShutdownHook(db);
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
