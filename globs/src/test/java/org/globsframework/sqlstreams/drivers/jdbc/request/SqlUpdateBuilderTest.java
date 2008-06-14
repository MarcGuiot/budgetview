package org.globsframework.sqlstreams.drivers.jdbc.request;

import org.globsframework.metamodel.DummyObject;
import org.globsframework.model.Key;
import org.globsframework.model.KeyBuilder;
import static org.globsframework.model.KeyBuilder.newKey;
import org.globsframework.sqlstreams.SqlRequest;
import org.globsframework.sqlstreams.UpdateBuilder;
import org.globsframework.sqlstreams.constraints.Constraints;
import org.globsframework.sqlstreams.constraints.impl.KeyConstraint;
import org.globsframework.sqlstreams.drivers.jdbc.DbServicesTestCase;
import org.globsframework.streams.GlobStream;
import org.globsframework.streams.accessors.utils.ValueDateAccessor;
import org.globsframework.streams.accessors.utils.ValueDoubleAccessor;
import org.globsframework.streams.accessors.utils.ValueIntegerAccessor;
import org.globsframework.streams.xml.XmlGlobStreamReader;
import org.globsframework.utils.Dates;

import java.util.Date;

public class SqlUpdateBuilderTest extends DbServicesTestCase {

  public void testUpdate() throws Exception {
    GlobStream streamToWrite =
      XmlGlobStreamReader.parse(directory,
                                "<dummyObject id='1' name='hello' value='1.1' present='true' password='zerzer'/>" +
                                "<dummyObject id='2' name='hello' value='0.0' present='true' password='gzsefd'/>");
    populate(sqlConnection, streamToWrite);

    ValueIntegerAccessor keyValue = new ValueIntegerAccessor();
    UpdateBuilder updateBuilder = sqlConnection.getUpdateBuilder(DummyObject.TYPE, Constraints.equal(DummyObject.ID, keyValue));

    Date date = Dates.parse("2000/01/01");
    updateBuilder.update(DummyObject.DATE, date);

    Date timestamp = Dates.parseTimestamp("03/10/2002 12:34:20");
    updateBuilder.update(DummyObject.TIMESTAMP, new ValueDateAccessor(timestamp));

    ValueDoubleAccessor valueAccessor = new ValueDoubleAccessor(2.2);
    updateBuilder.updateUntyped(DummyObject.VALUE, valueAccessor);
    updateBuilder.updateUntyped(DummyObject.PASSWORD, "some blog".getBytes());

    SqlRequest updateRequest = updateBuilder.getRequest();
    Key key1 = newKey(DummyObject.TYPE, 1);

    keyValue.setValue(1);
    updateRequest.run();
    checkDb(key1, DummyObject.DATE, date, sqlConnection);
    checkDb(key1, DummyObject.VALUE, 2.2, sqlConnection);
    assertEquals(new String((byte[])getNextValue(key1, sqlConnection, DummyObject.PASSWORD)), "some blog");

    valueAccessor.setValue(3.3);
    updateRequest.run();
    checkDb(key1, DummyObject.VALUE, 3.3, sqlConnection);

    keyValue.setValue(2);
    Key key2 = newKey(DummyObject.TYPE, 2);
    updateRequest.run();
    checkDb(key2, DummyObject.VALUE, 3.3, sqlConnection);
  }

  public void testUpdateWithKey() throws Exception {
    GlobStream streamToWrite =
      XmlGlobStreamReader.parse(directory,
                                "<dummyObject id='1' name='hello' value='1.1' present='true'/>" +
                                "<dummyObject id='2' name='hello' value='0.0' present='true'/>");
    populate(sqlConnection, streamToWrite);

    Key key1 = KeyBuilder.init(DummyObject.ID, 2).get();
    KeyConstraint keyAccessor = new KeyConstraint(DummyObject.TYPE);
    keyAccessor.setValue(key1);
    sqlConnection.getUpdateBuilder(DummyObject.TYPE, Constraints.keyEquals(keyAccessor))
      .update(DummyObject.NAME, "world")
      .getRequest()
      .run();
    checkDb(key1, DummyObject.NAME, "world", sqlConnection);
  }
}
