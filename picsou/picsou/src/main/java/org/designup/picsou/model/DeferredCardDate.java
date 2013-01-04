package org.designup.picsou.model;

import org.designup.picsou.gui.accounts.utils.MonthDay;
import com.budgetview.shared.utils.PicsouGlobSerializer;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.index.MultiFieldUniqueIndex;
import org.globsframework.metamodel.annotations.*;
import org.globsframework.metamodel.fields.LinkField;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.utils.GlobTypeLoader;
import org.globsframework.model.FieldValues;
import org.globsframework.model.FieldSetter;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.serialization.SerializedByteArrayOutput;
import org.globsframework.utils.serialization.SerializedOutput;
import org.globsframework.utils.serialization.SerializedInput;
import org.globsframework.utils.serialization.SerializedInputOutputFactory;

public class DeferredCardDate {
  public static GlobType TYPE;

  @Key
  public static IntegerField ID;

  @Target(Account.class)
  public static LinkField ACCOUNT;

  @Target(Month.class)
  public static LinkField MONTH;

  @Target(MonthDay.class)
  public static LinkField DAY;

  public static MultiFieldUniqueIndex ACCOUNT_AND_DATE;

  static {
    GlobTypeLoader loader = GlobTypeLoader.init(DeferredCardDate.class, "DeferredCardDate");
    loader.defineMultiFieldUniqueIndex(ACCOUNT_AND_DATE, ACCOUNT, MONTH);
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
      outputStream.writeInteger(values.get(ID));
      outputStream.writeInteger(values.get(ACCOUNT));
      outputStream.writeInteger(values.get(MONTH));
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
      fieldSetter.set(ID, input.readInteger());
      fieldSetter.set(ACCOUNT, input.readInteger());
      fieldSetter.set(MONTH, input.readInteger());
      fieldSetter.set(DAY, input.readInteger());
    }
  }
}