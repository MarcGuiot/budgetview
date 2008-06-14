package org.globsframework.sqlstreams.drivers.jdbc;

import org.globsframework.metamodel.DummyObject;
import org.globsframework.metamodel.DummyObject2;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.sqlstreams.SelectQuery;
import org.globsframework.sqlstreams.SqlConnection;
import org.globsframework.sqlstreams.constraints.Constraint;
import org.globsframework.sqlstreams.constraints.Constraints;
import static org.globsframework.sqlstreams.constraints.Constraints.and;
import static org.globsframework.sqlstreams.constraints.Constraints.equal;
import org.globsframework.sqlstreams.exceptions.SqlException;
import org.globsframework.streams.GlobStream;
import org.globsframework.streams.accessors.IntegerAccessor;
import org.globsframework.streams.accessors.StringAccessor;
import org.globsframework.streams.accessors.utils.ValueIntegerAccessor;
import org.globsframework.streams.xml.XmlGlobStreamReader;
import org.globsframework.utils.Functor;
import org.globsframework.utils.Ref;
import org.globsframework.utils.TestUtils;

import java.util.Arrays;

public class SqlSelectQueryTest extends DbServicesTestCase {

  public void testSimpleRequest() throws Exception {
    GlobStream streamToWrite =
      XmlGlobStreamReader.parse(directory,
                                "<dummyObject id='1' name='hello' value='1.1' present='true'/>");
    populate(sqlConnection, streamToWrite);

    Ref<IntegerAccessor> idAccessor = new Ref<IntegerAccessor>();
    Ref<StringAccessor> nameAccessor = new Ref<StringAccessor>();
    SelectQuery query =
      sqlConnection.getQueryBuilder(DummyObject.TYPE, equal(DummyObject.ID, 1))
        .select(DummyObject.ID, idAccessor)
        .select(DummyObject.NAME, nameAccessor)
        .select(DummyObject.PRESENT)
        .select(DummyObject.VALUE).getQuery();

    GlobStream requestStream = query.execute();
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
    SelectQuery query =
      sqlConnection.getQueryBuilder(DummyObject.TYPE, equal(DummyObject.ID, value))
        .select(DummyObject.NAME)
        .getQuery();
    GlobStream hellotream = query.execute();
    assertTrue(hellotream.next());
    assertEquals("hello", hellotream.getAccessor(DummyObject.NAME).getObjectValue());

    value.setValue(2);
    GlobStream worldStream = query.execute();
    assertTrue(worldStream.next());
    assertEquals("world", worldStream.getAccessor(DummyObject.NAME).getObjectValue());

  }

  public void testAnd() throws Exception {
    SqlConnection sqlConnection = init();

    GlobList list =
      sqlConnection.getQueryBuilder(DummyObject.TYPE,
                                    and(Constraints.equal(DummyObject.NAME, "hello"),
                                        Constraints.equal(DummyObject.ID, 1)))
        .selectAll()
        .getQuery()
        .executeAsGlobs();
    assertEquals(1, list.size());
    assertEquals(1, list.get(0).get(DummyObject.ID).intValue());
  }

  public void testNullAnd() throws Exception {
    SqlConnection sqlConnection = init();
    sqlConnection.getQueryBuilder(DummyObject.TYPE, and(null,
                                                        equal(DummyObject.ID, 1)))
      .getQuery().executeUnique();
  }

  public void testNullOr() throws Exception {
    SqlConnection sqlConnection = init();
    sqlConnection.getQueryBuilder(DummyObject.TYPE, Constraints.or(null,
                                                                   equal(DummyObject.ID, 1)))
      .getQuery().executeUnique();
  }

  public void testJointure() throws Exception {
    populate(sqlConnection,
             XmlGlobStreamReader.parse(directory,
                                       "<dummyObject id='1' name='hello' value='1.1' present='true'/>" +
                                       "<dummyObject id='3' name='world' value='2.2' present='false'/>"));
    populate(sqlConnection,
             XmlGlobStreamReader.parse(directory,
                                       "<dummyObject2 id='2' label='world'/>"));

    Glob glob = execute(Constraints.fieldEqual(DummyObject.NAME, DummyObject2.LABEL));
    assertEquals(glob.get(DummyObject.ID).intValue(), 3);
  }

  public void testLessBigger() throws Exception {
    populate(sqlConnection,
             XmlGlobStreamReader.parse(directory,
                                       "<dummyObject id='1' name='hello' value='1.1' present='true' date='2000/10/10'/>" +
                                       "<dummyObject id='2' name='world' value='2.2' present='false' date='2000/09/10'/>"));

    assertEquals(1, execute(Constraints.lessUncheck(DummyObject.VALUE, 1.2)).get(DummyObject.ID).intValue());
    assertEquals(1, execute(Constraints.lessUncheck(DummyObject.VALUE, 1.1)).get(DummyObject.ID).intValue());
    assertEquals(1, execute(Constraints.Lesser(DummyObject.VALUE, 1.2)).get(DummyObject.ID).intValue());
    assertEquals(2, execute(Constraints.greater(DummyObject.VALUE, 1.2)).get(DummyObject.ID).intValue());
    assertEquals(2, execute(Constraints.greater(DummyObject.VALUE, 2.2)).get(DummyObject.ID).intValue());
    assertEquals(2, execute(Constraints.strictlyGreater(DummyObject.VALUE, 1.2)).get(DummyObject.ID).intValue());
    checkEmpty(Constraints.strictlyGreater(DummyObject.VALUE, 2.2));
    checkEmpty(Constraints.Lesser(DummyObject.VALUE, 1.1));
    checkEmpty(Constraints.strictlyGreater(DummyObject.VALUE, 3.2));
    checkEmpty(Constraints.Lesser(DummyObject.VALUE, 0.1));
    checkEmpty(Constraints.greater(DummyObject.VALUE, 3.2));
    checkEmpty(Constraints.lessUncheck(DummyObject.VALUE, 0.1));
  }

  public void testMixedExcecuteOnSameQueryIsNotsuported() throws Exception {

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
    SelectQuery query = sqlConnection.getQueryBuilder(DummyObject.TYPE,
                                                      Constraints.fieldEqual(DummyObject.NAME, DummyObject2.LABEL))
      .select(DummyObject.ID, ref).getQuery();
    final GlobStream firstGlobStream = query.execute();
    final IntegerAccessor firstAccessor = ref.get();
    GlobStream secondGlobStream = query.execute();
    IntegerAccessor secondAccessor = ref.get();
    TestUtils.assertFails(new Functor() {
      public void run() throws Exception {
        firstGlobStream.next();
        firstAccessor.getValue();
      }
    }, SqlException.class);
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
    GlobList list = sqlConnection.getQueryBuilder(DummyObject.TYPE,
                                                  Constraints.in(DummyObject.ID, Arrays.asList(values))).getQuery().executeAsGlobs();
    assertEquals(4, list.size());
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
    assertTrue(sqlConnection.getQueryBuilder(DummyObject.TYPE, constraint).getQuery().executeAsGlobs().isEmpty());
  }

  private Glob execute(Constraint constraint) {
    return sqlConnection.getQueryBuilder(DummyObject.TYPE, constraint)
      .getQuery().executeUnique();
  }
}
