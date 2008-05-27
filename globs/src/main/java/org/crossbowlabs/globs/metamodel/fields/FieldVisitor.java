package org.crossbowlabs.globs.metamodel.fields;

public interface FieldVisitor {
  void visitInteger(IntegerField field) throws Exception;

  void visitDouble(DoubleField field) throws Exception;

  void visitString(StringField field) throws Exception;

  void visitBoolean(BooleanField field) throws Exception;

  void visitLong(LongField field) throws Exception;

  void visitDate(DateField field) throws Exception;

  void visitTimeStamp(TimeStampField field) throws Exception;

  void visitLink(LinkField field) throws Exception;

  void visitBlob(BlobField field) throws Exception;

}

