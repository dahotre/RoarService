package ligo.meta;

import org.neo4j.graphdb.RelationshipType;

/**
 *
 */
public interface RelationType<S, E> extends RelationshipType {
  public Class<S> getStartNodeType();

  public Class<E> getEndNodeType();

  public Class<?> getOtherNodeType(Class<?> node);
}
