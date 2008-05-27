package org.crossbowlabs.globs.sqlstreams.drivers.jdbc.request;

import org.crossbowlabs.globs.metamodel.DummyObject;
import static org.crossbowlabs.globs.model.KeyBuilder.newKey;
import org.crossbowlabs.globs.sqlstreams.drivers.jdbc.DbServicesTestCase;
import org.crossbowlabs.globs.streams.accessors.utils.ValueBlobAccessor;
import org.crossbowlabs.globs.streams.accessors.utils.ValueIntegerAccessor;
import org.crossbowlabs.globs.streams.accessors.utils.ValueStringAccessor;

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
