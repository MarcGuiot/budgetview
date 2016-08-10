package org.globsframework.sqlstreams.drivers.jdbc.impl;

import org.globsframework.metamodel.Field;
import org.globsframework.metamodel.fields.*;
import org.globsframework.sqlstreams.GlobsDatabase;
import org.globsframework.sqlstreams.annotations.AutoIncrement;
import org.globsframework.sqlstreams.utils.StringPrettyWriter;

public abstract class SqlFieldCreationVisitor implements FieldVisitor {
  private GlobsDatabase globsDB;
  protected StringPrettyWriter prettyWriter;
  private boolean appendComma;

  public SqlFieldCreationVisitor(GlobsDatabase globsDB, StringPrettyWriter prettyWriter) {
    this.globsDB = globsDB;
    this.prettyWriter = prettyWriter;
  }

  public FieldVisitor appendComma(boolean appendComma) {
    this.appendComma = appendComma;
    return this;
  }

  public void visitInteger(IntegerField field) throws Exception {
    add("INTEGER", field);
  }

  public void visitLong(LongField field) throws Exception {
    add("BIGINT", field);
  }

  public void visitDouble(DoubleField field) throws Exception {
    add("DOUBLE", field);
  }

  public void visitString(StringField field) throws Exception {
    add("VARCHAR", field);
  }

  public void visitDate(DateField field) throws Exception {
    add("DATE", field);
  }

  public void visitBoolean(BooleanField field) throws Exception {
    add("BOOLEAN", field);
  }

  public void visitTimeStamp(TimeStampField field) throws Exception {
    add("TIMESTAMP", field);
  }

  public void visitBlob(BlobField field) throws Exception {
    add("BLOB", field);
  }

  public void visitLink(LinkField field) throws Exception {
    visitInteger(field);
  }

  protected void add(String param, Field field) {
    boolean isAutoIncrementField = field.hasAnnotation(AutoIncrement.class);
    String columnName = globsDB.getColumnName(field);
    if (columnName != null) {
      prettyWriter
        .append(columnName)
        .append(" ")
        .append(param)
        .append(isAutoIncrementField ? " " + getAutoIncrementKeyWord() : "")
        .appendIf(", ", appendComma);
    }
  }

  public abstract String getAutoIncrementKeyWord();
}
