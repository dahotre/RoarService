package me.roar.model.repository;

import ligo.exceptions.IllegalLabelExtractionAttemptException;
import ligo.repository.EntityRepo;
import me.roar.model.node.Roar;

import java.util.Date;
import java.util.Set;

/**
 * CRUD ops for Roar
 */
public class RoarRepo extends EntityRepo {

  public static final String TEXT = "text";

  public RoarRepo() {
    super();
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

  public Set<Roar> findByText(final String text) throws IllegalLabelExtractionAttemptException {
    return find(TEXT, text, Roar.class);
  }

  public Roar find(final Long id) {
    return find(id, Roar.class);
  }

  public void delete(final long id) {
    delete(Roar.class, id);
  }

}
