package org.designup.picsou.model;

import org.designup.picsou.server.serialization.PicsouGlobSerializer;
import org.designup.picsou.triggers.SameAccountChecker;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.DefaultBoolean;
import org.globsframework.metamodel.annotations.DefaultInteger;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.annotations.Target;
import org.globsframework.metamodel.fields.*;
import org.globsframework.metamodel.utils.GlobTypeLoader;
import org.globsframework.model.FieldSetter;
import static org.globsframework.model.FieldValue.value;
import org.globsframework.model.FieldValues;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.exceptions.ItemNotFound;
import org.globsframework.utils.serialization.SerializedByteArrayOutput;
import org.globsframework.utils.serialization.SerializedInput;
import org.globsframework.utils.serialization.SerializedInputOutputFactory;
import org.globsframework.utils.serialization.SerializedOutput;

import java.util.HashSet;
import java.util.Set;

public class Account {
  public static final String SUMMARY_ACCOUNT_NUMBER = null;
  public static final int MAIN_SUMMARY_ACCOUNT_ID = -1;
  public static final int SAVINGS_SUMMARY_ACCOUNT_ID = -2;
  public static final int ALL_SUMMARY_ACCOUNT_ID = -3;
  public static final Set<Integer> SUMMARY_ACCOUNT_IDS = new HashSet<Integer>();
  public static org.globsframework.model.Key MAIN_SUMMARY_KEY;
  public static org.globsframework.model.Key SAVINGS_SUMMARY_KEY;
  public static org.globsframework.model.Key ALL_SUMMARY_KEY;

  public static GlobType TYPE;

  @Key
  public static IntegerField ID;

  public static StringField NUMBER;

  @Target(BankEntity.class)
  public static LinkField BANK_ENTITY;

  public static IntegerField BRANCH_ID;

  public static StringField NAME;

  public static DoubleField BALANCE;

  @Target(Transaction.class)
  public static LinkField TRANSACTION_ID;

  public static DateField BALANCE_DATE;

  public static DateField CLOSED_DATE;

  @DefaultBoolean(false)
  public static BooleanField IS_CARD_ACCOUNT;

  @Target(AccountType.class)
  @DefaultInteger(1)
  public static LinkField ACCOUNT_TYPE;

  @Target(AccountUpdateMode.class)
  @DefaultInteger(1)
  public static LinkField UPDATE_MODE;

  @DefaultBoolean(false)
  public static BooleanField IS_IMPORTED_ACCOUNT;

  static {
    GlobTypeLoader.init(Account.class, "account");
    MAIN_SUMMARY_KEY = org.globsframework.model.Key.create(TYPE, MAIN_SUMMARY_ACCOUNT_ID);
    SAVINGS_SUMMARY_KEY = org.globsframework.model.Key.create(TYPE, SAVINGS_SUMMARY_ACCOUNT_ID);
    ALL_SUMMARY_KEY = org.globsframework.model.Key.create(TYPE, ALL_SUMMARY_ACCOUNT_ID);
    SUMMARY_ACCOUNT_IDS.add(MAIN_SUMMARY_ACCOUNT_ID);
    SUMMARY_ACCOUNT_IDS.add(SAVINGS_SUMMARY_ACCOUNT_ID);
    SUMMARY_ACCOUNT_IDS.add(ALL_SUMMARY_ACCOUNT_ID);
  }

  public static void createSummary(GlobRepository repository) {
    repository.create(TYPE,
                      value(ID, MAIN_SUMMARY_ACCOUNT_ID),
                      value(NUMBER, SUMMARY_ACCOUNT_NUMBER),
                      value(IS_IMPORTED_ACCOUNT, true));
    repository.create(TYPE,
                      value(ID, SAVINGS_SUMMARY_ACCOUNT_ID),
                      value(NUMBER, SUMMARY_ACCOUNT_NUMBER));
    repository.create(TYPE,
                      value(ID, ALL_SUMMARY_ACCOUNT_ID),
                      value(NUMBER, SUMMARY_ACCOUNT_NUMBER));
  }

  public static Glob getBank(Glob account, GlobRepository repository) {
    Glob bankEntity = repository.findLinkTarget(account, Account.BANK_ENTITY);
    if (bankEntity == null) {
      throw new ItemNotFound("Account with no bank entity: " + account);
    }
    return BankEntity.getBank(bankEntity, repository);
  }

  public static boolean shoudCreateMirror(Glob fromAccount, Glob toAccount) {

    return (fromAccount != null) && (toAccount != null) &&
           ((fromAccount.get(Account.IS_IMPORTED_ACCOUNT) && !toAccount.get(Account.IS_IMPORTED_ACCOUNT))
            ||
            (!fromAccount.get(Account.IS_IMPORTED_ACCOUNT) && toAccount.get(Account.IS_IMPORTED_ACCOUNT)));
  }

