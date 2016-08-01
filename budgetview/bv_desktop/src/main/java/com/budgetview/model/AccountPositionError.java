package com.budgetview.model;

import com.budgetview.shared.utils.PicsouGlobSerializer;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.DefaultBoolean;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.annotations.Target;
import org.globsframework.metamodel.fields.*;
import org.globsframework.metamodel.utils.GlobTypeLoader;
import org.globsframework.model.FieldSetter;
import org.globsframework.model.FieldValues;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.serialization.SerializedByteArrayOutput;
import org.globsframework.utils.serialization.SerializedInput;
import org.globsframework.utils.serialization.SerializedInputOutputFactory;
import org.globsframework.utils.serialization.SerializedOutput;

public class AccountPositionError {
  public static GlobType TYPE;

  @Key @Target(Account.class)
  public static LinkField ACCOUNT;

  @DefaultBoolean(false)
  public static BooleanField CLEARED;

  public static DateField UPDATE_DATE;

  public static DoubleField IMPORTED_POSITION;

  public static DoubleField LAST_REAL_OPERATION_POSITION;

  // Full date : see Month.toFullDate
  public static IntegerField LAST_PREVIOUS_IMPORT_DATE;

  static {
    GlobTypeLoader.init(AccountPositionError.class, "accountPositionError");
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
      outputStream.writeDate(values.get(UPDATE_DATE));
      outputStream.writeBoolean(values.get(CLEARED));
      outputStream.writeDouble(values.get(IMPORTED_POSITION));
      outputStream.writeDouble(values.get(LAST_REAL_OPERATION_POSITION));
      outputStream.writeInteger(values.get(LAST_PREVIOUS_IMPORT_DATE));
      return serializedByteArrayOutput.toByteArray();
    }

    public void deserializeData(int version, FieldSetter fieldSetter, byte[] data, Integer id) {
      if (version == 1) {
        deserializeV1(fieldSetter, data);
      }
    }

    private void deserializeV1(FieldSetter fieldSetter, byte[] data) {
      SerializedInput input = SerializedInputOutputFactory.init(data);
      fieldSetter.set(UPDATE_DATE, input.readDate());
      fieldSetter.set(CLEARED, input.readBoolean());
      fieldSetter.set(IMPORTED_POSITION, input.readDouble());
      fieldSetter.set(LAST_REAL_OPERATION_POSITION, input.readDouble());
      fieldSetter.set(LAST_PREVIOUS_IMPORT_DATE, input.readInteger());
    }
  }
}