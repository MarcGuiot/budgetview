package org.crossbowlabs.globs.xml;

import junit.framework.TestCase;
import org.crossbowlabs.globs.metamodel.DummyObject;
import org.crossbowlabs.globs.model.FieldValuesBuilder;
import static org.crossbowlabs.globs.model.KeyBuilder.newKey;
import org.crossbowlabs.globs.model.delta.DefaultChangeSet;

import java.io.StringWriter;

public class XmlChangeSetWriterTest extends TestCase {
  public void test() throws Exception {
    DefaultChangeSet changeSet = new DefaultChangeSet();
    changeSet.processCreation(DummyObject.TYPE,
                              FieldValuesBuilder.init()
                                .set(DummyObject.ID, 1)
                                .set(DummyObject.NAME, "obj1")
                                .get());

    changeSet.processUpdate(newKey(DummyObject.TYPE, 2), DummyObject.VALUE, 2.3);
    changeSet.processDeletion(newKey(DummyObject.TYPE, 3), FieldValuesBuilder.init(DummyObject.NAME, "obj3").get());

    StringWriter writer = new StringWriter();
    XmlChangeSetWriter.write(changeSet, writer);
    XmlTestUtils.assertEquivalent("<changes>" +
                                  "  <create type='dummyObject' id='1' name='obj1'/>" +
                                  "  <update type='dummyObject' id='2' value='2.3'/>" +
                                  "  <delete type='dummyObject' id='3' name='obj3'/>" +
                                  "</changes>",
                                  writer.toString());
  }
}
