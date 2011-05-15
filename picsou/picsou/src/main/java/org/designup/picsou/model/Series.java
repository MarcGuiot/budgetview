package org.designup.picsou.model;

import org.designup.picsou.server.serialization.PicsouGlobSerializer;
import org.designup.picsou.utils.Lang;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.*;
import org.globsframework.metamodel.fields.*;
import org.globsframework.metamodel.utils.GlobTypeLoader;
import org.globsframework.model.FieldSetter;
import org.globsframework.model.FieldValues;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.GlobMatcher;
import org.globsframework.utils.Strings;
import org.globsframework.utils.exceptions.ItemNotFound;
import org.globsframework.utils.serialization.SerializedByteArrayOutput;
import org.globsframework.utils.serialization.SerializedInput;
import org.globsframework.utils.serialization.SerializedInputOutputFactory;
import org.globsframework.utils.serialization.SerializedOutput;

public class Series {
  public static GlobType TYPE;

  @Key
  public static IntegerField ID;

  @NamingField
  @Required
  public static StringField NAME;

  @Target(BudgetArea.class)
  @Required
  public static LinkField BUDGET_AREA;

  public static StringField DESCRIPTION;

  @Target(ProfileType.class)
  @DefaultInteger(2)
  @Required
  public static LinkField PROFILE_TYPE;

  @Target(Month.class)
  public static IntegerField FIRST_MONTH;

  @Target(Month.class)
  public static IntegerField LAST_MONTH;

  @DefaultInteger(1)
  @Required
  public static IntegerField DAY;

  @DoublePrecision(4)
  public static DoubleField INITIAL_AMOUNT;

  @DefaultBoolean(true)
  @Required
  public static BooleanField IS_AUTOMATIC;

  @DefaultBoolean(false)
  @Required
  public static BooleanField SHOULD_REPORT;

  /**
   * sert pour en savings mais aussi pour les compte carte a debit differe
   */
  @Target(Account.class)
  public static LinkField FROM_ACCOUNT; 

  /**
   * cette serie appartient au compte courant mais ses transactions impactent le compte courant pointé
   */
  @Target(Account.class)
  public static LinkField TO_ACCOUNT;

  /**
   *  si les deux comptes sont importés. reference la series miroir
   */
  @Target(Series.class)
  public static LinkField MIRROR_SERIES;

  /**
   * la serie miroir a les montant de budget negatif elle est donc pour le compte "from"
   */
  @DefaultBoolean(false)
  @Required
  public static BooleanField IS_MIRROR;

  @Target(Account.class)
  public static LinkField TARGET_ACCOUNT;

  @DefaultBoolean(true)
  @Required
  public static BooleanField JANUARY;

  @DefaultBoolean(true)
  @Required
  public static BooleanField FEBRUARY;

  @DefaultBoolean(true)
  @Required
  public static BooleanField MARCH;

  @DefaultBoolean(true)
  @Required
  public static BooleanField APRIL;

  @DefaultBoolean(true)
  @Required
  public static BooleanField MAY;

  @DefaultBoolean(true)
  @Required
  public static BooleanField JUNE;

  @DefaultBoolean(true)
  @Required
  public static BooleanField JULY;

  @DefaultBoolean(true)
  @Required
  public static BooleanField AUGUST;

  @DefaultBoolean(true)
  @Required
  public static BooleanField SEPTEMBER;

  @DefaultBoolean(true)
  @Required
  public static BooleanField OCTOBER;

  @DefaultBoolean(true)
  @Required
  public static BooleanField NOVEMBER;

  @DefaultBoolean(true)
  @Required
  public static BooleanField DECEMBER;

  /**
   * @deprecated
   */
  public static final Integer OCCASIONAL_SERIES_ID = 0;
  public static final Integer UNCATEGORIZED_SERIES_ID = 1;

  public static org.globsframework.model.Key UNCATEGORIZED_SERIES;

  public static final GlobMatcher USER_SERIES_MATCHER;

