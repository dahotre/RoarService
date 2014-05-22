package me.roar.model.relationship;

import ligo.meta.BaseRelationship;
import ligo.meta.BasicRelationType;
import ligo.meta.RelationType;
import me.roar.model.node.Lion;
import me.roar.model.node.Roar;
import org.neo4j.graphdb.Direction;

import java.util.Map;

/**
 * Relationship of a lion with its roar
 */
public class Roars implements BaseRelationship<Lion, Roar> {

  private static final BasicRelationType<Lion, Roar> ROARS =
      new BasicRelationType<>(Lion.class, Roar.class, "roars");
  private final Map<String, ?> properties;
  private final Direction direction;

  public Roars(Map<String, ?> properties, Direction direction) {
    this.properties = properties;
    this.direction = direction;
  }

  public Roars() {
    this.properties = null;
    this.direction = Direction.OUTGOING;
  }

  @Override
  public RelationType<Lion, Roar> getRelationType() {
    return ROARS;
  }

  @Override
  public Map<String, ?> getProperties() {
    return properties;
  }

  @Override
  public Direction getDirection() {
    return direction;
  }
}
