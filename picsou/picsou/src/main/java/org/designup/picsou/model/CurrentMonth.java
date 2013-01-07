package org.designup.picsou.model;

import org.designup.picsou.gui.time.TimeService;
import com.budgetview.shared.utils.PicsouGlobSerializer;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.DefaultInteger;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.annotations.Target;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.fields.LinkField;
import org.globsframework.metamodel.utils.GlobTypeLoader;
import org.globsframework.model.FieldSetter;
import org.globsframework.model.FieldValues;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.serialization.SerializedByteArrayOutput;
import org.globsframework.utils.serialization.SerializedInput;
import org.globsframework.utils.serialization.SerializedInputOutputFactory;
import org.globsframework.utils.serialization.SerializedOutput;

public class CurrentMonth {

  public static GlobType TYPE;

  @Key
  public static IntegerField ID;

  @DefaultInteger(0)
  @Target(Month.class)
  public static LinkField LAST_TRANSACTION_MONTH;

  @DefaultInteger(1)
  public static IntegerField LAST_TRANSACTION_DAY;

  @Target(Month.class)
  @DefaultInteger(0)
  public static LinkField CURRENT_MONTH;

  @DefaultInteger(0)
  public static IntegerField CURRENT_DAY;

  public static org.globsframework.model.Key KEY;

  static {
    GlobTypeLoader.init(CurrentMonth.class, "currentMonth");
    KEY = org.globsframework.model.Key.create(TYPE, 0);
  }

  public static Integer getLastTransactionMonth(GlobRepository repository) {
    return repository.get(CurrentMonth.KEY).get(LAST_TRANSACTION_MONTH);
  }

  public static Integer getLastTransactionDay(GlobRepository repository) {
    return repository.get(CurrentMonth.KEY).get(LAST_TRANSACTION_DAY);
  }

  public static Integer getCurrentMonth(GlobRepository repository) {
    return repository.get(CurrentMonth.KEY).get(CURRENT_MONTH);
  }

  public static Integer findCurrentMonth(GlobRepository repository) {
    Glob month = repository.find(CurrentMonth.KEY);
    if (month != null) {
      return month.get(CURRENT_MONTH);
    }
    return null;
  }

  public static boolean isCurrentMonth(int monthId, GlobRepository repository) {
    Glob currentMonth = repository.find(CurrentMonth.KEY);
    return currentMonth != null && currentMonth.get(LAST_TRANSACTION_MONTH) == monthId;
  }
  
  public static Integer getLastMonth(GlobRepository repository) {
    return repository.getAll(Month.TYPE).getSortedSet(Month.ID).last();
  }

  public static class Serializer implements PicsouGlobSerializer {

    public byte[] serializeData(FieldValues values) {
      SerializedByteArrayOutput serializedByteArrayOutput = new SerializedByteArrayOutput();
      SerializedOutput outputStream = serializedByteArrayOutput.getOutput();
      outputStream.writeInteger(values.get(ID));
      outputStream.writeInteger(values.get(LAST_TRANSACTION_MONTH));
      outputStream.writeInteger(values.get(LAST_TRANSACTION_DAY));
      outputStream.writeInteger(values.get(CURRENT_MONTH));
      outputStream.writeInteger(values.get(CURRENT_DAY));
      return serializedByteArrayOutput.toByteArray();
    }

    public void deserializeData(int version, FieldSetter fieldSetter, byte[] data, Integer id) {
      if (version == 1) {
        deserializeDataV1(fieldSetter, data);
      }
      else if (version == 2) {
        deserializeDataV2(fieldSetter, data);
      }
    }

    private void deserializeDataV1(FieldSetter fieldSetter, byte[] data) {
      SerializedInput input = SerializedInputOutputFactory.init(data);
      fieldSetter.set(ID, input.readInteger());
      fieldSetter.set(LAST_TRANSACTION_MONTH, input.readInteger());
      fieldSetter.set(LAST_TRANSACTION_DAY, input.readInteger());
      fieldSetter.set(CURRENT_MONTH, TimeService.getCurrentMonth());
      fieldSetter.set(CURRENT_DAY, TimeService.getCurrentDay());
    }

    private void deserializeDataV2(FieldSetter fieldSetter, byte[] data) {
      SerializedInput input = SerializedInputOutputFactory.init(data);
      fieldSetter.set(ID, input.readInteger());
      fieldSetter.set(LAST_TRANSACTION_MONTH, input.readInteger());
      fieldSetter.set(LAST_TRANSACTION_DAY, input.readInteger());
      fieldSetter.set(CURRENT_MONTH, input.readInteger());
      fieldSetter.set(CURRENT_DAY, input.readInteger());
    }

    public int getWriteVersion() {
      return 2;
    }

    public boolean shouldBeSaved(GlobRepository repository, FieldValues fieldValues) {
      return true;
    }
  }
}