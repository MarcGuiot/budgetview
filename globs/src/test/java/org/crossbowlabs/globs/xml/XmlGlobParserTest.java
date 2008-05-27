package org.crossbowlabs.globs.xml;

import junit.framework.TestCase;
import org.crossbowlabs.globs.metamodel.*;
import org.crossbowlabs.globs.metamodel.annotations.Key;
import org.crossbowlabs.globs.metamodel.annotations.Target;
import org.crossbowlabs.globs.metamodel.fields.IntegerField;
import org.crossbowlabs.globs.metamodel.fields.LinkField;
import org.crossbowlabs.globs.metamodel.utils.DefaultGlobModel;
import org.crossbowlabs.globs.metamodel.utils.GlobTypeLoader;
import org.crossbowlabs.globs.model.*;
import static org.crossbowlabs.globs.model.KeyBuilder.newKey;
import org.crossbowlabs.globs.utils.exceptions.InvalidParameter;
import org.crossbowlabs.globs.utils.exceptions.ItemNotFound;
import org.crossbowlabs.globs.utils.exceptions.ItemAmbiguity;

public class XmlGlobParserTest extends TestCase {
  private GlobRepository repository;

  protected void setUp() throws Exception {
    repository = GlobRepositoryBuilder.createEmpty();
  }

  public void testStandardCase() throws Exception {
    parse("<dummyObject id='1' name='foo'/>");
    GlobList objects = repository.getAll(DummyObject.TYPE);
    assertEquals(1, objects.size());
    Glob object = objects.get(0);
    assertEquals(1, object.get(DummyObject.ID).intValue());
    assertEquals("foo", object.get(DummyObject.NAME));
  }

  public void testThrowsMeaningfulExceptionsForInvalidValues() throws Exception {
    try {
      parse("<dummyObject id='thisIsNotAnInt'/>");
      fail();
    }
    catch (InvalidParameter e) {
      assertEquals("'thisIsNotAnInt' is not a proper value for field 'id' in type 'dummyObject'",
                   e.getMessage());
    }
  }

  public void testGeneratesIntegerIdsIfNeeded() throws Exception {
    parse("<dummyObject name='foo'/>");
    GlobList objects = repository.getAll(DummyObject.TYPE);
    assertEquals(1, objects.size());
    Glob object = objects.get(0);
    assertEquals(0, object.get(DummyObject.ID).intValue());
    assertEquals("foo", object.get(DummyObject.NAME));
  }

  public void testLinkField() throws Exception {
    parse("<dummyObject id='1' name='foo'/>" +
          "<dummyObject id='2' link='1'/>");
    assertEquals(getObject(1), repository.findLinkTarget(getObject(2), DummyObject.LINK));
  }

  public void testLinkFieldWithTargetName() throws Exception {
    parse("<dummyObject id='1' name='foo'/>" +
          "<dummyObject id='2' linkName='foo'/>");
    Glob obj2 = getObject(2);
    assertEquals(1, obj2.get(DummyObject.LINK).intValue());
    assertEquals(getObject(1), repository.findLinkTarget(obj2, DummyObject.LINK));
  }

  public void testIdPartOfLinkFieldTakesPrecedenceOverNamePart() throws Exception {
    parse("<dummyObject id='1' name='foo'/>" +
          "<dummyObject id='2' name='bar'/>" +
          "<dummyObject id='3' link='2' linkName='foo'/>");
    assertEquals(getObject(2), repository.findLinkTarget(getObject(3), DummyObject.LINK));
  }

  public void testUsingALinkFieldAsAnId() throws Exception {
    parse("<dummyObject id='1' name='foo'/>" +
          "<dummyObjectWithLinkFieldId link='1'/>");
    Glob source = repository.get(newKey(DummyObjectWithLinkFieldId.TYPE, 1));
    assertEquals(getObject(1), repository.findLinkTarget(source, DummyObjectWithLinkFieldId.LINK));
  }

  public void testUsingANameLinkAsAnId() throws Exception {
    parse("<dummyObject id='1' name='foo'/>" +
          "<dummyObjectWithLinkFieldId linkName='foo'/>");
    Glob source = repository.get(newKey(DummyObjectWithLinkFieldId.TYPE, 1));
    assertEquals(1, source.get(DummyObjectWithLinkFieldId.LINK).intValue());
    assertEquals(getObject(1), repository.findLinkTarget(source, DummyObjectWithLinkFieldId.LINK));
  }

  public void testCompositeLink() throws Exception {
    parse("<dummyObjectWithCompositeKey id1='1' id2='2'/>" +
          "<dummyObjectWithLinks id='1' targetId1='1' targetId2='2'/>");
    Glob source = repository.get(newKey(DummyObjectWithLinks.TYPE, 1));
    Glob target = repository.findLinkTarget(source, DummyObjectWithLinks.COMPOSITE_LINK);
    assertEquals(1, target.get(DummyObjectWithCompositeKey.ID1).intValue());
    assertEquals(2, target.get(DummyObjectWithCompositeKey.ID2).intValue());
  }

