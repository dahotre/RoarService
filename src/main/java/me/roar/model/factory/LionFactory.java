package me.roar.model.factory;

import ligo.exceptions.IllegalLabelExtractionAttemptException;
import ligo.factory.EntityFactory;
import me.roar.model.Lion;
import org.neo4j.graphdb.GraphDatabaseService;

import java.util.Date;

/**
 * CRUD ops for Lion
 */
public class LionFactory extends EntityFactory<Lion> {

  private static final String NAME = "name";

  public LionFactory(){
    super(Lion.class);
  }

  public LionFactory(GraphDatabaseService db) {
    super(Lion.class, db);
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

  public Lion find(final Long id) {
    return find(id, Lion.class);
  }

  public void deleteByName(final String name) {
    try {
      delete(Lion.class, NAME, name);
    } catch (IllegalLabelExtractionAttemptException e) {
      e.printStackTrace();
    }
  }

}
