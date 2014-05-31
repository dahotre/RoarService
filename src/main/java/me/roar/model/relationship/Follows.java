package me.roar.model.relationship;

import ligo.meta.BaseRelationship;
import ligo.meta.BasicRelationType;
import ligo.meta.RelationType;
import me.roar.model.node.Lion;
import me.roar.model.node.Sheep;
import org.neo4j.graphdb.Direction;

import java.util.Map;

/**
 * Relationship representing a Sheep following a Lion
 */
public class Follows implements BaseRelationship<Sheep, Lion> {
  private static final BasicRelationType<Sheep, Lion> FOLLOWS =
      new BasicRelationType<>(Sheep.class, Lion.class, "follows");
  private static final Follows newInstance = new Follows();
  private final Map<String, ?> properties;
  private final Direction direction;

  public Follows(Map<String, ?> properties, Direction direction) {
    this.properties = properties;
    this.direction = direction;
  }

  private Follows() {
    this.properties = null;
    this.direction = Direction.OUTGOING;
  }

  public static Follows newInstance() {
    return newInstance;
  }

  @Override
  public RelationType<Sheep, Lion> getRelationType() {
    return FOLLOWS;
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
