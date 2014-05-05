package me.roar.model;

import ligo.meta.Index;
import ligo.meta.LigoEntity;
import ligo.meta.Entity;
import ligo.meta.EntityType;

/**
 * Lion entity
 */
@Entity(type = EntityType.NODE, label = "Lion")
public class Lion extends LigoEntity {
  private String name;

  @Index
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
}
