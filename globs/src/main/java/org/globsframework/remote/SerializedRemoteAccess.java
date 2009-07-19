package org.globsframework.remote;

import org.globsframework.metamodel.Field;
import org.globsframework.metamodel.fields.*;
import org.globsframework.model.*;
import org.globsframework.utils.serialization.SerializedByteArrayOutput;
import org.globsframework.utils.serialization.SerializedOutput;

import java.util.Date;

public class SerializedRemoteAccess {

  public interface ChangeVisitor extends ChangeSetVisitor {
    void complete();
  }

  public byte[] apply(ChangeSet changeSet) {
    SerializedByteArrayOutput output = new SerializedByteArrayOutput();
    GlobChangeVisitorSerializer globChangeVisitorSerializer = new GlobChangeVisitorSerializer(output.getOutput());
    changeSet.safeVisit(globChangeVisitorSerializer);
    globChangeVisitorSerializer.complete();
    return output.toByteArray();
  }

  public static ChangeVisitor getChangeVisitor(SerializedOutput outputStream) {
    return new GlobChangeVisitorSerializer(outputStream);
  }

  private static class GlobChangeVisitorSerializer implements ChangeVisitor, FieldVisitor {
    private final SerializedOutput output;
    private Object value;

    public GlobChangeVisitorSerializer(SerializedOutput output) {
      this.output = output;
    }

    public void visitCreation(Key key, FieldValues values) throws Exception {
      output.writeByte(RemoteExecutor.TAG.CREATE.getId());
      output.writeJavaString(key.getGlobType().getName());
      writeKey(key);
      for (Field field : key.getGlobType().getFields()) {
        value = null;
        if (values.contains(field)) {
          value = values.getValue(field);
        }
        if (!key.contains(field)) {
          field.safeVisit(GlobChangeVisitorSerializer.this);
        }
      }
    }

    public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
      output.writeByte(RemoteExecutor.TAG.UPDATE.getId());
      output.writeJavaString(key.getGlobType().getName());
      writeKey(key);
      output.write(values.size());
      values.apply(new FieldValues.Functor() {
        public void process(Field field, Object value) throws Exception {
          output.write(field.getIndex());
          GlobChangeVisitorSerializer.this.value = value;
          field.safeVisit(GlobChangeVisitorSerializer.this);
        }
      });
    }

    public void visitDeletion(Key key, FieldValues values) throws Exception {
      output.writeByte(RemoteExecutor.TAG.DELETE.getId());
      output.writeJavaString(key.getGlobType().getName());
      writeKey(key);
    }

    private void writeKey(Key key) {
      Field[] list = key.getGlobType().getKeyFields();
      output.write(list.length);
      for (Field field : list) {
        output.write(field.getIndex());
        value = key.getValue(field);
        field.safeVisit(this);
      }
    }

    public void visitInteger(IntegerField field) throws Exception {
      output.writeInteger((Integer)value);
    }

    public void visitDouble(DoubleField field) throws Exception {
      output.writeDouble((Double)value);
    }

    public void visitString(StringField field) throws Exception {
      output.writeJavaString((String)value);
    }

    public void visitDate(DateField field) throws Exception {
      output.writeDate((Date)value);
    }

    public void visitBoolean(BooleanField field) throws Exception {
      output.writeBoolean((Boolean)value);
    }

    public void visitTimeStamp(TimeStampField field) throws Exception {
      output.writeDate((Date)value);
    }

    public void visitBlob(BlobField field) throws Exception {
      output.writeBytes((byte[])value);
    }

    public void visitLong(LongField field) throws Exception {
      output.writeLong((Long)value);
    }

    public void visitLink(LinkField field) throws Exception {
      visitInteger(field);
    }

    public void complete() {
      output.writeByte(RemoteExecutor.TAG.NO_MORE.getId());
    }
  }
}
