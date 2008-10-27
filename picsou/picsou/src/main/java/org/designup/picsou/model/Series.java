package org.designup.picsou.model;

import org.designup.picsou.server.serialization.PicsouGlobSerializer;
import org.designup.picsou.utils.Lang;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.*;
import org.globsframework.metamodel.fields.*;
import org.globsframework.metamodel.utils.GlobTypeLoader;
import org.globsframework.model.FieldSetter;
import org.globsframework.model.FieldValues;
import org.globsframework.utils.exceptions.ItemNotFound;
import org.globsframework.utils.serialization.SerializedByteArrayOutput;
import org.globsframework.utils.serialization.SerializedInput;
import org.globsframework.utils.serialization.SerializedInputOutputFactory;
import org.globsframework.utils.serialization.SerializedOutput;

public class Series {
  public static GlobType TYPE;

  @Key
  public static IntegerField ID;

  /**
   * @deprecated TODO: migrer LABEL dans NAME, et garder NAME
   */
  public static StringField LABEL;

  /**
   * @deprecated TODO: migrer LABEL dans NAME, et garder NAME
   */
  @NamingField
  public static StringField NAME;

  @Target(BudgetArea.class)
  public static LinkField BUDGET_AREA;

  @Target(Category.class)
  public static LinkField DEFAULT_CATEGORY;

  @Target(ProfileType.class)
  @DefaultInteger(2)
  public static LinkField PROFILE_TYPE;

  @Target(Month.class)
  public static IntegerField FIRST_MONTH;

  @Target(Month.class)
  public static IntegerField LAST_MONTH;

  public static IntegerField OCCURENCES_COUNT;

  @DefaultInteger(1)
  public static IntegerField DAY;

  public static DoubleField INITIAL_AMOUNT;

  @DefaultBoolean(true)
  public static BooleanField IS_AUTOMATIC;

  @DefaultBoolean(true)
  public static BooleanField JANUARY;

  @DefaultBoolean(true)
  public static BooleanField FEBRUARY;

  @DefaultBoolean(true)
  public static BooleanField MARCH;

  @DefaultBoolean(true)
  public static BooleanField APRIL;

  @DefaultBoolean(true)
  public static BooleanField MAY;

  @DefaultBoolean(true)
  public static BooleanField JUNE;

  @DefaultBoolean(true)
  public static BooleanField JULY;

  @DefaultBoolean(true)
  public static BooleanField AUGUST;

  @DefaultBoolean(true)
  public static BooleanField SEPTEMBER;

  @DefaultBoolean(true)
  public static BooleanField OCTOBER;

  @DefaultBoolean(true)
  public static BooleanField NOVEMBER;

  @DefaultBoolean(true)
  public static BooleanField DECEMBER;

  public static final Integer OCCASIONAL_SERIES_ID = 0;
  public static final Integer UNCATEGORIZED_SERIES_ID = 1;

  public static org.globsframework.model.Key OCCASIONAL_SERIES;

  static {
    GlobTypeLoader.init(Series.class, "series");
    OCCASIONAL_SERIES = org.globsframework.model.Key.create(TYPE, OCCASIONAL_SERIES_ID);
  }

  public static BooleanField getField(int monthId) {
    switch (Month.toMonth(monthId)) {
      case 1:
        return JANUARY;
      case 2:
        return FEBRUARY;
      case 3:
        return MARCH;
      case 4:
        return APRIL;
      case 5:
        return MAY;
      case 6:
        return JUNE;
      case 7:
        return JULY;
      case 8:
        return AUGUST;
      case 9:
        return SEPTEMBER;
      case 10:
        return OCTOBER;
      case 11:
        return NOVEMBER;
      case 12:
        return DECEMBER;
    }
    throw new ItemNotFound(Month.toString(monthId) + " is not a month number");
  }

  public static BooleanField[] getMonths() {
    return new BooleanField[]{JANUARY, FEBRUARY, MARCH, APRIL, MAY, JUNE, JULY, AUGUST,
                              SEPTEMBER, OCTOBER, NOVEMBER, DECEMBER};
  }

  public static String getPlannedTransactionLabel(Integer seriesId, FieldValues series) {
    if (seriesId == 0) {
      return Lang.get("transaction.planned", BudgetArea.OCCASIONAL.getLabel());
    }
    return Lang.get("transaction.planned", series.get(Series.LABEL));
  }

