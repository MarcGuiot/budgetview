package com.budgetview.model;

import com.budgetview.model.util.TypeLoader;
import com.budgetview.shared.utils.GlobSerializer;
import org.globsframework.metamodel.Field;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.annotations.NoObfuscation;
import org.globsframework.metamodel.fields.BooleanField;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.model.FieldSetter;
import org.globsframework.model.FieldValues;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.serialization.SerializedByteArrayOutput;
import org.globsframework.utils.serialization.SerializedInput;
import org.globsframework.utils.serialization.SerializedInputOutputFactory;
import org.globsframework.utils.serialization.SerializedOutput;

public class AddOns {
  public static final Integer SINGLETON_ID = 0;
  public static org.globsframework.model.Key KEY;

  public static GlobType TYPE;

  @Key
  public static IntegerField ID;

  @NoObfuscation
  public static BooleanField EXTRA_RANGE;
  @NoObfuscation
  public static BooleanField PROJECTS;
  @NoObfuscation
  public static BooleanField GROUPS;
  @NoObfuscation
  public static BooleanField ANALYSIS;
  @NoObfuscation
  public static BooleanField MOBILE;

  static {
    TypeLoader.init(AddOns.class, "addOns");
    KEY = org.globsframework.model.Key.create(TYPE, SINGLETON_ID);
  }

  public static Glob get(GlobRepository repository) {
    return repository.get(KEY);
  }

  public static boolean isEnabled(BooleanField field, GlobRepository repository) {
    Glob addons = repository.find(KEY);
    return addons != null && addons.isTrue(field);
  }

  public static void setEnabled(BooleanField field, GlobRepository repository, boolean enabled) {
    repository.update(AddOns.KEY, field, enabled);
  }

  public static void setAllEnabled(GlobRepository repository, boolean enabled) {
    repository.startChangeSet();
    try {
      for (Field field : TYPE.getFields()) {
        if (field instanceof BooleanField) {
          repository.update(KEY, field, enabled);
        }
      }
    }
    finally {
      repository.completeChangeSet();
    }
  }

  public static class Serializer implements GlobSerializer {

    public int getWriteVersion() {
      return 3;
    }

    public boolean shouldBeSaved(GlobRepository repository, FieldValues fieldValues) {
      return true;
    }

    public byte[] serializeData(FieldValues values) {
      SerializedByteArrayOutput serializedByteArrayOutput = new SerializedByteArrayOutput();
      SerializedOutput outputStream = serializedByteArrayOutput.getOutput();
      outputStream.writeBoolean(values.get(EXTRA_RANGE));
      outputStream.writeBoolean(values.get(PROJECTS));
      outputStream.writeBoolean(values.get(GROUPS));
      outputStream.writeBoolean(values.get(ANALYSIS));
      outputStream.writeBoolean(values.get(MOBILE));
      return serializedByteArrayOutput.toByteArray();
    }

    public void deserializeData(int version, byte[] data, Integer id, FieldSetter fieldSetter) {
      if (version == 3) {
        deserializeDataV3(fieldSetter, data);
      }
      else if (version == 2) {
        deserializeDataV2(fieldSetter, data);
      }
      else if (version == 1) {
        deserializeDataV1(fieldSetter, data);
      }
    }

    private void deserializeDataV3(FieldSetter fieldSetter, byte[] data) {
      SerializedInput input = SerializedInputOutputFactory.init(data);
      fieldSetter.set(EXTRA_RANGE, input.readBoolean());
      fieldSetter.set(PROJECTS, input.readBoolean());
      fieldSetter.set(GROUPS, input.readBoolean());
      fieldSetter.set(ANALYSIS, input.readBoolean());
      fieldSetter.set(MOBILE, input.readBoolean());
    }

    private void deserializeDataV2(FieldSetter fieldSetter, byte[] data) {
      SerializedInput input = SerializedInputOutputFactory.init(data);
      Boolean projects = input.readBoolean();
      fieldSetter.set(EXTRA_RANGE, projects);
      fieldSetter.set(PROJECTS, projects);
      fieldSetter.set(GROUPS, input.readBoolean());
      fieldSetter.set(ANALYSIS, input.readBoolean());
      fieldSetter.set(MOBILE, input.readBoolean());
    }

    private void deserializeDataV1(FieldSetter fieldSetter, byte[] data) {
      SerializedInput input = SerializedInputOutputFactory.init(data);
      Boolean projects = input.readBoolean();
      fieldSetter.set(EXTRA_RANGE, projects);
      fieldSetter.set(PROJECTS, projects);
      fieldSetter.set(GROUPS, input.readBoolean());
      fieldSetter.set(ANALYSIS, input.readBoolean());
      fieldSetter.set(MOBILE, projects);
    }
  }
}
