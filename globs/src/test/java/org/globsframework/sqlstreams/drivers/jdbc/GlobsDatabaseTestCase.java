package org.globsframework.sqlstreams.drivers.jdbc;

import org.globsframework.metamodel.*;
import org.globsframework.metamodel.utils.DefaultGlobModel;
import org.globsframework.model.Key;
import org.globsframework.sqlstreams.*;
import org.globsframework.sqlstreams.constraints.Constraint;
import org.globsframework.sqlstreams.constraints.Where;
import org.globsframework.sqlstreams.utils.MultiCreateBuilder;
import org.globsframework.streams.GlobStream;
import org.globsframework.streams.accessors.Accessor;
import org.globsframework.utils.ServicesTestCase;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public abstract class GlobsDatabaseTestCase extends ServicesTestCase {
  protected JdbcGlobsDatabase sqlService;
  protected SqlConnection sqlConnection;
  protected DefaultGlobModel globModel;

  protected void setUp() throws Exception {
    super.setUp();
    globModel = new DefaultGlobModel(DummyObject.TYPE, DummyObject2.TYPE);
    sqlConnection = initDbConnection();
    sqlConnection.createTables(DummyObject.TYPE, DummyObject2.TYPE);
    emptyTable();
  }

  protected void tearDown() throws Exception {
    super.tearDown();
    emptyTable();
    sqlConnection = null;
  }

  private void emptyTable() {
    sqlConnection.emptyTable(DummyObject.TYPE, DummyObject2.TYPE);
    sqlConnection.commit();
  }

  private SqlConnection initDbConnection() {

    directory.add(GlobModel.class, globModel);

    sqlService = new JdbcGlobsDatabase("jdbc:hsqldb:.", "sa", "");
    directory.add(GlobsDatabase.class, sqlService);
    return sqlService.connect();
  }

  protected void checkDb(Key key, Field field, Object value, SqlConnection sqlConnection) {
    assertEquals(value, getNextValue(key, sqlConnection, field));
  }

  public Object getNextValue(Key key, SqlConnection sqlConnection, Field field) {
    GlobType globType = key.getGlobType();
    Constraint constraint = null;

    for (Field keyField : globType.getKeyFields()) {
      constraint = Where.and(constraint, Where.fieldEqualsValue(keyField, key.getValue(keyField)));
    }
    SqlSelectBuilder queryBuilder = sqlConnection.startSelect(key.getGlobType(), constraint);
    Accessor accessor = queryBuilder.retrieveUnTyped(field);
    GlobStream globStream = queryBuilder.getQuery().getStream();
    assertTrue(globStream.next());
    return accessor.getObjectValue();
  }

  protected void populate(SqlConnection connection, GlobStream stream) {
    Set<GlobType> types = new HashSet<GlobType>();
    for (Field field : stream.getFields()) {
      types.add(field.getGlobType());
    }
    MultiCreateBuilder createBuilder = new MultiCreateBuilder(connection, types);
    Collection<Field> fields = stream.getFields();
    for (Field field : fields) {
      createBuilder.setValue(field, stream.getAccessor(field));
    }
    SqlRequest request = createBuilder.getRequest();
    while (stream.next()) {
      request.execute();
    }
    connection.commit();
  }

}
