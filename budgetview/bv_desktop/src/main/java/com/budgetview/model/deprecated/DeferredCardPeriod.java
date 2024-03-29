package com.budgetview.model.deprecated;

import com.budgetview.desktop.accounts.utils.MonthDay;
import com.budgetview.model.Account;
import com.budgetview.model.Month;
import com.budgetview.model.util.TypeLoader;
import com.budgetview.shared.utils.GlobSerializer;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.DefaultInteger;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.annotations.Target;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.fields.LinkField;
import org.globsframework.model.FieldSetter;
import org.globsframework.model.FieldValues;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.serialization.SerializedByteArrayOutput;
import org.globsframework.utils.serialization.SerializedInput;
import org.globsframework.utils.serialization.SerializedInputOutputFactory;

@Deprecated
public class DeferredCardPeriod {
  public static GlobType TYPE;

  @Key
  public static IntegerField ID;

  @Target(Account.class)
  public static LinkField ACCOUNT;

  @Target(Month.class)
  public static LinkField FROM_MONTH;

  @Target(MonthDay.class)
  @DefaultInteger(31)
  public static LinkField DAY;


  static {
    TypeLoader.init(DeferredCardPeriod.class, "DeferredCardPeriod");
  }

  public static class Serializer implements GlobSerializer {

    public int getWriteVersion() {
      return 2;
    }

    public boolean shouldBeSaved(GlobRepository repository, FieldValues fieldValues) {
      return true;
    }

    public byte[] serializeData(FieldValues values) {
      SerializedByteArrayOutput serializedByteArrayOutput = new SerializedByteArrayOutput();
      return serializedByteArrayOutput.toByteArray();
    }

    public void deserializeData(int version, byte[] data, Integer id, FieldSetter fieldSetter) {
      if (version == 1) {
        deserializeDataV1(fieldSetter, data);
      }
      else if (version == 2){
        // no read ==> deferredCardPeriod remove
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