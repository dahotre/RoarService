package ligo.utils;

import me.roar.model.Lion;
import org.junit.Test;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * Tests beanify's static methods
 */
public class BeanifyTest {

  private static final String LION_NAME = "lion1";
  private static final Date DT = new Date();

  @Test
  public void testPopulate() {
    Map<String, Object> lionProperties = new HashMap<>();
    lionProperties.put("name", LION_NAME);
    lionProperties.put("cat", DT.getTime());
    lionProperties.put("uat", DT.getTime());

    Lion lion = new Lion();
    Beanify.populate(lion, lionProperties);

    assertEquals(LION_NAME, lion.getName());
    assertEquals(DT, lion.getCreatedAt());
    assertEquals(DT, lion.getUpdatedAt());
    assertEquals(DT.getTime(), lion.getcAt());
    assertEquals(DT.getTime(), lion.getuAt());
  }
}
