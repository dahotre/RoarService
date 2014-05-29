package me.roar.model.repository;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import ligo.repository.EntityRepo;
import me.roar.model.node.Lion;
import me.roar.model.node.Sheep;
import me.roar.model.relationship.Bleats;
import me.roar.model.relationship.Follows;
import org.neo4j.graphdb.Direction;

import java.util.Date;

public class SheepRepo extends EntityRepo {
  public SheepRepo() {
    super();
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

  public void follows(Sheep sheep, Lion lion) {
    addRelatives(sheep,
        new Follows(ImmutableMap.of("cAt", new Date().getTime()), Direction.OUTGOING),
        lion);
  }
}
