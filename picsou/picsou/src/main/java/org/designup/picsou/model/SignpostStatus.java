package org.designup.picsou.model;

import org.designup.picsou.server.serialization.PicsouGlobSerializer;
import org.globsframework.metamodel.Field;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.fields.BooleanField;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.utils.GlobTypeLoader;
import org.globsframework.model.FieldSetter;
import org.globsframework.model.FieldValues;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.Utils;
import org.globsframework.utils.serialization.SerializedByteArrayOutput;
import org.globsframework.utils.serialization.SerializedInput;
import org.globsframework.utils.serialization.SerializedInputOutputFactory;
import org.globsframework.utils.serialization.SerializedOutput;

public class SignpostStatus {

  public static final Integer SINGLETON_ID = 0;
  public static org.globsframework.model.Key KEY;

  public static GlobType TYPE;

  @Key
  public static IntegerField ID;

  public static BooleanField IMPORT_SHOWN;
  public static BooleanField WELCOME_SHOWN;
  public static BooleanField CATEGORIZATION_SELECTION_SHOWN;
  public static BooleanField CATEGORIZATION_AREA_SHOWN;
  public static BooleanField FIRST_CATEGORIZATION_DONE_SHOWN;
  public static BooleanField CATEGORIZATION_COMPLETION_SHOWN;
  public static BooleanField SERIES_PERIODICITY_SHOWN;
  public static BooleanField SERIES_PERIODICITY_CLOSED;
  public static BooleanField SERIES_GAUGE_SHOWN;
  public static BooleanField SERIES_AMOUNT_SHOWN;
  public static BooleanField SERIES_AMOUNT_CLOSED;
  public static BooleanField END_OF_MONTH_POSITION_SHOWN;

  public static IntegerField AMOUNT_SERIES;
  public static IntegerField PERIODICITY_SERIES;

  static {
    GlobTypeLoader.init(SignpostStatus.class, "signpostStatus");
    KEY = org.globsframework.model.Key.create(TYPE, SINGLETON_ID);
  }

  public static boolean isCompleted(BooleanField completionField, GlobRepository repository) {
    Glob status = repository.find(KEY);
    return (status != null) && status.isTrue(completionField);
  }

  public static void setCompleted(BooleanField completionField, GlobRepository repository) {
    repository.findOrCreate(KEY);
    repository.update(KEY, completionField, true);
  }

  public static void setAllCompleted(GlobRepository repository) {
    repository.findOrCreate(KEY);
    for (Field field : TYPE.getFields()) {
      if (field instanceof BooleanField) {
        repository.update(KEY, field, Boolean.TRUE);
      }
    }
  }

  public static void setAmountSeriesKey(org.globsframework.model.Key seriesKey,
                                        GlobRepository repository) {
    repository.findOrCreate(KEY);
    repository.update(KEY, AMOUNT_SERIES, seriesKey.get(Series.ID));
  }

  public static boolean isAmountSeries(GlobRepository repository, org.globsframework.model.Key key) {
    return Utils.equal(key.get(Series.ID),
                       repository.findOrCreate(KEY).get(AMOUNT_SERIES));
  }

  public static void setPeriodicitySeriesKey(org.globsframework.model.Key seriesKey,
                                             GlobRepository repository) {
    repository.findOrCreate(KEY);
    repository.update(KEY, PERIODICITY_SERIES, seriesKey.get(Series.ID));
  }

  public static boolean isPeriodicitySeries(GlobRepository repository, org.globsframework.model.Key key) {
    return Utils.equal(key.get(Series.ID),
                       repository.findOrCreate(KEY).get(PERIODICITY_SERIES));
  }

  public static class Serializer implements PicsouGlobSerializer {

    public byte[] serializeData(FieldValues values) {
      SerializedByteArrayOutput serializedByteArrayOutput = new SerializedByteArrayOutput();
      SerializedOutput outputStream = serializedByteArrayOutput.getOutput();
      outputStream.writeBoolean(values.get(IMPORT_SHOWN));
      outputStream.writeBoolean(values.get(WELCOME_SHOWN));
      outputStream.writeBoolean(values.get(CATEGORIZATION_SELECTION_SHOWN));
      outputStream.writeBoolean(values.get(CATEGORIZATION_AREA_SHOWN));
      outputStream.writeBoolean(values.get(CATEGORIZATION_COMPLETION_SHOWN));
      outputStream.writeBoolean(values.get(SERIES_PERIODICITY_SHOWN));
      outputStream.writeBoolean(values.get(SERIES_PERIODICITY_CLOSED));
      outputStream.writeBoolean(values.get(SERIES_GAUGE_SHOWN));
      outputStream.writeBoolean(values.get(SERIES_AMOUNT_SHOWN));
      outputStream.writeBoolean(values.get(SERIES_AMOUNT_CLOSED));
      outputStream.writeBoolean(values.get(END_OF_MONTH_POSITION_SHOWN));
      outputStream.writeInteger(values.get(AMOUNT_SERIES));
      outputStream.writeInteger(values.get(PERIODICITY_SERIES));
      outputStream.writeBoolean(values.get(FIRST_CATEGORIZATION_DONE_SHOWN));
      return serializedByteArrayOutput.toByteArray();
    }