  public static class Serializer implements PicsouGlobSerializer {

    public byte[] serializeData(FieldValues fieldValues) {
      SerializedByteArrayOutput serializedByteArrayOutput = new SerializedByteArrayOutput();
      SerializedOutput output = serializedByteArrayOutput.getOutput();
      output.writeUtf8String(fieldValues.get(Series.LABEL));
      output.writeUtf8String(fieldValues.get(Series.NAME));
      output.writeInteger(fieldValues.get(Series.BUDGET_AREA));
      output.writeInteger(fieldValues.get(Series.DEFAULT_CATEGORY));
      output.writeInteger(fieldValues.get(Series.PROFILE_TYPE));
      output.writeInteger(fieldValues.get(Series.FIRST_MONTH));
      output.writeInteger(fieldValues.get(Series.LAST_MONTH));
      output.writeInteger(fieldValues.get(Series.OCCURENCES_COUNT));
      output.writeInteger(fieldValues.get(Series.DAY));
      output.writeDouble(fieldValues.get(Series.INITIAL_AMOUNT));
      output.writeBoolean(fieldValues.get(Series.IS_AUTOMATIC));
      output.writeBoolean(fieldValues.get(Series.JANUARY));
      output.writeBoolean(fieldValues.get(Series.FEBRUARY));
      output.writeBoolean(fieldValues.get(Series.MARCH));
      output.writeBoolean(fieldValues.get(Series.APRIL));
      output.writeBoolean(fieldValues.get(Series.MAY));
      output.writeBoolean(fieldValues.get(Series.JUNE));
      output.writeBoolean(fieldValues.get(Series.JULY));
      output.writeBoolean(fieldValues.get(Series.AUGUST));
      output.writeBoolean(fieldValues.get(Series.SEPTEMBER));
      output.writeBoolean(fieldValues.get(Series.OCTOBER));
      output.writeBoolean(fieldValues.get(Series.NOVEMBER));
      output.writeBoolean(fieldValues.get(Series.DECEMBER));
      return serializedByteArrayOutput.toByteArray();
    }

    public void deserializeData(int version, FieldSetter fieldSetter, byte[] data) {
      if (version == 1) {
        deserializeDataV1(fieldSetter, data);
      }
      if (version == 2) {
        deserializeDataV2(fieldSetter, data);
      }
      if (version == 3) {
        deserializeDataV3(fieldSetter, data);
      }
    }

    private void deserializeDataV1(FieldSetter fieldSetter, byte[] data) {
      SerializedInput input = SerializedInputOutputFactory.init(data);
      fieldSetter.set(Series.LABEL, input.readString());
      fieldSetter.set(Series.NAME, input.readString());
      fieldSetter.set(Series.BUDGET_AREA, input.readInteger());
      fieldSetter.set(Series.DEFAULT_CATEGORY, input.readInteger());
      Integer profileType = input.readInteger();
      if (profileType == null) {
        profileType = ProfileType.CUSTOM.getId();
      }
      fieldSetter.set(Series.PROFILE_TYPE, profileType);
      fieldSetter.set(Series.FIRST_MONTH, input.readInteger());
      fieldSetter.set(Series.LAST_MONTH, input.readInteger());
      fieldSetter.set(Series.OCCURENCES_COUNT, input.readInteger());
      fieldSetter.set(Series.DAY, input.readInteger());
      fieldSetter.set(Series.INITIAL_AMOUNT, input.readDouble());
      fieldSetter.set(Series.JANUARY, input.readBoolean());
      fieldSetter.set(Series.FEBRUARY, input.readBoolean());
      fieldSetter.set(Series.MARCH, input.readBoolean());
      fieldSetter.set(Series.APRIL, input.readBoolean());
      fieldSetter.set(Series.MAY, input.readBoolean());
      fieldSetter.set(Series.JUNE, input.readBoolean());
      fieldSetter.set(Series.JULY, input.readBoolean());
      fieldSetter.set(Series.AUGUST, input.readBoolean());
      fieldSetter.set(Series.SEPTEMBER, input.readBoolean());
      fieldSetter.set(Series.OCTOBER, input.readBoolean());
      fieldSetter.set(Series.NOVEMBER, input.readBoolean());
      fieldSetter.set(Series.DECEMBER, input.readBoolean());
    }

