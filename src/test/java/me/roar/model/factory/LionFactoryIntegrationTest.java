package me.roar.model.factory;

import me.roar.fixture.BaseIntegrationTest;
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
public class LionFactoryIntegrationTest extends BaseIntegrationTest {

  private static final String LION_NAME = "Ian";
  private LionFactory lionFactory = new LionFactory(dbConfig.getDb());

  @Before
  public void setup() {
    lionFactory.deleteByName(LION_NAME);
  }

  @Test
  public void testCreateUniqueLion() throws InterruptedException {
    Lion lion = lionFactory.createUniqueLion(LION_NAME);
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
  }
}
