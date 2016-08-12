package org.globsframework.sqlstreams.drivers.jdbc.builder;

import org.globsframework.metamodel.Field;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.fields.*;
import org.globsframework.sqlstreams.GlobsDatabase;
import org.globsframework.sqlstreams.SqlRequest;
import org.globsframework.sqlstreams.constraints.Constraint;
import org.globsframework.sqlstreams.drivers.jdbc.impl.BlobUpdater;
import org.globsframework.sqlstreams.drivers.jdbc.request.JdbcUpdateRequest;
import org.globsframework.sqlstreams.exceptions.GlobsSQLException;
import org.globsframework.streams.accessors.*;
import org.globsframework.streams.accessors.utils.*;
import org.globsframework.utils.exceptions.InvalidState;

import java.sql.Connection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class SqlUpdateBuilder implements org.globsframework.sqlstreams.SqlUpdateBuilder {
  private Map<Field, Accessor> accessors = new HashMap<Field, Accessor>();
  private Connection connection;
  private GlobType globType;
  private GlobsDatabase db;
  private Constraint constraint;
  private BlobUpdater blobUpdater;

  public SqlUpdateBuilder(Connection connection, GlobType globType, GlobsDatabase db,
                          Constraint constraint, BlobUpdater blobUpdater) {
    this.blobUpdater = blobUpdater;
    this.connection = connection;
    this.globType = globType;
    this.db = db;
    this.constraint = constraint;
  }

  public org.globsframework.sqlstreams.SqlUpdateBuilder setAll(GlobAccessor accessor) {
    for (Field field : globType.getFields()) {
      accessors.put(field, new GlobFieldAccessor(field, accessor));
    }
    return this;
  }

  public org.globsframework.sqlstreams.SqlUpdateBuilder setValue(Field field, final Object value) {
    field.safeVisit(new FieldVisitor() {
      public void visitInteger(IntegerField field) throws Exception {
        set(field, (Integer)value);
      }

      public void visitLong(LongField field) throws Exception {
        set(field, (Long)value);
      }

      public void visitDouble(DoubleField field) throws Exception {
        set(field, (Double)value);
      }

      public void visitString(StringField field) throws Exception {
        set(field, (String)value);
      }

      public void visitDate(DateField field) throws Exception {
        set(field, (Date)value);
      }

      public void visitBoolean(BooleanField field) throws Exception {
        set(field, (Boolean)value);
      }

      public void visitTimeStamp(TimeStampField field) throws Exception {
        set(field, (Date)value);
      }

      public void visitBlob(BlobField field) throws Exception {
        set(field, (byte[])value);
      }

      public void visitLink(LinkField field) throws Exception {
        set(field, (Integer)value);
      }
    });
    return this;
  }

  public org.globsframework.sqlstreams.SqlUpdateBuilder setValue(Field field, Accessor accessor) {
    accessors.put(field, accessor);
    return this;
  }

  public org.globsframework.sqlstreams.SqlUpdateBuilder set(IntegerField field, IntegerAccessor accessor) {
    accessors.put(field, accessor);
    return this;
  }

  public org.globsframework.sqlstreams.SqlUpdateBuilder set(IntegerField field, Integer value) {
    return set(field, new ValueIntegerAccessor(value));
  }

  public org.globsframework.sqlstreams.SqlUpdateBuilder set(LongField field, LongAccessor accessor) {
    accessors.put(field, accessor);
    return this;
  }

  public org.globsframework.sqlstreams.SqlUpdateBuilder set(LongField field, Long value) {
    return set(field, new ValueLongAccessor(value));
  }

  public org.globsframework.sqlstreams.SqlUpdateBuilder set(DoubleField field, DoubleAccessor accessor) {
    accessors.put(field, accessor);
    return this;
  }

  public org.globsframework.sqlstreams.SqlUpdateBuilder set(DoubleField field, Double value) {
    return set(field, new ValueDoubleAccessor(value));
  }

  public org.globsframework.sqlstreams.SqlUpdateBuilder set(DateField field, DateAccessor accessor) {
    accessors.put(field, accessor);
    return this;
  }

  public org.globsframework.sqlstreams.SqlUpdateBuilder set(DateField field, Date value) {
    return set(field, new ValueDateAccessor(value));
  }

  public org.globsframework.sqlstreams.SqlUpdateBuilder set(TimeStampField field, Date value) {
    return set(field, new ValueDateAccessor(value));
  }

  public org.globsframework.sqlstreams.SqlUpdateBuilder set(TimeStampField field, DateAccessor accessor) {
    accessors.put(field, accessor);
    return this;
  }

  public org.globsframework.sqlstreams.SqlUpdateBuilder set(StringField field, StringAccessor accessor) {
    accessors.put(field, accessor);
    return this;
  }

  public org.globsframework.sqlstreams.SqlUpdateBuilder set(StringField field, String value) {
    return set(field, new ValueStringAccessor(value));
  }

  public org.globsframework.sqlstreams.SqlUpdateBuilder set(BooleanField field, BooleanAccessor accessor) {
    accessors.put(field, accessor);
    return this;
  }

  public org.globsframework.sqlstreams.SqlUpdateBuilder set(BooleanField field, Boolean value) {
    return set(field, new ValueBooleanAccessor(value));
  }

  public org.globsframework.sqlstreams.SqlUpdateBuilder set(BlobField field, byte[] value) {
    return set(field, new ValueBlobAccessor(value));
  }

  public org.globsframework.sqlstreams.SqlUpdateBuilder set(BlobField field, BlobAccessor accessor) {
    accessors.put(field, accessor);
    return this;
  }

  public org.globsframework.sqlstreams.SqlUpdateBuilder set(LinkField field, IntegerAccessor accessor) {
    accessors.put(field, accessor);
    return this;
  }

  public org.globsframework.sqlstreams.SqlUpdateBuilder set(LinkField field, Integer value) {
    return set(field, new ValueIntegerAccessor(value));
  }

  public SqlRequest getRequest() throws InvalidState {
    try {
      return new JdbcUpdateRequest(globType, constraint, accessors, connection, db, blobUpdater);
    }
    finally {
      accessors.clear();
    }
  }

  public void run() throws GlobsSQLException {
    getRequest().execute();
  }
}
