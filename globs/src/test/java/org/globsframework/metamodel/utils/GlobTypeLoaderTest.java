package org.globsframework.metamodel.utils;

import junit.framework.TestCase;
import org.globsframework.metamodel.*;
import org.globsframework.metamodel.annotations.DefaultBoolean;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.annotations.Required;
import org.globsframework.metamodel.annotations.Target;
import org.globsframework.metamodel.fields.*;
import org.globsframework.metamodel.index.UniqueIndex;
import org.globsframework.model.Glob;
import org.globsframework.model.impl.ReadOnlyGlob;
import org.globsframework.model.utils.GlobBuilder;
import org.globsframework.model.utils.GlobConstantContainer;
import org.globsframework.utils.ArrayTestUtils;
import org.globsframework.utils.Dates;
import org.globsframework.utils.TestUtils;
import org.globsframework.utils.exceptions.InvalidParameter;
import org.globsframework.utils.exceptions.ItemAlreadyExists;
import org.globsframework.utils.exceptions.MissingInfo;
import org.globsframework.utils.exceptions.UnexpectedApplicationState;

import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.util.Date;
import java.util.List;

public class GlobTypeLoaderTest extends TestCase {

  public static class AnObject {

    public static GlobType TYPE;

    @Key
    public static IntegerField ID;

    public static StringField STRING;
    public static DoubleField DOUBLE;
    public static BooleanField BOOLEAN;
    public static DateField DATE;
    public static LongField LONG;
    public static BlobField BLOB;

    public static UniqueIndex ID_INDEX;

    static {
      GlobTypeLoader.init(AnObject.class)
        .defineUniqueIndex(ID_INDEX, ID);
    }

    private static Glob glob =
      GlobBuilder.init(TYPE)
        .set(ID, 1)
        .set(STRING, "string1")
        .set(DOUBLE, 1.1)
        .set(BOOLEAN, false)
        .set(LONG, 15L)
        .set(DATE, Dates.parse("2006/12/25"))
        .set(BLOB, TestUtils.SAMPLE_BYTE_ARRAY)
        .get();
  }

  public void testDefaultCase() throws Exception {
    assertEquals("anObject", AnObject.TYPE.getName());
    assertEquals(1, AnObject.glob.get(AnObject.ID).intValue());
    assertEquals("string1", AnObject.glob.get(AnObject.STRING));
    assertEquals(1.1, AnObject.glob.get(AnObject.DOUBLE));
    assertEquals(Boolean.FALSE, AnObject.glob.get(AnObject.BOOLEAN));
    assertEquals(Dates.parse("2006/12/25"), AnObject.glob.get(AnObject.DATE));
    assertEquals(new Long(15), AnObject.glob.get(AnObject.LONG));
    assertEquals(TestUtils.SAMPLE_BYTE_ARRAY, AnObject.glob.get(AnObject.BLOB));

    assertEquals(0, AnObject.ID.getIndex());
    assertEquals(3, AnObject.BOOLEAN.getIndex());
    assertEquals(6, AnObject.BLOB.getIndex());

    assertTrue(AnObject.ID.isKeyField());
    assertFalse(AnObject.STRING.isKeyField());
    assertFalse(AnObject.BOOLEAN.isKeyField());

    Link[] expecteLinks = {AnObjectWithALinkField.LINK, AnObjectWithASingleIntegerFieldUsedAsALink.LINK};
    TestUtils.assertSetEquals(AnObject.TYPE.getInboundLinks(), expecteLinks);
  }

  private static class AnObjectWithNoTypeDef {
    public static IntegerField ID;
  }

  public void testObjectWithNoTypeDef() throws Exception {
    try {
      GlobTypeLoader.init(AnObjectWithNoTypeDef.class);
      fail();
    }
    catch (MissingInfo e) {
      assertEquals("Class " + AnObjectWithNoTypeDef.class.getName() +
                   " must have a TYPE field of class " + GlobType.class.getName(), e.getMessage());
    }
  }

  private static class AnObjectWithSeveralTypeDefs {
    public static GlobType TYPE1;
    public static GlobType TYPE2;
    public static IntegerField ID;
  }

  public void testObjectWithSeveralTypeDefs() throws Exception {
    try {
      GlobTypeLoader.init(AnObjectWithSeveralTypeDefs.class);
      fail();
    }
    catch (ItemAlreadyExists e) {
      assertEquals("Class " + AnObjectWithSeveralTypeDefs.class.getName() +
                   " must have only one TYPE field of class " + GlobType.class.getName(), e.getMessage());
    }
  }

