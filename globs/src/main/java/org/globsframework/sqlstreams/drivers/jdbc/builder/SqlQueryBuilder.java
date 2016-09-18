package org.globsframework.sqlstreams.drivers.jdbc.builder;

import org.globsframework.metamodel.Field;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.fields.*;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.sqlstreams.GlobsDatabase;
import org.globsframework.sqlstreams.SqlSelect;
import org.globsframework.sqlstreams.SqlSelectBuilder;
import org.globsframework.sqlstreams.accessors.*;
import org.globsframework.sqlstreams.constraints.Constraint;
import org.globsframework.sqlstreams.drivers.jdbc.impl.BlobUpdater;
import org.globsframework.sqlstreams.drivers.jdbc.impl.FieldToSqlAccessorVisitor;
import org.globsframework.sqlstreams.drivers.jdbc.select.SqlSelectQuery;
import org.globsframework.streams.accessors.*;
import org.globsframework.streams.accessors.utils.AbstractGlobAccessor;
import org.globsframework.utils.Ref;
import org.globsframework.utils.exceptions.ItemNotFound;
import org.globsframework.utils.exceptions.TooManyItems;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

public class SqlQueryBuilder implements SqlSelectBuilder {
  private Connection connection;
  private GlobType globType;
  private Constraint constraint;
  private GlobsDatabase globsDB;
  private BlobUpdater blobUpdater;
  private boolean autoClose = true;
  private Field orderByField;
  private Map<Field, SqlAccessor> fieldToAccessor = new HashMap<Field, SqlAccessor>();

  public SqlQueryBuilder(Connection connection, GlobType globType, Constraint constraint, GlobsDatabase globsDB, BlobUpdater blobUpdater) {
    this.connection = connection;
    this.globType = globType;
    this.constraint = constraint;
    this.globsDB = globsDB;
    this.blobUpdater = blobUpdater;
  }

  public SqlSelect getQuery() {
    try {
      completeWithKeys();
      return new SqlSelectQuery(connection, constraint, fieldToAccessor, globsDB, blobUpdater, orderByField, autoClose);
    }
    finally {
      fieldToAccessor.clear();
    }
  }

  public SqlSelect getNotAutoCloseQuery() {
    autoClose = false;
    return getQuery();
  }

  public GlobList getList() {
    return getQuery().getList();
  }

  public Glob getUnique() throws ItemNotFound, TooManyItems {
    return getQuery().getUnique();
  }

  private void completeWithKeys() {
    for (Field field : globType.getKeyFields()) {
      if (!fieldToAccessor.containsKey(field)) {
        select(field);
      }
    }
  }

  public SqlSelectBuilder select(Field field) {
    FieldToSqlAccessorVisitor visitor = new FieldToSqlAccessorVisitor();
    field.safeVisit(visitor);
    fieldToAccessor.put(field, visitor.getAccessor());
    return this;
  }

  public SqlSelectBuilder selectAll() {
    for (Field field : globType.getFields()) {
      select(field);
    }
    return this;
  }

  public SqlSelectBuilder select(DateField field, Ref<DateAccessor> ref) {
    return createAccessor(field, ref, new DateSqlAccessor());
  }

  public SqlSelectBuilder select(IntegerField field, Ref<IntegerAccessor> ref) {
    return createAccessor(field, ref, new IntegerSqlAccessor());
  }

  public SqlSelectBuilder select(LongField field, Ref<LongAccessor> accessor) {
    return createAccessor(field, accessor, new LongSqlAccessor());
  }

  public SqlSelectBuilder select(BooleanField field, Ref<BooleanAccessor> ref) {
    return createAccessor(field, ref, new BooleanSqlAccessor());
  }

  public SqlSelectBuilder select(StringField field, Ref<StringAccessor> ref) {
    return createAccessor(field, ref, new StringSqlAccessor());
  }