  static {
    GlobTypeLoader.init(Series.class, "series");
    UNCATEGORIZED_SERIES = org.globsframework.model.Key.create(TYPE, UNCATEGORIZED_SERIES_ID);
    USER_SERIES_MATCHER = org.globsframework.model.utils.GlobMatchers.fieldIn(BUDGET_AREA,
                                                                              BudgetArea.INCOME.getId(),
                                                                              BudgetArea.RECURRING.getId(),
                                                                              BudgetArea.VARIABLE.getId(),
                                                                              BudgetArea.EXTRAS.getId());
  }

  public static BooleanField getMonthField(int monthId) {
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
    return series.get(Series.NAME); //Lang.get("transaction.planned", );
  }

  public static String getUncategorizedName() {
    return Lang.get("series.uncategorized");
  }

  public static boolean isValidMonth(int monthToCheck, Glob series) {
    Integer firstMonth = series.get(FIRST_MONTH);
    firstMonth = firstMonth == null ? 0 : firstMonth;
    Integer lastMonth = series.get(LAST_MONTH);
    lastMonth = lastMonth == null ? Integer.MAX_VALUE : lastMonth;
    return monthToCheck >= firstMonth &&
           monthToCheck <= lastMonth &&
           series.isTrue(getMonthField(monthToCheck));
  }

  public static BudgetArea getBudgetArea(Integer seriesId, GlobRepository repository) {
    Glob series = repository.get(org.globsframework.model.Key.create(Series.TYPE, seriesId));
    return getBudgetArea(series);
  }

  public static BudgetArea getBudgetArea(Glob series) {
    return BudgetArea.get(series.get(Series.BUDGET_AREA));
  }

  public static boolean isFrom(Glob series, Glob fromAccount) {
    return fromAccount.get(Account.ID).equals(series.get(TARGET_ACCOUNT));
  }

  public static boolean isTo(Glob series, Glob toAccount) {
    return toAccount.get(Account.ID).equals(series.get(TARGET_ACCOUNT));
  }

  public static boolean isSavingToExternal(FieldValues series) {
    return series.get(Series.BUDGET_AREA).equals(BudgetArea.SAVINGS.getId()) &&
           series.get(Series.TARGET_ACCOUNT).equals(Account.EXTERNAL_ACCOUNT_ID);
  }

  public static class Serializer implements PicsouGlobSerializer {

    public int getWriteVersion() {
      return 11;
    }

    public byte[] serializeData(FieldValues fieldValues) {
      SerializedByteArrayOutput serializedByteArrayOutput = new SerializedByteArrayOutput();
      SerializedOutput output = serializedByteArrayOutput.getOutput();
      output.writeUtf8String(fieldValues.get(Series.NAME));
      output.writeInteger(fieldValues.get(Series.BUDGET_AREA));
      output.writeUtf8String(fieldValues.get(Series.DESCRIPTION));
      output.writeInteger(fieldValues.get(Series.PROFILE_TYPE));
      output.writeInteger(fieldValues.get(Series.FIRST_MONTH));
      output.writeInteger(fieldValues.get(Series.LAST_MONTH));
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
      output.writeInteger(fieldValues.get(Series.TO_ACCOUNT));
      output.writeInteger(fieldValues.get(Series.FROM_ACCOUNT));
      output.writeInteger(fieldValues.get(Series.MIRROR_SERIES));
      output.writeBoolean(fieldValues.get(Series.SHOULD_REPORT));
      output.writeInteger(fieldValues.get(Series.TARGET_ACCOUNT));
      return serializedByteArrayOutput.toByteArray();
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
      else if (version == 4) {
        deserializeDataV4(fieldSetter, data);
      }
      else if (version == 5) {
        deserializeDataV5(fieldSetter, data);
      }
      else if (version == 6) {
        deserializeDataV6(fieldSetter, data);
      }
      else if (version == 7) {
        deserializeDataV7(fieldSetter, data);
      }
      else if (version == 8) {
        deserializeDataV8(fieldSetter, data);
      }
      else if (version == 9) {
        deserializeDataV9(fieldSetter, data);
      }
      else if (version == 10) {
        deserializeDataV10(fieldSetter, data);
      }
      else if (version == 11) {
        deserializeDataV11(fieldSetter, data);
      }
    }

