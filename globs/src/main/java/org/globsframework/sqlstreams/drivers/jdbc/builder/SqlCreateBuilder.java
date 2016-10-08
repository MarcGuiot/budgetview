package org.globsframework.sqlstreams.drivers.jdbc.builder;

import org.globsframework.metamodel.Field;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.fields.*;
import org.globsframework.sqlstreams.GlobsDatabase;
import org.globsframework.sqlstreams.SqlCreateRequest;
import org.globsframework.sqlstreams.drivers.jdbc.JdbcConnection;
import org.globsframework.sqlstreams.drivers.jdbc.impl.BlobUpdater;
import org.globsframework.sqlstreams.drivers.jdbc.request.JdbcCreateRequest;
import org.globsframework.sqlstreams.exceptions.GlobsSQLException;
import org.globsframework.streams.accessors.*;
import org.globsframework.streams.accessors.utils.*;
import org.globsframework.utils.collections.Pair;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SqlCreateBuilder implements org.globsframework.sqlstreams.SqlCreateBuilder {
  private Connection connection;
  private GlobType globType;
  private GlobsDatabase globsDB;
  private BlobUpdater blobUpdater;
  private JdbcConnection jdbcConnection;
  private List<Pair<Field, Accessor>> fields = new ArrayList<Pair<Field, Accessor>>();

  public SqlCreateBuilder(Connection connection, GlobType globType, GlobsDatabase globsDB,
                          BlobUpdater blobUpdater, JdbcConnection jdbcConnection) {
    this.connection = connection;
    this.globType = globType;
    this.globsDB = globsDB;
    this.blobUpdater = blobUpdater;
    this.jdbcConnection = jdbcConnection;
  }

  public org.globsframework.sqlstreams.SqlCreateBuilder setValue(Field field, Accessor accessor) {
    fields.add(new Pair<Field, Accessor>(field, accessor));
    return this;
  }

  public org.globsframework.sqlstreams.SqlCreateBuilder setValue(Field field, final Object value) {
    field.safeVisit(new FieldVisitor() {
      public void visitInteger(IntegerField field) throws Exception {
        setValue(field, new ValueIntegerAccessor((Integer) value));
      }

      public void visitLong(LongField field) throws Exception {
        setValue(field, new ValueLongAccessor((Long) value));
      }

      public void visitDouble(DoubleField field) throws Exception {
        setValue(field, new ValueDoubleAccessor((Double) value));
      }

      public void visitString(StringField field) throws Exception {
        setValue(field, new ValueStringAccessor((String) value));
      }

      public void visitDate(DateField field) throws Exception {
        setValue(field, new ValueDateAccessor((Date) value));
      }

      public void visitBoolean(BooleanField field) throws Exception {
        setValue(field, new ValueBooleanAccessor((Boolean) value));
      }

      public void visitTimeStamp(TimeStampField field) throws Exception {
        setValue(field, new ValueDateAccessor((Date) value));
      }

      public void visitBlob(BlobField field) throws Exception {
        setValue(field, new ValueBlobAccessor((byte[]) value));
      }

      public void visitLink(LinkField field) throws Exception {
        visitInteger(field);
      }
    });
    return this;
  }

  public org.globsframework.sqlstreams.SqlCreateBuilder set(IntegerField field, IntegerAccessor accessor) {
    return setValue(field, accessor);
  }

  public org.globsframework.sqlstreams.SqlCreateBuilder set(LongField field, LongAccessor accessor) {
    return setValue(field, accessor);
  }

  public org.globsframework.sqlstreams.SqlCreateBuilder set(StringField field, StringAccessor accessor) {
    return setValue(field, accessor);
  }

  public org.globsframework.sqlstreams.SqlCreateBuilder set(TimeStampField field, DateAccessor accessor) {
    return setValue(field, accessor);
  }

  public org.globsframework.sqlstreams.SqlCreateBuilder set(TimeStampField field, Date date) {
    return setValue(field, new ValueDateAccessor(date));
  }

  public org.globsframework.sqlstreams.SqlCreateBuilder set(BlobField field, BlobAccessor accessor) {
    return setValue(field, accessor);
  }

  public org.globsframework.sqlstreams.SqlCreateBuilder set(DateField field, DateAccessor accessor) {
    return setValue(field, accessor);
  }

  public org.globsframework.sqlstreams.SqlCreateBuilder set(BlobField field, byte[] values) {
    return setValue(field, new ValueBlobAccessor(values));
  }

  public org.globsframework.sqlstreams.SqlCreateBuilder set(StringField field, String value) {
    return setValue(field, new ValueStringAccessor(value));
  }

  public org.globsframework.sqlstreams.SqlCreateBuilder set(BooleanField field, Boolean value) {
    return setValue(field, new ValueBooleanAccessor(value));
  }

  public org.globsframework.sqlstreams.SqlCreateBuilder set(LongField field, Long value) {
    return setValue(field, new ValueLongAccessor(value));
  }

  public org.globsframework.sqlstreams.SqlCreateBuilder set(IntegerField field, Integer value) {
    return setValue(field, new ValueIntegerAccessor(value));
  }

  public org.globsframework.sqlstreams.SqlCreateBuilder set(DateField field, Date date) {
    return setValue(field, new ValueDateAccessor(date));
  }

  public org.globsframework.sqlstreams.SqlCreateBuilder setAll(GlobAccessor accessor) {
    for (Field field : globType.getFields()) {
      fields.add(new Pair<Field, Accessor>(field, new GlobFieldAccessor(field, accessor)));
    }
    return this;
  }

  public SqlCreateRequest getRequest() {
    return new JdbcCreateRequest(fields, connection, globType, globsDB, blobUpdater, jdbcConnection);
  }

  public void run() throws GlobsSQLException {
    getRequest().execute();
  }
}
