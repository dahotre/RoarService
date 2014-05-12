package ligo.utils;

import ligo.meta.RelationType;

/**
 * Implements all the RelationType methods in a common sense way
 */
public class BasicRelationType<S, E> implements RelationType {
  private Class<S> startNodeType;
  private Class<E> endNodeType;
  private String name;

  public BasicRelationType(Class<S> startNodeType, Class<E> endNodeType, String name) {
    this.startNodeType = startNodeType;
    this.endNodeType = endNodeType;
    this.name = name;
  }

  @Override
  public Class<S> getStartNodeType() {
    return startNodeType;
  }

  @Override
  public Class<E> getEndNodeType() {
    return endNodeType;
  }

  @Override
  public Class<?> getOtherNodeType(Class node) {
    return (node == startNodeType) ? endNodeType : startNodeType;
  }

  @Override
  public String name() {
    return this.name;
  }
}
