package ligo.utils;

import me.roar.model.Lion;
import org.junit.Ignore;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * TODO : Needs fixing
 */
public class BeanifyTest {

  private static final String LION_NAME = "lion1";
  private static final Date DT = new Date();

  @Test
  @Ignore
  public void testNode() throws InvocationTargetException, IllegalAccessException {
    Beanify<Lion> beanify = new Beanify<>();
    Map<String, Object> lionProperties = new HashMap<>();
    lionProperties.put("name", LION_NAME);
    lionProperties.put("createdAt", DT);
    lionProperties.put("updatedAt", DT);

    Lion lion = beanify.get(lionProperties, Lion.class);
    assertNotNull(lion);
    assertEquals(LION_NAME, lion.getName());
    assertEquals(DT, lion.getCreatedAt());
    assertEquals(DT, lion.getUpdatedAt());
    assertEquals(DT.getTime(), lion.getcAt());
    assertEquals(DT.getTime(), lion.getuAt());
  }
}
