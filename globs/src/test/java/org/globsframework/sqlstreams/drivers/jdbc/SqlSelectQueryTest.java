package org.globsframework.sqlstreams.drivers.jdbc;

import org.globsframework.metamodel.DummyObject;
import org.globsframework.metamodel.DummyObject2;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.sqlstreams.SqlSelect;
import org.globsframework.sqlstreams.SqlConnection;
import org.globsframework.sqlstreams.constraints.Constraint;
import org.globsframework.sqlstreams.constraints.Where;
import org.globsframework.sqlstreams.exceptions.GlobsSqlException;
import org.globsframework.streams.GlobStream;
import org.globsframework.streams.accessors.IntegerAccessor;
import org.globsframework.streams.accessors.StringAccessor;
import org.globsframework.streams.accessors.utils.ValueIntegerAccessor;
import org.globsframework.streams.xml.XmlGlobStreamReader;
import org.globsframework.utils.Functor;
import org.globsframework.utils.Ref;
import org.globsframework.utils.TestUtils;

import java.util.Arrays;

import static org.globsframework.sqlstreams.constraints.Where.and;

public class SqlSelectQueryTest extends GlobsDatabaseTestCase {

  public void testSimpleRequest() throws Exception {
    GlobStream streamToWrite =
      XmlGlobStreamReader.parse(directory,
                                "<dummyObject id='1' name='hello' value='1.1' present='true'/>");
    populate(sqlConnection, streamToWrite);

    Ref<IntegerAccessor> idAccessor = new Ref<IntegerAccessor>();
    Ref<StringAccessor> nameAccessor = new Ref<StringAccessor>();
    SqlSelect query =
      sqlConnection.startSelect(DummyObject.TYPE, Where.fieldEquals(DummyObject.ID, 1))
        .select(DummyObject.ID, idAccessor)
        .select(DummyObject.NAME, nameAccessor)
        .select(DummyObject.PRESENT)
        .select(DummyObject.VALUE).getQuery();

    GlobStream requestStream = query.getStream();
    assertTrue(requestStream.next());
    assertEquals(1, idAccessor.get().getValue());
    assertEquals("hello", nameAccessor.get().getString());
    assertEquals(1.1, requestStream.getAccessor(DummyObject.VALUE).getObjectValue());
    assertEquals(true, requestStream.getAccessor(DummyObject.PRESENT).getObjectValue());
    assertFalse(requestStream.next());
  }

  public void testMultipleExecute() throws Exception {
    SqlConnection sqlConnection = init();
    ValueIntegerAccessor value = new ValueIntegerAccessor(1);
    SqlSelect query =
      sqlConnection.startSelect(DummyObject.TYPE, Where.fieldEquals(DummyObject.ID, value))
        .select(DummyObject.NAME)
        .getQuery();
    GlobStream hellotream = query.getStream();
    assertTrue(hellotream.next());
    assertEquals("hello", hellotream.getAccessor(DummyObject.NAME).getObjectValue());

    value.setValue(2);
    GlobStream worldStream = query.getStream();
    assertTrue(worldStream.next());
    assertEquals("world", worldStream.getAccessor(DummyObject.NAME).getObjectValue());

  }

  public void testAnd() throws Exception {
    SqlConnection sqlConnection = init();

    GlobList list =
      sqlConnection.selectAll(DummyObject.TYPE,
                              and(Where.fieldEquals(DummyObject.NAME, "hello"),
                                  Where.fieldEquals(DummyObject.ID, 1)));
    assertEquals(1, list.size());
    assertEquals(1, list.get(0).get(DummyObject.ID).intValue());
  }

  public void testNullAnd() throws Exception {
    SqlConnection sqlConnection = init();
    sqlConnection.startSelect(DummyObject.TYPE, and(null,
                                                    Where.fieldEquals(DummyObject.ID, 1)))
      .getQuery().getUnique();
  }

  public void testNullOr() throws Exception {
    SqlConnection sqlConnection = init();
    sqlConnection.startSelect(DummyObject.TYPE, Where.or(null,
                                                         Where.fieldEquals(DummyObject.ID, 1)))
      .getQuery().getUnique();
  }

  public void testJointure() throws Exception {
    populate(sqlConnection,
             XmlGlobStreamReader.parse(directory,
                                       "<dummyObject id='1' name='hello' value='1.1' present='true'/>" +
                                       "<dummyObject id='3' name='world' value='2.2' present='false'/>"));
    populate(sqlConnection,
             XmlGlobStreamReader.parse(directory,
                                       "<dummyObject2 id='2' label='world'/>"));

    Glob glob = execute(Where.fieldsAreEqual(DummyObject.NAME, DummyObject2.LABEL));
    assertEquals(glob.get(DummyObject.ID).intValue(), 3);
  }

