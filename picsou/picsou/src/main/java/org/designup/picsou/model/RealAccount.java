package org.designup.picsou.model;

import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.utils.GlobTypeLoader;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.fields.StringField;
import org.globsframework.metamodel.fields.BooleanField;
import org.globsframework.metamodel.fields.LinkField;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.annotations.Target;
import org.globsframework.metamodel.annotations.DefaultBoolean;
import org.globsframework.model.FieldValues;
import org.globsframework.model.FieldSetter;
import org.globsframework.utils.serialization.SerializedByteArrayOutput;
import org.globsframework.utils.serialization.SerializedOutput;
import org.globsframework.utils.serialization.SerializedInput;
import org.globsframework.utils.serialization.SerializedInputOutputFactory;
import org.designup.picsou.server.serialization.PicsouGlobSerializer;

public class RealAccount {

  public static GlobType TYPE;

  @Key
  public static IntegerField ID;

  @Target(Bank.class)
  public static LinkField BANK; // to protect if same name

  public static StringField NUMBER;

  public static StringField POSITION;

  public static StringField NAME;

  @Target(value = AccountType.class)
  public static LinkField ACCOUNT_TYPE;

  @DefaultBoolean(false)
  public static BooleanField IMPORTED;

  @DefaultBoolean(false)
  public static BooleanField SAVINGS;

  @Target(Account.class)
  public static LinkField ACCOUNT;

  public static StringField FILE_NAME;

  static {
    GlobTypeLoader.init(RealAccount.class, "realAccount");
  }

  public static class Serializer implements PicsouGlobSerializer {

    public int getWriteVersion() {
      return 1;
    }

    public byte[] serializeData(FieldValues fieldValues) {
      SerializedByteArrayOutput serializedByteArrayOutput = new SerializedByteArrayOutput();
      SerializedOutput output = serializedByteArrayOutput.getOutput();
      output.writeInteger(fieldValues.get(BANK));
      output.writeUtf8String(fieldValues.get(NUMBER));
      output.writeUtf8String(fieldValues.get(POSITION));
      output.writeUtf8String(fieldValues.get(NAME));
      output.writeInteger(fieldValues.get(ACCOUNT_TYPE));
      output.writeBoolean(fieldValues.get(IMPORTED));
      output.writeBoolean(fieldValues.get(SAVINGS));
      output.writeInteger(fieldValues.get(ACCOUNT));
      return serializedByteArrayOutput.toByteArray();
    }

    public void deserializeData(int version, FieldSetter fieldSetter, byte[] data, Integer id) {
      if (version == 1) {
        deserializeDataV1(fieldSetter, data);
      }
    }

    private void deserializeDataV1(FieldSetter fieldSetter, byte[] data) {
      SerializedInput input = SerializedInputOutputFactory.init(data);
      fieldSetter.set(BANK, input.readInteger());
      fieldSetter.set(NUMBER, input.readUtf8String());
      fieldSetter.set(POSITION, input.readUtf8String());
      fieldSetter.set(NAME, input.readUtf8String());
      fieldSetter.set(ACCOUNT_TYPE, input.readInteger());
      fieldSetter.set(IMPORTED, input.readBoolean());
      fieldSetter.set(SAVINGS, input.readBoolean());
      fieldSetter.set(ACCOUNT, input.readInteger());
    }
  }

}
