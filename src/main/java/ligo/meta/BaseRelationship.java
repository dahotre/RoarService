package ligo.meta;

import org.neo4j.graphdb.Direction;

import java.util.Map;

/**
 * Type marker for a Relationship
 */
public interface BaseRelationship<S, E> {
  public RelationType<S, E> getRelationType();

  public Map<String, ? extends Object> getProperties();

  public Direction getDirection();
}
