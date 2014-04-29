package me.roar.model.factory;

import me.roar.model.Lion;
import me.roar.utils.Beanify;
import org.neo4j.graphdb.*;

import java.util.Date;

/**
 * CRUD ops for Lion
 */
public class LionFactory extends EntityFactory<Lion> {
  public static final Label LION_LABEL = DynamicLabel.label("lion");

  public static final String NAME = "name";

  public LionFactory(){
    super(Lion.class);
  }

  public LionFactory(GraphDatabaseService db) {
    super(Lion.class, db);
  }

  private LionFactory(Class<? extends Lion> impl) {
    super(impl);
  }

  private LionFactory(Class<? extends Lion> impl, GraphDatabaseService db) {
    super(impl, db);
  }

  /**
   * Create unique node labeled Lion in the DB with the given name
   * 
   * @param name
   * @return
   */
  public Lion createUniqueLion(final String name) {
    Lion l = null;
    try (Transaction tx = db.beginTx();
         ResourceIterator<Node> resourceIterator = db.findNodesByLabelAndProperty(LION_LABEL, NAME, name).iterator()) {
      if (resourceIterator == null || !resourceIterator.hasNext()) {
        l = createLion(name);
      } else {
        while (resourceIterator.hasNext()) {
          // should never have more than 1 element here
          l = new Beanify<>(new Lion()).get(resourceIterator.next());
          break;
        }
      }
      resourceIterator.close();
      tx.success();
    }
    return l;
  }

  public Lion findByName(final String name) {
    return find(LION_LABEL, NAME, name);
  }

  public void deleteByName(final String name) {
    delete(LION_LABEL, NAME, name);
  }

  private Lion createLion(final String name) {
    Node lionNode = db.createNode(LION_LABEL);
    lionNode.setProperty(NAME, name);
    final long currentTime = new Date().getTime();
    lionNode.setProperty(cAT, currentTime);
    lionNode.setProperty(uAT, currentTime);
    return new Beanify<>(new Lion()).get(lionNode);
  }

}
