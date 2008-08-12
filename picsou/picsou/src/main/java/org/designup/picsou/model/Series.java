package org.designup.picsou.model;

import org.designup.picsou.server.serialization.PicsouGlobSerializer;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.*;
import org.globsframework.metamodel.fields.*;
import org.globsframework.metamodel.utils.GlobTypeLoader;
import org.globsframework.model.FieldSetter;
import org.globsframework.model.FieldValues;
import org.globsframework.utils.exceptions.InvalidData;
import org.globsframework.utils.serialization.SerializedByteArrayOutput;
import org.globsframework.utils.serialization.SerializedInput;
import org.globsframework.utils.serialization.SerializedInputOutputFactory;
import org.globsframework.utils.serialization.SerializedOutput;

public class Series {
  public static GlobType TYPE;

  @NoObfuscation
  @Key
  public static IntegerField ID;

  @NoObfuscation
  public static StringField LABEL;

  @NoObfuscation
  @NamingField
  public static StringField NAME;

  @NoObfuscation
  @Target(BudgetArea.class)
  public static LinkField BUDGET_AREA;

  @NoObfuscation
  @Target(Category.class)
  public static LinkField DEFAULT_CATEGORY;

  @NoObfuscation
  @Target(ProfileType.class)
  public static LinkField PROFILE_TYPE;

  public static IntegerField FIRST_MONTH;

  public static IntegerField LAST_MONTH;

  public static IntegerField OCCURENCES_COUNT;

  public static IntegerField DAY;

  public static DoubleField AMOUNT;

  public static DoubleField MIN_AMOUNT;

  public static DoubleField MAX_AMOUNT;

  @NoObfuscation
  @DefaultBoolean(false)
  public static BooleanField JANUARY;
  @NoObfuscation
  @DefaultBoolean(false)
  public static BooleanField FEBRUARY;
  @NoObfuscation
  @DefaultBoolean(false)
  public static BooleanField MARCH;
  @NoObfuscation
  @DefaultBoolean(false)
  public static BooleanField APRIL;
  @NoObfuscation
  @DefaultBoolean(false)
  public static BooleanField MAY;
  @NoObfuscation
  @DefaultBoolean(false)
  public static BooleanField JUNE;
  @NoObfuscation
  @DefaultBoolean(false)
  public static BooleanField JULY;
  @NoObfuscation
  @DefaultBoolean(false)
  public static BooleanField AUGUST;
  @NoObfuscation
  @DefaultBoolean(false)
  public static BooleanField SEPTEMBER;
  @NoObfuscation
  @DefaultBoolean(false)
  public static BooleanField OCTOBER;
  @NoObfuscation
  @DefaultBoolean(false)
  public static BooleanField NOVEMBER;
  @NoObfuscation
  @DefaultBoolean(false)
  public static BooleanField DECEMBER;

  public static final Integer OCCASIONAL_SERIES_ID = 0;

  static {
    GlobTypeLoader.init(Series.class, "series");
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
    throw new InvalidData(Month.toString(monthId) + " not managed");
  }

  public static class Serialization implements PicsouGlobSerializer {

    public byte[] serializeData(FieldValues fieldValues) {
      SerializedByteArrayOutput serializedByteArrayOutput = new SerializedByteArrayOutput();
      SerializedOutput output = serializedByteArrayOutput.getOutput();
      output.writeString(fieldValues.get(Series.LABEL));
      output.writeString(fieldValues.get(Series.NAME));
      output.writeInteger(fieldValues.get(Series.BUDGET_AREA));
      output.writeInteger(fieldValues.get(Series.DEFAULT_CATEGORY));
      output.writeInteger(fieldValues.get(Series.PROFILE_TYPE));
      output.writeInteger(fieldValues.get(Series.FIRST_MONTH));
      output.writeInteger(fieldValues.get(Series.LAST_MONTH));
      output.writeInteger(fieldValues.get(Series.OCCURENCES_COUNT));
      output.writeInteger(fieldValues.get(Series.DAY));
      output.writeDouble(fieldValues.get(Series.AMOUNT));
      output.writeDouble(fieldValues.get(Series.MIN_AMOUNT));
      output.writeDouble(fieldValues.get(Series.MAX_AMOUNT));
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
    }

    private void deserializeDataV1(FieldSetter fieldSetter, byte[] data) {
      SerializedInput input = SerializedInputOutputFactory.init(data);
      fieldSetter.set(Series.LABEL, input.readString());
      fieldSetter.set(Series.NAME, input.readString());
      fieldSetter.set(Series.BUDGET_AREA, input.readInteger());
      fieldSetter.set(Series.DEFAULT_CATEGORY, input.readInteger());
      fieldSetter.set(Series.PROFILE_TYPE, input.readInteger());
      fieldSetter.set(Series.FIRST_MONTH, input.readInteger());
      fieldSetter.set(Series.LAST_MONTH, input.readInteger());
      fieldSetter.set(Series.OCCURENCES_COUNT, input.readInteger());
      fieldSetter.set(Series.DAY, input.readInteger());
      fieldSetter.set(Series.AMOUNT, input.readDouble());
      fieldSetter.set(Series.MIN_AMOUNT, input.readDouble());
      fieldSetter.set(Series.MAX_AMOUNT, input.readDouble());
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
      return 1;
    }
  }
}
