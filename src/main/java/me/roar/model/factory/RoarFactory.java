package me.roar.model.factory;

import ligo.exceptions.IllegalLabelExtractionAttemptException;
import ligo.factory.EntityFactory;
import me.roar.model.Roar;
import org.neo4j.graphdb.GraphDatabaseService;

import java.util.Date;

/**
 * CRUD ops for Roar
 */
public class RoarFactory extends EntityFactory {

  public static final String TEXT = "text";

  public RoarFactory(GraphDatabaseService db) {
    super(db);
  }

  /**
   * Create unique node labeled Roar in the DB
   *
   * @param roar Roar instance
   * @return Roar instance saved to the DB
   */
  public Roar create(Roar roar) {
    roar.setUpdatedAt(new Date());
    return createUnique(roar);
  }

  public Roar findByText(final String text) throws IllegalLabelExtractionAttemptException {
    return find(TEXT, text, Roar.class);
  }

  public Roar find(final Long id) {
    return find(id, Roar.class);
  }

  public void delete(final long id) {
    delete(Roar.class, id);
  }

}
