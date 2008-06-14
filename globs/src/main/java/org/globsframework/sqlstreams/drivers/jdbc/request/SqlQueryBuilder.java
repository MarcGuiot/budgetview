package org.globsframework.sqlstreams.drivers.jdbc.request;

import org.globsframework.metamodel.Field;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.fields.*;
import org.globsframework.sqlstreams.SelectBuilder;
import org.globsframework.sqlstreams.SelectQuery;
import org.globsframework.sqlstreams.SqlService;
import org.globsframework.sqlstreams.accessors.*;
import org.globsframework.sqlstreams.constraints.Constraint;
import org.globsframework.sqlstreams.drivers.jdbc.BlobUpdater;
import org.globsframework.sqlstreams.drivers.jdbc.SqlSelectQuery;
import org.globsframework.sqlstreams.drivers.jdbc.impl.FieldToSqlAccessorVisitor;
import org.globsframework.streams.accessors.*;
import org.globsframework.utils.Ref;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

public class SqlQueryBuilder implements SelectBuilder {
  private Connection connection;
  private GlobType globType;
  private Constraint constraint;
  private SqlService sqlService;
  private BlobUpdater blobUpdater;
  private Map<Field, SqlAccessor> fieldToAccessorHolder = new HashMap<Field, SqlAccessor>();

  public SqlQueryBuilder(Connection connection, GlobType globType, Constraint constraint, SqlService sqlService, BlobUpdater blobUpdater) {
    this.connection = connection;
    this.globType = globType;
    this.constraint = constraint;
    this.sqlService = sqlService;
    this.blobUpdater = blobUpdater;
  }

  public SelectQuery getQuery() {
    try {
      completeWithKeys();
      return new SqlSelectQuery(connection, constraint, fieldToAccessorHolder, sqlService, blobUpdater);
    }
    finally {
      fieldToAccessorHolder.clear();
    }
  }

  private void completeWithKeys() {
    for (Field field : globType.getKeyFields()) {
      if (!fieldToAccessorHolder.containsKey(field)) {
        select(field);
      }
    }
  }

  public SelectBuilder select(Field field) {
    FieldToSqlAccessorVisitor visitor = new FieldToSqlAccessorVisitor();
    field.safeVisit(visitor);
    fieldToAccessorHolder.put(field, visitor.getAccessor());
    return this;
  }

  public SelectBuilder selectAll() {
    for (Field field : globType.getFields()) {
      select(field);
    }
    return this;
  }

  public SelectBuilder select(DateField field, Ref<DateAccessor> ref) {
    return createAccessor(field, ref, new DateSqlAccessor());
  }

  public SelectBuilder select(IntegerField field, Ref<IntegerAccessor> ref) {
    return createAccessor(field, ref, new IntegerSqlAccessor());
  }

  public SelectBuilder select(LongField field, Ref<LongAccessor> accessor) {
    return createAccessor(field, accessor, new LongSqlAccessor());
  }

  public SelectBuilder select(BooleanField field, Ref<BooleanAccessor> ref) {
    return createAccessor(field, ref, new BooleanSqlAccessor());
  }

  public SelectBuilder select(StringField field, Ref<StringAccessor> ref) {
    return createAccessor(field, ref, new StringSqlAccessor());
  }

  public SelectBuilder select(DoubleField field, Ref<DoubleAccessor> ref) {
    return createAccessor(field, ref, new DoubleSqlAccessor());
  }

  public SelectBuilder select(TimeStampField field, Ref<TimestampAccessor> ref) {
    return createAccessor(field, ref, new TimestampSqlAccessor());
  }

  public SelectBuilder select(BlobField field, Ref<BlobAccessor> accessor) {
    return createAccessor(field, accessor, new BlobSqlAccessor());
  }

  public DateAccessor retrieve(DateField field) {
    DateSqlAccessor accessor = new DateSqlAccessor();
    fieldToAccessorHolder.put(field, accessor);
    return accessor;
  }

  public TimestampSqlAccessor retrieve(TimeStampField field) {
    TimestampSqlAccessor accessor = new TimestampSqlAccessor();
    fieldToAccessorHolder.put(field, accessor);
    return accessor;
  }

  public BooleanAccessor retrieve(BooleanField field) {
    BooleanSqlAccessor accessor = new BooleanSqlAccessor();
    fieldToAccessorHolder.put(field, accessor);
    return accessor;
  }

  public IntegerAccessor retrieve(IntegerField field) {
    IntegerSqlAccessor accessor = new IntegerSqlAccessor();
    fieldToAccessorHolder.put(field, accessor);
    return accessor;
  }

  public LongAccessor retrieve(LongField field) {
    LongSqlAccessor accessor = new LongSqlAccessor();
    fieldToAccessorHolder.put(field, accessor);
    return accessor;
  }

  public StringAccessor retrieve(StringField field) {
    StringSqlAccessor accessor = new StringSqlAccessor();
    fieldToAccessorHolder.put(field, accessor);
    return accessor;
  }

  public DoubleAccessor retrieve(DoubleField field) {
    DoubleSqlAccessor accessor = new DoubleSqlAccessor();
    fieldToAccessorHolder.put(field, accessor);
    return accessor;
  }

  public BlobSqlAccessor retrieve(BlobField field) {
    BlobSqlAccessor accessor = new BlobSqlAccessor();
    fieldToAccessorHolder.put(field, accessor);
    return accessor;
  }

  public Accessor retrieveUnTyped(Field field) {
    AccessorToFieldVisitor visitor = new AccessorToFieldVisitor();
    field.safeVisit(visitor);
    return visitor.get();
  }

  private <T extends Accessor, D extends T> SelectBuilder createAccessor(Field field, Ref<T> ref, D accessor) {
    ref.set(accessor);
    fieldToAccessorHolder.put(field, (SqlAccessor)accessor);
    return this;
  }

  private class AccessorToFieldVisitor implements FieldVisitor {
    private Accessor accessor;

    public AccessorToFieldVisitor() {
    }

    public void visitInteger(IntegerField field) throws Exception {
      accessor = retrieve(field);
    }

    public void visitDouble(DoubleField field) throws Exception {
      accessor = retrieve(field);
    }

    public void visitString(StringField field) throws Exception {
      accessor = retrieve(field);
    }

    public void visitDate(DateField field) throws Exception {
      accessor = retrieve(field);
    }

    public void visitBoolean(BooleanField field) throws Exception {
      accessor = retrieve(field);
    }

    public void visitTimeStamp(TimeStampField field) throws Exception {
      accessor = retrieve(field);
    }

    public void visitBlob(BlobField field) throws Exception {
      accessor = retrieve(field);
    }

    public void visitLong(LongField field) throws Exception {
      accessor = retrieve(field);
    }

    public void visitLink(LinkField field) throws Exception {
      accessor = retrieve(field);
    }

    public Accessor get() {
      return accessor;
    }
  }
}

