package me.roar.model.factory;

import ligo.exceptions.IllegalLabelExtractionAttemptException;
import ligo.factory.EntityFactory;
import me.roar.model.Lion;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;

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
  public Lion createByName(final String name) {
    Lion l = new Lion();
    l.setName(name);
    l.setCreatedAt(new Date());
    try {
      return createUnique(l);
    } catch (IllegalLabelExtractionAttemptException e) {
      e.printStackTrace();
    }
    return null;
  }

  public Lion findByName(final String name) throws IllegalLabelExtractionAttemptException {
    return find(NAME, name, Lion.class);
  }

  public void deleteByName(final String name) {
    delete(LION_LABEL, NAME, name);
  }

}
