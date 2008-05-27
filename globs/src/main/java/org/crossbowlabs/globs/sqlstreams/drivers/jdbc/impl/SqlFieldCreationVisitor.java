package org.crossbowlabs.globs.sqlstreams.drivers.jdbc.impl;

import org.crossbowlabs.globs.metamodel.Field;
import org.crossbowlabs.globs.metamodel.fields.*;
import org.crossbowlabs.globs.sqlstreams.SqlService;
import org.crossbowlabs.globs.sqlstreams.annotations.AutoIncrement;
import org.crossbowlabs.globs.sqlstreams.utils.StringPrettyWriter;

public abstract class SqlFieldCreationVisitor implements FieldVisitor {
  private SqlService sqlService;
  protected StringPrettyWriter prettyWriter;
  private boolean appendComma;

  public SqlFieldCreationVisitor(SqlService sqlService, StringPrettyWriter prettyWriter) {
    this.sqlService = sqlService;
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
    String columnName = sqlService.getColumnName(field);
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
