package org.designup.picsou.model;

import org.designup.picsou.gui.time.TimeService;
import org.designup.picsou.server.serialization.PicsouGlobSerializer;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.DefaultBoolean;
import org.globsframework.metamodel.annotations.DefaultInteger;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.annotations.Target;
import org.globsframework.metamodel.fields.*;
import org.globsframework.metamodel.utils.GlobTypeLoader;
import org.globsframework.model.FieldSetter;
import org.globsframework.model.FieldValues;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.serialization.SerializedByteArrayOutput;
import org.globsframework.utils.serialization.SerializedInput;
import org.globsframework.utils.serialization.SerializedInputOutputFactory;
import org.globsframework.utils.serialization.SerializedOutput;

public class UserPreferences {

  public static final Integer SINGLETON_ID = 0;
  public static org.globsframework.model.Key KEY;

  public static GlobType TYPE;

  @Key
  public static IntegerField ID;

  public static StringField LAST_IMPORT_DIRECTORY;

  public static StringField LAST_BACKUP_RESTORE_DIRECTORY;

  @DefaultInteger(12)
  public static IntegerField FUTURE_MONTH_COUNT;

  @DefaultBoolean(false)
  public static BooleanField REGISTERED_USER;

  @DefaultInteger(1)
  public static IntegerField CATEGORIZATION_FILTERING_MODE;

  @Target(SeriesOrder.class)
  public static LinkField SERIES_ORDER_INCOME;

  @Target(SeriesOrder.class)
  public static LinkField SERIES_ORDER_SAVINGS;

  @Target(SeriesOrder.class)
  public static LinkField SERIES_ORDER_RECURRING;

  @Target(SeriesOrder.class)
  public static LinkField SERIES_ORDER_VARIABLE;

  @Target(SeriesOrder.class)
  public static LinkField SERIES_ORDER_EXTRA;

  public static DateField LAST_VALID_DAY;

  @DefaultInteger(10)
  public static IntegerField PERIOD_COUNT_FOR_PLANNED;

  @DefaultInteger(3)
  public static IntegerField MONTH_FOR_PLANNED;

  @DefaultBoolean(true)
  public static BooleanField MULTIPLE_PLANNED;

  @DefaultBoolean(true)
  public static BooleanField SHOW_BUDGET_AREA_DESCRIPTIONS;

  @DefaultBoolean(false)
  public static BooleanField SHOW_RECONCILIATION;

  @DefaultInteger(0)
  public static IntegerField EXIT_COUNT;

  @DefaultBoolean(false)
  public static BooleanField EVALUATION_SHOWN;

  public static IntegerField TRANSACTION_POS1;
  public static IntegerField TRANSACTION_POS2;
  public static IntegerField TRANSACTION_POS3;
  public static IntegerField TRANSACTION_POS4;
  public static IntegerField TRANSACTION_POS5;
  public static IntegerField TRANSACTION_POS6;
  public static IntegerField TRANSACTION_POS7;
  public static IntegerField TRANSACTION_POS8;
  public static IntegerField TRANSACTION_POS9;

  static {
    GlobTypeLoader.init(UserPreferences.class, "userPreferences");
    KEY = org.globsframework.model.Key.create(TYPE, SINGLETON_ID);
  }

  public static boolean isReconciliationShown(GlobRepository repository) {
    Glob prefs = repository.findOrCreate(KEY);
    return prefs.isTrue(SHOW_RECONCILIATION);
  }

  public static class Serializer implements PicsouGlobSerializer {

    public int getWriteVersion() {
      return 12;
    }
    
