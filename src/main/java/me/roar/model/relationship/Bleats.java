package me.roar.model.relationship;

import com.google.gson.Gson;
import ligo.meta.BaseRelationship;
import ligo.meta.BasicRelationType;
import ligo.meta.RelationType;
import me.roar.model.node.Sheep;
import org.neo4j.graphdb.Direction;

import java.util.Map;

/**
 * Represents the relationship between sheeps. Only 1 bleat per sheep-sheep is persisted.
 */
public class Bleats implements BaseRelationship<Sheep, Sheep> {
  private static final BasicRelationType<Sheep, Sheep> BLEATS =
      new BasicRelationType<>(Sheep.class, Sheep.class, "bleats");

  private final Map<String, ?> properties;
  private final Direction direction;

  public Bleats(Map<String, ?> properties, Direction direction) {
    this.properties = properties;
    this.direction = direction;
  }

  public Bleats(Map<String, ?> properties) {
    this.properties = null;
    this.direction = Direction.OUTGOING;
  }

  @Override
  public RelationType<Sheep, Sheep> getRelationType() {
    return BLEATS;
  }

  @Override
  public Map<String, ?> getProperties() {
    return properties;
  }

  @Override
  public Direction getDirection() {
    return direction;
  }

  @Override
  public String toString() {
    return new Gson().toJson(this);
  }
}