    private void deserializeDataV1(FieldSetter fieldSetter, byte[] data) {
      SerializedInput input = SerializedInputOutputFactory.init(data);
      processLabelAndName(fieldSetter, input);
      fieldSetter.set(Series.BUDGET_AREA, input.readInteger());
      input.readInteger(); // DEFAULT_CATEGORY removed
      Integer profileType = input.readInteger();
      if (profileType == null) {
        profileType = ProfileType.CUSTOM.getId();
      }
      fieldSetter.set(Series.PROFILE_TYPE, profileType);
      fieldSetter.set(Series.FIRST_MONTH, input.readInteger());
      fieldSetter.set(Series.LAST_MONTH, input.readInteger());
      input.readInteger();
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
      processLabelAndName(fieldSetter, input);
      fieldSetter.set(Series.BUDGET_AREA, input.readInteger());
      input.readInteger(); // DEFAULT_CATEGORY removed
      Integer profileType = input.readInteger();
      if (profileType == null) {
        profileType = ProfileType.CUSTOM.getId();
      }
      fieldSetter.set(Series.PROFILE_TYPE, profileType);
      fieldSetter.set(Series.FIRST_MONTH, input.readInteger());
      fieldSetter.set(Series.LAST_MONTH, input.readInteger());
      input.readInteger();
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
      processLabelAndName(fieldSetter, input);
      fieldSetter.set(Series.BUDGET_AREA, input.readInteger());
      input.readInteger(); // DEFAULT_CATEGORY removed
      Integer profileType = input.readInteger();
      if (profileType == null) {
        profileType = ProfileType.CUSTOM.getId();
      }
      fieldSetter.set(Series.PROFILE_TYPE, profileType);
      fieldSetter.set(Series.FIRST_MONTH, input.readInteger());
      fieldSetter.set(Series.LAST_MONTH, input.readInteger());
      input.readInteger();
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

    private void deserializeDataV4(FieldSetter fieldSetter, byte[] data) {
      SerializedInput input = SerializedInputOutputFactory.init(data);
      processLabelAndName(fieldSetter, input);
      fieldSetter.set(Series.BUDGET_AREA, input.readInteger());
      input.readInteger(); // DEFAULT_CATEGORY removed
      Integer profileType = input.readInteger();
      if (profileType == null) {
        profileType = ProfileType.CUSTOM.getId();
      }
      fieldSetter.set(Series.PROFILE_TYPE, profileType);
      fieldSetter.set(Series.FIRST_MONTH, input.readInteger());
      fieldSetter.set(Series.LAST_MONTH, input.readInteger());
      input.readInteger();
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
      fieldSetter.set(Series.TO_ACCOUNT, input.readInteger());
      fieldSetter.set(Series.FROM_ACCOUNT, input.readInteger());
    }

    private void deserializeDataV5(FieldSetter fieldSetter, byte[] data) {
      SerializedInput input = SerializedInputOutputFactory.init(data);
      processLabelAndName(fieldSetter, input);
      fieldSetter.set(Series.BUDGET_AREA, input.readInteger());
      input.readInteger(); // DEFAULT_CATEGORY removed
      Integer profileType = input.readInteger();
      if (profileType == null) {
        profileType = ProfileType.CUSTOM.getId();
      }
      fieldSetter.set(Series.PROFILE_TYPE, profileType);
      fieldSetter.set(Series.FIRST_MONTH, input.readInteger());
      fieldSetter.set(Series.LAST_MONTH, input.readInteger());
      input.readInteger();
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
      fieldSetter.set(Series.TO_ACCOUNT, input.readInteger());
      fieldSetter.set(Series.FROM_ACCOUNT, input.readInteger());
      fieldSetter.set(Series.IS_MIRROR, input.readBoolean());
      fieldSetter.set(Series.MIRROR_SERIES, input.readInteger());
    }

    private void processLabelAndName(FieldSetter fieldSetter, SerializedInput input) {
      String label = input.readJavaString();
      String name = input.readJavaString();
      if (Strings.isNotEmpty(name)) {
        fieldSetter.set(Series.NAME, name);
      }
      else if (Strings.isNotEmpty(label)) {
        fieldSetter.set(Series.NAME, label);
      }
      else {
        fieldSetter.set(Series.NAME, "");
      }
    }

    private void deserializeDataV6(FieldSetter fieldSetter, byte[] data) {
      SerializedInput input = SerializedInputOutputFactory.init(data);
      fieldSetter.set(Series.NAME, input.readJavaString());
      fieldSetter.set(Series.BUDGET_AREA, input.readInteger());
      input.readInteger(); // DEFAULT_CATEGORY removed
      Integer profileType = input.readInteger();
      if (profileType == null) {
        profileType = ProfileType.CUSTOM.getId();
      }
      fieldSetter.set(Series.PROFILE_TYPE, profileType);
      fieldSetter.set(Series.FIRST_MONTH, input.readInteger());
      fieldSetter.set(Series.LAST_MONTH, input.readInteger());
      input.readInteger();
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
      fieldSetter.set(Series.TO_ACCOUNT, input.readInteger());
      fieldSetter.set(Series.FROM_ACCOUNT, input.readInteger());
      fieldSetter.set(Series.IS_MIRROR, input.readBoolean());
      fieldSetter.set(Series.MIRROR_SERIES, input.readInteger());
    }

    private void deserializeDataV7(FieldSetter fieldSetter, byte[] data) {
      SerializedInput input = SerializedInputOutputFactory.init(data);
      fieldSetter.set(Series.NAME, input.readUtf8String());
      fieldSetter.set(Series.BUDGET_AREA, input.readInteger());
      input.readInteger(); // DEFAULT_CATEGORY removed
      fieldSetter.set(Series.PROFILE_TYPE, input.readInteger());
      fieldSetter.set(Series.FIRST_MONTH, input.readInteger());
      fieldSetter.set(Series.LAST_MONTH, input.readInteger());
      input.readInteger();
      fieldSetter.set(Series.DAY, input.readInteger());
      fieldSetter.set(Series.INITIAL_AMOUNT, input.readDouble());
      fieldSetter.set(Series.IS_AUTOMATIC, input.readBoolean());
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
      fieldSetter.set(Series.TO_ACCOUNT, input.readInteger());
      fieldSetter.set(Series.FROM_ACCOUNT, input.readInteger());
      fieldSetter.set(Series.IS_MIRROR, input.readBoolean());
      fieldSetter.set(Series.MIRROR_SERIES, input.readInteger());
    }

    private void deserializeDataV8(FieldSetter fieldSetter, byte[] data) {
      SerializedInput input = SerializedInputOutputFactory.init(data);
      fieldSetter.set(Series.NAME, input.readUtf8String());
      fieldSetter.set(Series.BUDGET_AREA, input.readInteger());
      input.readInteger(); // DEFAULT_CATEGORY removed
      fieldSetter.set(Series.PROFILE_TYPE, input.readInteger());
      fieldSetter.set(Series.FIRST_MONTH, input.readInteger());
      fieldSetter.set(Series.LAST_MONTH, input.readInteger());
      fieldSetter.set(Series.DAY, input.readInteger());
      fieldSetter.set(Series.INITIAL_AMOUNT, input.readDouble());
      fieldSetter.set(Series.IS_AUTOMATIC, input.readBoolean());
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
      fieldSetter.set(Series.TO_ACCOUNT, input.readInteger());
      fieldSetter.set(Series.FROM_ACCOUNT, input.readInteger());
      fieldSetter.set(Series.IS_MIRROR, input.readBoolean());
      fieldSetter.set(Series.MIRROR_SERIES, input.readInteger());
    }

    private void deserializeDataV9(FieldSetter fieldSetter, byte[] data) {
      SerializedInput input = SerializedInputOutputFactory.init(data);
      fieldSetter.set(Series.NAME, input.readUtf8String());
      fieldSetter.set(Series.BUDGET_AREA, input.readInteger());
      fieldSetter.set(Series.DESCRIPTION, input.readUtf8String());
      fieldSetter.set(Series.PROFILE_TYPE, input.readInteger());
      fieldSetter.set(Series.FIRST_MONTH, input.readInteger());
      fieldSetter.set(Series.LAST_MONTH, input.readInteger());
      fieldSetter.set(Series.DAY, input.readInteger());
      fieldSetter.set(Series.INITIAL_AMOUNT, input.readDouble());
      fieldSetter.set(Series.IS_AUTOMATIC, input.readBoolean());
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
      fieldSetter.set(Series.TO_ACCOUNT, input.readInteger());
      fieldSetter.set(Series.FROM_ACCOUNT, input.readInteger());
      fieldSetter.set(Series.IS_MIRROR, input.readBoolean());
      fieldSetter.set(Series.MIRROR_SERIES, input.readInteger());
    }
    
    private void deserializeDataV10(FieldSetter fieldSetter, byte[] data) {
      SerializedInput input = SerializedInputOutputFactory.init(data);
      fieldSetter.set(Series.NAME, input.readUtf8String());
      fieldSetter.set(Series.BUDGET_AREA, input.readInteger());
      fieldSetter.set(Series.DESCRIPTION, input.readUtf8String());
      fieldSetter.set(Series.PROFILE_TYPE, input.readInteger());
      fieldSetter.set(Series.FIRST_MONTH, input.readInteger());
      fieldSetter.set(Series.LAST_MONTH, input.readInteger());
      fieldSetter.set(Series.DAY, input.readInteger());
      fieldSetter.set(Series.INITIAL_AMOUNT, input.readDouble());
      fieldSetter.set(Series.IS_AUTOMATIC, input.readBoolean());
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
      fieldSetter.set(Series.TO_ACCOUNT, input.readInteger());
      fieldSetter.set(Series.FROM_ACCOUNT, input.readInteger());
      fieldSetter.set(Series.IS_MIRROR, input.readBoolean());
      fieldSetter.set(Series.MIRROR_SERIES, input.readInteger());
      fieldSetter.set(Series.SHOULD_REPORT, input.readBoolean());
    }

    private void deserializeDataV11(FieldSetter fieldSetter, byte[] data) {
      SerializedInput input = SerializedInputOutputFactory.init(data);
      fieldSetter.set(Series.NAME, input.readUtf8String());
      fieldSetter.set(Series.BUDGET_AREA, input.readInteger());
      fieldSetter.set(Series.DESCRIPTION, input.readUtf8String());
      fieldSetter.set(Series.PROFILE_TYPE, input.readInteger());
      fieldSetter.set(Series.FIRST_MONTH, input.readInteger());
      fieldSetter.set(Series.LAST_MONTH, input.readInteger());
      fieldSetter.set(Series.DAY, input.readInteger());
      fieldSetter.set(Series.INITIAL_AMOUNT, input.readDouble());
      fieldSetter.set(Series.IS_AUTOMATIC, input.readBoolean());
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
      fieldSetter.set(Series.TO_ACCOUNT, input.readInteger());
      fieldSetter.set(Series.FROM_ACCOUNT, input.readInteger());
      fieldSetter.set(Series.MIRROR_SERIES, input.readInteger());
      fieldSetter.set(Series.SHOULD_REPORT, input.readBoolean());
      fieldSetter.set(Series.TARGET_ACCOUNT, input.readInteger());
    }
  }
}
