package org.globsframework.model.utils;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;
import org.globsframework.metamodel.GlobModel;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.Link;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.annotations.Target;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.fields.LinkField;
import org.globsframework.metamodel.fields.StringField;
import org.globsframework.metamodel.utils.GlobModelBuilder;
import org.globsframework.metamodel.utils.GlobTypeLoader;
import org.globsframework.model.ChangeSet;
import org.globsframework.xml.XmlChangeSetParser;
import org.globsframework.xml.XmlChangeSetVisitor;
import org.globsframework.xml.XmlTestUtils;

import java.io.StringReader;
import java.io.StringWriter;

public class ChangeSetSequencerTest extends TestCase {

  public static class ObjectWithCompositeKey {
    public static GlobType TYPE;

    @Key
    public static IntegerField ID1;

    @Key
    public static IntegerField ID2;

    public static StringField NAME;

    static {
      GlobTypeLoader.init(ObjectWithCompositeKey.class);
    }
  }

  public static class LinkedToObjectWithCompositeKey {
    public static GlobType TYPE;

    @Key
    public static IntegerField ID;

    @Target(ObjectWithCompositeKey.class)
    public static IntegerField LINK1;

    @Target(ObjectWithCompositeKey.class)
    public static IntegerField LINK2;

    public static Link LINK;

    static {
      GlobTypeLoader.init(LinkedToObjectWithCompositeKey.class)
        .defineLink(LinkedToObjectWithCompositeKey.LINK)
        .add(LinkedToObjectWithCompositeKey.LINK1, ObjectWithCompositeKey.ID1)
        .add(LinkedToObjectWithCompositeKey.LINK2, ObjectWithCompositeKey.ID2);
    }
  }

  public void testSingleType() throws Exception {
    checkSequence("<changes>"
                  + "  <delete type='objectWithCompositeKey' id1='0' id2='3'/>"
                  + "  <create type='objectWithCompositeKey' id1='0' id2='1'/>"
                  + "  <update type='objectWithCompositeKey' id1='0' id2='2' name='newName'/>"
                  + "</changes>",
                  "<changes>"
                  + "  <create type='objectWithCompositeKey' id1='0' id2='1'/>"
                  + "  <update type='objectWithCompositeKey' id1='0' id2='2' name='newName' _name='(null)'/>"
                  + "  <delete type='objectWithCompositeKey' id1='0' id2='3'/>"
                  + "</changes>");
  }

  public void testSingleTypeWithUpdateOnCreate() throws Exception {
    checkSequence("<changes>"
                  + "  <create type='objectWithCompositeKey' id1='0' id2='1'/>"
                  + "  <update type='objectWithCompositeKey' id1='0' id2='1' name='newName'/>"
                  + "</changes>",
                  "<changes>"
                  + "  <create type='objectWithCompositeKey' id1='0' id2='1' name='newName'/>"
                  + "</changes>");
  }

  public static class ObjectWithSelfReference {
    public static GlobType TYPE;

    @Key
    public static IntegerField ID;

    @Target(ObjectWithSelfReference.class)
    public static LinkField LINK;

    static {
      GlobTypeLoader.init(ObjectWithSelfReference.class);
    }
  }

  public void testObjectWithSelfReference() throws Exception {
    try {
      checkSequence("<changes>"
                    + "  <create type='objectWithSelfReference' id='0' link='1'/>"
                    + "  <create type='objectWithSelfReference' id='1' link='0'/>"
                    + "</changes>",
                    "<changes>"
                    + "  <create type='objectWithSelfReference' id='1'/>"
                    + "  <create type='objectWithSelfReference' id='0'/>"
                    + "  <update type='objectWithSelfReference' id='1' link='0' _link='(null)'/>"
                    + "  <update type='objectWithSelfReference' id='0' link='1' _link='(null)'/>"
                    + "</changes>");
    }
    catch (AssertionFailedError e) {
      // the order withing the create and update sequences may vary
      checkSequence("<changes>"
                    + "  <create type='objectWithSelfReference' id='0' link='1'/>"
                    + "  <create type='objectWithSelfReference' id='1' link='0'/>"
                    + "</changes>",
                    "<changes>"
                    + "  <create type='objectWithSelfReference' id='0'/>"
                    + "  <create type='objectWithSelfReference' id='1'/>"
                    + "  <update type='objectWithSelfReference' id='0' link='1' _link='(null)'/>"
                    + "  <update type='objectWithSelfReference' id='1' link='0' _link='(null)'/>"
                    + "</changes>");
    }
  }

  public static class LinkCycle1 {
    public static GlobType TYPE;

    @Key
    public static IntegerField ID1;

    @Key
    public static IntegerField ID2;

    @Target(ObjectWithCompositeKey.class)
    public static IntegerField LINK1;

    @Target(ObjectWithCompositeKey.class)
    public static IntegerField LINK2;

    public static Link LINK;

    static {
      GlobTypeLoader.init(LinkCycle1.class)
        .defineLink(LINK)
        .add(LINK1, LinkCycle2.ID1)
        .add(LINK2, LinkCycle2.ID2);
    }
  }

  public static class LinkCycle2 {
    public static GlobType TYPE;

    @Key
    public static IntegerField ID1;

