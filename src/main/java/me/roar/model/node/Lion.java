package me.roar.model.node;

import com.google.gson.Gson;
import ligo.meta.*;

import java.util.List;

/**
 * Lion entity
 */
@Entity(type = EntityType.NODE, label = "Lion")
public class Lion extends LigoEntity {
  @Property
  @Indexed(type = IndexType.FULL_TEXT, name = "lion_name_ft")
  private String name;
  @Property
  @Indexed(type = IndexType.EXACT)
  private int age;
  private List<Roar> roars;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Lion withName(String name) {
    setName(name);
    return this;
  }

  public int getAge() {
    return age;
  }

  public void setAge(int age) {
    this.age = age;
  }

  public Lion withAge(int age) {
    setAge(age);
    return this;
  }

  @Override
  public String toString() {
    return new Gson().toJson(this);
  }
}
