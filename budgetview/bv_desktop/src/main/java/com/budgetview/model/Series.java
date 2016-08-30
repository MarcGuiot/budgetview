package com.budgetview.model;

import com.budgetview.desktop.model.SeriesStat;
import com.budgetview.shared.model.BudgetArea;
import com.budgetview.shared.utils.PicsouGlobSerializer;
import com.budgetview.utils.Lang;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.*;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.fields.*;
import org.globsframework.metamodel.utils.GlobTypeLoader;
import org.globsframework.model.*;
import org.globsframework.model.utils.GlobMatcher;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.Strings;
import org.globsframework.utils.Utils;
import org.globsframework.utils.exceptions.ItemNotFound;
import org.globsframework.utils.serialization.SerializedByteArrayOutput;
import org.globsframework.utils.serialization.SerializedInput;
import org.globsframework.utils.serialization.SerializedInputOutputFactory;
import org.globsframework.utils.serialization.SerializedOutput;

import java.util.Set;

import static org.globsframework.model.utils.GlobMatchers.linkedTo;

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

  @DefaultBoolean(true)
  public static BooleanField ACTIVE;

  @Target(SeriesGroup.class)
  public static LinkField GROUP;

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

  /**
   * sert pour les savings mais aussi pour les compte carte a debit differe
   */
  @Target(Account.class)
  public static LinkField FROM_ACCOUNT;

  /**
   * cette serie appartient au compte courant mais ses transactions impactent le compte courant pointé
   */
  @Target(Account.class)
  public static LinkField TO_ACCOUNT;

  /**
   * si les deux comptes sont importés. reference la series miroir
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

  @DefaultBoolean(false)
  public static BooleanField IS_INITIAL;

  @DefaultBoolean(false)
  public static BooleanField FORCE_SINGLE_OPERATION;

  @Target(DayOfMonth.class)
  @DefaultInteger(15)
  public static LinkField FORCE_SINGLE_OPERATION_DAY;

  /**
   * @deprecated
   */
  public static final Integer OCCASIONAL_SERIES_ID = 0;
  public static final Integer UNCATEGORIZED_SERIES_ID = 1;
  public static final Integer ACCOUNT_SERIES_ID = -1;

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

  public static String getAccountSeriesName() {
    return "account event";
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

  public static boolean isTransfer(FieldValues series) {
    return BudgetArea.TRANSFER.getId().equals(series.get(Series.BUDGET_AREA));
  }

  public static boolean isTransferToExternal(FieldValues series) {
    return series.get(Series.BUDGET_AREA).equals(BudgetArea.TRANSFER.getId()) &&
           series.get(Series.TARGET_ACCOUNT).equals(Account.EXTERNAL_ACCOUNT_ID);
  }

  public static boolean shouldInvertAmounts(Glob series, Integer referenceAccountId, GlobRepository repository) {
    if (referenceAccountId == null) {
      return false;
    }
    Glob account = repository.find(org.globsframework.model.Key.create(Account.TYPE, referenceAccountId));
    if (account == null || !isTransfer(series)) {
      return false;
    }

    if (series.get(FROM_ACCOUNT).equals(series.get(TARGET_ACCOUNT))) {
      return referenceAccountId.equals(series.get(TO_ACCOUNT));
    }
    else if (series.get(TO_ACCOUNT).equals(series.get(TARGET_ACCOUNT))) {
      return referenceAccountId.equals(series.get(FROM_ACCOUNT));
    }
    return false;
  }


  public static boolean isUncategorized(Integer seriesId) {
    return UNCATEGORIZED_SERIES_ID.equals(seriesId);
  }

  public static boolean hasRealTransactions(final GlobRepository repository, final Integer seriesId) {
    return !getRealTransactions(seriesId, repository).isEmpty();
  }

  public static GlobList getRealTransactions(Integer seriesId, GlobRepository repository) {
    return repository.findByIndex(Transaction.SERIES_INDEX, Transaction.SERIES, seriesId)
      .getGlobs().filter(GlobMatchers.isFalse(Transaction.PLANNED), repository);
  }

  public static GlobList getRealTransactions(Integer seriesId, GlobList transactionList, GlobRepository repository) {
    return transactionList.filter(GlobMatchers.isFalse(Transaction.PLANNED), repository);
  }

  public static void delete(Glob series, GlobRepository repository) {
    if (series != null && series.exists()) {
      repository.startChangeSet();
      try {
        Transaction.uncategorize(repository.getAll(Transaction.TYPE, linkedTo(series, Transaction.SERIES)), repository);
        Glob mirror = repository.findLinkTarget(series, Series.MIRROR_SERIES);
        if (mirror != null) {
          Transaction.uncategorize(repository.getAll(Transaction.TYPE, linkedTo(mirror, Transaction.SERIES)), repository);
          doDelete(mirror, repository);
        }
        doDelete(series, repository);
      }
      finally {
        repository.completeChangeSet();
      }
    }
  }

  public static void doDelete(Glob series, GlobRepository repository) {
    SeriesStat.deleteAllForSeries(series, repository);
    SeriesBudget.deleteAllForSeries(series, repository);
    repository.delete(series);
  }

  public static Glob createMirror(Glob series, Integer targetAccountId, GlobRepository repository) {
    Glob mirrorSeries = repository.create(TYPE,
                                          FieldValuesBuilder.init(series)
                                            .remove(ID)
                                            .set(MIRROR_SERIES, series.get(ID))
                                            .set(TARGET_ACCOUNT, targetAccountId)
                                            .toArray());
    repository.update(series.getKey(), Series.MIRROR_SERIES, mirrorSeries.get(Series.ID));
    return mirrorSeries;
  }

  public static boolean isSeriesForAccount(Glob series, Integer selectedAccountId, GlobRepository repository) {
    Integer targetAccount = series.get(TARGET_ACCOUNT);
    if (Utils.equal(selectedAccountId, targetAccount)) {
      return true;
    }
    if (Utils.equal(Account.MAIN_SUMMARY_ACCOUNT_ID, targetAccount)) {
      Glob account = repository.find(org.globsframework.model.Key.create(Account.TYPE, selectedAccountId));
      if (account != null && Account.isMain(account)) {
        return true;
      }
    }
    return false;
  }

  public static boolean isSeriesForAccounts(Glob series, final Set<Integer> accountIds, GlobRepository repository) {
    Integer targetAccount = series.get(TARGET_ACCOUNT);
    if (accountIds.contains(targetAccount)) {
      return true;
    }
    if (Utils.equal(Account.MAIN_SUMMARY_ACCOUNT_ID, targetAccount)) {
      for (Integer accountId : accountIds) {
        Glob account = repository.find(org.globsframework.model.Key.create(Account.TYPE, accountId));
        if (account != null && Account.isMain(account)) {
          return true;
        }
      }
    }
    return false;
  }

  public static boolean isSeriesInGroupForAccount(Glob group, final Set<Integer> accountIds, GlobRepository repository) {
    for (Glob series : repository.findLinkedTo(group, GROUP)) {
      if (isSeriesForAccounts(series, accountIds, repository)) {
        return true;
      }
    }
    return false;
  }

  public static boolean isValid(Glob series) {
    return !series.get(BUDGET_AREA).equals(BudgetArea.TRANSFER.getId()) ||
           ((series.get(FROM_ACCOUNT) != null && series.get(TO_ACCOUNT) != null) &&
            !series.get(FROM_ACCOUNT).equals(series.get(TO_ACCOUNT)));
  }

  public static class Serializer implements PicsouGlobSerializer {

    public int getWriteVersion() {
      return 15;
    }

    public boolean shouldBeSaved(GlobRepository repository, FieldValues fieldValues) {
      return true;
    }

    public byte[] serializeData(FieldValues fieldValues) {
      SerializedByteArrayOutput serializedByteArrayOutput = new SerializedByteArrayOutput();
      SerializedOutput output = serializedByteArrayOutput.getOutput();
      output.writeUtf8String(fieldValues.get(Series.NAME));
      output.writeInteger(fieldValues.get(Series.BUDGET_AREA));
      output.writeUtf8String(fieldValues.get(Series.DESCRIPTION));
      output.writeInteger(fieldValues.get(Series.PROFILE_TYPE));
      output.writeBoolean(fieldValues.get(Series.ACTIVE));
      output.writeInteger(fieldValues.get(Series.GROUP));
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
      output.writeInteger(fieldValues.get(Series.TARGET_ACCOUNT));
      output.writeBoolean(fieldValues.get(Series.IS_INITIAL));
      output.writeBoolean(fieldValues.get(Series.FORCE_SINGLE_OPERATION));
      output.writeInteger(fieldValues.get(Series.FORCE_SINGLE_OPERATION_DAY));
      return serializedByteArrayOutput.toByteArray();
    }

    public void deserializeData(int version, FieldSetter fieldSetter, byte[] data, Integer id) {
      if (version == 15) {
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
      fieldSetter.set(Series.ACTIVE, true);
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
      fieldSetter.set(Series.FORCE_SINGLE_OPERATION, false);
      fieldSetter.set(Series.FORCE_SINGLE_OPERATION_DAY, null);
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
      fieldSetter.set(Series.ACTIVE, true);
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
      fieldSetter.set(Series.FORCE_SINGLE_OPERATION, false);
      fieldSetter.set(Series.FORCE_SINGLE_OPERATION_DAY, null);
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
      fieldSetter.set(Series.ACTIVE, true);
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
      fieldSetter.set(Series.FORCE_SINGLE_OPERATION, false);
      fieldSetter.set(Series.FORCE_SINGLE_OPERATION_DAY, null);
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
      fieldSetter.set(Series.ACTIVE, true);
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
      fieldSetter.set(Series.FORCE_SINGLE_OPERATION, false);
      fieldSetter.set(Series.FORCE_SINGLE_OPERATION_DAY, null);
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
      fieldSetter.set(Series.ACTIVE, true);
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
      fieldSetter.set(Series.FORCE_SINGLE_OPERATION, false);
      fieldSetter.set(Series.FORCE_SINGLE_OPERATION_DAY, null);
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
      fieldSetter.set(Series.ACTIVE, true);
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
      fieldSetter.set(Series.FORCE_SINGLE_OPERATION, false);
      fieldSetter.set(Series.FORCE_SINGLE_OPERATION_DAY, null);
    }

    private void deserializeDataV7(FieldSetter fieldSetter, byte[] data) {
      SerializedInput input = SerializedInputOutputFactory.init(data);
      fieldSetter.set(Series.NAME, input.readUtf8String());
      fieldSetter.set(Series.BUDGET_AREA, input.readInteger());
      input.readInteger(); // DEFAULT_CATEGORY removed
      fieldSetter.set(Series.PROFILE_TYPE, input.readInteger());
      fieldSetter.set(Series.ACTIVE, true);
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
      fieldSetter.set(Series.FORCE_SINGLE_OPERATION, false);
      fieldSetter.set(Series.FORCE_SINGLE_OPERATION_DAY, null);
    }

    private void deserializeDataV8(FieldSetter fieldSetter, byte[] data) {
      SerializedInput input = SerializedInputOutputFactory.init(data);
      fieldSetter.set(Series.NAME, input.readUtf8String());
      fieldSetter.set(Series.BUDGET_AREA, input.readInteger());
      input.readInteger(); // DEFAULT_CATEGORY removed
      fieldSetter.set(Series.PROFILE_TYPE, input.readInteger());
      fieldSetter.set(Series.ACTIVE, true);
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
      fieldSetter.set(Series.FORCE_SINGLE_OPERATION, false);
      fieldSetter.set(Series.FORCE_SINGLE_OPERATION_DAY, null);
    }

    private void deserializeDataV9(FieldSetter fieldSetter, byte[] data) {
      SerializedInput input = SerializedInputOutputFactory.init(data);
      fieldSetter.set(Series.NAME, input.readUtf8String());
      fieldSetter.set(Series.BUDGET_AREA, input.readInteger());
      fieldSetter.set(Series.DESCRIPTION, input.readUtf8String());
      fieldSetter.set(Series.PROFILE_TYPE, input.readInteger());
      fieldSetter.set(Series.ACTIVE, true);
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
      fieldSetter.set(Series.FORCE_SINGLE_OPERATION, false);
      fieldSetter.set(Series.FORCE_SINGLE_OPERATION_DAY, null);
    }

    private void deserializeDataV10(FieldSetter fieldSetter, byte[] data) {
      SerializedInput input = SerializedInputOutputFactory.init(data);
      fieldSetter.set(Series.NAME, input.readUtf8String());
      fieldSetter.set(Series.BUDGET_AREA, input.readInteger());
      fieldSetter.set(Series.DESCRIPTION, input.readUtf8String());
      fieldSetter.set(Series.PROFILE_TYPE, input.readInteger());
      fieldSetter.set(Series.ACTIVE, true);
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
      input.readBoolean(); //Series.SHOULD_REPORT
      fieldSetter.set(Series.FORCE_SINGLE_OPERATION, false);
      fieldSetter.set(Series.FORCE_SINGLE_OPERATION_DAY, null);
    }

    private void deserializeDataV11(FieldSetter fieldSetter, byte[] data) {
      SerializedInput input = SerializedInputOutputFactory.init(data);
      fieldSetter.set(Series.NAME, input.readUtf8String());
      fieldSetter.set(Series.BUDGET_AREA, input.readInteger());
      fieldSetter.set(Series.DESCRIPTION, input.readUtf8String());
      fieldSetter.set(Series.PROFILE_TYPE, input.readInteger());
      fieldSetter.set(Series.ACTIVE, true);
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
      input.readBoolean(); //Series.SHOULD_REPORT
      fieldSetter.set(Series.TARGET_ACCOUNT, input.readInteger());
      fieldSetter.set(Series.FORCE_SINGLE_OPERATION, false);
      fieldSetter.set(Series.FORCE_SINGLE_OPERATION_DAY, null);
    }

    private void deserializeDataV12(FieldSetter fieldSetter, byte[] data) {
      SerializedInput input = SerializedInputOutputFactory.init(data);
      fieldSetter.set(Series.NAME, input.readUtf8String());
      fieldSetter.set(Series.BUDGET_AREA, input.readInteger());
      fieldSetter.set(Series.DESCRIPTION, input.readUtf8String());
      fieldSetter.set(Series.PROFILE_TYPE, input.readInteger());
      fieldSetter.set(Series.ACTIVE, true);
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
      input.readBoolean(); //Series.SHOULD_REPORT
      fieldSetter.set(Series.TARGET_ACCOUNT, input.readInteger());
      fieldSetter.set(Series.IS_INITIAL, input.readBoolean());
      fieldSetter.set(Series.FORCE_SINGLE_OPERATION, false);
      fieldSetter.set(Series.FORCE_SINGLE_OPERATION_DAY, null);
    }

    private void deserializeDataV13(FieldSetter fieldSetter, byte[] data) {
      SerializedInput input = SerializedInputOutputFactory.init(data);
      fieldSetter.set(Series.NAME, input.readUtf8String());
      fieldSetter.set(Series.BUDGET_AREA, input.readInteger());
      fieldSetter.set(Series.DESCRIPTION, input.readUtf8String());
      fieldSetter.set(Series.PROFILE_TYPE, input.readInteger());
      fieldSetter.set(Series.ACTIVE, true);
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
      input.readBoolean(); //Series.SHOULD_REPORT
      fieldSetter.set(Series.TARGET_ACCOUNT, input.readInteger());
      fieldSetter.set(Series.IS_INITIAL, input.readBoolean());
      fieldSetter.set(Series.FORCE_SINGLE_OPERATION, input.readBoolean());
      fieldSetter.set(Series.FORCE_SINGLE_OPERATION_DAY, input.readInteger());
    }

    private void deserializeDataV14(FieldSetter fieldSetter, byte[] data) {
      SerializedInput input = SerializedInputOutputFactory.init(data);
      fieldSetter.set(Series.NAME, input.readUtf8String());
      fieldSetter.set(Series.BUDGET_AREA, input.readInteger());
      fieldSetter.set(Series.DESCRIPTION, input.readUtf8String());
      fieldSetter.set(Series.PROFILE_TYPE, input.readInteger());
      fieldSetter.set(Series.ACTIVE, true);
      fieldSetter.set(Series.GROUP, input.readInteger());
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
      input.readBoolean(); //Series.SHOULD_REPORT
      fieldSetter.set(Series.TARGET_ACCOUNT, input.readInteger());
      fieldSetter.set(Series.IS_INITIAL, input.readBoolean());
      fieldSetter.set(Series.FORCE_SINGLE_OPERATION, input.readBoolean());
      fieldSetter.set(Series.FORCE_SINGLE_OPERATION_DAY, input.readInteger());
    }

    private void deserializeDataV15(FieldSetter fieldSetter, byte[] data) {
      SerializedInput input = SerializedInputOutputFactory.init(data);
      fieldSetter.set(Series.NAME, input.readUtf8String());
      fieldSetter.set(Series.BUDGET_AREA, input.readInteger());
      fieldSetter.set(Series.DESCRIPTION, input.readUtf8String());
      fieldSetter.set(Series.PROFILE_TYPE, input.readInteger());
      fieldSetter.set(Series.ACTIVE, input.readBoolean());
      fieldSetter.set(Series.GROUP, input.readInteger());
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
      fieldSetter.set(Series.TARGET_ACCOUNT, input.readInteger());
      fieldSetter.set(Series.IS_INITIAL, input.readBoolean());
      fieldSetter.set(Series.FORCE_SINGLE_OPERATION, input.readBoolean());
      fieldSetter.set(Series.FORCE_SINGLE_OPERATION_DAY, input.readInteger());
    }
  }
}