    public byte[] serializeData(FieldValues values) {
      SerializedByteArrayOutput serializedByteArrayOutput = new SerializedByteArrayOutput();
      SerializedOutput outputStream = serializedByteArrayOutput.getOutput();
      outputStream.writeUtf8String(values.get(LAST_IMPORT_DIRECTORY));
      outputStream.writeUtf8String(values.get(LAST_BACKUP_RESTORE_DIRECTORY));
      outputStream.writeInteger(values.get(FUTURE_MONTH_COUNT));
      outputStream.writeBoolean(values.get(REGISTERED_USER));
      outputStream.writeInteger(values.get(CATEGORIZATION_FILTERING_MODE));
      outputStream.writeDate(values.get(LAST_VALID_DAY));
      outputStream.writeInteger(values.get(SERIES_ORDER_INCOME));
      outputStream.writeInteger(values.get(SERIES_ORDER_RECURRING));
      outputStream.writeInteger(values.get(SERIES_ORDER_VARIABLE));
      outputStream.writeInteger(values.get(SERIES_ORDER_SAVINGS));
      outputStream.writeInteger(values.get(SERIES_ORDER_EXTRA));
      outputStream.writeBoolean(values.get(SHOW_BUDGET_AREA_DESCRIPTIONS));
      outputStream.writeInteger(values.get(PERIOD_COUNT_FOR_PLANNED));
      outputStream.writeInteger(values.get(MONTH_FOR_PLANNED));
      outputStream.writeBoolean(values.get(MULTIPLE_PLANNED));
      outputStream.writeBoolean(values.get(SHOW_RECONCILIATION));
      outputStream.writeInteger(values.get(EXIT_COUNT));
      outputStream.writeBoolean(values.get(EVALUATION_SHOWN));
      outputStream.writeInteger(values.get(TRANSACTION_POS1));
      outputStream.writeInteger(values.get(TRANSACTION_POS2));
      outputStream.writeInteger(values.get(TRANSACTION_POS3));
      outputStream.writeInteger(values.get(TRANSACTION_POS4));
      outputStream.writeInteger(values.get(TRANSACTION_POS5));
      outputStream.writeInteger(values.get(TRANSACTION_POS6));
      outputStream.writeInteger(values.get(TRANSACTION_POS7));
      outputStream.writeInteger(values.get(TRANSACTION_POS8));
      outputStream.writeInteger(values.get(TRANSACTION_POS9));
      return serializedByteArrayOutput.toByteArray();
    }