  public static boolean areNoneImported(Glob fromAccount, Glob toAccount) {
    if (fromAccount == null && toAccount == null) {
      return false;
    }
    if (fromAccount != null && toAccount == null) {
      return !fromAccount.get(Account.IS_IMPORTED_ACCOUNT);
    }
    if (fromAccount == null) {
      return !toAccount.get(Account.IS_IMPORTED_ACCOUNT);
    }
    return !toAccount.get(Account.IS_IMPORTED_ACCOUNT) && !fromAccount.get(Account.IS_IMPORTED_ACCOUNT);
  }

  public static boolean areBothImported(Glob fromAccount, Glob toAccount) {
    return !(fromAccount == null || toAccount == null)
           && toAccount.get(Account.IS_IMPORTED_ACCOUNT)
           && fromAccount.get(Account.IS_IMPORTED_ACCOUNT);
  }

  public static boolean isUserCreatedSavingsAccount(Glob account) {
    return (account != null) &&
           AccountType.SAVINGS.getId().equals(account.get(Account.ACCOUNT_TYPE)) &&
           !SAVINGS_SUMMARY_KEY.equals(account.getKey());
  }

  public static double getMultiplierForInOrOutputOfTheAccount(Glob fromAccount, Glob toAccount, Glob forAccount) {

    if (fromAccount == null && toAccount == null) {
      throw new RuntimeException("Should not be call if both account are null");
    }
    if (fromAccount != null && toAccount == null) {
      return -1;
    }
    if (fromAccount == null) {
      return 1;
    }
    if (forAccount.getKey().equals(toAccount.getKey())) {
      return 1;
    }
    if (forAccount.getKey().equals(fromAccount.getKey())) {
      return -1;
    }
    throw new RuntimeException("Call with bad account");
  }

  public static double getMultiplierWithMainAsPointOfView(Glob fromAccount, Glob toAccount, GlobRepository repository) {
    SameAccountChecker mainAccountChecker = SameAccountChecker.getSameAsMain(repository);
    return getMultiplierWithMainAsPointOfView(fromAccount, toAccount, repository, mainAccountChecker);
  }

  private static double getMultiplierWithMainAsPointOfView(Glob fromAccount, Glob toAccount,
                                                           GlobRepository repository, SameAccountChecker mainAccountChecker) {
    double multiplier;
    Integer fromAccountIdPointOfView = toAccount == null ?
                                       (fromAccount == null ? null : fromAccount.get(ID))
                                                         : toAccount.get(ID);
    if (fromAccountIdPointOfView == null) {
      multiplier = 0;
    }
    else {
      if (fromAccount != null && mainAccountChecker.isSame(fromAccount.get(ID))) {
        fromAccountIdPointOfView = fromAccount.get(ID);
      }
      if (toAccount != null && mainAccountChecker.isSame(toAccount.get(ID))) {
        fromAccountIdPointOfView = toAccount.get(ID);
      }
      multiplier = getMultiplierForInOrOutputOfTheAccount(fromAccount, toAccount,
                                                          repository.get(org.globsframework.model.Key.create(TYPE, fromAccountIdPointOfView)));
    }
    return multiplier;
  }

  public static boolean onlyOneIsImported(Glob account1, Glob account2) {
    return account1 != null && account2 != null &&
           account1.get(Account.IS_IMPORTED_ACCOUNT) != account2.get(Account.IS_IMPORTED_ACCOUNT);
  }

  public static class Serializer implements PicsouGlobSerializer {

    public int getWriteVersion() {
      return 5;
    }

    public byte[] serializeData(FieldValues values) {
      SerializedByteArrayOutput serializedByteArrayOutput = new SerializedByteArrayOutput();
      SerializedOutput outputStream = serializedByteArrayOutput.getOutput();
      outputStream.writeUtf8String(values.get(NUMBER));
      outputStream.writeInteger(values.get(BANK_ENTITY));
      outputStream.writeInteger(values.get(BRANCH_ID));
      outputStream.writeUtf8String(values.get(NAME));
      outputStream.writeDouble(values.get(BALANCE));
      outputStream.writeInteger(values.get(TRANSACTION_ID));
      outputStream.writeDate(values.get(BALANCE_DATE));
      outputStream.writeBoolean(values.get(IS_CARD_ACCOUNT));
      outputStream.writeInteger(values.get(ACCOUNT_TYPE));
      outputStream.writeInteger(values.get(UPDATE_MODE));
      outputStream.writeBoolean(values.get(IS_IMPORTED_ACCOUNT));
      outputStream.writeDate(values.get(CLOSED_DATE));
      return serializedByteArrayOutput.toByteArray();
    }