    public int getWriteVersion() {
      return 3;
    }

    public void deserializeData(int version, FieldSetter fieldSetter, byte[] data, Integer id) {
      if (version == 1) {
        deserializeDataV1(fieldSetter, data);
      }
      else if (version == 2) {
        deserializeDataV2(fieldSetter, data);
      }
      else if (version == 3) {
        deserializeDataV3(fieldSetter, data);
      }
    }

    private void deserializeDataV3(FieldSetter fieldSetter, byte[] data) {
      SerializedInput input = SerializedInputOutputFactory.init(data);
      fieldSetter.set(IMPORT_SHOWN, input.readBoolean());
      fieldSetter.set(WELCOME_SHOWN, input.readBoolean());
      fieldSetter.set(CATEGORIZATION_SELECTION_SHOWN, input.readBoolean());
      fieldSetter.set(CATEGORIZATION_AREA_SHOWN, input.readBoolean());
      fieldSetter.set(CATEGORIZATION_COMPLETION_SHOWN, input.readBoolean());
      fieldSetter.set(SERIES_PERIODICITY_SHOWN, input.readBoolean());
      fieldSetter.set(SERIES_PERIODICITY_CLOSED, input.readBoolean());
      fieldSetter.set(SERIES_GAUGE_SHOWN, input.readBoolean());
      fieldSetter.set(SERIES_AMOUNT_SHOWN, input.readBoolean());
      fieldSetter.set(SERIES_AMOUNT_CLOSED, input.readBoolean());
      fieldSetter.set(END_OF_MONTH_POSITION_SHOWN, input.readBoolean());
      fieldSetter.set(AMOUNT_SERIES, input.readInteger());
      fieldSetter.set(PERIODICITY_SERIES, input.readInteger());
      fieldSetter.set(FIRST_CATEGORIZATION_DONE_SHOWN, input.readBoolean());
    }

    private void deserializeDataV2(FieldSetter fieldSetter, byte[] data) {
      SerializedInput input = SerializedInputOutputFactory.init(data);
      fieldSetter.set(IMPORT_SHOWN, input.readBoolean());
      fieldSetter.set(WELCOME_SHOWN, input.readBoolean());
      fieldSetter.set(CATEGORIZATION_SELECTION_SHOWN, input.readBoolean());
      fieldSetter.set(CATEGORIZATION_AREA_SHOWN, input.readBoolean());
      fieldSetter.set(CATEGORIZATION_COMPLETION_SHOWN, input.readBoolean());
      fieldSetter.set(SERIES_PERIODICITY_SHOWN, input.readBoolean());
      fieldSetter.set(SERIES_PERIODICITY_CLOSED, input.readBoolean());
      fieldSetter.set(SERIES_GAUGE_SHOWN, input.readBoolean());
      fieldSetter.set(SERIES_AMOUNT_SHOWN, input.readBoolean());
      fieldSetter.set(SERIES_AMOUNT_CLOSED, input.readBoolean());
      fieldSetter.set(END_OF_MONTH_POSITION_SHOWN, input.readBoolean());
      fieldSetter.set(AMOUNT_SERIES, input.readInteger());
      fieldSetter.set(PERIODICITY_SERIES, input.readInteger());
    }

    private void deserializeDataV1(FieldSetter fieldSetter, byte[] data) {
      SerializedInput input = SerializedInputOutputFactory.init(data);
      fieldSetter.set(IMPORT_SHOWN, input.readBoolean());
      fieldSetter.set(WELCOME_SHOWN, input.readBoolean());
      fieldSetter.set(CATEGORIZATION_SELECTION_SHOWN, input.readBoolean());
      fieldSetter.set(CATEGORIZATION_AREA_SHOWN, input.readBoolean());
      fieldSetter.set(CATEGORIZATION_COMPLETION_SHOWN, input.readBoolean());
      fieldSetter.set(SERIES_PERIODICITY_SHOWN, input.readBoolean());
      fieldSetter.set(SERIES_PERIODICITY_CLOSED, input.readBoolean());
      fieldSetter.set(SERIES_GAUGE_SHOWN, input.readBoolean());
      fieldSetter.set(SERIES_AMOUNT_SHOWN, input.readBoolean());
      fieldSetter.set(SERIES_AMOUNT_CLOSED, input.readBoolean());
      fieldSetter.set(END_OF_MONTH_POSITION_SHOWN, input.readBoolean());
    }
  }
}
