package org.designup.picsou.model;

import org.designup.picsou.server.serialization.PicsouGlobSerializer;
import org.globsframework.metamodel.Field;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.DefaultInteger;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.annotations.Target;
import org.globsframework.metamodel.annotations.NoObfuscation;
import org.globsframework.metamodel.fields.BooleanField;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.fields.LinkField;
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

  public static BooleanField WELCOME_SHOWN;
  public static BooleanField GOTO_DATA_DONE;
  @NoObfuscation
  public static BooleanField IMPORT_STARTED;
  public static BooleanField GOTO_CATEGORIZATION_DONE;
  @NoObfuscation
  public static BooleanField CATEGORIZATION_SELECTION_DONE;
  @NoObfuscation
  public static BooleanField CATEGORIZATION_AREA_SELECTION_DONE;
  @NoObfuscation
  public static BooleanField FIRST_CATEGORIZATION_DONE;
  @NoObfuscation
  public static BooleanField CATEGORIZATION_SKIPPED;
  @NoObfuscation
  public static BooleanField GOTO_BUDGET_SHOWN;
  public static BooleanField GOTO_BUDGET_DONE;
  public static BooleanField SERIES_AMOUNT_SHOWN;
  public static BooleanField SERIES_AMOUNT_DONE;
  public static BooleanField FIRST_RECONCILIATION_SHOWN;
  public static BooleanField FIRST_RECONCILIATION_DONE;
  public static BooleanField SAVINGS_VIEW_TOGGLE_SHOWN;

  @Target(SignpostSectionType.class)
  @DefaultInteger(0)
  public static LinkField CURRENT_SECTION;

  // Reference series for "edit amount" and "edit periodicity" steps
  public static IntegerField AMOUNT_SERIES;
  public static IntegerField PERIODICITY_SERIES;

  static {
    GlobTypeLoader.init(SignpostStatus.class, "signpostStatus");
    KEY = org.globsframework.model.Key.create(TYPE, SINGLETON_ID);
  }

  public static void init(GlobRepository repository) {
    repository.startChangeSet();
    try {
      setCompleted(SignpostStatus.WELCOME_SHOWN, repository);
      Glob status = repository.get(KEY);
      if (status.get(CURRENT_SECTION) == SignpostSectionType.NOT_STARTED.getId()) {
        repository.update(KEY, CURRENT_SECTION, SignpostSectionType.IMPORT.id);
      }
    }
    finally {
      repository.completeChangeSet();
    }
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
    repository.startChangeSet();
    try {
      repository.findOrCreate(KEY);
      for (Field field : TYPE.getFields()) {
        if (field instanceof BooleanField) {
          repository.update(KEY, field, Boolean.TRUE);
        }
      }
      setInitialGuidanceCompleted(repository);
    }
    finally {
      repository.completeChangeSet();
    }
  }

  public static void setAllBeforeBudgetCompleted(GlobRepository repository) {
    repository.startChangeSet();
    setCompleted(CATEGORIZATION_SELECTION_DONE, repository);
    setCompleted(CATEGORIZATION_AREA_SELECTION_DONE, repository);
    setCompleted(FIRST_CATEGORIZATION_DONE, repository);
    setCompleted(GOTO_CATEGORIZATION_DONE, repository);
    setCompleted(GOTO_DATA_DONE, repository);
    setCompleted(IMPORT_STARTED, repository);
    repository.completeChangeSet();
  }

  public static boolean isInitialGuidanceCompleted(GlobRepository repository) {
    Glob status = repository.find(KEY);
    return (status != null) && Utils.equal(status.get(CURRENT_SECTION), SignpostSectionType.COMPLETED.getId());
  }

  public static void setInitialGuidanceCompleted(GlobRepository repository) {
    repository.update(KEY, CURRENT_SECTION, SignpostSectionType.COMPLETED.getId());
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

  public static void setSection(SignpostSectionType section, GlobRepository repository) {
    repository.startChangeSet();
    try {
      repository.findOrCreate(KEY);
      repository.setTarget(SignpostStatus.KEY,
                           SignpostStatus.CURRENT_SECTION,
                           section.getKey());
    }
    finally {
      repository.completeChangeSet();
    }
  }

  public static class Serializer implements PicsouGlobSerializer {

    public int getWriteVersion() {
      return 8;
    }

    public void deserializeData(int version, FieldSetter fieldSetter, byte[] data, Integer id) {
      if (version == 8) {
        deserializeDataV8(fieldSetter, data);
      }
      else if (version == 7) {
        deserializeDataV7(fieldSetter, data);
      }
      else if (version == 6) {
        deserializeDataV6(fieldSetter, data);
      }
      else if (version == 5) {
        deserializeDataV5(fieldSetter, data);
      }
      else if (version == 4) {
        deserializeDataV4(fieldSetter, data);
      }
      else if (version == 3) {
        deserializeDataV3(fieldSetter, data);
      }
      else if (version == 2) {
        deserializeDataV2(fieldSetter, data);
      }
      else if (version == 1) {
        deserializeDataV1(fieldSetter, data);
      }
    }

    public byte[] serializeData(FieldValues values) {
      SerializedByteArrayOutput serializedByteArrayOutput = new SerializedByteArrayOutput();
      SerializedOutput outputStream = serializedByteArrayOutput.getOutput();
      outputStream.writeBoolean(values.get(IMPORT_STARTED));
      outputStream.writeBoolean(values.get(WELCOME_SHOWN));
      outputStream.writeBoolean(values.get(GOTO_DATA_DONE));
      outputStream.writeBoolean(values.get(GOTO_CATEGORIZATION_DONE));
      outputStream.writeBoolean(values.get(CATEGORIZATION_SELECTION_DONE));
      outputStream.writeBoolean(values.get(CATEGORIZATION_AREA_SELECTION_DONE));
      outputStream.writeBoolean(values.get(GOTO_BUDGET_SHOWN));
      outputStream.writeBoolean(values.get(GOTO_BUDGET_DONE));
      outputStream.writeBoolean(values.get(SERIES_AMOUNT_SHOWN));
      outputStream.writeBoolean(values.get(SERIES_AMOUNT_DONE));
      outputStream.writeInteger(values.get(AMOUNT_SERIES));
      outputStream.writeInteger(values.get(PERIODICITY_SERIES));
      outputStream.writeBoolean(values.get(FIRST_CATEGORIZATION_DONE));
      outputStream.writeBoolean(values.get(CATEGORIZATION_SKIPPED));
      outputStream.writeInteger(values.get(CURRENT_SECTION));
      outputStream.writeBoolean(values.get(FIRST_RECONCILIATION_SHOWN));
      outputStream.writeBoolean(values.get(FIRST_RECONCILIATION_DONE));
      outputStream.writeBoolean(values.get(SAVINGS_VIEW_TOGGLE_SHOWN));
      return serializedByteArrayOutput.toByteArray();
    }

    private void deserializeDataV8(FieldSetter fieldSetter, byte[] data) {
      SerializedInput input = SerializedInputOutputFactory.init(data);
      fieldSetter.set(IMPORT_STARTED, input.readBoolean());
      fieldSetter.set(WELCOME_SHOWN, input.readBoolean());
      fieldSetter.set(GOTO_DATA_DONE, input.readBoolean());
      fieldSetter.set(GOTO_CATEGORIZATION_DONE, input.readBoolean());
      fieldSetter.set(CATEGORIZATION_SELECTION_DONE, input.readBoolean());
      fieldSetter.set(CATEGORIZATION_AREA_SELECTION_DONE, input.readBoolean());
      fieldSetter.set(GOTO_BUDGET_SHOWN, input.readBoolean());
      fieldSetter.set(GOTO_BUDGET_DONE, input.readBoolean());
      fieldSetter.set(SERIES_AMOUNT_SHOWN, input.readBoolean());
      fieldSetter.set(SERIES_AMOUNT_DONE, input.readBoolean());
      fieldSetter.set(AMOUNT_SERIES, input.readInteger());
      fieldSetter.set(PERIODICITY_SERIES, input.readInteger());
      fieldSetter.set(FIRST_CATEGORIZATION_DONE, input.readBoolean());
      fieldSetter.set(CATEGORIZATION_SKIPPED, input.readBoolean());
      fieldSetter.set(CURRENT_SECTION, input.readInteger());
      fieldSetter.set(FIRST_RECONCILIATION_SHOWN, input.readBoolean());
      fieldSetter.set(FIRST_RECONCILIATION_DONE, input.readBoolean());
      fieldSetter.set(SAVINGS_VIEW_TOGGLE_SHOWN, input.readBoolean());
    }

    private void deserializeDataV7(FieldSetter fieldSetter, byte[] data) {
      SerializedInput input = SerializedInputOutputFactory.init(data);
      fieldSetter.set(IMPORT_STARTED, input.readBoolean());
      fieldSetter.set(WELCOME_SHOWN, input.readBoolean());
      fieldSetter.set(GOTO_DATA_DONE, input.readBoolean());
      fieldSetter.set(GOTO_CATEGORIZATION_DONE, input.readBoolean());
      fieldSetter.set(CATEGORIZATION_SELECTION_DONE, input.readBoolean());
      fieldSetter.set(CATEGORIZATION_AREA_SELECTION_DONE, input.readBoolean());
      fieldSetter.set(GOTO_BUDGET_SHOWN, input.readBoolean());
      fieldSetter.set(GOTO_BUDGET_DONE, input.readBoolean());
      fieldSetter.set(SERIES_AMOUNT_SHOWN, input.readBoolean());
      fieldSetter.set(SERIES_AMOUNT_DONE, input.readBoolean());
      fieldSetter.set(AMOUNT_SERIES, input.readInteger());
      fieldSetter.set(PERIODICITY_SERIES, input.readInteger());
      fieldSetter.set(FIRST_CATEGORIZATION_DONE, input.readBoolean());
      fieldSetter.set(CATEGORIZATION_SKIPPED, input.readBoolean());
      fieldSetter.set(CURRENT_SECTION, input.readInteger());
      fieldSetter.set(FIRST_RECONCILIATION_SHOWN, input.readBoolean());
      fieldSetter.set(FIRST_RECONCILIATION_DONE, input.readBoolean());
    }

    private void deserializeDataV6(FieldSetter fieldSetter, byte[] data) {
      SerializedInput input = SerializedInputOutputFactory.init(data);
      fieldSetter.set(IMPORT_STARTED, input.readBoolean());
      fieldSetter.set(WELCOME_SHOWN, input.readBoolean());
      fieldSetter.set(GOTO_DATA_DONE, input.readBoolean());
      fieldSetter.set(GOTO_CATEGORIZATION_DONE, input.readBoolean());
      fieldSetter.set(CATEGORIZATION_SELECTION_DONE, input.readBoolean());
      fieldSetter.set(CATEGORIZATION_AREA_SELECTION_DONE, input.readBoolean());
      fieldSetter.set(GOTO_BUDGET_SHOWN, input.readBoolean());
      fieldSetter.set(GOTO_BUDGET_DONE, input.readBoolean());
      fieldSetter.set(SERIES_AMOUNT_SHOWN, input.readBoolean());
      fieldSetter.set(SERIES_AMOUNT_DONE, input.readBoolean());
      fieldSetter.set(AMOUNT_SERIES, input.readInteger());
      fieldSetter.set(PERIODICITY_SERIES, input.readInteger());
      fieldSetter.set(FIRST_CATEGORIZATION_DONE, input.readBoolean());
      fieldSetter.set(CATEGORIZATION_SKIPPED, input.readBoolean());
      fieldSetter.set(CURRENT_SECTION, input.readInteger());
    }

    private void deserializeDataV5(FieldSetter fieldSetter, byte[] data) {
      SerializedInput input = SerializedInputOutputFactory.init(data);
      fieldSetter.set(IMPORT_STARTED, input.readBoolean());
      fieldSetter.set(WELCOME_SHOWN, input.readBoolean());
      fieldSetter.set(GOTO_DATA_DONE, input.readBoolean());
      fieldSetter.set(GOTO_CATEGORIZATION_DONE, input.readBoolean());
      fieldSetter.set(CATEGORIZATION_SELECTION_DONE, input.readBoolean());
      fieldSetter.set(CATEGORIZATION_AREA_SELECTION_DONE, input.readBoolean());
      fieldSetter.set(GOTO_BUDGET_SHOWN, input.readBoolean());
      fieldSetter.set(GOTO_BUDGET_DONE, input.readBoolean());
      input.readBoolean(); // SERIES_PERIODICITY_SHOWN
      input.readBoolean(); // SERIES_PERIODICITY_DONE
      fieldSetter.set(SERIES_AMOUNT_SHOWN, input.readBoolean());
      fieldSetter.set(SERIES_AMOUNT_DONE, input.readBoolean());
      fieldSetter.set(AMOUNT_SERIES, input.readInteger());
      fieldSetter.set(PERIODICITY_SERIES, input.readInteger());
      fieldSetter.set(FIRST_CATEGORIZATION_DONE, input.readBoolean());
      fieldSetter.set(CATEGORIZATION_SKIPPED, input.readBoolean());
      fieldSetter.set(CURRENT_SECTION, input.readInteger());
    }

    private void deserializeDataV4(FieldSetter fieldSetter, byte[] data) {
      SerializedInput input = SerializedInputOutputFactory.init(data);
      fieldSetter.set(IMPORT_STARTED, input.readBoolean());
      fieldSetter.set(WELCOME_SHOWN, input.readBoolean());
      fieldSetter.set(GOTO_DATA_DONE, input.readBoolean());
      fieldSetter.set(GOTO_CATEGORIZATION_DONE, input.readBoolean());
      fieldSetter.set(CATEGORIZATION_SELECTION_DONE, input.readBoolean());
      fieldSetter.set(CATEGORIZATION_AREA_SELECTION_DONE, input.readBoolean());
      fieldSetter.set(GOTO_BUDGET_SHOWN, input.readBoolean());
      fieldSetter.set(GOTO_BUDGET_DONE, input.readBoolean());
      input.readBoolean(); // SERIES_PERIODICITY_SHOWN
      input.readBoolean(); // SERIES_PERIODICITY_DONE
      fieldSetter.set(SERIES_AMOUNT_SHOWN, input.readBoolean());
      fieldSetter.set(SERIES_AMOUNT_DONE, input.readBoolean());
      fieldSetter.set(AMOUNT_SERIES, input.readInteger());
      fieldSetter.set(PERIODICITY_SERIES, input.readInteger());
      fieldSetter.set(FIRST_CATEGORIZATION_DONE, input.readBoolean());
      fieldSetter.set(CURRENT_SECTION, input.readInteger());
    }

    private void deserializeDataV3(FieldSetter fieldSetter, byte[] data) {
      SerializedInput input = SerializedInputOutputFactory.init(data);
      fieldSetter.set(IMPORT_STARTED, input.readBoolean());
      fieldSetter.set(WELCOME_SHOWN, input.readBoolean());
      fieldSetter.set(GOTO_DATA_DONE, true);
      fieldSetter.set(GOTO_CATEGORIZATION_DONE, true);
      fieldSetter.set(CATEGORIZATION_SELECTION_DONE, input.readBoolean());
      fieldSetter.set(CATEGORIZATION_AREA_SELECTION_DONE, input.readBoolean());
      fieldSetter.set(GOTO_BUDGET_SHOWN, true);
      fieldSetter.set(GOTO_BUDGET_DONE, input.readBoolean());
      input.readBoolean(); // SERIES_PERIODICITY_SHOWN
      input.readBoolean(); // SERIES_PERIODICITY_DONE
      input.readBoolean(); // Skip SERIES_GAUGE_SHOWN
      fieldSetter.set(SERIES_AMOUNT_SHOWN, input.readBoolean());
      fieldSetter.set(SERIES_AMOUNT_DONE, input.readBoolean());
      input.readBoolean(); // Skip END_OF_MONTH_POSITION_SHOWN
      fieldSetter.set(AMOUNT_SERIES, input.readInteger());
      fieldSetter.set(PERIODICITY_SERIES, input.readInteger());
      fieldSetter.set(FIRST_CATEGORIZATION_DONE, input.readBoolean());
      fieldSetter.set(CURRENT_SECTION, SignpostSectionType.COMPLETED.getId());
    }

    private void deserializeDataV2(FieldSetter fieldSetter, byte[] data) {
      SerializedInput input = SerializedInputOutputFactory.init(data);
      fieldSetter.set(IMPORT_STARTED, input.readBoolean());
      fieldSetter.set(WELCOME_SHOWN, input.readBoolean());
      fieldSetter.set(GOTO_DATA_DONE, true);
      fieldSetter.set(GOTO_CATEGORIZATION_DONE, true);
      fieldSetter.set(CATEGORIZATION_SELECTION_DONE, input.readBoolean());
      fieldSetter.set(CATEGORIZATION_AREA_SELECTION_DONE, input.readBoolean());
      fieldSetter.set(GOTO_BUDGET_SHOWN, true);
      fieldSetter.set(GOTO_BUDGET_DONE, input.readBoolean());
      input.readBoolean(); // SERIES_PERIODICITY_SHOWN
      input.readBoolean(); // SERIES_PERIODICITY_DONE
      input.readBoolean(); // Skip SERIES_GAUGE_SHOWN
      fieldSetter.set(SERIES_AMOUNT_SHOWN, input.readBoolean());
      fieldSetter.set(SERIES_AMOUNT_DONE, input.readBoolean());
      input.readBoolean(); // Skip END_OF_MONTH_POSITION_SHOWN
      fieldSetter.set(AMOUNT_SERIES, input.readInteger());
      fieldSetter.set(PERIODICITY_SERIES, input.readInteger());
      fieldSetter.set(CURRENT_SECTION, SignpostSectionType.COMPLETED.getId());
    }

    private void deserializeDataV1(FieldSetter fieldSetter, byte[] data) {
      SerializedInput input = SerializedInputOutputFactory.init(data);
      fieldSetter.set(IMPORT_STARTED, input.readBoolean());
      fieldSetter.set(WELCOME_SHOWN, input.readBoolean());
      fieldSetter.set(GOTO_DATA_DONE, true);
      fieldSetter.set(GOTO_CATEGORIZATION_DONE, true);
      fieldSetter.set(CATEGORIZATION_SELECTION_DONE, input.readBoolean());
      fieldSetter.set(CATEGORIZATION_AREA_SELECTION_DONE, input.readBoolean());
      fieldSetter.set(GOTO_BUDGET_SHOWN, true);
      fieldSetter.set(GOTO_BUDGET_DONE, input.readBoolean());
      input.readBoolean(); // SERIES_PERIODICITY_SHOWN
      input.readBoolean(); // SERIES_PERIODICITY_DONE
      input.readBoolean(); // Skip SERIES_GAUGE_SHOWN
      fieldSetter.set(SERIES_AMOUNT_SHOWN, input.readBoolean());
      fieldSetter.set(SERIES_AMOUNT_DONE, input.readBoolean());
      input.readBoolean(); // Skip END_OF_MONTH_POSITION_SHOWN
      fieldSetter.set(CURRENT_SECTION, SignpostSectionType.COMPLETED.getId());
    }
  }
}
