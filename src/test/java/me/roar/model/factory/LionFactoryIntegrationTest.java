package me.roar.model.factory;

import ligo.exceptions.IllegalLabelExtractionAttemptException;
import me.roar.fixture.TestConstants;
import me.roar.model.Lion;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests LionFactory
 */
public class LionFactoryIntegrationTest {

  private static final String LION_NAME = "Ian";
  private static final LionFactory lionFactory = new LionFactory(TestConstants.DB_CONFIG.getDb());

  @Before
  public void setup() {
    lionFactory.deleteByName(LION_NAME);
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
    final Lion foundLion = lionFactory.findByName(LION_NAME);
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
  }
}
