package me.roar.utils;

import ligo.meta.RelationType;
import ligo.utils.BasicRelationType;
import me.roar.model.Lion;
import me.roar.model.Roar;

/**
 * A map of all relation types
 */
public class MyRelationTypes {
  public static final RelationType ROARS =
      new BasicRelationType<>(Lion.class, Roar.class, MyRelationNames.ROARS);
}
