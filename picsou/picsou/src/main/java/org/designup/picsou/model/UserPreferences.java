package org.designup.picsou.model;

import com.budgetview.shared.utils.PicsouGlobSerializer;
import org.apache.commons.lang3.RandomStringUtils;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.*;
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

  @DefaultBoolean(false)
  public static BooleanField RECONCILIATION_FILTERING_TIP_SHOWN;

  public static IntegerField TRANSACTION_POS1;
  public static IntegerField TRANSACTION_POS2;
  public static IntegerField TRANSACTION_POS3;
  public static IntegerField TRANSACTION_POS4;
  public static IntegerField TRANSACTION_POS5;
  public static IntegerField TRANSACTION_POS6;
  public static IntegerField TRANSACTION_POS7;
  public static IntegerField TRANSACTION_POS8;
  public static IntegerField TRANSACTION_POS9;

  @Target(ColorTheme.class)
  @DefaultInteger(1)
  public static LinkField COLOR_THEME;

  @Target(NumericDateType.class)
  public static LinkField NUMERIC_DATE_TYPE;

  @Target(TextDateType.class)
  public static LinkField TEXT_DATE_TYPE;

  public static StringField MAIL_FOR_MOBILE;
  public static StringField PASSWORD_FOR_MOBILE;

  @DefaultBoolean(false)
  public static BooleanField SHOW_TRANSACTION_GRAPH;

  @DefaultBoolean(true)
  public static BooleanField SHOW_MAIN_ACCOUNT_LIST_IN_SUMMARY;

  @DefaultBoolean(true)
  public static BooleanField SHOW_SAVINGS_ACCOUNT_LIST_IN_SUMMARY;

  /** @deprecated */
  @Target(AnalysisViewType.class)
  @DefaultInteger(0)
  public static LinkField ANALYSIS_VIEW_TYPE;

  @DefaultDouble(-100.00)
  public static DoubleField RAINY_WEATHER_THRESHOLD;

  static {
    GlobTypeLoader.init(UserPreferences.class, "userPreferences");
    KEY = org.globsframework.model.Key.create(TYPE, SINGLETON_ID);
  }

  public static Glob get(GlobRepository repository) {
    return repository.get(KEY);
  }

  public static boolean isReconciliationShown(GlobRepository repository) {
    Glob prefs = repository.find(KEY);
    return (prefs != null) && prefs.isTrue(SHOW_RECONCILIATION);
  }

  public static void initMobilePassword(GlobRepository repository, boolean force) {
    Glob prefs = repository.findOrCreate(KEY);
    if (prefs.get(PASSWORD_FOR_MOBILE) == null || force) {
      repository.update(KEY, PASSWORD_FOR_MOBILE, RandomStringUtils.randomAlphanumeric(6).toLowerCase());
    }
  }

  public static class Serializer implements PicsouGlobSerializer {

    public int getWriteVersion() {
      return 21;
    }

    public boolean shouldBeSaved(GlobRepository repository, FieldValues fieldValues) {
      return true;
    }

    public byte[] serializeData(FieldValues values) {
      SerializedByteArrayOutput serializedByteArrayOutput = new SerializedByteArrayOutput();
      SerializedOutput outputStream = serializedByteArrayOutput.getOutput();
      outputStream.writeUtf8String(values.get(LAST_IMPORT_DIRECTORY));
      outputStream.writeUtf8String(values.get(LAST_BACKUP_RESTORE_DIRECTORY));
      outputStream.writeInteger(values.get(FUTURE_MONTH_COUNT));
      outputStream.writeBoolean(values.get(REGISTERED_USER));
      outputStream.writeInteger(values.get(CATEGORIZATION_FILTERING_MODE));
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
      outputStream.writeInteger(values.get(COLOR_THEME));
      outputStream.writeInteger(values.get(NUMERIC_DATE_TYPE));
      outputStream.writeInteger(values.get(TEXT_DATE_TYPE));
      outputStream.writeBoolean(values.get(RECONCILIATION_FILTERING_TIP_SHOWN));
      outputStream.writeUtf8String(values.get(MAIL_FOR_MOBILE));
      outputStream.writeUtf8String(values.get(PASSWORD_FOR_MOBILE));
      outputStream.writeBoolean(values.get(SHOW_TRANSACTION_GRAPH));
      outputStream.writeBoolean(values.get(SHOW_MAIN_ACCOUNT_LIST_IN_SUMMARY));
      outputStream.writeBoolean(values.get(SHOW_SAVINGS_ACCOUNT_LIST_IN_SUMMARY));
      outputStream.writeInteger(values.get(ANALYSIS_VIEW_TYPE));
      outputStream.writeDouble(values.get(RAINY_WEATHER_THRESHOLD));
      return serializedByteArrayOutput.toByteArray();
    }

    public void deserializeData(int version, FieldSetter fieldSetter, byte[] data, Integer id) {
      if (version == 21) {
        deserializeDataV21(fieldSetter, data);
      }
      else if (version == 20) {
        deserializeDataV20(fieldSetter, data);
      }
      else if (version == 19) {
        deserializeDataV19(fieldSetter, data);
      }
      else if (version == 18) {
        deserializeDataV18(fieldSetter, data);
      }
      else if (version == 17) {
        deserializeDataV17(fieldSetter, data);
      }
      else if (version == 16) {
        deserializeDataV16(fieldSetter, data);
      }
      else if (version == 15) {
        deserializeDataV15(fieldSetter, data);
      }
      else if (version == 14) {
        deserializeDataV14(fieldSetter, data);
      }
      else if (version == 13) {
        deserializeDataV13(fieldSetter, data);
      }
      else if (version == 12) {
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

    private void deserializeDataV21(FieldSetter fieldSetter, byte[] data) {
      SerializedInput input = SerializedInputOutputFactory.init(data);
      fieldSetter.set(LAST_IMPORT_DIRECTORY, input.readUtf8String());
      fieldSetter.set(LAST_BACKUP_RESTORE_DIRECTORY, input.readUtf8String());
      fieldSetter.set(FUTURE_MONTH_COUNT, input.readInteger());
      fieldSetter.set(REGISTERED_USER, input.readBoolean());
      fieldSetter.set(CATEGORIZATION_FILTERING_MODE, input.readInteger());
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
      fieldSetter.set(COLOR_THEME, input.readInteger());
      fieldSetter.set(NUMERIC_DATE_TYPE, input.readInteger());
      fieldSetter.set(TEXT_DATE_TYPE, input.readInteger());
      fieldSetter.set(RECONCILIATION_FILTERING_TIP_SHOWN, input.readBoolean());
      fieldSetter.set(MAIL_FOR_MOBILE, input.readUtf8String());
      fieldSetter.set(PASSWORD_FOR_MOBILE, input.readUtf8String());
      fieldSetter.set(SHOW_TRANSACTION_GRAPH, input.readBoolean());
      fieldSetter.set(SHOW_MAIN_ACCOUNT_LIST_IN_SUMMARY, input.readBoolean());
      fieldSetter.set(SHOW_SAVINGS_ACCOUNT_LIST_IN_SUMMARY, input.readBoolean());
      fieldSetter.set(ANALYSIS_VIEW_TYPE, input.readInteger());
      fieldSetter.set(RAINY_WEATHER_THRESHOLD, input.readDouble());
    }

    private void deserializeDataV20(FieldSetter fieldSetter, byte[] data) {
      SerializedInput input = SerializedInputOutputFactory.init(data);
      fieldSetter.set(LAST_IMPORT_DIRECTORY, input.readUtf8String());
      fieldSetter.set(LAST_BACKUP_RESTORE_DIRECTORY, input.readUtf8String());
      fieldSetter.set(FUTURE_MONTH_COUNT, input.readInteger());
      fieldSetter.set(REGISTERED_USER, input.readBoolean());
      fieldSetter.set(CATEGORIZATION_FILTERING_MODE, input.readInteger());
      input.readDate(); // LAST_VALID_DAY
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
      fieldSetter.set(COLOR_THEME, input.readInteger());
      fieldSetter.set(NUMERIC_DATE_TYPE, input.readInteger());
      fieldSetter.set(TEXT_DATE_TYPE, input.readInteger());
      fieldSetter.set(RECONCILIATION_FILTERING_TIP_SHOWN, input.readBoolean());
      fieldSetter.set(MAIL_FOR_MOBILE, input.readUtf8String());
      fieldSetter.set(PASSWORD_FOR_MOBILE, input.readUtf8String());
      fieldSetter.set(SHOW_TRANSACTION_GRAPH, input.readBoolean());
      input.readBoolean(); // SHOW_PROJECT_DETAILS
      fieldSetter.set(SHOW_MAIN_ACCOUNT_LIST_IN_SUMMARY, input.readBoolean());
      fieldSetter.set(SHOW_SAVINGS_ACCOUNT_LIST_IN_SUMMARY, input.readBoolean());
      fieldSetter.set(ANALYSIS_VIEW_TYPE, input.readInteger());
    }

    private void deserializeDataV19(FieldSetter fieldSetter, byte[] data) {
      SerializedInput input = SerializedInputOutputFactory.init(data);
      fieldSetter.set(LAST_IMPORT_DIRECTORY, input.readUtf8String());
      fieldSetter.set(LAST_BACKUP_RESTORE_DIRECTORY, input.readUtf8String());
      fieldSetter.set(FUTURE_MONTH_COUNT, input.readInteger());
      fieldSetter.set(REGISTERED_USER, input.readBoolean());
      fieldSetter.set(CATEGORIZATION_FILTERING_MODE, input.readInteger());
      input.readDate(); // LAST_VALID_DAY
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
      fieldSetter.set(COLOR_THEME, input.readInteger());
      fieldSetter.set(NUMERIC_DATE_TYPE, input.readInteger());
      fieldSetter.set(TEXT_DATE_TYPE, input.readInteger());
      fieldSetter.set(RECONCILIATION_FILTERING_TIP_SHOWN, input.readBoolean());
      fieldSetter.set(MAIL_FOR_MOBILE, input.readUtf8String());
      fieldSetter.set(PASSWORD_FOR_MOBILE, input.readUtf8String());
      fieldSetter.set(SHOW_TRANSACTION_GRAPH, input.readBoolean());
      input.readBoolean(); // SHOW_PROJECT_DETAILS
      fieldSetter.set(SHOW_MAIN_ACCOUNT_LIST_IN_SUMMARY, input.readBoolean());
      fieldSetter.set(SHOW_SAVINGS_ACCOUNT_LIST_IN_SUMMARY, input.readBoolean());
      fieldSetter.set(ANALYSIS_VIEW_TYPE, AnalysisViewType.BUDGET.getId());
    }

    private void deserializeDataV18(FieldSetter fieldSetter, byte[] data) {
      SerializedInput input = SerializedInputOutputFactory.init(data);
      fieldSetter.set(LAST_IMPORT_DIRECTORY, input.readUtf8String());
      fieldSetter.set(LAST_BACKUP_RESTORE_DIRECTORY, input.readUtf8String());
      fieldSetter.set(FUTURE_MONTH_COUNT, input.readInteger());
      fieldSetter.set(REGISTERED_USER, input.readBoolean());
      fieldSetter.set(CATEGORIZATION_FILTERING_MODE, input.readInteger());
      input.readDate(); // LAST_VALID_DAY
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
      fieldSetter.set(COLOR_THEME, input.readInteger());
      fieldSetter.set(NUMERIC_DATE_TYPE, input.readInteger());
      fieldSetter.set(TEXT_DATE_TYPE, input.readInteger());
      fieldSetter.set(RECONCILIATION_FILTERING_TIP_SHOWN, input.readBoolean());
      fieldSetter.set(MAIL_FOR_MOBILE, input.readUtf8String());
      fieldSetter.set(PASSWORD_FOR_MOBILE, input.readUtf8String());
      fieldSetter.set(SHOW_TRANSACTION_GRAPH, input.readBoolean());
      input.readBoolean(); // SHOW_PROJECT_DETAILS
      fieldSetter.set(SHOW_MAIN_ACCOUNT_LIST_IN_SUMMARY, false);
      fieldSetter.set(SHOW_SAVINGS_ACCOUNT_LIST_IN_SUMMARY, false);
      fieldSetter.set(ANALYSIS_VIEW_TYPE, AnalysisViewType.BUDGET.getId());
    }

    private void deserializeDataV17(FieldSetter fieldSetter, byte[] data) {
      SerializedInput input = SerializedInputOutputFactory.init(data);
      fieldSetter.set(LAST_IMPORT_DIRECTORY, input.readUtf8String());
      fieldSetter.set(LAST_BACKUP_RESTORE_DIRECTORY, input.readUtf8String());
      fieldSetter.set(FUTURE_MONTH_COUNT, input.readInteger());
      fieldSetter.set(REGISTERED_USER, input.readBoolean());
      fieldSetter.set(CATEGORIZATION_FILTERING_MODE, input.readInteger());
      input.readDate(); // LAST_VALID_DAY
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
      fieldSetter.set(COLOR_THEME, input.readInteger());
      fieldSetter.set(NUMERIC_DATE_TYPE, input.readInteger());
      fieldSetter.set(TEXT_DATE_TYPE, input.readInteger());
      fieldSetter.set(RECONCILIATION_FILTERING_TIP_SHOWN, input.readBoolean());
      fieldSetter.set(MAIL_FOR_MOBILE, input.readUtf8String());
      fieldSetter.set(PASSWORD_FOR_MOBILE, input.readUtf8String());
      fieldSetter.set(SHOW_TRANSACTION_GRAPH, input.readBoolean());
      input.readBoolean(); // SHOW_PROJECT_DETAILS
      fieldSetter.set(SHOW_MAIN_ACCOUNT_LIST_IN_SUMMARY, false);
      fieldSetter.set(SHOW_SAVINGS_ACCOUNT_LIST_IN_SUMMARY, false);
      fieldSetter.set(ANALYSIS_VIEW_TYPE, AnalysisViewType.BUDGET.getId());
    }

    private void deserializeDataV16(FieldSetter fieldSetter, byte[] data) {
      SerializedInput input = SerializedInputOutputFactory.init(data);
      fieldSetter.set(LAST_IMPORT_DIRECTORY, input.readUtf8String());
      fieldSetter.set(LAST_BACKUP_RESTORE_DIRECTORY, input.readUtf8String());
      fieldSetter.set(FUTURE_MONTH_COUNT, input.readInteger());
      fieldSetter.set(REGISTERED_USER, input.readBoolean());
      fieldSetter.set(CATEGORIZATION_FILTERING_MODE, input.readInteger());
      input.readDate(); // LAST_VALID_DAY
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
      fieldSetter.set(COLOR_THEME, input.readInteger());
      fieldSetter.set(NUMERIC_DATE_TYPE, input.readInteger());
      fieldSetter.set(TEXT_DATE_TYPE, input.readInteger());
      fieldSetter.set(RECONCILIATION_FILTERING_TIP_SHOWN, input.readBoolean());
      fieldSetter.set(MAIL_FOR_MOBILE, input.readUtf8String());
      fieldSetter.set(PASSWORD_FOR_MOBILE, input.readUtf8String());
      fieldSetter.set(SHOW_TRANSACTION_GRAPH, true);
      fieldSetter.set(SHOW_MAIN_ACCOUNT_LIST_IN_SUMMARY, false);
      fieldSetter.set(SHOW_SAVINGS_ACCOUNT_LIST_IN_SUMMARY, false);
      fieldSetter.set(ANALYSIS_VIEW_TYPE, AnalysisViewType.BUDGET.getId());
    }

    private void deserializeDataV15(FieldSetter fieldSetter, byte[] data) {
      SerializedInput input = SerializedInputOutputFactory.init(data);
      fieldSetter.set(LAST_IMPORT_DIRECTORY, input.readUtf8String());
      fieldSetter.set(LAST_BACKUP_RESTORE_DIRECTORY, input.readUtf8String());
      fieldSetter.set(FUTURE_MONTH_COUNT, input.readInteger());
      fieldSetter.set(REGISTERED_USER, input.readBoolean());
      fieldSetter.set(CATEGORIZATION_FILTERING_MODE, input.readInteger());
      input.readDate(); // LAST_VALID_DAY
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
      fieldSetter.set(COLOR_THEME, input.readInteger());
      fieldSetter.set(NUMERIC_DATE_TYPE, input.readInteger());
      fieldSetter.set(TEXT_DATE_TYPE, input.readInteger());
      fieldSetter.set(RECONCILIATION_FILTERING_TIP_SHOWN, input.readBoolean());
      fieldSetter.set(SHOW_TRANSACTION_GRAPH, true);
      fieldSetter.set(SHOW_MAIN_ACCOUNT_LIST_IN_SUMMARY, false);
      fieldSetter.set(SHOW_SAVINGS_ACCOUNT_LIST_IN_SUMMARY, false);
      fieldSetter.set(ANALYSIS_VIEW_TYPE, AnalysisViewType.BUDGET.getId());
    }

    private void deserializeDataV14(FieldSetter fieldSetter, byte[] data) {
      SerializedInput input = SerializedInputOutputFactory.init(data);
      fieldSetter.set(LAST_IMPORT_DIRECTORY, input.readUtf8String());
      fieldSetter.set(LAST_BACKUP_RESTORE_DIRECTORY, input.readUtf8String());
      fieldSetter.set(FUTURE_MONTH_COUNT, input.readInteger());
      fieldSetter.set(REGISTERED_USER, input.readBoolean());
      fieldSetter.set(CATEGORIZATION_FILTERING_MODE, input.readInteger());
      input.readDate(); // LAST_VALID_DAY
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
      fieldSetter.set(COLOR_THEME, input.readInteger());
      fieldSetter.set(NUMERIC_DATE_TYPE, input.readInteger());
      fieldSetter.set(TEXT_DATE_TYPE, input.readInteger());
      fieldSetter.set(SHOW_TRANSACTION_GRAPH, false);
      fieldSetter.set(SHOW_TRANSACTION_GRAPH, true);
      fieldSetter.set(SHOW_MAIN_ACCOUNT_LIST_IN_SUMMARY, false);
      fieldSetter.set(SHOW_SAVINGS_ACCOUNT_LIST_IN_SUMMARY, false);
      fieldSetter.set(ANALYSIS_VIEW_TYPE, AnalysisViewType.BUDGET.getId());
    }

    private void deserializeDataV13(FieldSetter fieldSetter, byte[] data) {
      SerializedInput input = SerializedInputOutputFactory.init(data);
      fieldSetter.set(LAST_IMPORT_DIRECTORY, input.readUtf8String());
      fieldSetter.set(LAST_BACKUP_RESTORE_DIRECTORY, input.readUtf8String());
      fieldSetter.set(FUTURE_MONTH_COUNT, input.readInteger());
      fieldSetter.set(REGISTERED_USER, input.readBoolean());
      fieldSetter.set(CATEGORIZATION_FILTERING_MODE, input.readInteger());
      input.readDate(); // LAST_VALID_DAY
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
      fieldSetter.set(COLOR_THEME, input.readInteger());
      fieldSetter.set(SHOW_TRANSACTION_GRAPH, true);
      fieldSetter.set(SHOW_MAIN_ACCOUNT_LIST_IN_SUMMARY, false);
      fieldSetter.set(SHOW_SAVINGS_ACCOUNT_LIST_IN_SUMMARY, false);
      fieldSetter.set(ANALYSIS_VIEW_TYPE, AnalysisViewType.BUDGET.getId());
    }

    private void deserializeDataV12(FieldSetter fieldSetter, byte[] data) {
      SerializedInput input = SerializedInputOutputFactory.init(data);
      fieldSetter.set(LAST_IMPORT_DIRECTORY, input.readUtf8String());
      fieldSetter.set(LAST_BACKUP_RESTORE_DIRECTORY, input.readUtf8String());
      fieldSetter.set(FUTURE_MONTH_COUNT, input.readInteger());
      fieldSetter.set(REGISTERED_USER, input.readBoolean());
      fieldSetter.set(CATEGORIZATION_FILTERING_MODE, input.readInteger());
      input.readDate(); // LAST_VALID_DAY
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
      fieldSetter.set(SHOW_TRANSACTION_GRAPH, true);
      fieldSetter.set(SHOW_MAIN_ACCOUNT_LIST_IN_SUMMARY, false);
      fieldSetter.set(SHOW_SAVINGS_ACCOUNT_LIST_IN_SUMMARY, false);
      fieldSetter.set(ANALYSIS_VIEW_TYPE, AnalysisViewType.BUDGET.getId());
    }

    private void deserializeDataV11(FieldSetter fieldSetter, byte[] data) {
      SerializedInput input = SerializedInputOutputFactory.init(data);
      fieldSetter.set(LAST_IMPORT_DIRECTORY, input.readUtf8String());
      fieldSetter.set(LAST_BACKUP_RESTORE_DIRECTORY, input.readUtf8String());
      fieldSetter.set(FUTURE_MONTH_COUNT, input.readInteger());
      fieldSetter.set(REGISTERED_USER, input.readBoolean());
      fieldSetter.set(CATEGORIZATION_FILTERING_MODE, input.readInteger());
      input.readDate(); // LAST_VALID_DAY
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
      fieldSetter.set(SHOW_TRANSACTION_GRAPH, true);
      fieldSetter.set(SHOW_MAIN_ACCOUNT_LIST_IN_SUMMARY, false);
      fieldSetter.set(SHOW_SAVINGS_ACCOUNT_LIST_IN_SUMMARY, false);
      fieldSetter.set(ANALYSIS_VIEW_TYPE, AnalysisViewType.BUDGET.getId());
    }

    private void deserializeDataV10(FieldSetter fieldSetter, byte[] data) {
      SerializedInput input = SerializedInputOutputFactory.init(data);
      fieldSetter.set(LAST_IMPORT_DIRECTORY, input.readUtf8String());
      fieldSetter.set(LAST_BACKUP_RESTORE_DIRECTORY, input.readUtf8String());
      fieldSetter.set(FUTURE_MONTH_COUNT, input.readInteger());
      fieldSetter.set(REGISTERED_USER, input.readBoolean());
      fieldSetter.set(CATEGORIZATION_FILTERING_MODE, input.readInteger());
      input.readDate(); // LAST_VALID_DAY
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
      fieldSetter.set(SHOW_TRANSACTION_GRAPH, true);
      fieldSetter.set(SHOW_MAIN_ACCOUNT_LIST_IN_SUMMARY, false);
      fieldSetter.set(SHOW_SAVINGS_ACCOUNT_LIST_IN_SUMMARY, false);
      fieldSetter.set(ANALYSIS_VIEW_TYPE, AnalysisViewType.BUDGET.getId());
    }

    private void deserializeDataV9(FieldSetter fieldSetter, byte[] data) {
      SerializedInput input = SerializedInputOutputFactory.init(data);
      fieldSetter.set(LAST_IMPORT_DIRECTORY, input.readUtf8String());
      fieldSetter.set(LAST_BACKUP_RESTORE_DIRECTORY, input.readUtf8String());
      fieldSetter.set(FUTURE_MONTH_COUNT, input.readInteger());
      fieldSetter.set(REGISTERED_USER, input.readBoolean());
      fieldSetter.set(CATEGORIZATION_FILTERING_MODE, input.readInteger());
      input.readDate(); // LAST_VALID_DAY
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
      fieldSetter.set(SHOW_TRANSACTION_GRAPH, true);
      fieldSetter.set(SHOW_MAIN_ACCOUNT_LIST_IN_SUMMARY, false);
      fieldSetter.set(SHOW_SAVINGS_ACCOUNT_LIST_IN_SUMMARY, false);
      fieldSetter.set(ANALYSIS_VIEW_TYPE, AnalysisViewType.BUDGET.getId());
    }

    private void deserializeDataV8(FieldSetter fieldSetter, byte[] data) {
      SerializedInput input = SerializedInputOutputFactory.init(data);
      fieldSetter.set(LAST_IMPORT_DIRECTORY, input.readUtf8String());
      fieldSetter.set(LAST_BACKUP_RESTORE_DIRECTORY, input.readUtf8String());
      fieldSetter.set(FUTURE_MONTH_COUNT, input.readInteger());
      fieldSetter.set(REGISTERED_USER, input.readBoolean());
      fieldSetter.set(CATEGORIZATION_FILTERING_MODE, input.readInteger());
      input.readDate(); // LAST_VALID_DAY
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
      fieldSetter.set(SHOW_TRANSACTION_GRAPH, true);
      fieldSetter.set(SHOW_MAIN_ACCOUNT_LIST_IN_SUMMARY, false);
      fieldSetter.set(SHOW_SAVINGS_ACCOUNT_LIST_IN_SUMMARY, false);
      fieldSetter.set(ANALYSIS_VIEW_TYPE, AnalysisViewType.BUDGET.getId());
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
      input.readDate(); // LAST_VALID_DAY
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
      fieldSetter.set(SHOW_TRANSACTION_GRAPH, true);
      fieldSetter.set(SHOW_MAIN_ACCOUNT_LIST_IN_SUMMARY, false);
      fieldSetter.set(SHOW_SAVINGS_ACCOUNT_LIST_IN_SUMMARY, false);
      fieldSetter.set(ANALYSIS_VIEW_TYPE, AnalysisViewType.BUDGET.getId());
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
      input.readDate(); // LAST_VALID_DAY
      fieldSetter.set(SHOW_BUDGET_AREA_DESCRIPTIONS, true);
      fieldSetter.set(PERIOD_COUNT_FOR_PLANNED, 6);
      fieldSetter.set(MONTH_FOR_PLANNED, 1);
      fieldSetter.set(MULTIPLE_PLANNED, false);
      fieldSetter.set(EXIT_COUNT, 0);
      fieldSetter.set(SHOW_TRANSACTION_GRAPH, true);
      fieldSetter.set(SHOW_MAIN_ACCOUNT_LIST_IN_SUMMARY, false);
      fieldSetter.set(SHOW_SAVINGS_ACCOUNT_LIST_IN_SUMMARY, false);
      fieldSetter.set(ANALYSIS_VIEW_TYPE, AnalysisViewType.BUDGET.getId());
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
      input.readDate(); // LAST_VALID_DAY
      fieldSetter.set(SHOW_BUDGET_AREA_DESCRIPTIONS, true);
      fieldSetter.set(PERIOD_COUNT_FOR_PLANNED, 6);
      fieldSetter.set(MONTH_FOR_PLANNED, 1);
      fieldSetter.set(MULTIPLE_PLANNED, false);
      fieldSetter.set(EXIT_COUNT, 0);
      fieldSetter.set(SHOW_TRANSACTION_GRAPH, true);
      fieldSetter.set(SHOW_MAIN_ACCOUNT_LIST_IN_SUMMARY, false);
      fieldSetter.set(SHOW_SAVINGS_ACCOUNT_LIST_IN_SUMMARY, false);
      fieldSetter.set(ANALYSIS_VIEW_TYPE, AnalysisViewType.BUDGET.getId());
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
      input.readDate(); // LAST_VALID_DAY
      fieldSetter.set(SHOW_BUDGET_AREA_DESCRIPTIONS, true);
      fieldSetter.set(PERIOD_COUNT_FOR_PLANNED, 6);
      fieldSetter.set(MONTH_FOR_PLANNED, 1);
      fieldSetter.set(MULTIPLE_PLANNED, false);
      fieldSetter.set(EXIT_COUNT, 0);
      fieldSetter.set(SHOW_TRANSACTION_GRAPH, true);
      fieldSetter.set(SHOW_MAIN_ACCOUNT_LIST_IN_SUMMARY, false);
      fieldSetter.set(SHOW_SAVINGS_ACCOUNT_LIST_IN_SUMMARY, false);
      fieldSetter.set(ANALYSIS_VIEW_TYPE, AnalysisViewType.BUDGET.getId());
    }

    private void deserializeDataV3(FieldSetter fieldSetter, byte[] data) {
      SerializedInput input = SerializedInputOutputFactory.init(data);
      fieldSetter.set(LAST_IMPORT_DIRECTORY, input.readUtf8String());
      fieldSetter.set(FUTURE_MONTH_COUNT, input.readInteger());
      fieldSetter.set(REGISTERED_USER, input.readBoolean());
      fieldSetter.set(CATEGORIZATION_FILTERING_MODE, input.readInteger());
      input.readBoolean(); // SHOW_BUDGET_VIEW_WIZARD
      input.readBoolean(); // SHOW_CATEGORIZATION_HELP_MESSAGE
      input.readDate(); // LAST_VALID_DAY
      fieldSetter.set(SHOW_BUDGET_AREA_DESCRIPTIONS, true);
      fieldSetter.set(PERIOD_COUNT_FOR_PLANNED, 6);
      fieldSetter.set(MONTH_FOR_PLANNED, 1);
      fieldSetter.set(MULTIPLE_PLANNED, false);
      fieldSetter.set(EXIT_COUNT, 0);
      fieldSetter.set(SHOW_TRANSACTION_GRAPH, true);
      fieldSetter.set(SHOW_MAIN_ACCOUNT_LIST_IN_SUMMARY, false);
      fieldSetter.set(SHOW_SAVINGS_ACCOUNT_LIST_IN_SUMMARY, false);
      fieldSetter.set(ANALYSIS_VIEW_TYPE, AnalysisViewType.BUDGET.getId());
    }

    private void deserializeDataV2(FieldSetter fieldSetter, byte[] data) {
      SerializedInput input = SerializedInputOutputFactory.init(data);
      fieldSetter.set(LAST_IMPORT_DIRECTORY, input.readJavaString());
      fieldSetter.set(FUTURE_MONTH_COUNT, input.readInteger());
      fieldSetter.set(REGISTERED_USER, input.readBoolean());
      fieldSetter.set(CATEGORIZATION_FILTERING_MODE, input.readInteger());
      input.readDate(); // LAST_VALID_DAY
      fieldSetter.set(SHOW_BUDGET_AREA_DESCRIPTIONS, true);
      fieldSetter.set(PERIOD_COUNT_FOR_PLANNED, 6);
      fieldSetter.set(MONTH_FOR_PLANNED, 1);
      fieldSetter.set(MULTIPLE_PLANNED, false);
      fieldSetter.set(EXIT_COUNT, 0);
      fieldSetter.set(SHOW_TRANSACTION_GRAPH, true);
      fieldSetter.set(SHOW_MAIN_ACCOUNT_LIST_IN_SUMMARY, false);
      fieldSetter.set(SHOW_SAVINGS_ACCOUNT_LIST_IN_SUMMARY, false);
      fieldSetter.set(ANALYSIS_VIEW_TYPE, AnalysisViewType.BUDGET.getId());
    }

    private void deserializeDataV1(FieldSetter fieldSetter, byte[] data) {
      SerializedInput input = SerializedInputOutputFactory.init(data);
      fieldSetter.set(LAST_IMPORT_DIRECTORY, input.readJavaString());
      fieldSetter.set(FUTURE_MONTH_COUNT, input.readInteger());
      fieldSetter.set(REGISTERED_USER, input.readBoolean());
      input.readDate(); // LAST_VALID_DAY
      fieldSetter.set(SHOW_BUDGET_AREA_DESCRIPTIONS, true);
      fieldSetter.set(PERIOD_COUNT_FOR_PLANNED, 6);
      fieldSetter.set(MONTH_FOR_PLANNED, 1);
      fieldSetter.set(MULTIPLE_PLANNED, false);
      fieldSetter.set(EXIT_COUNT, 0);
      fieldSetter.set(SHOW_TRANSACTION_GRAPH, true);
      fieldSetter.set(SHOW_MAIN_ACCOUNT_LIST_IN_SUMMARY, false);
      fieldSetter.set(SHOW_SAVINGS_ACCOUNT_LIST_IN_SUMMARY, false);
      fieldSetter.set(ANALYSIS_VIEW_TYPE, AnalysisViewType.BUDGET.getId());
    }
  }
}