package me.roar.model.factory;

import ligo.exceptions.IllegalLabelExtractionAttemptException;
import ligo.factory.EntityFactory;
import me.roar.model.Lion;
import org.neo4j.graphdb.GraphDatabaseService;

import java.util.Date;

/**
 * CRUD ops for Lion
 */
public class LionFactory extends EntityFactory {

  private static final String NAME = "name";

  public LionFactory(GraphDatabaseService db) {
    super(db);
  }

  /**
   * Create unique node labeled Lion in the DB
   *
   * @param lion Lion instance
   * @return Lion instance saved to the DB
   */
  public Lion create(Lion lion) {
    lion.setCreatedAt(new Date());
    return createUnique(lion);
  }

  public Lion findByName(final String name) throws IllegalLabelExtractionAttemptException {
    return find(NAME, name, Lion.class);
  }

  public Lion find(final Long id) {
    return find(id, Lion.class);
  }

  public void deleteByName(final String name) {
    delete(Lion.class, NAME, name);
  }

  public void delete(final long id) {
    delete(Lion.class, id);
  }

}
