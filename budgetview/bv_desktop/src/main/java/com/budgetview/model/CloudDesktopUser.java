package com.budgetview.model;

import com.budgetview.model.util.TypeLoader;
import com.budgetview.shared.utils.GlobSerializer;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.fields.BooleanField;
import org.globsframework.metamodel.fields.DateField;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.fields.StringField;
import org.globsframework.model.FieldSetter;
import org.globsframework.model.FieldValues;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.serialization.SerializedByteArrayOutput;
import org.globsframework.utils.serialization.SerializedInput;
import org.globsframework.utils.serialization.SerializedInputOutputFactory;
import org.globsframework.utils.serialization.SerializedOutput;

import static org.globsframework.model.FieldValue.value;

public class CloudDesktopUser {
  public static final Integer SINGLETON_ID = 0;
  public static org.globsframework.model.Key KEY;

  public static GlobType TYPE;

  @Key
  public static IntegerField ID;

  public static StringField EMAIL;

  public static IntegerField CLOUD_USER_ID;

  public static IntegerField DEVICE_ID;

  public static StringField DEVICE_TOKEN;

  public static BooleanField REGISTERED;

  public static BooleanField SYNCHRO_ENABLED;

  public static IntegerField LAST_UPDATE;

  public static DateField SUBSCRIPTION_END_DATE;

  public static boolean isTrue(BooleanField field, GlobRepository repository) {
    Glob user = repository.find(KEY);
    return user != null && user.isTrue(field);
  }

  static {
    TypeLoader.init(CloudDesktopUser.class, "cloudDesktopUser");
    KEY = org.globsframework.model.Key.create(TYPE, SINGLETON_ID);
  }

  public static boolean isRegistered(GlobRepository repository) {
    return repository.contains(CloudDesktopUser.KEY) && repository.get(CloudDesktopUser.KEY).isTrue(CloudDesktopUser.REGISTERED);
  }

  public static void unregister(GlobRepository repository) {
    if (repository.contains(CloudDesktopUser.KEY)) {
      repository.update(CloudDesktopUser.KEY, value(CloudDesktopUser.REGISTERED, false));
    }
  }

  public static class Serializer implements GlobSerializer {

    public int getWriteVersion() {
      return 1;
    }

    public boolean shouldBeSaved(GlobRepository repository, FieldValues fieldValues) {
      return true;
    }

    public byte[] serializeData(FieldValues values) {
      SerializedByteArrayOutput serializedByteArrayOutput = new SerializedByteArrayOutput();
      SerializedOutput outputStream = serializedByteArrayOutput.getOutput();
      outputStream.writeUtf8String(values.get(EMAIL));
      outputStream.writeInteger(values.get(CLOUD_USER_ID));
      outputStream.writeInteger(values.get(DEVICE_ID));
      outputStream.writeUtf8String(values.get(DEVICE_TOKEN));
      outputStream.writeBoolean(values.get(REGISTERED));
      outputStream.writeBoolean(values.get(SYNCHRO_ENABLED));
      outputStream.writeInteger(values.get(LAST_UPDATE));
      outputStream.writeDate(values.get(SUBSCRIPTION_END_DATE));
      return serializedByteArrayOutput.toByteArray();
    }

    public void deserializeData(int version, byte[] data, Integer id, FieldSetter fieldSetter) {
      if (version == 1) {
        deserializeV1(fieldSetter, data);
      }
    }

    private void deserializeV1(FieldSetter fieldSetter, byte[] data) {
      SerializedInput input = SerializedInputOutputFactory.init(data);
      fieldSetter.set(EMAIL, input.readUtf8String());
      fieldSetter.set(CLOUD_USER_ID, input.readInteger());
      fieldSetter.set(DEVICE_ID, input.readInteger());
      fieldSetter.set(DEVICE_TOKEN, input.readUtf8String());
      fieldSetter.set(REGISTERED, input.readBoolean());
      fieldSetter.set(SYNCHRO_ENABLED, input.readBoolean());
      fieldSetter.set(LAST_UPDATE, input.readInteger());
      fieldSetter.set(SUBSCRIPTION_END_DATE, input.readDate());
    }
  }
}
