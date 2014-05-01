package ligo.utils;

import ligo.meta.Transient;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.Map;

import static org.junit.Assert.*;

public class EntityUtilsTest {

  @Test
  public void testIsTransient() {

    final Method[] methods = TestClass.class.getMethods();
    for (Method method : methods) {
      System.out.println("Testing " + method.getName());
      if (method.getName().equals("getTransientItem")) {
        assertTrue(EntityUtils.isTransient(method));
      }
      else {
        assertFalse(EntityUtils.isTransient(method));
      }
    }
  }

  @Test
  public void testExtractPersistableProperties() {
    TestClass testInstance = new TestClass();
    final Map<String, Object> properties =
        EntityUtils.extractPersistableProperties(testInstance);
    assertNotNull(properties);
    assertEquals(1, properties.size());
    assertTrue(properties.containsKey("NonTransientItem"));
    assertTrue((boolean) properties.get("NonTransientItem"));
  }


  class TestClass {
    @Transient public boolean getTransientItem() {
      return false;
    }
    public boolean getNonTransientItem() {
      return true;
    }
  }
}
