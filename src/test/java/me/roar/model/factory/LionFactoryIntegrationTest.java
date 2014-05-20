package me.roar.model.factory;

import com.google.common.collect.Sets;
import ligo.exceptions.IllegalLabelExtractionAttemptException;
import me.roar.fixture.TestConstants;
import me.roar.model.Lion;
import me.roar.model.Roar;
import me.roar.utils.MyRelationTypes;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;

import java.util.Date;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * Tests LionFactory
 */
public class LionFactoryIntegrationTest {

  private static final String LION_NAME = "Ian";
  private static final String ROAR_TEXT_1 = "I architects";
  private static final String ROAR_TEXT_2 = "I chief";
  private static final LionFactory lionFactory = new LionFactory(TestConstants.DB_CONFIG.getDb());
  private static final RoarFactory roarFactory = new RoarFactory(TestConstants.DB_CONFIG.getDb());

  @Before
  public void setup() {
    lionFactory.deleteByName(LION_NAME);
    roarFactory.delete(Roar.class, RoarFactory.TEXT, ROAR_TEXT_1);
    roarFactory.delete(Roar.class, RoarFactory.TEXT, ROAR_TEXT_2);
  }

  @Test
  public void testCreateUniqueLion() throws InterruptedException, IllegalLabelExtractionAttemptException {
    final long startTime = new Date().getTime();
    Lion lion = lionFactory.create(new Lion().withName(LION_NAME).withAge(10));
    assertNotNull(lion);
    assertEquals(LION_NAME, lion.getName());
    assertTrue(lion.getCreatedAt().compareTo(new Date()) <= 0);

    final long currentTime = new Date().getTime();
    Thread.sleep(1000l);
    final Set<Lion> lions = lionFactory.findByName(LION_NAME);
    assertNotNull(lions);
    final Lion foundLion = lions.iterator().next();
    assertNotNull(foundLion);
    assertEquals(LION_NAME, foundLion.getName());

    assertTrue(foundLion.getcAt() <= currentTime);
    assertTrue(foundLion.getuAt() <= currentTime);
    assertTrue(foundLion.getcAt() >= startTime);
    assertTrue(foundLion.getuAt() >= startTime);

    final Long id = foundLion.getId();
    assertNotNull(id);

    final Lion lionById = lionFactory.find(id);
    assertNotNull(lionById);
    assertEquals(LION_NAME, lionById.getName());

    final Set<Lion> searchedByName = lionFactory.searchByName(LION_NAME);
    assertNotNull(searchedByName);
    final Lion lionBySearch = searchedByName.iterator().next();
    assertNotNull(lionBySearch);
    assertEquals(id, lionBySearch.getId());
    assertEquals(LION_NAME, lionBySearch.getName());

    lionFactory.delete(id);
    final Lion lion2 = lionFactory.find(id);
    assertNull(lion2);
  }

  @Test
  public void testGetRelatives() {
    Lion lion = lionFactory.create(new Lion().withName(LION_NAME).withAge(10));
    assertNotNull(lion);
    assertEquals(LION_NAME, lion.getName());
    Roar roar = roarFactory.create(new Roar().withText(ROAR_TEXT_1));
    assertNotNull(roar);
    assertEquals(ROAR_TEXT_1, roar.getText());
    GraphDatabaseService db = TestConstants.DB_CONFIG.getDb();
    try (Transaction tx = db.beginTx()) {
      final Node roarNode = db.getNodeById(roar.getId());
      final Node lionNode = db.getNodeById(lion.getId());
      lionNode.createRelationshipTo(roarNode, MyRelationTypes.ROARS);
      tx.success();
    }

    final Set<Roar> relatives = lionFactory.getRelatives(lion, MyRelationTypes.ROARS,
        Direction.OUTGOING);
    assertNotNull(relatives);
    assertTrue(relatives.iterator().hasNext());
    final Roar foundRoar = relatives.iterator().next();
    assertNotNull(foundRoar);
    assertEquals(roar.getId(), foundRoar.getId());
    assertEquals(ROAR_TEXT_1, foundRoar.getText());
  }

  @Test
  public void testAddRelative() {
    Lion lion = lionFactory.create(new Lion().withName(LION_NAME).withAge(10));
    Roar roar = new Roar().withText(ROAR_TEXT_1);
    lionFactory.addRoar(lion, roar);
    final Set<Roar> roars = lionFactory.getRoars(lion);
    assertNotNull(roars);
    assertEquals("expecting 1 roar", 1, roars.size());
    Roar roarFromRelation = roars.iterator().next();
    assertEquals(ROAR_TEXT_1, roarFromRelation.getText());
    Roar roarFromSearch = roarFactory.findByText(ROAR_TEXT_1).iterator().next();
    assertNotNull(roarFromSearch);
    assertEquals(roarFromRelation.getId(), roarFromSearch.getId());
  }

  @Test
  public void testAddMultipleRelatives() {
    final Lion lion = lionFactory.create(new Lion().withName(LION_NAME).withAge(10));
    final Roar roar1 = roarFactory.create(new Roar().withText(ROAR_TEXT_1));

    lionFactory.addRoar(lion, roar1, new Roar().withText(ROAR_TEXT_2));

    Set<Roar> roars = lionFactory.getRoars(lion);
    assertNotNull(roars);
    assertEquals("Expecting 2 roars", 2, roars.size());
    for (Roar roar : roars) {
      assertTrue(Sets.newHashSet(ROAR_TEXT_1, ROAR_TEXT_2).contains(roar.getText()));
    }
  }
}
