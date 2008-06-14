package org.globsframework.metamodel.utils;

import junit.framework.TestCase;
import org.globsframework.metamodel.Field;
import org.globsframework.metamodel.GlobModel;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.properties.Property;
import org.globsframework.utils.Functor;
import org.globsframework.utils.TestUtils;
import org.globsframework.utils.exceptions.UnexpectedApplicationState;

public class DefaultGlobTypeTest extends TestCase {
  private GlobType globType;
  private Field field;
  private GlobModel globModel;

  public void testGlobTypeProperty() throws Exception {
    initGlobType();

    Property<GlobType, Object> globTypeProperty = globModel.createGlobTypeProperty("globType info");
    assertEquals("globType info", globTypeProperty.getName());
    assertEquals(0, globTypeProperty.getId());

    globType.updateProperty(globTypeProperty, 3);
    assertEquals(3, globType.getProperty(globTypeProperty));

    globType.updateProperty(globTypeProperty, 4);
    assertEquals(4, globType.getProperty(globTypeProperty));

    Property<Field, Object> property = globModel.createFieldProperty("field info");
    assertEquals(0, property.getId());
    field.updateProperty(property, 2);
    assertEquals(2, field.getProperty(property));

    field.updateProperty(property, 4);
    assertEquals(4, field.getProperty(property));
    assertEquals("field info", property.getName());
  }

  public void testFields() {
    initGlobType();
    assertEquals("type", globType.getName());
    assertNotNull(globType.findField("field1"));
    assertNull(globType.findField("Field1"));
    assertNotNull(field);
    TestUtils.assertFails(new Functor() {
      public void run() throws Exception {
        globType.getFields();
      }
    }, UnexpectedApplicationState.class);
  }

  public void testFieldProperty() throws Exception {
    initGlobType();
    Property<Field, Object> property = globModel.createFieldProperty("field property");
    field.updateProperty(property, 3);
    assertEquals(3, field.getProperty(property));
  }

  private static class Type {
    public static GlobType TYPE;
    @Key
    public static IntegerField FIELD1;
  }

  private void initGlobType() {
    Type.TYPE = null;
    GlobTypeLoader loader = GlobTypeLoader.init(Type.class);
    globType = loader.getType();
    field = globType.getField("field1");
    globModel = new DefaultGlobModel(globType);
  }
}