    private void deserializeDataV2(FieldSetter fieldSetter, byte[] data) {
      SerializedInput input = SerializedInputOutputFactory.init(data);
      fieldSetter.set(Series.LABEL, input.readString());
      fieldSetter.set(Series.NAME, input.readString());
      fieldSetter.set(Series.BUDGET_AREA, input.readInteger());
      fieldSetter.set(Series.DEFAULT_CATEGORY, input.readInteger());
      Integer profileType = input.readInteger();
      if (profileType == null) {
        profileType = ProfileType.CUSTOM.getId();
      }
      fieldSetter.set(Series.PROFILE_TYPE, profileType);
      fieldSetter.set(Series.FIRST_MONTH, input.readInteger());
      fieldSetter.set(Series.LAST_MONTH, input.readInteger());
      fieldSetter.set(Series.OCCURENCES_COUNT, input.readInteger());
      fieldSetter.set(Series.DAY, input.readInteger());
      fieldSetter.set(Series.INITIAL_AMOUNT, input.readDouble());
      Boolean isAutomatic = input.readBoolean();
      if (isAutomatic == null) {
        isAutomatic = false;
      }
      fieldSetter.set(Series.IS_AUTOMATIC, isAutomatic);
      fieldSetter.set(Series.JANUARY, input.readBoolean());
      fieldSetter.set(Series.FEBRUARY, input.readBoolean());
      fieldSetter.set(Series.MARCH, input.readBoolean());
      fieldSetter.set(Series.APRIL, input.readBoolean());
      fieldSetter.set(Series.MAY, input.readBoolean());
      fieldSetter.set(Series.JUNE, input.readBoolean());
      fieldSetter.set(Series.JULY, input.readBoolean());
      fieldSetter.set(Series.AUGUST, input.readBoolean());
      fieldSetter.set(Series.SEPTEMBER, input.readBoolean());
      fieldSetter.set(Series.OCTOBER, input.readBoolean());
      fieldSetter.set(Series.NOVEMBER, input.readBoolean());
      fieldSetter.set(Series.DECEMBER, input.readBoolean());
    }

    private void deserializeDataV3(FieldSetter fieldSetter, byte[] data) {
      SerializedInput input = SerializedInputOutputFactory.init(data);
      fieldSetter.set(Series.LABEL, input.readUtf8String());
      fieldSetter.set(Series.NAME, input.readUtf8String());
      fieldSetter.set(Series.BUDGET_AREA, input.readInteger());
      fieldSetter.set(Series.DEFAULT_CATEGORY, input.readInteger());
      Integer profileType = input.readInteger();
      if (profileType == null) {
        profileType = ProfileType.CUSTOM.getId();
      }
      fieldSetter.set(Series.PROFILE_TYPE, profileType);
      fieldSetter.set(Series.FIRST_MONTH, input.readInteger());
      fieldSetter.set(Series.LAST_MONTH, input.readInteger());
      fieldSetter.set(Series.OCCURENCES_COUNT, input.readInteger());
      fieldSetter.set(Series.DAY, input.readInteger());
      fieldSetter.set(Series.INITIAL_AMOUNT, input.readDouble());
      Boolean isAutomatic = input.readBoolean();
      if (isAutomatic == null) {
        isAutomatic = false;
      }
      fieldSetter.set(Series.IS_AUTOMATIC, isAutomatic);
      fieldSetter.set(Series.JANUARY, input.readBoolean());
      fieldSetter.set(Series.FEBRUARY, input.readBoolean());
      fieldSetter.set(Series.MARCH, input.readBoolean());
      fieldSetter.set(Series.APRIL, input.readBoolean());
      fieldSetter.set(Series.MAY, input.readBoolean());
      fieldSetter.set(Series.JUNE, input.readBoolean());
      fieldSetter.set(Series.JULY, input.readBoolean());
      fieldSetter.set(Series.AUGUST, input.readBoolean());
      fieldSetter.set(Series.SEPTEMBER, input.readBoolean());
      fieldSetter.set(Series.OCTOBER, input.readBoolean());
      fieldSetter.set(Series.NOVEMBER, input.readBoolean());
      fieldSetter.set(Series.DECEMBER, input.readBoolean());
    }

    public int getWriteVersion() {
      return 3;
    }
  }
}