    public void deserializeData(int version, FieldSetter fieldSetter, byte[] data) {
      if (version == 5) {
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

    private void deserializeDataV5(FieldSetter fieldSetter, byte[] data) {
      SerializedInput input = SerializedInputOutputFactory.init(data);
      fieldSetter.set(NUMBER, input.readUtf8String());
      fieldSetter.set(BANK_ENTITY, input.readInteger());
      fieldSetter.set(BRANCH_ID, input.readInteger());
      fieldSetter.set(NAME, input.readUtf8String());
      fieldSetter.set(BALANCE, input.readDouble());
      fieldSetter.set(TRANSACTION_ID, input.readInteger());
      fieldSetter.set(BALANCE_DATE, input.readDate());
      fieldSetter.set(IS_CARD_ACCOUNT, input.readBoolean());
      fieldSetter.set(ACCOUNT_TYPE, input.readInteger());
      fieldSetter.set(UPDATE_MODE, input.readInteger());
      fieldSetter.set(IS_IMPORTED_ACCOUNT, input.readBoolean());
      fieldSetter.set(CLOSED_DATE, input.readDate());
    }

    private void deserializeDataV4(FieldSetter fieldSetter, byte[] data) {
      SerializedInput input = SerializedInputOutputFactory.init(data);
      fieldSetter.set(NUMBER, input.readUtf8String());
      fieldSetter.set(BANK_ENTITY, input.readInteger());
      fieldSetter.set(BRANCH_ID, input.readInteger());
      fieldSetter.set(NAME, input.readUtf8String());
      fieldSetter.set(BALANCE, input.readDouble());
      fieldSetter.set(TRANSACTION_ID, input.readInteger());
      fieldSetter.set(BALANCE_DATE, input.readDate());
      fieldSetter.set(IS_CARD_ACCOUNT, input.readBoolean());
      fieldSetter.set(ACCOUNT_TYPE, input.readInteger());
      fieldSetter.set(UPDATE_MODE, AccountUpdateMode.AUTOMATIC.getId());
      fieldSetter.set(IS_IMPORTED_ACCOUNT, input.readBoolean());
      fieldSetter.set(CLOSED_DATE, input.readDate());
    }

    private void deserializeDataV3(FieldSetter fieldSetter, byte[] data) {
      SerializedInput input = SerializedInputOutputFactory.init(data);
      fieldSetter.set(NUMBER, input.readUtf8String());
      fieldSetter.set(BANK_ENTITY, input.readInteger());
      fieldSetter.set(BRANCH_ID, input.readInteger());
      fieldSetter.set(NAME, input.readUtf8String());
      fieldSetter.set(BALANCE, input.readDouble());
      fieldSetter.set(TRANSACTION_ID, input.readInteger());
      fieldSetter.set(BALANCE_DATE, input.readDate());
      fieldSetter.set(IS_CARD_ACCOUNT, input.readBoolean());
      fieldSetter.set(ACCOUNT_TYPE, input.readInteger());
      fieldSetter.set(UPDATE_MODE, AccountUpdateMode.AUTOMATIC.getId());
      fieldSetter.set(IS_IMPORTED_ACCOUNT, input.readBoolean());
    }

    private void deserializeDataV2(FieldSetter fieldSetter, byte[] data) {
      SerializedInput input = SerializedInputOutputFactory.init(data);
      fieldSetter.set(NUMBER, input.readUtf8String());
      fieldSetter.set(BANK_ENTITY, input.readInteger());
      fieldSetter.set(BRANCH_ID, input.readInteger());
      fieldSetter.set(NAME, input.readUtf8String());
      fieldSetter.set(BALANCE, input.readDouble());
      fieldSetter.set(TRANSACTION_ID, input.readInteger());
      fieldSetter.set(BALANCE_DATE, input.readDate());
      fieldSetter.set(IS_CARD_ACCOUNT, input.readBoolean());
      fieldSetter.set(ACCOUNT_TYPE, AccountType.MAIN.getId());
      fieldSetter.set(UPDATE_MODE, AccountUpdateMode.AUTOMATIC.getId());
      fieldSetter.set(IS_IMPORTED_ACCOUNT, true);
    }

    private void deserializeDataV1(FieldSetter fieldSetter, byte[] data) {
      SerializedInput input = SerializedInputOutputFactory.init(data);
      fieldSetter.set(NUMBER, input.readString());
      fieldSetter.set(BANK_ENTITY, input.readInteger());
      fieldSetter.set(BRANCH_ID, input.readInteger());
      fieldSetter.set(NAME, input.readString());
      fieldSetter.set(BALANCE, input.readDouble());
      fieldSetter.set(TRANSACTION_ID, input.readInteger());
      fieldSetter.set(BALANCE_DATE, input.readDate());
      fieldSetter.set(IS_CARD_ACCOUNT, input.readBoolean());
      fieldSetter.set(ACCOUNT_TYPE, AccountType.MAIN.getId());
      fieldSetter.set(UPDATE_MODE, AccountUpdateMode.AUTOMATIC.getId());
      fieldSetter.set(IS_IMPORTED_ACCOUNT, true);
    }
  }
}
