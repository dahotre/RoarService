package me.roar.utils;

import org.neo4j.graphdb.RelationshipType;

/**
 * All relations
 */
public enum RelationType implements RelationshipType {
  ROARS,
  ROARED_AT,
  FOLLOWS,
  BLEATS;
}
