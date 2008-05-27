package org.crossbowlabs.globs.sqlstreams.drivers.jdbc.request;

import org.crossbowlabs.globs.metamodel.Field;
import org.crossbowlabs.globs.metamodel.GlobType;
import org.crossbowlabs.globs.metamodel.fields.*;
import org.crossbowlabs.globs.sqlstreams.CreateBuilder;
import org.crossbowlabs.globs.sqlstreams.SqlRequest;
import org.crossbowlabs.globs.sqlstreams.SqlService;
import org.crossbowlabs.globs.sqlstreams.accessors.LongGeneratedKeyAccessor;
import org.crossbowlabs.globs.sqlstreams.drivers.jdbc.BlobUpdater;
import org.crossbowlabs.globs.sqlstreams.drivers.jdbc.JdbcConnection;
import org.crossbowlabs.globs.sqlstreams.drivers.jdbc.SqlCreateRequest;
import org.crossbowlabs.globs.streams.accessors.*;
import org.crossbowlabs.globs.streams.accessors.utils.*;
import org.crossbowlabs.globs.utils.Pair;

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

  public CreateBuilder set(BlobField field, BlobAccessor accessor) {
    return setObject(field, accessor);
  }

  public CreateBuilder set(BlobField field, byte[] values) {
    setObject(field, new ValueBlobAccessor(values));
    return this;
  }

  public CreateBuilder set(StringField field, String value) {
    setObject(field, new ValueStringAccessor(value));
    return this;
  }

  public CreateBuilder set(LongField field, Long value) {
    setObject(field, new ValueLongAccessor(value));
    return this;
  }

  public CreateBuilder set(IntegerField field, Integer value) {
    setObject(field, new ValueIntegerAccessor(value));
    return this;
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