  public SqlSelectBuilder select(DoubleField field, Ref<DoubleAccessor> ref) {
    return createAccessor(field, ref, new DoubleSqlAccessor());
  }

  public SqlSelectBuilder select(TimeStampField field, Ref<TimestampAccessor> ref) {
    return createAccessor(field, ref, new TimestampSqlAccessor());
  }

  public SqlSelectBuilder select(BlobField field, Ref<BlobAccessor> accessor) {
    return createAccessor(field, accessor, new BlobSqlAccessor());
  }

  public DateAccessor retrieve(DateField field) {
    DateSqlAccessor accessor = new DateSqlAccessor();
    fieldToAccessor.put(field, accessor);
    return accessor;
  }

  public TimestampSqlAccessor retrieve(TimeStampField field) {
    TimestampSqlAccessor accessor = new TimestampSqlAccessor();
    fieldToAccessor.put(field, accessor);
    return accessor;
  }

  public BooleanAccessor retrieve(BooleanField field) {
    BooleanSqlAccessor accessor = new BooleanSqlAccessor();
    fieldToAccessor.put(field, accessor);
    return accessor;
  }

  public IntegerAccessor retrieve(IntegerField field) {
    IntegerSqlAccessor accessor = new IntegerSqlAccessor();
    fieldToAccessor.put(field, accessor);
    return accessor;
  }

  public LongAccessor retrieve(LongField field) {
    LongSqlAccessor accessor = new LongSqlAccessor();
    fieldToAccessor.put(field, accessor);
    return accessor;
  }

  public StringAccessor retrieve(StringField field) {
    StringSqlAccessor accessor = new StringSqlAccessor();
    fieldToAccessor.put(field, accessor);
    return accessor;
  }

  public DoubleAccessor retrieve(DoubleField field) {
    DoubleSqlAccessor accessor = new DoubleSqlAccessor();
    fieldToAccessor.put(field, accessor);
    return accessor;
  }

  public BlobSqlAccessor retrieve(BlobField field) {
    BlobSqlAccessor accessor = new BlobSqlAccessor();
    fieldToAccessor.put(field, accessor);
    return accessor;
  }

  public Accessor retrieveValue(Field field) {
    AccessorToFieldVisitor visitor = new AccessorToFieldVisitor();
    field.safeVisit(visitor);
    return visitor.get();
  }

  public CompleteGlobAccessor retrieveAll() {
    CompleteGlobAccessor globAccessor = new CompleteGlobAccessor();
    for (Field field : globType.getFields()) {
      Accessor accessor = retrieveValue(field);
      globAccessor.set(field, accessor);
    }
    return globAccessor;
  }

  public SqlSelectBuilder orderBy(IntegerField field) {
    this.orderByField = field;
    return this;
  }

  public SqlSelectBuilder orderBy(DateField field) {
    this.orderByField = field;
    return this;
  }

  public SqlSelectBuilder orderBy(TimeStampField field) {
    this.orderByField = field;
    return this;
  }

  private class CompleteGlobAccessor extends AbstractGlobAccessor implements GlobAccessor {

    private Accessor[] accessors;

    public CompleteGlobAccessor() {
      super(globType);
      accessors = new Accessor[globType.getFieldCount()];
    }

    private void set(Field field, Accessor accessor) {
      accessors[field.getIndex()] = accessor;
    }

    protected Object doGet(Field field) {
      if (!field.getGlobType().equals(globType)) {
        throw new ItemNotFound("Field '" + field.getName() + "' is declared for type '" +
                               field.getGlobType().getName() + "' and not for '" + globType.getName() + "'");
      }
      return accessors[field.getIndex()].getObjectValue();
    }
  }

  private <T extends Accessor, D extends T> SqlSelectBuilder createAccessor(Field field, Ref<T> ref, D accessor) {
    ref.set(accessor);
    fieldToAccessor.put(field, (SqlAccessor) accessor);
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

