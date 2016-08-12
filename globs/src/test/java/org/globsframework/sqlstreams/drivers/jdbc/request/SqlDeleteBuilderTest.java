package org.globsframework.sqlstreams.drivers.jdbc.request;

import org.globsframework.metamodel.DummyObject;
import org.globsframework.metamodel.DummyObject2;
import org.globsframework.model.GlobList;
import org.globsframework.sqlstreams.constraints.Constraint;
import org.globsframework.sqlstreams.constraints.Where;
import org.globsframework.sqlstreams.drivers.jdbc.GlobsDatabaseTestCase;
import org.globsframework.streams.GlobStream;
import org.globsframework.streams.xml.XmlGlobStreamReader;

public class SqlDeleteBuilderTest extends GlobsDatabaseTestCase {

  public void testDelete() throws Exception {
    GlobStream streamToWrite =
      XmlGlobStreamReader.parse(directory,
                                "<dummyObject id='1' name='hello' value='1.1' present='true'/>");
    populate(sqlConnection, streamToWrite);
    sqlConnection.startDelete(DummyObject.TYPE).execute();
    assertEquals(0, sqlConnection.startSelect(DummyObject.TYPE).getQuery().getList().size());
  }

  public void testDeleteWithConstraint() throws Exception {
    populate(sqlConnection, XmlGlobStreamReader.parse(directory,
                                                      "<dummyObject id='1' name='hello' value='1.1' present='true'/>" +
                                                      "<dummyObject id='2' name='world' value='1.1' present='true'/>"));
    populate(sqlConnection, XmlGlobStreamReader.parse(directory,
                                                      "<dummyObject2 id='1' label='hello'/>"));
    Constraint constraint = Where.fieldEquals(DummyObject.NAME, "hello");
    sqlConnection.startDelete(DummyObject.TYPE, constraint).execute();
    GlobList globs = sqlConnection.startSelect(DummyObject.TYPE).getQuery().getList();
    assertEquals(1, globs.size());
    assertEquals(2, globs.get(0).get(DummyObject.ID).intValue());
    assertEquals(1, sqlConnection.startSelect(DummyObject2.TYPE).getQuery().getList().size());
  }
}
