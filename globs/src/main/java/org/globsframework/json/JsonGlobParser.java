package org.globsframework.json;

import org.globsframework.metamodel.Field;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.fields.*;
import org.globsframework.model.*;
import org.globsframework.utils.exceptions.InvalidParameter;
import org.json.JSONObject;

import java.io.IOException;

public class JsonGlobParser {
  public static Glob jsonToGlob(JSONObject json, GlobType type, Integer id, GlobRepository repository, FieldValue... additionalValues) throws IOException {
    FieldValues fieldValues = getFieldValues(json, type, id, additionalValues);
    return repository.create(type, fieldValues.toArray());
  }

  public static Glob jsonToGlob(JSONObject json, GlobType type, GlobRepository repository, FieldValue... additionalValues) throws IOException {
    FieldValues fieldValues = getFieldValues(json, type, null, additionalValues);
    return repository.create(type, fieldValues.toArray());
  }

  private static MutableFieldValues getFieldValues(final JSONObject json, GlobType type, Integer id, FieldValue... additionalValues) throws IOException {
    final FieldValuesBuilder builder = FieldValuesBuilder.init(additionalValues);
    for (Field field : type.getFields()) {
      if (field.isKeyField()) {
        if (id != null) {
          builder.setValue(field, id);
          continue;
        }
      }
      final String name = field.getName();
      if (!json.has(name)) {
        continue;
      }
      try {
        field.visit(new FieldVisitor() {
          public void visitInteger(IntegerField field) throws Exception {
            builder.set(field, json.getInt(name));
          }

          public void visitDouble(DoubleField field) throws Exception {
            builder.set(field, json.getDouble(name));
          }

          public void visitString(StringField field) throws Exception {
            builder.set(field, json.optString(name, null));
          }

          public void visitBoolean(BooleanField field) throws Exception {
            builder.set(field, json.getBoolean(name));
          }

          public void visitLong(LongField field) throws Exception {
            builder.set(field, json.getLong(name));
          }

          public void visitDate(DateField field) throws Exception {
            throw new InvalidParameter("Unexpected type Date for field: " + name);
          }

          public void visitTimeStamp(TimeStampField field) throws Exception {
            throw new InvalidParameter("Unexpected type TimeStamp for field: " + name);
          }

          public void visitLink(LinkField field) throws Exception {
            throw new InvalidParameter("Unexpected type Link for field: " + name);
          }

          public void visitBlob(BlobField field) throws Exception {
            throw new InvalidParameter("Unexpected type Blob for field: " + name);
          }
        });
      }
      catch (Exception e) {
        throw new IOException("Error parsing field '" + name + "' for object:\n" + json.toString(2), e);
      }
    }
    return builder.get();
  }
}
