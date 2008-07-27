package org.globsframework.sqlstreams.drivers.jdbc.request;

import org.globsframework.metamodel.Field;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.fields.*;
import org.globsframework.sqlstreams.CreateBuilder;
import org.globsframework.sqlstreams.SqlRequest;
import org.globsframework.sqlstreams.SqlService;
import org.globsframework.sqlstreams.accessors.LongGeneratedKeyAccessor;
import org.globsframework.sqlstreams.drivers.jdbc.BlobUpdater;
import org.globsframework.sqlstreams.drivers.jdbc.JdbcConnection;
import org.globsframework.sqlstreams.drivers.jdbc.SqlCreateRequest;
import org.globsframework.streams.accessors.*;
import org.globsframework.streams.accessors.utils.*;
import org.globsframework.utils.Pair;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SqlCreateBuilder implements CreateBuilder {
  private Connection connection;
  private GlobType globType;
  private SqlService sqlService;
  private BlobUpdater blobUpdater;
  private JdbcConnection jdbcConnection;
  private List<Pair<Field, Accessor>> fields = new ArrayList<Pair<Field, Accessor>>();
  protected LongGeneratedKeyAccessor longGeneratedKeyAccessor;

  public SqlCreateBuilder(Connection connection, GlobType globType, SqlService sqlService,
                          BlobUpdater blobUpdater, JdbcConnection jdbcConnection) {
    this.connection = connection;
    this.globType = globType;
    this.sqlService = sqlService;
    this.blobUpdater = blobUpdater;
    this.jdbcConnection = jdbcConnection;
  }

  public CreateBuilder setObject(Field field, Accessor accessor) {
    fields.add(new Pair<Field, Accessor>(field, accessor));
    return this;
  }

  public CreateBuilder setObject(Field field, final Object value) {
    field.safeVisit(new FieldVisitor() {
      public void visitInteger(IntegerField field) throws Exception {
        setObject(field, new ValueIntegerAccessor((Integer)value));
      }

      public void visitLong(LongField field) throws Exception {
        setObject(field, new ValueLongAccessor((Long)value));
      }

      public void visitDouble(DoubleField field) throws Exception {
        setObject(field, new ValueDoubleAccessor((Double)value));
      }

      public void visitString(StringField field) throws Exception {
        setObject(field, new ValueStringAccessor((String)value));
      }

      public void visitDate(DateField field) throws Exception {
        setObject(field, new ValueDateAccessor((Date)value));
      }

      public void visitBoolean(BooleanField field) throws Exception {
        setObject(field, new ValueBooleanAccessor((Boolean)value));
      }

      public void visitTimeStamp(TimeStampField field) throws Exception {
        setObject(field, new ValueDateAccessor((Date)value));
      }

      public void visitBlob(BlobField field) throws Exception {
        setObject(field, new ValueBlobAccessor((byte[])value));
      }

      public void visitLink(LinkField field) throws Exception {
        visitInteger(field);
      }
    });
    return this;
  }

  public CreateBuilder set(IntegerField field, IntegerAccessor accessor) {
    return setObject(field, accessor);
  }

  public CreateBuilder set(LongField field, LongAccessor accessor) {
    return setObject(field, accessor);
  }

  public CreateBuilder set(StringField field, StringAccessor accessor) {
    return setObject(field, accessor);
  }

  public CreateBuilder set(TimeStampField field, DateAccessor accessor) {
    return setObject(field, accessor);
  }

  public CreateBuilder set(TimeStampField field, Date date) {
    return setObject(field, new ValueDateAccessor(date));
  }

  public CreateBuilder set(BlobField field, BlobAccessor accessor) {
    return setObject(field, accessor);
  }

  public CreateBuilder set(DateField field, DateAccessor accessor) {
    return setObject(field, accessor);
  }

  public CreateBuilder set(BlobField field, byte[] values) {
    return setObject(field, new ValueBlobAccessor(values));
  }

  public CreateBuilder set(StringField field, String value) {
    return setObject(field, new ValueStringAccessor(value));
  }

  public CreateBuilder set(LongField field, Long value) {
    return setObject(field, new ValueLongAccessor(value));
  }

  public CreateBuilder set(IntegerField field, Integer value) {
    return setObject(field, new ValueIntegerAccessor(value));
  }

  public CreateBuilder set(DateField field, Date date) {
    return setObject(field, new ValueDateAccessor(date));
  }

  public LongAccessor getKeyGeneratedAccessor() {
    if (longGeneratedKeyAccessor == null) {
      longGeneratedKeyAccessor = new LongGeneratedKeyAccessor();
    }
    return longGeneratedKeyAccessor;
  }

  public SqlRequest getRequest() {
    return new SqlCreateRequest(fields, longGeneratedKeyAccessor, connection, globType, sqlService, blobUpdater, jdbcConnection);
  }
}