    @Key
    public static IntegerField ID2;

    @Target(ObjectWithCompositeKey.class)
    public static IntegerField LINK1;

    @Target(ObjectWithCompositeKey.class)
    public static IntegerField LINK2;

    public static Link LINK;

    static {
      GlobTypeLoader.init(LinkCycle2.class)
        .defineLink(LINK)
        .add(LINK1, LinkCycle1.ID1)
        .add(LINK2, LinkCycle1.ID2);
    }
  }

  public void testLink() throws Exception {
    checkSequence("<changes>"
                  + "  <create type='linkedToObjectWithCompositeKey' id='0' link1='1' link2='2'/>"
                  + "  <delete type='linkedToObjectWithCompositeKey' id='1' _link1='3' _link2='4'/>"
                  + "  <update type='linkedToObjectWithCompositeKey' id='2' link1='2' link2='3'/>"
                  + "  <delete type='objectWithCompositeKey' id1='3' id2='4'/>"
                  + "  <update type='objectWithCompositeKey' id1='2' id2='3' name='newName'/>"
                  + "  <create type='objectWithCompositeKey' id1='1' id2='2'/>"
                  + "</changes>",
                  "<changes>"
                  + "  <create type='objectWithCompositeKey' id1='1' id2='2'/>"
                  + "  <create type='linkedToObjectWithCompositeKey' id='0' link1='1' link2='2'/>"
                  + "  <update type='objectWithCompositeKey' id1='2' id2='3'"
                  + "          name='newName' _name='(null)'/>"
                  + "  <update type='linkedToObjectWithCompositeKey' id='2'"
                  + "          link1='2' _link1='(null)' "
                  + "          link2='3' _link2='(null)'/>"
                  + "  <delete type='linkedToObjectWithCompositeKey' id='1' _link1='3' _link2='4'/>"
                  + "  <delete type='objectWithCompositeKey' id1='3' id2='4'/>"
                  + "</changes>");
  }

  public void testLinkCycle() throws Exception {
    checkSequence("<changes>"
                  + "  <create type='linkCycle1' id1='0' id2='2' link1='1' link2='2'/>"
                  + "  <create type='linkCycle2' id1='1' id2='2' link1='1' link2='2'/>"
                  + "</changes>",
                  "<changes>"
                  + "  <create type='linkCycle2' id1='1' id2='2'/>"
                  + "  <create type='linkCycle1' id1='0' id2='2'" +
                  "            link1='1' link2='2'/>"
                  + "  <update type='linkCycle2' id1='1' id2='2' " +
                  "            link1='1' _link1='(null)' " +
                  "            link2='2' _link2='(null)'/>"
                  + "</changes>");
  }

  public static class LargeLinkCycle1 {
    public static GlobType TYPE;

    @Key
    public static IntegerField ID;

    @Target(LargeLinkCycle2.class)
    public static LinkField LINK;

    static {
      GlobTypeLoader.init(LargeLinkCycle1.class);
    }
  }

  public static class LargeLinkCycle2 {
    public static GlobType TYPE;

    @Key
    public static IntegerField ID;

    @Target(LargeLinkCycle3.class)
    public static LinkField LINK;

    static {
      GlobTypeLoader.init(LargeLinkCycle2.class);
    }
  }

  public static class LargeLinkCycle3 {
    public static GlobType TYPE;

    @Key
    public static IntegerField ID;

    @Target(LargeLinkCycle1.class)
    public static LinkField LINK;

    static {
      GlobTypeLoader.init(LargeLinkCycle3.class);
    }
  }

  public void testLargeLinkCycle() throws Exception {
    checkSequence("<changes>"
                  + "  <create type='largeLinkCycle1' id='1' link='2'/>"
                  + "  <create type='largeLinkCycle2' id='2' link='3'/>"
                  + "  <create type='largeLinkCycle3' id='3' link='1'/>"
                  + "</changes>",
                  "<changes>"
                  + "  <create type='largeLinkCycle3' id='3'/>"
                  + "  <create type='largeLinkCycle2' id='2' link='3'/>"
                  + "  <create type='largeLinkCycle1' id='1' link='2'/>"
                  + "  <update type='largeLinkCycle3' id='3' link='1' _link='(null)'/>"
                  + "</changes>");
  }

  private void checkSequence(String input, String expected) throws Exception {
    ChangeSet changeSet = XmlChangeSetParser.parse(Model.MODEL, new StringReader(input));

    StringWriter writer = new StringWriter();
    XmlChangeSetVisitor visitor = new XmlChangeSetVisitor(writer, 2);
    ChangeSetSequencer.process(changeSet, Model.MODEL, visitor);
    visitor.complete();

    XmlTestUtils.assertEquals(expected, writer.toString());
  }

  private static class Model {
    static final GlobModel MODEL = GlobModelBuilder.init(ObjectWithCompositeKey.TYPE,
                                                         LinkedToObjectWithCompositeKey.TYPE,
                                                         ObjectWithSelfReference.TYPE,
                                                         LinkCycle1.TYPE,
                                                         LinkCycle2.TYPE,
                                                         LargeLinkCycle1.TYPE,
                                                         LargeLinkCycle2.TYPE,
                                                         LargeLinkCycle3.TYPE
    ).get();
  }
}
