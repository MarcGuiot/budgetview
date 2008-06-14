package org.globsframework.sqlstreams.drivers.jdbc.impl;

import org.globsframework.metamodel.fields.*;
import org.globsframework.sqlstreams.accessors.*;

public class FieldToSqlAccessorVisitor implements FieldVisitor {
  private SqlAccessor accessor;

  public SqlAccessor getAccessor() {
    return accessor;
  }

  public void visitInteger(IntegerField field) throws Exception {
    accessor = new IntegerSqlAccessor();
  }

  public void visitLong(LongField field) throws Exception {
    accessor = new LongSqlAccessor();
  }

  public void visitDouble(DoubleField field) throws Exception {
    accessor = new DoubleSqlAccessor();
  }

  public void visitString(StringField field) throws Exception {
    accessor = new StringSqlAccessor();
  }

  public void visitDate(DateField field) throws Exception {
    accessor = new DateSqlAccessor();
  }

  public void visitBoolean(BooleanField field) throws Exception {
    accessor = new BooleanSqlAccessor();
  }

  public void visitTimeStamp(TimeStampField field) throws Exception {
    accessor = new TimestampSqlAccessor();
  }

  public void visitBlob(BlobField field) throws Exception {
    accessor = new BlobSqlAccessor();
  }

  public void visitLink(LinkField field) throws Exception {
    visitInteger(field);
  }
}
