package me.roar.model;

import ligo.meta.*;

/**
 * Lion entity
 */
@Entity(type = EntityType.NODE, label = "Lion")
public class Lion extends LigoEntity {
  private String name;
  private int age;

  @Index(type = IndexType.FULL_TEXT, name = "lion_name_ft")
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

  @Index(type = IndexType.EXACT)
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
}
