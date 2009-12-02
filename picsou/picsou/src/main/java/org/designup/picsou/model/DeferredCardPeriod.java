package org.designup.picsou.model;

import org.designup.picsou.model.Month;
import org.designup.picsou.model.Account;
import org.designup.picsou.server.serialization.PicsouGlobSerializer;
import org.designup.picsou.gui.accounts.Day;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.*;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.fields.LinkField;
import org.globsframework.metamodel.utils.GlobTypeLoader;
import org.globsframework.model.FieldValues;
import org.globsframework.model.FieldSetter;
import org.globsframework.utils.serialization.SerializedByteArrayOutput;
import org.globsframework.utils.serialization.SerializedOutput;
import org.globsframework.utils.serialization.SerializedInput;
import org.globsframework.utils.serialization.SerializedInputOutputFactory;

public class DeferredCardPeriod {
  public static GlobType TYPE;

  @Key
  public static IntegerField ID;

  @Target(Account.class)
  public static LinkField ACCOUNT;

  @Target(Month.class)
  public static LinkField FROM_MONTH;

  @Target(Day.class)
  public static LinkField DAY;


  static {
    GlobTypeLoader.init(DeferredCardPeriod.class, "DeferredCardPeriod");
  }
  public static class Serializer implements PicsouGlobSerializer {

    public int getWriteVersion() {
      return 1;
    }

    public byte[] serializeData(FieldValues values) {
      SerializedByteArrayOutput serializedByteArrayOutput = new SerializedByteArrayOutput();
      SerializedOutput outputStream = serializedByteArrayOutput.getOutput();
      outputStream.writeInteger(values.get(ACCOUNT));
      outputStream.writeInteger(values.get(FROM_MONTH));
      outputStream.writeInteger(values.get(DAY));
      return serializedByteArrayOutput.toByteArray();
    }

    public void deserializeData(int version, FieldSetter fieldSetter, byte[] data, Integer id) {
      if (version == 1) {
        deserializeDataV1(fieldSetter, data);
      }
    }

    private void deserializeDataV1(FieldSetter fieldSetter, byte[] data) {
      SerializedInput input = SerializedInputOutputFactory.init(data);
      fieldSetter.set(ACCOUNT, input.readInteger());
      fieldSetter.set(FROM_MONTH, input.readInteger());
      fieldSetter.set(DAY, input.readInteger());
    }

  }
}