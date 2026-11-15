package org.globsframework.sqlstreams.drivers.jdbc;

import junit.framework.TestCase;
import org.globsframework.metamodel.DummyObject;
import org.globsframework.model.Glob;
import org.globsframework.sqlstreams.SqlSelect;
import org.globsframework.sqlstreams.SqlConnection;
import org.globsframework.sqlstreams.constraints.Where;

public abstract class SqlExceptionTest extends TestCase {

  public abstract SqlConnection getDbConnection();

  protected void setUp() throws Exception {
    SqlConnection sqlConnection = getDbConnection();
    sqlConnection.createTables(DummyObject.TYPE);
    sqlConnection.startDelete(DummyObject.TYPE).execute();
    sqlConnection.commitAndClose();
  }

  public void testRollback() throws Exception {
    SqlConnection db1 = getDbConnection();
    db1.startCreate(DummyObject.TYPE).set(DummyObject.ID, 1).set(DummyObject.NAME, "toto").getRequest().execute();
    db1.commit();
    // Requête relue deux fois : getQuery() la referme après le premier getUnique().
    SqlSelect query1 = db1.startSelect(DummyObject.TYPE, Where.fieldEquals(DummyObject.ID, 1))
      .select(DummyObject.NAME).getNotAutoCloseQuery();
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

  // Désactivé (JUnit 3 : renommé pour ne plus commencer par "test") — suppose une lecture non isolée
  // (db2 lit la modification non validée de db1), ce que faisait HSQLDB 1.8. Depuis le passage à HSQLDB
  // 2.3.4 (fc89dcb0c, 2016), db2 attend le verrou de db1, tenu par le même fil : blocage sans fin.
  public void disabled_testConcurrentModification() throws Exception {
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

    SqlSelect query2 = db2.startSelect(DummyObject.TYPE, Where.fieldEquals(DummyObject.ID, 1))
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