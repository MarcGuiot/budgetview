package com.budgetview.model;

import com.budgetview.shared.utils.PicsouGlobSerializer;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.*;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.fields.BooleanField;
import org.globsframework.metamodel.fields.DoubleField;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.fields.LinkField;
import org.globsframework.metamodel.index.MultiFieldUniqueIndex;
import org.globsframework.metamodel.utils.GlobTypeLoader;
import org.globsframework.model.*;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.exceptions.InvalidState;
import org.globsframework.utils.serialization.SerializedByteArrayOutput;
import org.globsframework.utils.serialization.SerializedInput;
import org.globsframework.utils.serialization.SerializedInputOutputFactory;
import org.globsframework.utils.serialization.SerializedOutput;

import static org.globsframework.model.FieldValue.value;

public class SeriesBudget {
  public static GlobType TYPE;

  @Key
  public static IntegerField ID;

  @Target(Series.class)
  @Required
  public static LinkField SERIES;

  @Target(Month.class)
  @Required
  public static LinkField MONTH;

  @DoublePrecision(4)
  public static DoubleField PLANNED_AMOUNT;

  @DoublePrecision(4)
  public static DoubleField ACTUAL_AMOUNT;

  @DefaultInteger(1)
  @Required
  public static IntegerField DAY;

  @Required
  public static BooleanField ACTIVE;

  public static MultiFieldUniqueIndex SERIES_INDEX;

  static {
    GlobTypeLoader loader = GlobTypeLoader.init(SeriesBudget.class, "seriesBudget");
    loader.defineMultiFieldUniqueIndex(SERIES_INDEX, SERIES, MONTH);
  }

  public static GlobList getAll(Glob series, GlobRepository repository) {
    return getAll(series.get(Series.ID), repository);

  }

  public static GlobList getAll(Integer seriesId, GlobRepository repository) {
    return repository
      .findByIndex(SeriesBudget.SERIES_INDEX, SeriesBudget.SERIES, seriesId)
      .getGlobs();
  }

  public static GlobList getAll(Integer seriesId, Integer monthId, GlobRepository repository) {
    return repository
      .findByIndex(SeriesBudget.SERIES_INDEX, SeriesBudget.SERIES, seriesId)
      .findByIndex(SeriesBudget.MONTH, monthId)
      .getGlobs();
  }

  public static void deleteAllForSeries(Glob series, GlobRepository repository) {
    repository.delete(SeriesBudget.TYPE, GlobMatchers.fieldEquals(SeriesBudget.SERIES, series.get(Series.ID)));
  }

  public static Glob get(Integer seriesId, Integer monthId, GlobRepository repository) {
    return repository
      .findByIndex(SeriesBudget.SERIES_INDEX, SeriesBudget.SERIES, seriesId)
      .findByIndex(SeriesBudget.MONTH, monthId)
      .getGlobs()
      .getFirst();
  }

  public static Glob findOrCreate(Integer seriesId, Integer monthId, GlobRepository repository) {
    Glob result = find(seriesId, monthId, repository);
    if (result != null) {
      return result;
    }
    return repository.create(SeriesBudget.TYPE,
                             value(SeriesBudget.SERIES, seriesId),
                             value(SeriesBudget.MONTH, monthId));
  }

  public static Glob find(Integer seriesId, Integer monthId, GlobRepository repository) {
    GlobList list = getAll(seriesId, monthId, repository);
    if (list.size() > 1) {
      throw new InvalidState("Only one budget should exist for " + seriesId + " / " + monthId);
    }
    if (list.size() == 1) {
      return list.getFirst();
    }
    return null;
  }

  public static class Serializer implements PicsouGlobSerializer {

    public byte[] serializeData(FieldValues fieldValues) {
      SerializedByteArrayOutput serializedByteArrayOutput = new SerializedByteArrayOutput();
      SerializedOutput output = serializedByteArrayOutput.getOutput();
      output.writeInteger(fieldValues.get(SeriesBudget.SERIES));
      output.writeInteger(fieldValues.get(SeriesBudget.MONTH));
      output.writeDouble(fieldValues.get(SeriesBudget.PLANNED_AMOUNT));
      output.writeInteger(fieldValues.get(SeriesBudget.DAY));
      output.writeBoolean(fieldValues.get(SeriesBudget.ACTIVE));
      output.writeDouble(fieldValues.get(SeriesBudget.ACTUAL_AMOUNT));
      return serializedByteArrayOutput.toByteArray();
    }

    public void deserializeData(int version, FieldSetter fieldSetter, byte[] data, Integer id) {
      if (version == 1) {
        deserializeDataV1(fieldSetter, data);
      }
      if (version == 2) {
        deserializeDataV2(fieldSetter, data);
      }
    }

    private void deserializeDataV1(FieldSetter fieldSetter, byte[] data) {
      SerializedInput input = SerializedInputOutputFactory.init(data);
      fieldSetter.set(SeriesBudget.SERIES, input.readInteger());
      fieldSetter.set(SeriesBudget.MONTH, input.readInteger());
      fieldSetter.set(SeriesBudget.PLANNED_AMOUNT, input.readDouble());
      fieldSetter.set(SeriesBudget.DAY, input.readInteger());
      fieldSetter.set(SeriesBudget.ACTIVE, input.readBoolean());
      fieldSetter.set(SeriesBudget.ACTUAL_AMOUNT, null);
    }

    private void deserializeDataV2(FieldSetter fieldSetter, byte[] data) {
      SerializedInput input = SerializedInputOutputFactory.init(data);
      fieldSetter.set(SeriesBudget.SERIES, input.readInteger());
      fieldSetter.set(SeriesBudget.MONTH, input.readInteger());
      fieldSetter.set(SeriesBudget.PLANNED_AMOUNT, input.readDouble());
      fieldSetter.set(SeriesBudget.DAY, input.readInteger());
      fieldSetter.set(SeriesBudget.ACTIVE, input.readBoolean());
      fieldSetter.set(SeriesBudget.ACTUAL_AMOUNT, input.readDouble());
    }

    public int getWriteVersion() {
      return 2;
    }

    public boolean shouldBeSaved(GlobRepository repository, FieldValues fieldValues) {
      return true;
    }
  }
}
