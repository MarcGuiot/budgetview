package org.crossbowlabs.globs.sqlstreams.drivers.jdbc.request;

import org.crossbowlabs.globs.metamodel.DummyObject;
import org.crossbowlabs.globs.metamodel.DummyObject2;
import org.crossbowlabs.globs.model.GlobList;
import org.crossbowlabs.globs.sqlstreams.constraints.Constraint;
import org.crossbowlabs.globs.sqlstreams.constraints.Constraints;
import org.crossbowlabs.globs.sqlstreams.drivers.jdbc.DbServicesTestCase;
import org.crossbowlabs.globs.streams.GlobStream;
import org.crossbowlabs.globs.streams.xml.XmlGlobStreamReader;

public class SqlDeleteBuilderTest extends DbServicesTestCase {

  public void testDelete() throws Exception {
    GlobStream streamToWrite =
      XmlGlobStreamReader.parse(directory,
                                "<dummyObject id='1' name='hello' value='1.1' present='true'/>");
    populate(sqlConnection, streamToWrite);
    sqlConnection.getDeleteRequest(DummyObject.TYPE).run();
    assertEquals(0, sqlConnection.getQueryBuilder(DummyObject.TYPE).getQuery().executeAsGlobs().size());
  }

  public void testDeleteWithConstraint() throws Exception {
    populate(sqlConnection, XmlGlobStreamReader.parse(directory,
                                                      "<dummyObject id='1' name='hello' value='1.1' present='true'/>" +
                                                      "<dummyObject id='2' name='world' value='1.1' present='true'/>"));
    populate(sqlConnection, XmlGlobStreamReader.parse(directory,
                                                      "<dummyObject2 id='1' label='hello'/>"));
    Constraint constraint = Constraints.equal(DummyObject.NAME, "hello");
    sqlConnection.getDeleteRequest(DummyObject.TYPE, constraint).run();
    GlobList globs = sqlConnection.getQueryBuilder(DummyObject.TYPE).getQuery().executeAsGlobs();
    assertEquals(1, globs.size());
    assertEquals(2, globs.get(0).get(DummyObject.ID).intValue());
    assertEquals(1, sqlConnection.getQueryBuilder(DummyObject2.TYPE).getQuery().executeAsGlobs().size());
  }
}
