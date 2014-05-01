package ligo.factory;

import ligo.config.DBConfig;
import ligo.utils.Beanify;
import ligo.utils.EntityUtils;
import org.neo4j.graphdb.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

/**
 * Basic CRUD for a given Entity
 */
public abstract class EntityFactory<T> {
  public static final String cAT = "cAt";
  public static final String uAT = "uAt";

  protected GraphDatabaseService db;

  private Constructor<? extends T> constructor;

  private EntityFactory() {
  }

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

  public T find(final Label label, final String key, final String value) {
    try (Transaction tx = db.beginTx();
        ResourceIterator<Node> nodes =
            db.findNodesByLabelAndProperty(label, key, value).iterator()) {

      if (nodes != null && nodes.hasNext()) {
        Node node = nodes.next();
        return new Beanify<>(this.constructor.newInstance()).get(node);
      }
      tx.success();
    } catch (InvocationTargetException | IllegalAccessException | InstantiationException e) {
      e.printStackTrace();
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


  public T createUnique(final T t) {
    Map<String, Object> properties = EntityUtils.extractPersistableProperties(t);

//    try (Transaction tx = db.beginTx();
//         ResourceIterator<Node> resourceIterator = db.findNodesByLabelAndProperty(LION_LABEL, NAME, name).iterator()) {
//      if (resourceIterator == null || !resourceIterator.hasNext()) {
//        l = createLion(name);
//      } else {
//        while (resourceIterator.hasNext()) {
//          // should never have more than 1 element here
//          l = new Beanify<>(new Lion()).get(resourceIterator.next());
//          break;
//        }
//      }
//      resourceIterator.close();
//      tx.success();
//    }
    return t;
  }
}
