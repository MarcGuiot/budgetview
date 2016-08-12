package org.globsframework.sqlstreams.drivers.jdbc;

import junit.framework.TestCase;
import org.globsframework.metamodel.DummyObject;
import org.globsframework.model.Glob;
import org.globsframework.sqlstreams.SelectQuery;
import org.globsframework.sqlstreams.SqlConnection;
import org.globsframework.sqlstreams.constraints.Where;

public abstract class SqlExceptionTest extends TestCase {

  public abstract SqlConnection getDbConnection();

  protected void setUp() throws Exception {
    SqlConnection sqlConnection = getDbConnection();
    sqlConnection.createTables(DummyObject.TYPE);
    sqlConnection.deleteAll(DummyObject.TYPE);
    sqlConnection.commitAndClose();
  }

  public void testRollback() throws Exception {
    SqlConnection db1 = getDbConnection();
    db1.startCreate(DummyObject.TYPE).set(DummyObject.ID, 1).set(DummyObject.NAME, "toto").getRequest().execute();
    db1.commit();
    SelectQuery query1 = db1.startSelect(DummyObject.TYPE, Where.fieldEquals(DummyObject.ID, 1))
      .select(DummyObject.NAME).getQuery();
    assertEquals("toto", query1.getUnique().get(DummyObject.NAME));
    db1.startUpdate(DummyObject.TYPE, Where.fieldEquals(DummyObject.ID, 1))
      .set(DummyObject.NAME, "titi")
      .getRequest().execute();
    assertEquals("titi", query1.getUnique().get(DummyObject.NAME));
    db1.rollbackAndClose();
    SqlConnection db2 = getDbConnection();
    assertEquals("toto", db2.startSelect(DummyObject.TYPE, Where.fieldEquals(DummyObject.ID, 1))
      .select(DummyObject.NAME).getQuery().getUnique().get(DummyObject.NAME));
  }

  public void testConcurrentModification() throws Exception {
    SqlConnection db1 = getDbConnection();
    SqlConnection db2 = getDbConnection();
    db1.startCreate(DummyObject.TYPE)
      .set(DummyObject.ID, 1)
      .set(DummyObject.NAME, "toto")
      .run();
    db1.commit();
    Glob glob1 = db1.startSelect(DummyObject.TYPE, Where.fieldEquals(DummyObject.ID, 1))
      .select(DummyObject.NAME)
      .getUnique();
    assertEquals("toto", glob1.get(DummyObject.NAME));
    db1.startUpdate(DummyObject.TYPE, Where.fieldEquals(DummyObject.ID, 1))
      .set(DummyObject.NAME, "titi").getRequest().execute();

    SelectQuery query2 = db2.startSelect(DummyObject.TYPE, Where.fieldEquals(DummyObject.ID, 1))
      .select(DummyObject.NAME).getQuery();
    Glob glob2 = query2.getUnique();
    assertEquals("titi", glob2.get(DummyObject.NAME));
    db2.startUpdate(DummyObject.TYPE, Where.fieldEquals(DummyObject.ID, 1))
      .set(DummyObject.NAME, "tata").getRequest().execute();
//    db1.rollbackAndClose();
    glob2 = query2.getUnique();
    assertEquals("tata", glob2.get(DummyObject.NAME));
    db2.commit();
    db1.rollbackAndClose();
    Glob newGlob = getDbConnection().startSelect(DummyObject.TYPE, Where.fieldEquals(DummyObject.ID, 1))
      .select(DummyObject.NAME).getQuery().getUnique();
    assertEquals("toto", newGlob.get(DummyObject.NAME));
  }
}