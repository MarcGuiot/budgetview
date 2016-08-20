package org.globsframework.json;

import org.globsframework.metamodel.Field;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.fields.*;
import org.globsframework.model.*;
import org.globsframework.utils.exceptions.InvalidParameter;
import org.globsframework.utils.exceptions.ParsingFailed;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class JsonGlobParser {

  private GlobRepository repository;
  private Map<Field, String> fieldNames = new HashMap<Field, String>();
  private HashMap<Field, Converter> converters;

  public interface Converter {
    Object convert(Object value);
  }

  public JsonGlobParser(GlobRepository repository) {
    this.repository = repository;
  }

  public JsonGlobParser setName(Field field, String name) {
    fieldNames.put(field, name);
    return this;
  }

  public JsonGlobParser setConverter(Field field, Converter converter) {
    if (converters == null) {
      converters = new HashMap<Field, Converter>();
    }
    converters.put(field, converter);
    return this;
  }

  public Glob toGlob(JSONObject json, GlobType type, Integer id, FieldValue... additionalValues) throws ParsingFailed {
    FieldValues fieldValues = getFieldValues(json, type, id, additionalValues);
    return repository.create(type, fieldValues.toArray());
  }

  public Glob toGlob(JSONObject json, GlobType type, GlobRepository repository, FieldValue... additionalValues) throws ParsingFailed {
    FieldValues fieldValues = getFieldValues(json, type, null, additionalValues);
    return repository.create(type, fieldValues.toArray());
  }

  public FieldValues toFieldValues(JSONObject json, GlobType type) throws ParsingFailed {
    return getFieldValues(json, type, null);
  }

  private MutableFieldValues getFieldValues(final JSONObject json, GlobType type, Integer id, FieldValue... additionalValues) throws ParsingFailed {
    final FieldValuesBuilder builder = FieldValuesBuilder.init(additionalValues);
    for (Field field : type.getFields()) {
      if (field.isKeyField()) {
        if (id != null) {
          builder.setValue(field, id);
          continue;
        }
      }
      final String name = getFieldName(field);
      if (!json.has(name)) {
        continue;
      }
      try {
        if (converters != null && converters.containsKey(field)) {
          builder.setValue(field, converters.get(field).convert(json.opt(name)));
        }
        else {
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
              builder.set(field, JsonGlobFormat.parseDate(json.getString(name)));
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
      }
      catch (Exception e) {
        throw new ParsingFailed("Error parsing field '" + name + "' for object:\n" + json.toString(2), e);
      }
    }
    return builder.get();
  }

  private String getFieldName(Field field) {
    String name = fieldNames.get(field);
    if (name != null) {
      return name;
    }
    return field.getName();
  }
}
