package ligo.utils;

import ligo.exceptions.IllegalLabelExtractionAttemptException;
import ligo.meta.Entity;
import ligo.meta.Transient;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class EntityUtilsTest {

  @Test
  public void testIsTransient() {

    final Method[] methods = TestClass.class.getMethods();
    for (Method method : methods) {
      System.out.println("Testing " + method.getName());
      if (method.getName().equals("isTransientItem")) {
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
    assertEquals(2, properties.size());
    assertTrue(properties.containsKey("nontransientitem"));
    assertTrue((boolean) properties.get("nontransientitem"));
  }

  @Test(expected = IllegalLabelExtractionAttemptException.class)
  public void testExtractNodeLabel() throws IllegalLabelExtractionAttemptException {
    final String nodeLabel = EntityUtils.extractNodeLabel(TestClass.class);
    assertEquals("someclass", nodeLabel);

    EntityUtils.extractNodeLabel(String.class);
  }

  @Test
  public void testGenerateId() {
    final long l1 = EntityUtils.generateId();
    final long l2 = EntityUtils.generateId();
    assertNotEquals(l1, l2);
  }

  @Test
  public void testPopulate() {
    Map<String, Object> properites = new HashMap<>(3);
    properites.put("transientitem", true);
    properites.put("nontransientitem", false);
    properites.put("firstname", "Bobby");
    TestClass instance = new TestClass();
    EntityUtils.populate(instance, properites);
    assertEquals(true, instance.isTransientItem());
    assertEquals(false, instance.isNonTransientItem());
    assertEquals("Bobby", instance.getFirstName());
  }

  @Entity(label = "someclass")
  class TestClass {

    private String firstName = "Boss";
    private boolean transientItem = false;
    private boolean nonTransientItem = true;

    @Transient
    public boolean isTransientItem() {
      return transientItem;
    }

    public void setTransientItem(boolean transientItem) {
      this.transientItem = transientItem;
    }

    public boolean isNonTransientItem() {
      return nonTransientItem;
    }

    public void setNonTransientItem(boolean nonTransientItem) {
      this.nonTransientItem = nonTransientItem;
    }

    public String getFirstName() {
      return firstName;
    }
    public void setFirstName(String firstName) {
      this.firstName = firstName;
    }
  }
}
