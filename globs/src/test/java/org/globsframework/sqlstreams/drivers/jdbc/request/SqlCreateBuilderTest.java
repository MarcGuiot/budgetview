package org.globsframework.sqlstreams.drivers.jdbc.request;

import org.globsframework.metamodel.DummyObject;
import static org.globsframework.model.KeyBuilder.newKey;
import org.globsframework.sqlstreams.drivers.jdbc.DbServicesTestCase;
import org.globsframework.streams.accessors.utils.ValueBlobAccessor;
import org.globsframework.streams.accessors.utils.ValueIntegerAccessor;
import org.globsframework.streams.accessors.utils.ValueStringAccessor;

public class SqlCreateBuilderTest extends DbServicesTestCase {

  public void testSimpleCreate() throws Exception {
    sqlConnection.createTable(DummyObject.TYPE);
    sqlConnection.getCreateBuilder(DummyObject.TYPE)
      .set(DummyObject.ID, new ValueIntegerAccessor(1))
      .set(DummyObject.NAME, new ValueStringAccessor("hello"))
      .set(DummyObject.PASSWORD, new ValueBlobAccessor("world".getBytes()))
      .getRequest()
      .run();
    checkDb(newKey(DummyObject.TYPE, 1), DummyObject.NAME, "hello", sqlConnection);
    assertEquals("world",
                 new String((byte[])getNextValue(newKey(DummyObject.TYPE, 1), sqlConnection, DummyObject.PASSWORD)));
  }
}
