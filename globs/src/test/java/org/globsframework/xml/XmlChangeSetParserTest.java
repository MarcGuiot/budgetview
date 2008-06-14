package org.globsframework.xml;

import junit.framework.TestCase;
import org.globsframework.metamodel.DummyModel;
import org.globsframework.metamodel.DummyObject;
import org.globsframework.model.ChangeSet;
import org.globsframework.model.ChangeSetVisitor;
import org.globsframework.model.FieldValues;
import org.globsframework.model.Key;
import org.globsframework.utils.Dates;
import org.globsframework.utils.exceptions.InvalidParameter;
import org.globsframework.utils.exceptions.ItemNotFound;

import java.io.StringReader;

public class XmlChangeSetParserTest extends TestCase {
  public void testStandardCase() throws Exception {
    ChangeSet changeSet = XmlChangeSetParser.parse(DummyModel.get(), new StringReader(
      "<changes>"
      + "  <create type='dummyObject' id='2' name='name1' value='2.0' present='true'/>"
      + "  <update type='dummyObject' id='3' name='newName' date='2007/07/11'/>"
      + "  <delete type='dummyObject' id='4'/>"
      + "</changes>"
    ));

    assertEquals(3, changeSet.getChangeCount(DummyObject.TYPE));
    changeSet.visit(new ChangeSetVisitor() {
      public void visitCreation(Key key, FieldValues values) throws Exception {
        assertEquals(2, key.get(DummyObject.ID).intValue());
        assertEquals(3, values.size());
        assertEquals("name1", values.get(DummyObject.NAME));
        assertEquals(2.0, values.get(DummyObject.VALUE), 0.01);
        assertTrue(values.get(DummyObject.PRESENT));
      }

      public void visitUpdate(Key key, FieldValues values) throws Exception {
        assertEquals(3, key.get(DummyObject.ID).intValue());
        assertEquals(2, values.size());
        assertEquals("newName", values.get(DummyObject.NAME));
        assertEquals(Dates.parse("2007/07/11"), values.get(DummyObject.DATE));
      }

      public void visitDeletion(Key key, FieldValues values) throws Exception {
        assertEquals(4, key.get(DummyObject.ID).intValue());
        assertEquals(0, values.size());
      }
    });
  }

  public void testMissingType() throws Exception {
    try {
      XmlChangeSetParser.parse(DummyModel.get(), new StringReader(
        "<changes>"
        + "  <create id='2' name='name1'/>"
        + "</changes>"
      ));
      fail();
    }
    catch (InvalidParameter e) {
      assertEquals("Missing attribute 'type' in tag 'create'", e.getMessage());
    }
  }

  public void testWrongType() throws Exception {
    try {
      XmlChangeSetParser.parse(DummyModel.get(), new StringReader(
        "<changes>"
        + "  <create type='unknown' id='2' name='name1'/>"
        + "</changes>"
      ));
      fail();
    }
    catch (ItemNotFound e) {
      assertEquals("No object type found with name: unknown", e.getMessage());
    }
  }

  public void testUnknownField() throws Exception {
    try {
      XmlChangeSetParser.parse(DummyModel.get(), new StringReader(
        "<changes>"
        + "  <create type='dummyObject' id='2' toto='name1'/>"
        + "</changes>"
      ));
      fail();
    }
    catch (ItemNotFound e) {
      assertEquals("Unknown field 'toto' for type 'dummyObject'", e.getMessage());
    }
  }

  public void testInvalidValue() throws Exception {
    try {
      XmlChangeSetParser.parse(DummyModel.get(), new StringReader(
        "<changes>"
        + "  <create type='dummyObject' id='2' value='toto'/>"
        + "</changes>"
      ));
      fail();
    }
    catch (InvalidParameter e) {
      assertEquals("'toto' is not a proper value for field 'value' in type 'dummyObject'", e.getMessage());
    }
  }
}
