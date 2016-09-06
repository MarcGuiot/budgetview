package com.budgetview.model;

import com.budgetview.shared.utils.GlobSerializer;
import com.budgetview.utils.Lang;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.annotations.NamingField;
import org.globsframework.metamodel.annotations.Target;
import org.globsframework.metamodel.fields.BooleanField;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.fields.LinkField;
import org.globsframework.metamodel.fields.StringField;
import org.globsframework.metamodel.utils.GlobTypeLoader;
import org.globsframework.model.*;
import org.globsframework.utils.serialization.SerializedByteArrayOutput;
import org.globsframework.utils.serialization.SerializedInput;
import org.globsframework.utils.serialization.SerializedInputOutputFactory;
import org.globsframework.utils.serialization.SerializedOutput;

/**
 * @deprecated
 */
public class Category {

  public static GlobType TYPE;

  @Key
  public static IntegerField ID;

  public static StringField NAME;

  @NamingField
  public static StringField INNER_NAME;

  @Target(Category.class)
  public static LinkField MASTER;

  public static BooleanField SYSTEM;

  static {
    GlobTypeLoader.init(Category.class, "category");
  }

  public static String getName(Integer categoryId, GlobRepository repository) {
    Glob category = repository.get(org.globsframework.model.Key.create(TYPE, categoryId));
    String name = category.get(NAME);
    if (name != null) {
      return name;
    }

    String innerName = category.get(Category.INNER_NAME);
    String translation = Lang.find("category." + innerName);
    if (translation != null) {
      return translation;
    }

    return innerName;
  }

  public static class Serializer implements GlobSerializer {

    public byte[] serializeData(FieldValues values) {
      SerializedByteArrayOutput serializedByteArrayOutput = new SerializedByteArrayOutput();
      SerializedOutput outputStream = serializedByteArrayOutput.getOutput();
      outputStream.writeUtf8String(values.get(NAME));
      outputStream.writeUtf8String(values.get(INNER_NAME));
      outputStream.writeInteger(values.get(MASTER));
      outputStream.writeBoolean(values.get(SYSTEM));
      return serializedByteArrayOutput.toByteArray();
    }

    public void deserializeData(int version, byte[] data, Integer id, FieldSetter fieldSetter) {
      if (version == 1) {
        deserializeDataV1(fieldSetter, data);
      }
      else if (version == 2) {
        deserializeDataV2(fieldSetter, data);
      }
    }

    private void deserializeDataV1(FieldSetter fieldSetter, byte[] data) {
      SerializedInput input = SerializedInputOutputFactory.init(data);
      fieldSetter.set(NAME, input.readJavaString());
      fieldSetter.set(INNER_NAME, input.readJavaString());
      fieldSetter.set(MASTER, input.readInteger());
      fieldSetter.set(SYSTEM, input.readBoolean());
    }

    private void deserializeDataV2(FieldSetter fieldSetter, byte[] data) {
      SerializedInput input = SerializedInputOutputFactory.init(data);
      fieldSetter.set(NAME, input.readUtf8String());
      fieldSetter.set(INNER_NAME, input.readUtf8String());
      fieldSetter.set(MASTER, input.readInteger());
      fieldSetter.set(SYSTEM, input.readBoolean());
    }

    public int getWriteVersion() {
      return 2;
    }

    public boolean shouldBeSaved(GlobRepository repository, FieldValues fieldValues) {
      return true;
    }
  }

}