  public static class AnObjectForDoubleInit {
    public static GlobType TYPE;
    @Key
    public static IntegerField ID;
  }

  public void testCannotInitializeTheSameClassTwice() throws Exception {
    GlobTypeLoader.init(AnObjectForDoubleInit.class);
    try {
      GlobTypeLoader.init(AnObjectForDoubleInit.class);
      fail();
    }
    catch (UnexpectedApplicationState e) {
    }
  }

  private static class AnObjectWithNoKey {
    public static GlobType TYPE;
    public static IntegerField ID;
  }

  public void testObjectWithNoKey() throws Exception {
    try {
      GlobTypeLoader.init(AnObjectWithNoKey.class);
      fail();
    }
    catch (InvalidParameter e) {
      assertEquals("GlobType anObjectWithNoKey has no key field", e.getMessage());
    }
  }

  private static class AnObjectWithACompositeKey {
    public static GlobType TYPE;
    @Key
    public static IntegerField ID1;
    @Key
    public static IntegerField ID2;

    static {
      GlobTypeLoader.init(AnObjectWithACompositeKey.class);
    }
  }

  public void testAnObjectWithACompositeKey() throws Exception {
    TestUtils.assertSetEquals(AnObjectWithACompositeKey.TYPE.getKeyFields(),
                              AnObjectWithACompositeKey.ID1, AnObjectWithACompositeKey.ID2);
  }

  private static class AnObjectWithALinkField {
    public static GlobType TYPE;
    @Key
    public static IntegerField ID;
    @Target(AnObject.class)
    public static LinkField LINK;

    static {
      GlobTypeLoader.init(AnObjectWithALinkField.class);
    }
  }

  public void testLinkField() throws Exception {
    GlobType type = AnObjectWithALinkField.TYPE;

    assertEquals(AnObjectWithALinkField.LINK, type.getField("link"));
    assertEquals(AnObjectWithALinkField.LINK, type.getOutboundLink("link"));

    Link link = AnObjectWithALinkField.LINK;
    assertNotNull(link);
    assertEquals(type, link.getSourceType());
    assertEquals(AnObject.TYPE, link.getTargetType());

    assertEquals(AnObject.TYPE, AnObjectWithALinkField.LINK.getTargetType());
    assertEquals(type, AnObjectWithALinkField.LINK.getSourceType());
  }

  private static class AnObjectWithALinkFieldWithoutTheTargetAnnotation {
    public static GlobType TYPE;
    @Key
    public static IntegerField ID;
    public static LinkField LINK;
  }

  public void testAnObjectWithALinkFieldWithoutTheTargetAnnotation() throws Exception {
    try {
      GlobTypeLoader.init(AnObjectWithALinkFieldWithoutTheTargetAnnotation.class);
      fail();
    }
    catch (MissingInfo e) {
      assertEquals("Annotation " + Target.class.getName() +
                   " must be specified for LinkField 'link' for type: " +
                   AnObjectWithALinkFieldWithoutTheTargetAnnotation.TYPE.getName(),
                   e.getMessage());
    }
  }

  private static class AnObjectWithALinkFieldTargettingAMultiKeyObject {
    public static GlobType TYPE;
    @Key
    public static IntegerField ID;
    @Target(AnObjectWithACompositeKey.class)
    public static LinkField LINK;
  }

  public void testAnObjectWithALinkFieldTargettingAMultiKeyObject() throws Exception {
    try {
      GlobTypeLoader.init(AnObjectWithALinkFieldTargettingAMultiKeyObject.class);
      fail();
    }
    catch (InvalidParameter e) {
      assertEquals("LinkField 'link' in type '" +
                   AnObjectWithALinkFieldTargettingAMultiKeyObject.TYPE.getName() +
                   "' cannot reference target type 'anObjectWithACompositeKey' " +
                   "because it uses a composite key",
                   e.getMessage());
    }
  }

  private static class AnObjectWithALinkFieldTargettingANonGlobsObject {
    public static GlobType TYPE;
    @Key
    public static IntegerField ID;
    @Target(String.class)
    public static LinkField LINK;
  }

