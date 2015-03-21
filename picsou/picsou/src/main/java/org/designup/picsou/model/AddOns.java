package org.designup.picsou.model;

import com.budgetview.shared.utils.PicsouGlobSerializer;
import org.globsframework.metamodel.Field;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.annotations.NoObfuscation;
import org.globsframework.metamodel.fields.BooleanField;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.utils.GlobTypeLoader;
import org.globsframework.model.FieldSetter;
import org.globsframework.model.FieldValues;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.KeyChangeListener;
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
  public static BooleanField PROJECTS;
  @NoObfuscation
  public static BooleanField GROUPS;
  @NoObfuscation
  public static BooleanField ANALYSIS;
  @NoObfuscation
  public static BooleanField MOBILE;

  static {
    GlobTypeLoader.init(AddOns.class, "addOns");
    KEY = org.globsframework.model.Key.create(TYPE, SINGLETON_ID);
  }

  public static Glob get(GlobRepository repository) {
    return repository.get(KEY);
  }

  public static boolean isEnabled(BooleanField field, GlobRepository repository) {
    Glob addons = repository.find(KEY);
    return addons == null ? false : addons.isTrue(field);
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

  public interface Listener {
    void processAddOn(boolean enabled);
  }

  public static void addListener(final GlobRepository repository, final BooleanField field, final Listener listener) {
    KeyChangeListener keyListener = new KeyChangeListener(AddOns.KEY) {
      public void update() {
        Glob addOns = repository.find(AddOns.KEY);
        if (addOns != null) {
          listener.processAddOn(addOns.isTrue(field));
        }
      }
    };
    repository.addChangeListener(keyListener);
    keyListener.update();
  }

  public static class Serializer implements PicsouGlobSerializer {

    public int getWriteVersion() {
      return 1;
    }

    public boolean shouldBeSaved(GlobRepository repository, FieldValues fieldValues) {
      return true;
    }

    public byte[] serializeData(FieldValues values) {
      SerializedByteArrayOutput serializedByteArrayOutput = new SerializedByteArrayOutput();
      SerializedOutput outputStream = serializedByteArrayOutput.getOutput();
      outputStream.writeBoolean(values.get(PROJECTS));
      outputStream.writeBoolean(values.get(GROUPS));
      outputStream.writeBoolean(values.get(ANALYSIS));
      return serializedByteArrayOutput.toByteArray();
    }

    public void deserializeData(int version, FieldSetter fieldSetter, byte[] data, Integer id) {
      if (version == 1) {
        deserializeDataV1(fieldSetter, data);
      }
    }

    private void deserializeDataV1(FieldSetter fieldSetter, byte[] data) {
      SerializedInput input = SerializedInputOutputFactory.init(data);
      fieldSetter.set(PROJECTS, input.readBoolean());
      fieldSetter.set(GROUPS, input.readBoolean());
      fieldSetter.set(ANALYSIS, input.readBoolean());
    }
  }
}
