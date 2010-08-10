package org.designup.picsou.model;

import org.designup.picsou.gui.TimeService;
import org.designup.picsou.server.serialization.PicsouGlobSerializer;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.DefaultBoolean;
import org.globsframework.metamodel.annotations.DefaultInteger;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.fields.BooleanField;
import org.globsframework.metamodel.fields.DateField;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.fields.StringField;
import org.globsframework.metamodel.utils.GlobTypeLoader;
import org.globsframework.model.FieldSetter;
import org.globsframework.model.FieldValues;
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

  /**
   * 0 : no order 1/2 premiere colonne 3/4 second colonne...
   */
  public static IntegerField ORDER_INCOME;

  public static IntegerField ORDER_SAVINGS;

  public static IntegerField ORDER_RECURRING;

  public static IntegerField ORDER_VARIABLE;

  public static IntegerField ORDER_EXTRA;

  public static DateField LAST_VALID_DAY;

  @DefaultBoolean(true)
  public static BooleanField SHOW_BUDGET_AREA_DESCRIPTIONS;

  static {
    GlobTypeLoader.init(UserPreferences.class, "userPreferences");
    KEY = org.globsframework.model.Key.create(TYPE, SINGLETON_ID);
  }

  public static class Serializer implements PicsouGlobSerializer {

    public int getWriteVersion() {
      return 8;
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
      outputStream.writeInteger(values.get(ORDER_INCOME));
      outputStream.writeInteger(values.get(ORDER_RECURRING));
      outputStream.writeInteger(values.get(ORDER_VARIABLE));
      outputStream.writeInteger(values.get(ORDER_SAVINGS));
      outputStream.writeInteger(values.get(ORDER_EXTRA));
      outputStream.writeBoolean(values.get(SHOW_BUDGET_AREA_DESCRIPTIONS));
      return serializedByteArrayOutput.toByteArray();
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

    private void deserializeDataV8(FieldSetter fieldSetter, byte[] data) {
      SerializedInput input = SerializedInputOutputFactory.init(data);
      fieldSetter.set(LAST_IMPORT_DIRECTORY, input.readUtf8String());
      fieldSetter.set(LAST_BACKUP_RESTORE_DIRECTORY, input.readUtf8String());
      fieldSetter.set(FUTURE_MONTH_COUNT, input.readInteger());
      fieldSetter.set(REGISTERED_USER, input.readBoolean());
      fieldSetter.set(CATEGORIZATION_FILTERING_MODE, input.readInteger());
      fieldSetter.set(LAST_VALID_DAY, input.readDate());
      fieldSetter.set(ORDER_INCOME, input.readInteger());
      fieldSetter.set(ORDER_RECURRING, input.readInteger());
      fieldSetter.set(ORDER_VARIABLE, input.readInteger());
      fieldSetter.set(ORDER_SAVINGS, input.readInteger());
      fieldSetter.set(ORDER_EXTRA, input.readInteger());
      fieldSetter.set(SHOW_BUDGET_AREA_DESCRIPTIONS, input.readBoolean());
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
      fieldSetter.set(ORDER_INCOME, input.readInteger());
      fieldSetter.set(ORDER_RECURRING, input.readInteger());
      fieldSetter.set(ORDER_VARIABLE, input.readInteger());
      fieldSetter.set(ORDER_SAVINGS, input.readInteger());
      fieldSetter.set(ORDER_EXTRA, input.readInteger());
      fieldSetter.set(SHOW_BUDGET_AREA_DESCRIPTIONS, true);
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
    }

    private void deserializeDataV2(FieldSetter fieldSetter, byte[] data) {
      SerializedInput input = SerializedInputOutputFactory.init(data);
      fieldSetter.set(LAST_IMPORT_DIRECTORY, input.readJavaString());
      fieldSetter.set(FUTURE_MONTH_COUNT, input.readInteger());
      fieldSetter.set(REGISTERED_USER, input.readBoolean());
      fieldSetter.set(CATEGORIZATION_FILTERING_MODE, input.readInteger());
      fieldSetter.set(LAST_VALID_DAY, Month.addOneMonth(TimeService.getToday()));
      fieldSetter.set(SHOW_BUDGET_AREA_DESCRIPTIONS, true);
    }

    private void deserializeDataV1(FieldSetter fieldSetter, byte[] data) {
      SerializedInput input = SerializedInputOutputFactory.init(data);
      fieldSetter.set(LAST_IMPORT_DIRECTORY, input.readJavaString());
      fieldSetter.set(FUTURE_MONTH_COUNT, input.readInteger());
      fieldSetter.set(REGISTERED_USER, input.readBoolean());
      fieldSetter.set(LAST_VALID_DAY, Month.addOneMonth(TimeService.getToday()));
      fieldSetter.set(SHOW_BUDGET_AREA_DESCRIPTIONS, true);
    }
  }

}