  public void testAnObjectWithALinkFieldTargettingANonGlobsObject() throws Exception {
    try {
      GlobTypeLoader.init(AnObjectWithALinkFieldTargettingANonGlobsObject.class);
      fail();
    }
    catch (InvalidParameter e) {
      assertEquals("LinkField 'link' in type '" +
                   AnObjectWithALinkFieldTargettingANonGlobsObject.TYPE.getName() +
                   "' cannot reference target class '" + String.class.getName() +
                   "' because it does not define a Glob type",
                   e.getMessage());
    }
  }

  private static class AnObjectWithAStringId {
    public static GlobType TYPE;
    @Key
    public static StringField ID;

    static {
      GlobTypeLoader.init(AnObjectWithAStringId.class);
    }
  }

  private static class AnObjectWithALinkFieldTargettingAnObjectWithAStringId {
    public static GlobType TYPE;
    @Key
    public static IntegerField ID;
    @Target(AnObjectWithAStringId.class)
    public static LinkField LINK;
  }

  public void testAnObjectWithALinkFieldTargettingAnObjectWithAStringId() throws Exception {
    try {
      GlobTypeLoader.init(AnObjectWithALinkFieldTargettingAnObjectWithAStringId.class);
      fail();
    }
    catch (InvalidParameter e) {
      assertEquals("LinkField 'link' in type '" +
                   AnObjectWithALinkFieldTargettingAnObjectWithAStringId.TYPE.getName() +
                   "' cannot reference target type '" + AnObjectWithAStringId.TYPE.getName() +
                   "' because it does not use an integer key",
                   e.getMessage());
    }
  }

  private static class AnObjectWithASingleIntegerFieldUsedAsALink {
    public static GlobType TYPE;
    @Key
    public static IntegerField ID;
    public static IntegerField LINK_ID;
    public static Link LINK;

    static {
      GlobTypeLoader loader = GlobTypeLoader.init(AnObjectWithASingleIntegerFieldUsedAsALink.class);
      loader.defineLink(LINK).add(LINK_ID, AnObject.ID);
    }
  }

  public void testLinkFieldManagedWithAnIntegerField() throws Exception {
    Link link = AnObjectWithASingleIntegerFieldUsedAsALink.TYPE.getOutboundLink("link");
    assertNotNull(link);
    assertEquals(AnObjectWithASingleIntegerFieldUsedAsALink.TYPE, link.getSourceType());
    assertEquals(AnObject.TYPE, link.getTargetType());
  }

  public void testKeyFields() throws Exception {
    List<Field> fields = AnObject.TYPE.getKeyFields();
    ArrayTestUtils.assertContentEquals(fields, AnObject.ID);
  }

  @Retention(RUNTIME)
  public @interface MyAnnotation {
    String value();
  }

  public static class AnObjectWithCustomAnnotations {

    @MyAnnotation("class annotations")
    public static GlobType TYPE;
    @Key
    @MyAnnotation("field annotations")
    public static IntegerField ID;

    static {
      GlobTypeLoader.init(AnObjectWithCustomAnnotations.class);
    }
  }

  public void testAnnotationsAreAccessible() throws Exception {
    MyAnnotation fieldAnnotation = AnObjectWithCustomAnnotations.ID.getAnnotation(MyAnnotation.class);
    assertEquals("field annotations", fieldAnnotation.value());
    MyAnnotation classAnnotation = AnObjectWithCustomAnnotations.TYPE.getAnnotation(MyAnnotation.class);
    assertEquals("class annotations", classAnnotation.value());
  }

  public void testRetrievingAnnotatedFields() throws Exception {
    ArrayTestUtils.assertEquals(new Field[]{AnObjectWithCustomAnnotations.ID},
                                AnObjectWithCustomAnnotations.TYPE.getFieldsWithAnnotation(
                                  MyAnnotation.class));
  }

  public static class AnObjectWithCustomLinkAnnotations {
    public static GlobType TYPE;

    @Key
    public static IntegerField ID;

    @MyAnnotation("link annotation")
    public static Link LINK;

    static {
      GlobTypeLoader.init(AnObjectWithCustomLinkAnnotations.class);
    }
  }

  public void testLinkAnnotationsAreAccessible() throws Exception {
    MyAnnotation annotation = AnObjectWithCustomLinkAnnotations.LINK.getAnnotation(MyAnnotation.class);
    assertEquals("link annotation", annotation.value());
  }

  public void testAConstantsType() throws Exception {
    assertEquals(1,
                 DummyObjectWithConstants.CONSTANT_1.getGlob().get(DummyObjectWithConstants.ID).intValue());

    TestUtils.assertSetEquals(DummyObjectWithConstants.TYPE.getConstants(),
                              DummyObjectWithConstants.CONSTANT_1.getGlob(),
                              DummyObjectWithConstants.CONSTANT_2.getGlob());
  }

