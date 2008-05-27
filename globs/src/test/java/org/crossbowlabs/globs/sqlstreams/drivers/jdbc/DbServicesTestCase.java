package org.crossbowlabs.globs.sqlstreams.drivers.jdbc;

import org.crossbowlabs.globs.metamodel.*;
import org.crossbowlabs.globs.metamodel.utils.DefaultGlobModel;
import org.crossbowlabs.globs.model.Key;
import org.crossbowlabs.globs.sqlstreams.*;
import org.crossbowlabs.globs.sqlstreams.constraints.Constraint;
import org.crossbowlabs.globs.sqlstreams.constraints.Constraints;
import org.crossbowlabs.globs.sqlstreams.utils.MultiCreateBuilder;
import org.crossbowlabs.globs.streams.GlobStream;
import org.crossbowlabs.globs.streams.accessors.Accessor;
import org.crossbowlabs.globs.utils.ServicesTestCase;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public abstract class DbServicesTestCase extends ServicesTestCase {
  protected JdbcSqlService sqlService;
  protected SqlConnection sqlConnection;
  protected DefaultGlobModel globModel;

  protected void setUp() throws Exception {
    super.setUp();
    globModel = new DefaultGlobModel(DummyObject.TYPE, DummyObject2.TYPE);
    sqlConnection = initDb();
    sqlConnection.createTable(DummyObject.TYPE);
    sqlConnection.createTable(DummyObject2.TYPE);
    emptyTable();
  }

  protected void tearDown() throws Exception {
    super.tearDown();
    emptyTable();
    sqlConnection = null;
  }

  private void emptyTable() {
    sqlConnection.emptyTable(DummyObject.TYPE);
    sqlConnection.emptyTable(DummyObject2.TYPE);
    sqlConnection.commit();
  }

  private SqlConnection initDb() {

    directory.add(GlobModel.class, globModel);

    sqlService = new JdbcSqlService("jdbc:hsqldb:.", "sa", "");
//    sqlService = new JdbcDriverBasedSqlService("jdbc:mysql://Plone/test", "sa", "");
    directory.add(SqlService.class, sqlService);
    return sqlService.getDb();
  }

  protected void checkDb(Key key, Field field, Object value, SqlConnection sqlConnection) {
    assertEquals(value, getNextValue(key, sqlConnection, field));
  }

  public Object getNextValue(Key key, SqlConnection sqlConnection, Field field) {
    GlobType globType = key.getGlobType();
    Constraint constraint = null;

    for (Field keyField : globType.getKeyFields()) {
      constraint = Constraints.and(constraint, Constraints.equalsObject(keyField, key.getValue(keyField)));
    }
    SelectBuilder queryBuilder = sqlConnection.getQueryBuilder(key.getGlobType(), constraint);
    Accessor accessor = queryBuilder.retrieveUnTyped(field);
    GlobStream globStream = queryBuilder.getQuery().execute();
    assertTrue(globStream.next());
    return accessor.getObjectValue();
  }

  protected void populate(SqlConnection connection, GlobStream stream) {
    Set<GlobType> types = new HashSet<GlobType>();
    for (Field field : stream.getFields()) {
      types.add(field.getGlobType());
    }
    CreateBuilder createBuilder = new MultiCreateBuilder(connection, types);
    Collection<Field> fields = stream.getFields();
    for (Field field : fields) {
      createBuilder.setObject(field, stream.getAccessor(field));
    }
    SqlRequest request = createBuilder.getRequest();
    while (stream.next()) {
      request.run();
    }
    connection.commit();
  }

}