    public void deserializeData(int version, FieldSetter fieldSetter, byte[] data, Integer id) {
      if (version == 12) {
        deserializeDataV12(fieldSetter, data);
      }
      else if (version == 11) {
        deserializeDataV11(fieldSetter, data);
      }
      else if (version == 10) {
        deserializeDataV10(fieldSetter, data);
      }
      else if (version == 9) {
        deserializeDataV9(fieldSetter, data);
      }
      else if (version == 8) {
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

    private void deserializeDataV12(FieldSetter fieldSetter, byte[] data) {
      SerializedInput input = SerializedInputOutputFactory.init(data);
      fieldSetter.set(LAST_IMPORT_DIRECTORY, input.readUtf8String());
      fieldSetter.set(LAST_BACKUP_RESTORE_DIRECTORY, input.readUtf8String());
      fieldSetter.set(FUTURE_MONTH_COUNT, input.readInteger());
      fieldSetter.set(REGISTERED_USER, input.readBoolean());
      fieldSetter.set(CATEGORIZATION_FILTERING_MODE, input.readInteger());
      fieldSetter.set(LAST_VALID_DAY, input.readDate());
      fieldSetter.set(SERIES_ORDER_INCOME, input.readInteger());
      fieldSetter.set(SERIES_ORDER_RECURRING, input.readInteger());
      fieldSetter.set(SERIES_ORDER_VARIABLE, input.readInteger());
      fieldSetter.set(SERIES_ORDER_SAVINGS, input.readInteger());
      fieldSetter.set(SERIES_ORDER_EXTRA, input.readInteger());
      fieldSetter.set(SHOW_BUDGET_AREA_DESCRIPTIONS, input.readBoolean());
      fieldSetter.set(PERIOD_COUNT_FOR_PLANNED, input.readInteger());
      fieldSetter.set(MONTH_FOR_PLANNED, input.readInteger());
      fieldSetter.set(MULTIPLE_PLANNED, input.readBoolean());
      fieldSetter.set(SHOW_RECONCILIATION, input.readBoolean());
      fieldSetter.set(EXIT_COUNT, input.readInteger());
      fieldSetter.set(EVALUATION_SHOWN, input.readBoolean());
      fieldSetter.set(TRANSACTION_POS1, input.readInteger());
      fieldSetter.set(TRANSACTION_POS2, input.readInteger());
      fieldSetter.set(TRANSACTION_POS3, input.readInteger());
      fieldSetter.set(TRANSACTION_POS4, input.readInteger());
      fieldSetter.set(TRANSACTION_POS5, input.readInteger());
      fieldSetter.set(TRANSACTION_POS6, input.readInteger());
      fieldSetter.set(TRANSACTION_POS7, input.readInteger());
      fieldSetter.set(TRANSACTION_POS8, input.readInteger());
      fieldSetter.set(TRANSACTION_POS9, input.readInteger());
    }

    private void deserializeDataV11(FieldSetter fieldSetter, byte[] data) {
      SerializedInput input = SerializedInputOutputFactory.init(data);
      fieldSetter.set(LAST_IMPORT_DIRECTORY, input.readUtf8String());
      fieldSetter.set(LAST_BACKUP_RESTORE_DIRECTORY, input.readUtf8String());
      fieldSetter.set(FUTURE_MONTH_COUNT, input.readInteger());
      fieldSetter.set(REGISTERED_USER, input.readBoolean());
      fieldSetter.set(CATEGORIZATION_FILTERING_MODE, input.readInteger());
      fieldSetter.set(LAST_VALID_DAY, input.readDate());
      fieldSetter.set(SERIES_ORDER_INCOME, input.readInteger());
      fieldSetter.set(SERIES_ORDER_RECURRING, input.readInteger());
      fieldSetter.set(SERIES_ORDER_VARIABLE, input.readInteger());
      fieldSetter.set(SERIES_ORDER_SAVINGS, input.readInteger());
      fieldSetter.set(SERIES_ORDER_EXTRA, input.readInteger());
      fieldSetter.set(SHOW_BUDGET_AREA_DESCRIPTIONS, input.readBoolean());
      fieldSetter.set(PERIOD_COUNT_FOR_PLANNED, input.readInteger());
      fieldSetter.set(MONTH_FOR_PLANNED, input.readInteger());
      fieldSetter.set(MULTIPLE_PLANNED, input.readBoolean());
      fieldSetter.set(SHOW_RECONCILIATION, input.readBoolean());
      fieldSetter.set(EXIT_COUNT, 0);
      fieldSetter.set(TRANSACTION_POS1, input.readInteger());
      fieldSetter.set(TRANSACTION_POS2, input.readInteger());
      fieldSetter.set(TRANSACTION_POS3, input.readInteger());
      fieldSetter.set(TRANSACTION_POS4, input.readInteger());
      fieldSetter.set(TRANSACTION_POS5, input.readInteger());
      fieldSetter.set(TRANSACTION_POS6, input.readInteger());
      fieldSetter.set(TRANSACTION_POS7, input.readInteger());
      fieldSetter.set(TRANSACTION_POS8, input.readInteger());
      fieldSetter.set(TRANSACTION_POS9, input.readInteger());
    }

    private void deserializeDataV10(FieldSetter fieldSetter, byte[] data) {
      SerializedInput input = SerializedInputOutputFactory.init(data);
      fieldSetter.set(LAST_IMPORT_DIRECTORY, input.readUtf8String());
      fieldSetter.set(LAST_BACKUP_RESTORE_DIRECTORY, input.readUtf8String());
      fieldSetter.set(FUTURE_MONTH_COUNT, input.readInteger());
      fieldSetter.set(REGISTERED_USER, input.readBoolean());
      fieldSetter.set(CATEGORIZATION_FILTERING_MODE, input.readInteger());
      fieldSetter.set(LAST_VALID_DAY, input.readDate());
      fieldSetter.set(SERIES_ORDER_INCOME, input.readInteger());
      fieldSetter.set(SERIES_ORDER_RECURRING, input.readInteger());
      fieldSetter.set(SERIES_ORDER_VARIABLE, input.readInteger());
      fieldSetter.set(SERIES_ORDER_SAVINGS, input.readInteger());
      fieldSetter.set(SERIES_ORDER_EXTRA, input.readInteger());
      fieldSetter.set(SHOW_BUDGET_AREA_DESCRIPTIONS, input.readBoolean());
      fieldSetter.set(PERIOD_COUNT_FOR_PLANNED, input.readInteger());
      fieldSetter.set(MONTH_FOR_PLANNED, input.readInteger());
      fieldSetter.set(MULTIPLE_PLANNED, input.readBoolean());
      fieldSetter.set(SHOW_RECONCILIATION, input.readBoolean());
      fieldSetter.set(EXIT_COUNT, 0);
    }

    private void deserializeDataV9(FieldSetter fieldSetter, byte[] data) {
      SerializedInput input = SerializedInputOutputFactory.init(data);
      fieldSetter.set(LAST_IMPORT_DIRECTORY, input.readUtf8String());
      fieldSetter.set(LAST_BACKUP_RESTORE_DIRECTORY, input.readUtf8String());
      fieldSetter.set(FUTURE_MONTH_COUNT, input.readInteger());
      fieldSetter.set(REGISTERED_USER, input.readBoolean());
      fieldSetter.set(CATEGORIZATION_FILTERING_MODE, input.readInteger());
      fieldSetter.set(LAST_VALID_DAY, input.readDate());
      fieldSetter.set(SERIES_ORDER_INCOME, input.readInteger());
      fieldSetter.set(SERIES_ORDER_RECURRING, input.readInteger());
      fieldSetter.set(SERIES_ORDER_VARIABLE, input.readInteger());
      fieldSetter.set(SERIES_ORDER_SAVINGS, input.readInteger());
      fieldSetter.set(SERIES_ORDER_EXTRA, input.readInteger());
      fieldSetter.set(SHOW_BUDGET_AREA_DESCRIPTIONS, input.readBoolean());
      fieldSetter.set(PERIOD_COUNT_FOR_PLANNED, input.readInteger());
      fieldSetter.set(MONTH_FOR_PLANNED, input.readInteger());
      fieldSetter.set(MULTIPLE_PLANNED, input.readBoolean());
      fieldSetter.set(EXIT_COUNT, 0);
    }

    private void deserializeDataV8(FieldSetter fieldSetter, byte[] data) {
      SerializedInput input = SerializedInputOutputFactory.init(data);
      fieldSetter.set(LAST_IMPORT_DIRECTORY, input.readUtf8String());
      fieldSetter.set(LAST_BACKUP_RESTORE_DIRECTORY, input.readUtf8String());
      fieldSetter.set(FUTURE_MONTH_COUNT, input.readInteger());
      fieldSetter.set(REGISTERED_USER, input.readBoolean());
      fieldSetter.set(CATEGORIZATION_FILTERING_MODE, input.readInteger());
      fieldSetter.set(LAST_VALID_DAY, input.readDate());
      fieldSetter.set(SERIES_ORDER_INCOME, input.readInteger());
      fieldSetter.set(SERIES_ORDER_RECURRING, input.readInteger());
      fieldSetter.set(SERIES_ORDER_VARIABLE, input.readInteger());
      fieldSetter.set(SERIES_ORDER_SAVINGS, input.readInteger());
      fieldSetter.set(SERIES_ORDER_EXTRA, input.readInteger());
      fieldSetter.set(SHOW_BUDGET_AREA_DESCRIPTIONS, input.readBoolean());
      fieldSetter.set(PERIOD_COUNT_FOR_PLANNED, 6);
      fieldSetter.set(MONTH_FOR_PLANNED, 1);
      fieldSetter.set(MULTIPLE_PLANNED, false);
      fieldSetter.set(EXIT_COUNT, 0);
    }

    private void deserializeDataV7(FieldSetter fieldSetter, byte[] data) {
      SerializedInput input = SerializedInputOutputFactory.init(data);
      fieldSetter.set(LAST_IMPORT_DIRECTORY, input.readUtf8String());
      fieldSetter.set(LAST_BACKUP_RESTORE_DIRECTORY, input.readUtf8String());
      fieldSetter.set(FUTURE_MONTH_COUNT, input.readInteger());
      fieldSetter.set(REGISTERED_USER, input.readBoolean());
      fieldSetter.set(CATEGORIZATION_FILTERING_MODE, input.readInteger());
      input.readBoolean(); // SHOW_BUDGET_VIEW_WIZARD
      input.readInteger(); // CURRENT_WIZARD_PAGE
      input.readBoolean(); // SHOW_CATEGORIZATION_HELP_MESSAGE
      input.readBoolean(); // SHOW_VARIABLE_EDITION_MESSAGE
      fieldSetter.set(LAST_VALID_DAY, input.readDate());
      fieldSetter.set(SERIES_ORDER_INCOME, input.readInteger());
      fieldSetter.set(SERIES_ORDER_RECURRING, input.readInteger());
      fieldSetter.set(SERIES_ORDER_VARIABLE, input.readInteger());
      fieldSetter.set(SERIES_ORDER_SAVINGS, input.readInteger());
      fieldSetter.set(SERIES_ORDER_EXTRA, input.readInteger());
      fieldSetter.set(SHOW_BUDGET_AREA_DESCRIPTIONS, true);
      fieldSetter.set(PERIOD_COUNT_FOR_PLANNED, 6);
      fieldSetter.set(MONTH_FOR_PLANNED, 1);
      fieldSetter.set(MULTIPLE_PLANNED, false);
      fieldSetter.set(EXIT_COUNT, 0);
    }

    private void deserializeDataV6(FieldSetter fieldSetter, byte[] data) {
      SerializedInput input = SerializedInputOutputFactory.init(data);
      fieldSetter.set(LAST_IMPORT_DIRECTORY, input.readUtf8String());
      fieldSetter.set(LAST_BACKUP_RESTORE_DIRECTORY, input.readUtf8String());
      fieldSetter.set(FUTURE_MONTH_COUNT, input.readInteger());
      fieldSetter.set(REGISTERED_USER, input.readBoolean());
      fieldSetter.set(CATEGORIZATION_FILTERING_MODE, input.readInteger());
      input.readBoolean(); // SHOW_BUDGET_VIEW_WIZARD
      input.readInteger(); // CURRENT_WIZARD_PAGE
      input.readBoolean(); // SHOW_CATEGORIZATION_HELP_MESSAGE
      input.readBoolean(); // SHOW_VARIABLE_EDITION_MESSAGE
      fieldSetter.set(LAST_VALID_DAY, input.readDate());
      fieldSetter.set(SHOW_BUDGET_AREA_DESCRIPTIONS, true);
      fieldSetter.set(PERIOD_COUNT_FOR_PLANNED, 6);
      fieldSetter.set(MONTH_FOR_PLANNED, 1);
      fieldSetter.set(MULTIPLE_PLANNED, false);
      fieldSetter.set(EXIT_COUNT, 0);
    }

    private void deserializeDataV5(FieldSetter fieldSetter, byte[] data) {
      SerializedInput input = SerializedInputOutputFactory.init(data);
      fieldSetter.set(LAST_IMPORT_DIRECTORY, input.readUtf8String());
      fieldSetter.set(LAST_BACKUP_RESTORE_DIRECTORY, input.readUtf8String());
      fieldSetter.set(FUTURE_MONTH_COUNT, input.readInteger());
      fieldSetter.set(REGISTERED_USER, input.readBoolean());
      fieldSetter.set(CATEGORIZATION_FILTERING_MODE, input.readInteger());
      input.readBoolean(); // SHOW_BUDGET_VIEW_WIZARD
      input.readBoolean(); // SHOW_CATEGORIZATION_HELP_MESSAGE
      input.readBoolean(); // SHOW_VARIABLE_EDITION_MESSAGE
      fieldSetter.set(LAST_VALID_DAY, input.readDate());
      fieldSetter.set(SHOW_BUDGET_AREA_DESCRIPTIONS, true);
      fieldSetter.set(PERIOD_COUNT_FOR_PLANNED, 6);
      fieldSetter.set(MONTH_FOR_PLANNED, 1);
      fieldSetter.set(MULTIPLE_PLANNED, false);
      fieldSetter.set(EXIT_COUNT, 0);
    }

    private void deserializeDataV4(FieldSetter fieldSetter, byte[] data) {
      SerializedInput input = SerializedInputOutputFactory.init(data);
      fieldSetter.set(LAST_IMPORT_DIRECTORY, input.readUtf8String());
      fieldSetter.set(LAST_BACKUP_RESTORE_DIRECTORY, input.readUtf8String());
      fieldSetter.set(FUTURE_MONTH_COUNT, input.readInteger());
      fieldSetter.set(REGISTERED_USER, input.readBoolean());
      fieldSetter.set(CATEGORIZATION_FILTERING_MODE, input.readInteger());
      input.readBoolean(); // SHOW_BUDGET_VIEW_WIZARD
      input.readBoolean(); // SHOW_CATEGORIZATION_HELP_MESSAGE
      fieldSetter.set(LAST_VALID_DAY, input.readDate());
      fieldSetter.set(SHOW_BUDGET_AREA_DESCRIPTIONS, true);
      fieldSetter.set(PERIOD_COUNT_FOR_PLANNED, 6);
      fieldSetter.set(MONTH_FOR_PLANNED, 1);
      fieldSetter.set(MULTIPLE_PLANNED, false);
      fieldSetter.set(EXIT_COUNT, 0);
    }

    private void deserializeDataV3(FieldSetter fieldSetter, byte[] data) {
      SerializedInput input = SerializedInputOutputFactory.init(data);
      fieldSetter.set(LAST_IMPORT_DIRECTORY, input.readUtf8String());
      fieldSetter.set(FUTURE_MONTH_COUNT, input.readInteger());
      fieldSetter.set(REGISTERED_USER, input.readBoolean());
      fieldSetter.set(CATEGORIZATION_FILTERING_MODE, input.readInteger());
      input.readBoolean(); // SHOW_BUDGET_VIEW_WIZARD
      input.readBoolean(); // SHOW_CATEGORIZATION_HELP_MESSAGE
      fieldSetter.set(LAST_VALID_DAY, input.readDate());
      fieldSetter.set(SHOW_BUDGET_AREA_DESCRIPTIONS, true);
      fieldSetter.set(PERIOD_COUNT_FOR_PLANNED, 6);
      fieldSetter.set(MONTH_FOR_PLANNED, 1);
      fieldSetter.set(MULTIPLE_PLANNED, false);
      fieldSetter.set(EXIT_COUNT, 0);
    }

    private void deserializeDataV2(FieldSetter fieldSetter, byte[] data) {
      SerializedInput input = SerializedInputOutputFactory.init(data);
      fieldSetter.set(LAST_IMPORT_DIRECTORY, input.readJavaString());
      fieldSetter.set(FUTURE_MONTH_COUNT, input.readInteger());
      fieldSetter.set(REGISTERED_USER, input.readBoolean());
      fieldSetter.set(CATEGORIZATION_FILTERING_MODE, input.readInteger());
      fieldSetter.set(LAST_VALID_DAY, Month.addDurationMonth(TimeService.getToday()));
      fieldSetter.set(SHOW_BUDGET_AREA_DESCRIPTIONS, true);
      fieldSetter.set(PERIOD_COUNT_FOR_PLANNED, 6);
      fieldSetter.set(MONTH_FOR_PLANNED, 1);
      fieldSetter.set(MULTIPLE_PLANNED, false);
      fieldSetter.set(EXIT_COUNT, 0);
    }

    private void deserializeDataV1(FieldSetter fieldSetter, byte[] data) {
      SerializedInput input = SerializedInputOutputFactory.init(data);
      fieldSetter.set(LAST_IMPORT_DIRECTORY, input.readJavaString());
      fieldSetter.set(FUTURE_MONTH_COUNT, input.readInteger());
      fieldSetter.set(REGISTERED_USER, input.readBoolean());
      fieldSetter.set(LAST_VALID_DAY, Month.addDurationMonth(TimeService.getToday()));
      fieldSetter.set(SHOW_BUDGET_AREA_DESCRIPTIONS, true);
      fieldSetter.set(PERIOD_COUNT_FOR_PLANNED, 6);
      fieldSetter.set(MONTH_FOR_PLANNED, 1);
      fieldSetter.set(MULTIPLE_PLANNED, false);
      fieldSetter.set(EXIT_COUNT, 0);
    }
  }

}