  public static class ANonEnumConstantsType implements GlobConstantContainer {

    public static GlobType TYPE;

    @Key
    public static IntegerField ID;

    public ReadOnlyGlob getGlob() {
      return null;
    }
  }

  public void testANonEnumConstantsType() throws Exception {
    try {
      GlobTypeLoader.init(ANonEnumConstantsType.class);
      fail();
    }
    catch (Exception e) {
      assertEquals("Class ANonEnumConstantsType must be an enum in order to declare constants",
                   e.getMessage());
    }
  }

  public static enum AnEnumTypeWithoutContainerInterface {
    CONSTANT;

    public static GlobType TYPE;

    @Key
    public static IntegerField ID;
  }

  public void testAnEnumTypeWithoutContainerInterface() throws Exception {
    try {
      GlobTypeLoader.init(AnEnumTypeWithoutContainerInterface.class);
      fail();
    }
    catch (Exception e) {
      assertEquals("Class AnEnumTypeWithoutContainerInterface must implement GlobConstantContainer " +
                   "in order to declare constants", e.getMessage());
    }
  }

  public static class AnObjectWithRequiredFields {

    public static GlobType TYPE;

    @Key
    public static IntegerField ID;

    @Required
    public static StringField STRING;

    public static DoubleField DOUBLE;

    static {
      GlobTypeLoader.init(AnObjectWithRequiredFields.class);
    }
  }

  public void testAnObjectWithRequiredFields() throws Exception {
    assertTrue(AnObjectWithRequiredFields.ID.isRequired());
    assertTrue(AnObjectWithRequiredFields.STRING.isRequired());
    assertFalse(AnObjectWithRequiredFields.DOUBLE.isRequired());
  }

  public void testAnObjectWithDefaultValues() throws Exception {
    assertEquals(7, DummyObjectWithDefaultValues.INTEGER.getDefaultValue());
    assertEquals(3.14159265, DummyObjectWithDefaultValues.DOUBLE.getDefaultValue());
    assertEquals(5l, DummyObjectWithDefaultValues.LONG.getDefaultValue());
    assertEquals(true, DummyObjectWithDefaultValues.BOOLEAN.getDefaultValue());
    assertEquals("Hello", DummyObjectWithDefaultValues.STRING.getDefaultValue());
    TestUtils.assertDateEquals(new Date(), (Date)DummyObjectWithDefaultValues.DATE.getDefaultValue(), 360000);
    TestUtils.assertDateEquals(new Date(), (Date)DummyObjectWithDefaultValues.TIMESTAMP.getDefaultValue(), 360000);
  }

  private static class AnObjectWithADefaultValueTypeError {
    public static GlobType TYPE;
    @Key
    public static IntegerField ID;
    @DefaultBoolean(true)
    public static IntegerField COUNT;
  }

  public void testAnObjectWithADefaultValueTypeError() throws Exception {
    try {
      GlobTypeLoader.init(AnObjectWithADefaultValueTypeError.class);
      fail();
    }
    catch (InvalidParameter e) {
      assertEquals("Field anObjectWithADefaultValueTypeError.count should declare a default value " +
                   "with annotation @DefaultInteger instead of @DefaultBoolean",
                   e.getMessage());
    }
  }

  public static class AnObjectWithRequiredLinks {

    public static GlobType TYPE;

    @Key
    public static IntegerField ID;

    public static IntegerField LINK_ID;

    @Required
    public static Link LINK;

    static {
      GlobTypeLoader.init(AnObjectWithRequiredLinks.class)
        .defineLink(LINK).add(LINK_ID, DummyObject.ID);
    }
  }

  public void testAnObjectWithRequiredLinks() throws Exception {
    assertTrue(AnObjectWithRequiredLinks.LINK.isRequired());
  }

  public static class AnObjectWithRequiredLinkField {

    public static GlobType TYPE;

    @Key
    public static IntegerField ID;

    @Target(DummyObject.class)
    @Required
    public static LinkField LINK;


    static {
      GlobTypeLoader.init(AnObjectWithRequiredLinkField.class);
    }
  }

  public void testAnObjectWithRequiredLinkField() throws Exception {
    assertTrue(AnObjectWithRequiredLinkField.LINK.isRequired());
  }
}
