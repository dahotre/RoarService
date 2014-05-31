package me.roar.model.repository;

import ligo.repository.EntityRepo;
import me.roar.model.node.Lion;
import me.roar.model.node.Roar;
import me.roar.model.relationship.Roars;

import java.util.Date;
import java.util.Set;

/**
 * CRUD ops for Lion
 */
public class LionRepo extends EntityRepo {

  private static final String NAME = "name";

  public LionRepo() {
    super();
  }

  /**
   * Create unique node labeled Lion in the DB
   *
   * @param lion Lion instance
   * @return Lion instance saved to the DB
   */
  public Lion create(Lion lion) {
    lion.setCreatedAt(new Date());
    return save(lion);
  }

  public Set<Lion> findByName(final String name) {
    return find(NAME, name, Lion.class);
  }

  public Set<Lion> searchByName(final String name) {
    return search("lion_name_ft", NAME, name, Lion.class);
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

  public Set<Roar> getRoars(final Lion lion) {
    return getRelatives(lion, Roars.newInstance());
  }

  public void addRoar(final Lion lion, Roar... roars) {
    addRelatives(lion, Roars.newInstance(), roars);
  }

}