  public void testNamePartOfCompositeLinkTakesPrecedenceOverIdPart() throws Exception {
    parse("<dummyObjectWithCompositeKey id1='1' id2='2' name='foo'/>" +
          "<dummyObjectWithCompositeKey id1='2' id2='3' name='bar'/>" +
          "<dummyObjectWithLinks id='1' compositeLink='bar' targetId1='1' targetId2='2'/>");
    Glob source = repository.get(newKey(DummyObjectWithLinks.TYPE, 1));
    Glob target = repository.findLinkTarget(source, DummyObjectWithLinks.COMPOSITE_LINK);
    assertEquals(1, target.get(DummyObjectWithCompositeKey.ID1).intValue());
    assertEquals(2, target.get(DummyObjectWithCompositeKey.ID2).intValue());
  }

  public void testContainmentWithSingleLink() throws Exception {
    parse("<dummyObject id='1'>" +
          "  <dummyObjectWithLinks id='1'/>" +
          "</dummyObject>");

    Glob links = repository.get(newKey(DummyObjectWithLinks.TYPE, 1));
    assertEquals(1, links.get(DummyObjectWithLinks.PARENT_ID).intValue());
  }

  public void testContainmentWithCompositeLink() throws Exception {
    parse("<dummyObjectWithCompositeKey id1='1' id2='2'>" +
          "  <dummyObjectWithLinks id='1'/>" +
          "</dummyObjectWithCompositeKey>");

    Glob links = repository.get(newKey(DummyObjectWithLinks.TYPE, 1));
    assertEquals(1, links.get(DummyObjectWithLinks.TARGET_ID_1).intValue());
    assertEquals(2, links.get(DummyObjectWithLinks.TARGET_ID_2).intValue());
  }

  public static class AnObjectLinkingToATypeWithNoNamingField {
    public static GlobType TYPE;

    @Key
    public static IntegerField ID;

    @Target(DummyObject2.class)
    public static LinkField OBJ2;

    static {
      GlobTypeLoader.init(AnObjectLinkingToATypeWithNoNamingField.class);
    }
  }

  public void testUsingALinkFieldWithAnObjectThatHasNoNamingField() throws Exception {
    parse(new DefaultGlobModel(DummyObject2.TYPE, AnObjectLinkingToATypeWithNoNamingField.TYPE),
          "<dummyObject2 id='11'>" +
          "  <anObjectLinkingToATypeWithNoNamingField id='1'/>" +
          "</dummyObject2>");

    Glob source = repository.get(newKey(AnObjectLinkingToATypeWithNoNamingField.TYPE, 1));
    assertEquals(11, source.get(AnObjectLinkingToATypeWithNoNamingField.OBJ2).intValue());
  }

  public void testContainmentWithNoRelationshipError() throws Exception {
    try {
      parse("<dummyObject id='1'>" +
            "  <dummyObject2 id='1'/>" +
            "</dummyObject>");
      fail();
    }
    catch (ItemNotFound e) {
      assertEquals("There are no links from dummyObject2 to dummyObject" +
                   " - XML containment cannot be used", e.getMessage());
    }
  }

  public static class AnObject {
    public static GlobType TYPE;

    @Key
    public static IntegerField ID;

    static {
      GlobTypeLoader.init(AnObject.class);
    }
  }

  public static class AnObjectWithTwoLinks {
    public static GlobType TYPE;
    @Key
    public static IntegerField ID;
    @Target(AnObject.class)
    public static LinkField LINK1;
    @Target(AnObject.class)
    public static LinkField LINK2;

    static {
      GlobTypeLoader.init(AnObjectWithTwoLinks.class);
    }
  }

  public void testContainmentWithTooManyRelationshipsError() throws Exception {
    try {
      parse(new DefaultGlobModel(AnObject.TYPE, AnObjectWithTwoLinks.TYPE),
            "<anObject id='1'>" +
            "  <anObjectWithTwoLinks id='1'/>" +
            "</anObject>");
      fail();
    }
    catch (ItemAmbiguity e) {
      assertEquals("More than one Link from anObjectWithTwoLinks to anObject" +
                   " - XML containment cannot be used", e.getMessage());
    }
  }

  private void parse(String xmlStream) {
    parse(DummyModel.get(), xmlStream);
  }

  private void parse(GlobModel globModel, String xmlStream) {
    GlobTestUtils.parse(globModel, repository, xmlStream);
  }

  private Glob getObject(int id) {
    return repository.get(newKey(DummyObject.TYPE, id));
  }
}
