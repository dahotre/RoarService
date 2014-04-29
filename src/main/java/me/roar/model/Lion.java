package me.roar.model;

import me.roar.config.Entity;
import me.roar.model.factory.EntityType;

/**
 * Lion entity
 */
@Entity(type = EntityType.NODE)
public class Lion extends BaseNode {
  private String name;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
}
