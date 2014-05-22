package me.roar.model.node;

import com.google.gson.Gson;
import ligo.meta.*;

/**
 * Represents Sheep, the entity that follows Lion.
 */
@Entity(type = EntityType.NODE, label = "Sheep")
public class Sheep extends LigoEntity {
  private String name;

  @Indexed(type = IndexType.FULL_TEXT, name = "sheep_name_ft")
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public String toString() {
    return new Gson().toJson(this);
  }
}