  public void testLessBigger() throws Exception {
    populate(sqlConnection,
             XmlGlobStreamReader.parse(directory,
                                       "<dummyObject id='1' name='hello' value='1.1' present='true' date='2000/10/10'/>" +
                                       "<dummyObject id='2' name='world' value='2.2' present='false' date='2000/09/10'/>"));

    assertEquals(1, execute(Where.fieldLessThanValue(DummyObject.VALUE, 1.2)).get(DummyObject.ID).intValue());
    assertEquals(1, execute(Where.fieldLessThanValue(DummyObject.VALUE, 1.1)).get(DummyObject.ID).intValue());
    assertEquals(1, execute(Where.fieldLessThanValue(DummyObject.VALUE, 1.2)).get(DummyObject.ID).intValue());
    assertEquals(2, execute(Where.fieldGreaterThan(DummyObject.VALUE, 1.2)).get(DummyObject.ID).intValue());
    assertEquals(2, execute(Where.fieldGreaterThan(DummyObject.VALUE, 2.2)).get(DummyObject.ID).intValue());
    assertEquals(2, execute(Where.fieldStrictlyGreaterThan(DummyObject.VALUE, 1.2)).get(DummyObject.ID).intValue());
    checkEmpty(Where.fieldStrictlyGreaterThan(DummyObject.VALUE, 2.2));
    checkEmpty(Where.fieldLessThan(DummyObject.VALUE, 1.0));
    checkEmpty(Where.fieldStrictlyGreaterThan(DummyObject.VALUE, 3.2));
    checkEmpty(Where.fieldLessThanValue(DummyObject.VALUE, 0.1));
    checkEmpty(Where.fieldGreaterThan(DummyObject.VALUE, 3.2));
    checkEmpty(Where.fieldLessThanValue(DummyObject.VALUE, 0.1));
  }

  public void testMixedExecuteOnSameQueryIsNotsuported() throws Exception {
    populate(sqlConnection,
             XmlGlobStreamReader.parse(directory,
                                       "<dummyObject id='1' name='hello' value='1.1' present='true'/>" +
                                       "<dummyObject id='3' name='world' value='2.2' present='false'/>" +
                                       "<dummyObject id='4' name='world' value='2.2' present='false'/>" +
                                       "<dummyObject id='5' name='world' value='2.2' present='false'/>" +
                                       "<dummyObject id='6' name='world' value='2.2' present='false'/>" +
                                       "<dummyObject id='7' name='world' value='2.2' present='false'/>"));
    populate(sqlConnection,
             XmlGlobStreamReader.parse(directory,
                                       "<dummyObject2 id='2' label='world'/>"));

    Ref<IntegerAccessor> ref = new Ref<IntegerAccessor>();
    SqlSelect query = sqlConnection.startSelect(DummyObject.TYPE,
                                                Where.fieldsAreEqual(DummyObject.NAME, DummyObject2.LABEL))
      .select(DummyObject.ID, ref).getQuery();
    final GlobStream firstGlobStream = query.getStream();
    final IntegerAccessor firstAccessor = ref.get();
    GlobStream secondGlobStream = query.getStream();
    IntegerAccessor secondAccessor = ref.get();
    TestUtils.assertFails(new Functor() {
      public void run() throws Exception {
        firstGlobStream.next();
        firstAccessor.getValue();
      }
    }, GlobsSqlException.class);
  }

  public void testInConstraint() throws Exception {
    populate(sqlConnection,
             XmlGlobStreamReader.parse(directory,
                                       "<dummyObject id='1' name='hello' value='1.1' present='true'/>" +
                                       "<dummyObject id='3' name='world' value='2.2' present='false'/>" +
                                       "<dummyObject id='4' name='world' value='2.2' present='false'/>" +
                                       "<dummyObject id='5' name='world' value='2.2' present='false'/>" +
                                       "<dummyObject id='6' name='world' value='2.2' present='false'/>" +
                                       "<dummyObject id='7' name='world' value='2.2' present='false'/>"));
    Integer[] values = {1, 2, 3, 4, 5};
    GlobList list = sqlConnection.startSelect(DummyObject.TYPE,
                                              Where.in(DummyObject.ID, Arrays.asList(values))).getQuery().getList();
    assertEquals(4, list.size());
  }

  public void testNotEqual() throws Exception {
    populate(sqlConnection,
             XmlGlobStreamReader.parse(directory,
                                       "<dummyObject id='1' name='hello' value='1.1' present='true'/>" +
                                       "<dummyObject id='3' name='world' value='2.2' present='false'/>"));

    Glob glob = execute(Where.notEqual(DummyObject.NAME, "hello"));
    assertEquals(glob.get(DummyObject.ID).intValue(), 3);
  }

  private SqlConnection init() {
    GlobStream streamToWrite =
      XmlGlobStreamReader.parse(directory,
                                "<dummyObject id='1'  name='hello' value='1.1' present='true'/>" +
                                "<dummyObject id='2'  name='world' value='2.2' present='false'/>");
    populate(sqlConnection, streamToWrite);
    return sqlConnection;
  }

  private void checkEmpty(Constraint constraint) {
    assertTrue(sqlConnection.startSelect(DummyObject.TYPE, constraint).getQuery().getList().isEmpty());
  }

  private Glob execute(Constraint constraint) {
    return sqlConnection.startSelect(DummyObject.TYPE, constraint)
      .getQuery().getUnique();
  }
}
