package me.roar.model.repository;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import ligo.repository.EntityRepo;
import me.roar.model.relationship.Bleats;
import me.roar.model.node.Sheep;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;

import java.util.Date;

public class SheepRepo extends EntityRepo {
  public SheepRepo(GraphDatabaseService db) {
    super(db);
  }

  public Sheep create(Sheep sheep) {
    sheep.setCreatedAt(new Date());
    return createUnique(sheep);
  }

  public void bleats(Sheep sheepA, String bleatText, Sheep sheepB) {
    if (sheepA == null || sheepB == null || Strings.isNullOrEmpty(bleatText) ||
        sheepA.getId() == null || sheepB.getId() == null) {
      throw new IllegalArgumentException(
          "sheepA and sheepB should be non null and have valid IDs. bleatText cannot be blank.");
    }

    addRelatives(sheepA, new Bleats(ImmutableMap.of("text", bleatText), Direction.OUTGOING), sheepB);
  }
}
