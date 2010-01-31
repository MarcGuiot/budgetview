package org.designup.picsou.model;

import org.designup.picsou.server.serialization.PicsouGlobSerializer;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.*;
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

  public static StringField BANK_ENTITY_LABEL;

  @Target(Bank.class)
  public static LinkField BANK;

  public static IntegerField BRANCH_ID;

  public static StringField NAME;

  public static DoubleField POSITION;

  @Target(Transaction.class)
  public static LinkField TRANSACTION_ID;

  public static DateField POSITION_DATE;

  public static DoubleField FIRST_POSITION;

  public static DateField OPEN_DATE;

  public static DateField CLOSED_DATE;

  @Target(AccountCardType.class)
  @DefaultInteger(0)
  public static LinkField CARD_TYPE;

  @Target(AccountType.class)
  @DefaultInteger(1)
  @Required
  public static LinkField ACCOUNT_TYPE;

  @Target(AccountUpdateMode.class)
  @DefaultInteger(1)
  @Required
  public static LinkField UPDATE_MODE;

  @DefaultBoolean(false)
  @Required
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
    Glob bank = findBank(account, repository);
    if (bank == null) {
      throw new ItemNotFound("Account with no bank : " + account);
    }
    return bank;
  }

  public static Glob findBank(Glob account, GlobRepository repository) {
    Glob bank;
    if (account.get(Account.BANK) == null) { // l'upgrade n'est pas encore passé
      Glob bankEntity = repository.findLinkTarget(account, Account.BANK_ENTITY);
      bank = repository.findLinkTarget(bankEntity, BankEntity.BANK);
    }
    else {
      bank = repository.findLinkTarget(account, Account.BANK);
    }
    return bank;
  }

  public static boolean shouldCreateMirror(Glob fromAccount, Glob toAccount) {
    return (fromAccount != null) && (toAccount != null) &&
           ((fromAccount.isTrue(Account.IS_IMPORTED_ACCOUNT) && !toAccount.isTrue(Account.IS_IMPORTED_ACCOUNT))
            ||
            (!fromAccount.isTrue(Account.IS_IMPORTED_ACCOUNT) && toAccount.isTrue(Account.IS_IMPORTED_ACCOUNT)));
  }

  public static boolean areNoneImported(Glob fromAccount, Glob toAccount) {
    if (fromAccount == null && toAccount == null) {
      return false;
    }
    if (fromAccount != null && toAccount == null) {
      return !fromAccount.isTrue(Account.IS_IMPORTED_ACCOUNT);
    }
    if (fromAccount == null) {
      return !toAccount.isTrue(Account.IS_IMPORTED_ACCOUNT);
    }
    return !toAccount.isTrue(Account.IS_IMPORTED_ACCOUNT) && !fromAccount.isTrue(Account.IS_IMPORTED_ACCOUNT);
  }

  public static boolean areBothImported(Glob fromAccount, Glob toAccount) {
    return !(fromAccount == null || toAccount == null)
           && toAccount.isTrue(Account.IS_IMPORTED_ACCOUNT)
           && fromAccount.isTrue(Account.IS_IMPORTED_ACCOUNT);
  }

  public static boolean isUserCreatedSavingsAccount(Glob account) {
    return (account != null) &&
           AccountType.SAVINGS.getId().equals(account.get(Account.ACCOUNT_TYPE)) &&
           !SAVINGS_SUMMARY_KEY.equals(account.getKey());
  }

  public static double getMultiplierForInOrOutputOfTheAccount(Glob fromAccount, Glob toAccount, Glob forAccount) {

    if (fromAccount == null && toAccount == null) {
      throw new RuntimeException("Should not be called if both accounts are null");
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
    throw new RuntimeException("Called with bad account");
  }

  public static double getMultiplierWithMainAsPointOfView(Glob fromAccount, Glob toAccount,
                                                          GlobRepository repository) {
    double multiplier;
    Integer forAccountIdPointOfView = toAccount == null ?
                                      (fromAccount == null ? null : fromAccount.get(ID))
                                      : toAccount.get(ID);
    if (forAccountIdPointOfView == null) {
      multiplier = 0;
    }
    else {
      if (fromAccount != null && fromAccount.get(ACCOUNT_TYPE).equals(AccountType.MAIN.getId())) {
        forAccountIdPointOfView = fromAccount.get(ID);
      }
      // si les deux comptes sont des main comptes on prends le toAccount
      if (toAccount != null && toAccount.get(ACCOUNT_TYPE).equals(AccountType.MAIN.getId())) {
        forAccountIdPointOfView = toAccount.get(ID);
      }
      multiplier = getMultiplierForInOrOutputOfTheAccount(fromAccount, toAccount,
                                                          repository.get(org.globsframework.model.Key.create(TYPE, forAccountIdPointOfView)));
    }
    return multiplier;
  }

  public static boolean onlyOneIsImported(Glob account1, Glob account2) {
    return account1 != null && account2 != null &&
           account1.isTrue(Account.IS_IMPORTED_ACCOUNT) != account2.isTrue(Account.IS_IMPORTED_ACCOUNT);
  }

  public static double computeAmountMultiplier(Glob fromAccount, Glob toAccount, GlobRepository repository) {
    if (areBothImported(fromAccount, toAccount)) {
      return 1;
    }
    double multiplier = getMultiplierWithMainAsPointOfView(fromAccount, toAccount, repository);
    if (multiplier == 0) {
      if (fromAccount == null && toAccount == null) {
        multiplier = 1;
      }
      else {
        if (onlyOneIsImported(fromAccount, toAccount)) {
          if (fromAccount.isTrue(IS_IMPORTED_ACCOUNT)) {
            multiplier = -1;
          }
          else {
            multiplier = 1;
          }
        }
        else if (fromAccount == null) {
          multiplier = 1;
        }
        else {
          multiplier = -1;
        }
      }
    }
    return multiplier;
  }

  public static class Serializer implements PicsouGlobSerializer {

    public int getWriteVersion() {
      return 8;
    }

    public byte[] serializeData(FieldValues values) {
      SerializedByteArrayOutput serializedByteArrayOutput = new SerializedByteArrayOutput();
      SerializedOutput outputStream = serializedByteArrayOutput.getOutput();
      outputStream.writeUtf8String(values.get(NUMBER));
      outputStream.writeInteger(values.get(BANK_ENTITY));
      outputStream.writeInteger(values.get(BRANCH_ID));
      outputStream.writeUtf8String(values.get(NAME));
      outputStream.writeDouble(values.get(POSITION));
      outputStream.writeInteger(values.get(TRANSACTION_ID));
      outputStream.writeDate(values.get(POSITION_DATE));
      outputStream.writeInteger(values.get(ACCOUNT_TYPE));
      outputStream.writeInteger(values.get(UPDATE_MODE));
      outputStream.writeBoolean(values.get(IS_IMPORTED_ACCOUNT));
      outputStream.writeDate(values.get(CLOSED_DATE));
      outputStream.writeDate(values.get(OPEN_DATE));
      outputStream.writeDouble(values.get(FIRST_POSITION));
      outputStream.writeInteger(values.get(BANK));
      outputStream.writeUtf8String(values.get(BANK_ENTITY_LABEL));
      outputStream.writeInteger(values.get(CARD_TYPE));
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
      fieldSetter.set(NUMBER, input.readUtf8String());
      fieldSetter.set(BANK_ENTITY, input.readInteger());
      fieldSetter.set(BRANCH_ID, input.readInteger());
      fieldSetter.set(NAME, input.readUtf8String());
      fieldSetter.set(POSITION, input.readDouble());
      fieldSetter.set(TRANSACTION_ID, input.readInteger());
      fieldSetter.set(POSITION_DATE, input.readDate());
      fieldSetter.set(ACCOUNT_TYPE, input.readInteger());
      fieldSetter.set(UPDATE_MODE, input.readInteger());
      fieldSetter.set(IS_IMPORTED_ACCOUNT, input.readBoolean());
      fieldSetter.set(CLOSED_DATE, input.readDate());
      fieldSetter.set(OPEN_DATE, input.readDate());
      fieldSetter.set(FIRST_POSITION, input.readDouble());
      fieldSetter.set(BANK, input.readInteger());
      fieldSetter.set(BANK_ENTITY_LABEL, input.readUtf8String());
      fieldSetter.set(CARD_TYPE, input.readInteger());
    }

    private void deserializeDataV7(FieldSetter fieldSetter, byte[] data) {
      SerializedInput input = SerializedInputOutputFactory.init(data);
      fieldSetter.set(NUMBER, input.readUtf8String());
      fieldSetter.set(BANK_ENTITY, input.readInteger());
      fieldSetter.set(BRANCH_ID, input.readInteger());
      fieldSetter.set(NAME, input.readUtf8String());
      fieldSetter.set(POSITION, input.readDouble());
      fieldSetter.set(TRANSACTION_ID, input.readInteger());
      fieldSetter.set(POSITION_DATE, input.readDate());
      readAndUpdateCardType(fieldSetter, input);
      fieldSetter.set(ACCOUNT_TYPE, input.readInteger());
      fieldSetter.set(UPDATE_MODE, input.readInteger());
      fieldSetter.set(IS_IMPORTED_ACCOUNT, input.readBoolean());
      fieldSetter.set(CLOSED_DATE, input.readDate());
      fieldSetter.set(OPEN_DATE, input.readDate());
      fieldSetter.set(FIRST_POSITION, input.readDouble());
      fieldSetter.set(BANK, input.readInteger());
      fieldSetter.set(BANK_ENTITY_LABEL, input.readUtf8String());
    }

    private void readAndUpdateCardType(FieldSetter fieldSetter, SerializedInput input) {
      Boolean isCard = input.readBoolean();
      isCard = isCard == null ? false : isCard;
      fieldSetter.set(CARD_TYPE, isCard ?
                                 AccountCardType.DEFERRED.getId()
                                 : AccountCardType.NOT_A_CARD.getId());
    }

    private void deserializeDataV6(FieldSetter fieldSetter, byte[] data) {
      SerializedInput input = SerializedInputOutputFactory.init(data);
      fieldSetter.set(NUMBER, input.readUtf8String());
      fieldSetter.set(BANK_ENTITY, input.readInteger());
      fieldSetter.set(BRANCH_ID, input.readInteger());
      fieldSetter.set(NAME, input.readUtf8String());
      fieldSetter.set(POSITION, input.readDouble());
      fieldSetter.set(TRANSACTION_ID, input.readInteger());
      fieldSetter.set(POSITION_DATE, input.readDate());
      readAndUpdateCardType(fieldSetter, input);
      fieldSetter.set(ACCOUNT_TYPE, input.readInteger());
      fieldSetter.set(UPDATE_MODE, input.readInteger());
      fieldSetter.set(IS_IMPORTED_ACCOUNT, input.readBoolean());
      fieldSetter.set(CLOSED_DATE, input.readDate());
      fieldSetter.set(OPEN_DATE, input.readDate());
      fieldSetter.set(CLOSED_DATE, input.readDate()); // bug in previous serialize version : CLOSED_DATE twice
      fieldSetter.set(FIRST_POSITION, input.readDouble());
    }

    private void deserializeDataV5(FieldSetter fieldSetter, byte[] data) {
      SerializedInput input = SerializedInputOutputFactory.init(data);
      fieldSetter.set(NUMBER, input.readUtf8String());
      fieldSetter.set(BANK_ENTITY, input.readInteger());
      fieldSetter.set(BRANCH_ID, input.readInteger());
      fieldSetter.set(NAME, input.readUtf8String());
      fieldSetter.set(POSITION, input.readDouble());
      fieldSetter.set(TRANSACTION_ID, input.readInteger());
      fieldSetter.set(POSITION_DATE, input.readDate());
      readAndUpdateCardType(fieldSetter, input);
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
      fieldSetter.set(POSITION, input.readDouble());
      fieldSetter.set(TRANSACTION_ID, input.readInteger());
      fieldSetter.set(POSITION_DATE, input.readDate());
      readAndUpdateCardType(fieldSetter, input);
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
      fieldSetter.set(POSITION, input.readDouble());
      fieldSetter.set(TRANSACTION_ID, input.readInteger());
      fieldSetter.set(POSITION_DATE, input.readDate());
      readAndUpdateCardType(fieldSetter, input);
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
      fieldSetter.set(POSITION, input.readDouble());
      fieldSetter.set(TRANSACTION_ID, input.readInteger());
      fieldSetter.set(POSITION_DATE, input.readDate());
      readAndUpdateCardType(fieldSetter, input);
      fieldSetter.set(ACCOUNT_TYPE, AccountType.MAIN.getId());
      fieldSetter.set(UPDATE_MODE, AccountUpdateMode.AUTOMATIC.getId());
      fieldSetter.set(IS_IMPORTED_ACCOUNT, true);
    }

    private void deserializeDataV1(FieldSetter fieldSetter, byte[] data) {
      SerializedInput input = SerializedInputOutputFactory.init(data);
      fieldSetter.set(NUMBER, input.readJavaString());
      fieldSetter.set(BANK_ENTITY, input.readInteger());
      fieldSetter.set(BRANCH_ID, input.readInteger());
      fieldSetter.set(NAME, input.readJavaString());
      fieldSetter.set(POSITION, input.readDouble());
      fieldSetter.set(TRANSACTION_ID, input.readInteger());
      fieldSetter.set(POSITION_DATE, input.readDate());
      readAndUpdateCardType(fieldSetter, input);
      fieldSetter.set(ACCOUNT_TYPE, AccountType.MAIN.getId());
      fieldSetter.set(UPDATE_MODE, AccountUpdateMode.AUTOMATIC.getId());
      fieldSetter.set(IS_IMPORTED_ACCOUNT, true);
    }
  }
}
