package ligo.utils;

import ligo.exceptions.IllegalLabelExtractionAttemptException;
import ligo.meta.Entity;
import ligo.meta.IndexType;
import ligo.meta.Indexed;
import ligo.meta.Property;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;

public class EntityUtilsTest {

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
  public void testPopulate() {
    Map<String, Object> properites = new HashMap<>(3);
    properites.put("transientitem", true);
    properites.put("nontransientitem", false);
    properites.put("firstname", "Bobby");
    TestClass instance = new TestClass();
    Beanify.populate(instance, properites);
    assertEquals(true, instance.isTransientItem());
    assertEquals(false, instance.isNonTransientItem());
    assertEquals("Bobby", instance.getFirstName());
  }

  @Test
  public void testExtractIndexable() {
    final Set<Field> fields = EntityUtils.extractIndexable(TestClass.class);
    assertNotNull(fields);
    assertEquals("only 1 field is expected to be indexed", 1, fields.size());
    assertTrue(fields.iterator().next().isAnnotationPresent(Indexed.class));
  }

  @Entity(label = "someclass")
  class TestClass {

    @Property
    @Indexed(type = IndexType.FULL_TEXT, name = "testclass_firstname_ft")
    private String firstName = "Boss";

    private boolean transientItem = false;

    @Property
    private boolean nonTransientItem = true;

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
