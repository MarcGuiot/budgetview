package org.globsframework.remote;

import org.globsframework.metamodel.Field;
import org.globsframework.metamodel.GlobModel;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.fields.*;
import org.globsframework.model.FieldValues;
import org.globsframework.model.FieldValuesBuilder;
import org.globsframework.utils.serialization.SerializedInput;

public class RemoteExecutor {
  private GlobModel model;
  private RequestBuilder requestBuilder;

  enum TAG {
    CREATE(1),
    UPDATE(2),
    DELETE(3),
    NO_MORE(4);

    private byte id;

    TAG(int i) {
      this.id = (byte)i;
    }

    public byte getId() {
      return id;
    }
  }

  public interface Request {
    void update(Field field, Object value);
  }

  public interface CreateRequest extends Request {
    void create();
  }

  public interface UpdateRequest extends Request {
    void update();
  }

  public interface DeleteRequest {
    void delete();
  }

  public interface RequestBuilder {
    UpdateRequest getUpdate(GlobType globType, FieldValues fieldConstraint);

    CreateRequest getCreate(GlobType globType, FieldValues fieldValues);

    DeleteRequest getDelete(GlobType globType, FieldValues valuesConstraint);
  }

  public RemoteExecutor(GlobModel model, RequestBuilder requestBuilder) {
    this.model = model;
    this.requestBuilder = requestBuilder;
  }

  public void execute(SerializedInput input) {
    byte type = input.readByte();
    while (type != TAG.NO_MORE.getId()) {
      GlobType globType = model.getType(input.readJavaString());
      if (TAG.CREATE.getId() == type) {
        FieldValues fieldValues = readConstraintValues(globType, input);
        CreateRequest createCreateRequest = requestBuilder.getCreate(globType, fieldValues);
        Deserializer deserializer = new Deserializer(createCreateRequest, input);
        for (Field field : globType.getFields()) {
          if (!field.isKeyField()) {
            field.safeVisit(deserializer);
          }
        }
        createCreateRequest.create();
      }
      else if (TAG.DELETE.getId() == type) {
        FieldValues fieldValues = readConstraintValues(globType, input);
        requestBuilder.getDelete(globType, fieldValues).delete();
      }
      else if (TAG.UPDATE.getId() == type) {
        FieldValues fieldValues = readConstraintValues(globType, input);
        UpdateRequest request = requestBuilder.getUpdate(globType, fieldValues);
        Deserializer deserializer = new Deserializer(request, input);
        int size = input.readNotNullInt();
        while (size != 0) {
          size--;
          int index = input.readNotNullInt();
          globType.getField(index).safeVisit(deserializer);
        }
        request.update();
      }
      type = input.readByte();
    }
  }

  private FieldValues readConstraintValues(GlobType globType, SerializedInput input) {
    final FieldValuesBuilder values = FieldValuesBuilder.init();
    Deserializer deserializer = new Deserializer(new Request() {
      public void update(Field field, Object value) {
        values.setValue(field, value);
      }
    }, input);
    int attrCount = input.readNotNullInt();
    while (attrCount != 0) {
      attrCount--;
      int fieldId = input.readNotNullInt();
      globType.getField(fieldId).safeVisit(deserializer);
    }
    return values.get();
  }

  static class Deserializer implements FieldVisitor {
    private Request request;
    private SerializedInput input;

    public Deserializer(final Request request, SerializedInput input) {
      this.request = request;
      this.input = input;
    }

    public void visitInteger(IntegerField field) throws Exception {
      request.update(field, input.readInteger());
    }

    public void visitDouble(DoubleField field) throws Exception {
      request.update(field, input.readDouble());
    }

    public void visitString(StringField field) throws Exception {
      request.update(field, input.readJavaString());
    }

    public void visitDate(DateField field) throws Exception {
      request.update(field, input.readDate());
    }

    public void visitBoolean(BooleanField field) throws Exception {
      request.update(field, input.readBoolean());
    }

    public void visitTimeStamp(TimeStampField field) throws Exception {
      request.update(field, input.readDate());
    }

    public void visitBlob(BlobField field) throws Exception {
      request.update(field, input.readBytes());
    }

    public void visitLong(LongField field) throws Exception {
      request.update(field, input.readLong());
    }

    public void visitLink(LinkField field) throws Exception {
      request.update(field, input.readInteger());
    }
  }
}
