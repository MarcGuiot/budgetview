package com.budgetview.model;

import com.budgetview.shared.model.AccountType;
import com.budgetview.shared.model.Provider;
import com.budgetview.shared.utils.Amounts;
import com.budgetview.shared.utils.GlobSerializer;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.DefaultBoolean;
import org.globsframework.metamodel.annotations.DefaultInteger;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.annotations.Target;
import org.globsframework.metamodel.fields.*;
import org.globsframework.metamodel.utils.GlobTypeLoader;
import org.globsframework.model.*;
import org.globsframework.utils.Strings;
import org.globsframework.utils.Utils;
import org.globsframework.utils.serialization.SerializedByteArrayOutput;
import org.globsframework.utils.serialization.SerializedInput;
import org.globsframework.utils.serialization.SerializedInputOutputFactory;
import org.globsframework.utils.serialization.SerializedOutput;

import static org.globsframework.model.FieldValue.value;
import static org.globsframework.model.utils.GlobMatchers.and;
import static org.globsframework.model.utils.GlobMatchers.fieldEquals;

// Account-related information found in the bank files, as opposed to the Account type which is BudgetView's internal representation
public class RealAccount {

  public static GlobType TYPE;

  @Key
  public static IntegerField ID;

  public static StringField NAME;

  public static StringField NUMBER;

  public static StringField POSITION;

  public static DateField POSITION_DATE;

  @Target(Bank.class)
  public static LinkField BANK; // to protect if same name

  @Target(BankEntity.class)
  public static LinkField BANK_ENTITY;

  public static StringField BANK_ENTITY_LABEL;

  @Target(AccountType.class)
  public static LinkField ACCOUNT_TYPE;

  @Target(Account.class)
  public static LinkField ACCOUNT;

  public static IntegerField TRANSACTION_ID;

  @Target(AccountCardType.class)
  @DefaultInteger(0)
  public static LinkField CARD_TYPE;

  public static StringField FILE_NAME;

  public static StringField FILE_CONTENT;

  @Target(Provider.class)
  public static LinkField PROVIDER;

  public static IntegerField PROVIDER_ACCOUNT_ID;

  @DefaultBoolean(true)
  public static BooleanField ENABLED;

  static {
    GlobTypeLoader.init(RealAccount.class, "realAccount");
  }

  public static Glob findFromProvider(Integer providerId, Integer providerAccountId, GlobRepository repository) {
    GlobList accounts = repository.getAll(RealAccount.TYPE,
                                          and(fieldEquals(RealAccount.PROVIDER, providerId),
                                              fieldEquals(RealAccount.PROVIDER_ACCOUNT_ID, providerAccountId)));
    if (accounts.isEmpty()) {
      return null;
    }
    return accounts.getFirst();
  }

  public static Glob findByAccountNumber(String number, Integer bankId, GlobRepository repository) {
    GlobList accounts = repository.getAll(RealAccount.TYPE, and(fieldEquals(RealAccount.BANK, bankId),
                                                                fieldEquals(RealAccount.NUMBER, number)));
    if (accounts.isEmpty()) {
      return null;
    }
    return accounts.getFirst();
  }

  public static Glob findOrCreate(String name, String number, Integer bankId, GlobRepository repository) {
    GlobList accounts = repository.getAll(RealAccount.TYPE,
                                          and(fieldEquals(RealAccount.NAME, name),
                                              fieldEquals(RealAccount.NUMBER, number),
                                              fieldEquals(RealAccount.BANK, bankId)));
    if (accounts.size() > 0) {
      return accounts.getFirst();
    }
    return repository.create(RealAccount.TYPE,
                             value(RealAccount.ID, repository.getIdGenerator().getNextId(RealAccount.ID, 1)),
                             value(RealAccount.NAME, name),
                             value(RealAccount.NUMBER, number),
                             value(RealAccount.BANK, bankId));

  }

  public static boolean areStrictlyEquivalent(Glob realAccount1, Glob realAccount2) {
    return (Strings.isNotEmpty(realAccount1.get(NAME)) || Strings.isNotEmpty(realAccount1.get(NUMBER)))
           &&
           realAccount1.get(BANK) != null &&
           Utils.equalIgnoreCase(realAccount1.get(NAME), realAccount2.get(NAME)) &&
           Utils.equalIgnoreCase(realAccount1.get(NUMBER), realAccount2.get(NUMBER)) &&
           Utils.equal(realAccount1.get(BANK), realAccount2.get(BANK)) &&
           Utils.equal(realAccount1.get(BANK_ENTITY), realAccount2.get(BANK_ENTITY)) &&
           Utils.equal(realAccount1.get(CARD_TYPE), realAccount2.get(CARD_TYPE)) &&
           !Utils.equal(realAccount1.get(ID), realAccount2.get(ID));
  }

  public static boolean areEquivalent(Glob realAccount1, Glob realAccount2) {
    return (Strings.isNotEmpty(realAccount1.get(NAME)) || Strings.isNotEmpty(realAccount1.get(NUMBER))) &&
           Utils.equalIgnoreCase(realAccount1.get(NAME), realAccount2.get(NAME)) &&
           Utils.equalIgnoreCase(realAccount1.get(NUMBER), realAccount2.get(NUMBER)) &&
           !Utils.equal(realAccount1.get(ID), realAccount2.get(ID));
  }

  public static boolean areNearEquivalent(Glob realAccount1, Glob realAccount2) {
    return Utils.equalIgnoreCase(realAccount1.get(NAME), realAccount2.get(NAME)) &&
           Utils.equalIgnoreCase(realAccount1.get(NUMBER), realAccount2.get(NUMBER)) &&
           !Utils.equal(realAccount1.get(ID), realAccount2.get(ID));
  }

  public static Glob createAccountFromImported(Glob realAccount, GlobRepository repository, boolean isImported) {
    String amount = realAccount.get(POSITION);
    Double position = amount != null ? Amounts.extractAmount(amount) : null;
    return repository.create(Account.TYPE,
                             value(Account.BANK_ENTITY, realAccount.get(BANK_ENTITY)),
                             value(Account.BANK_ENTITY_LABEL, realAccount.get(BANK_ENTITY_LABEL)),
                             value(Account.ACCOUNT_TYPE, realAccount.get(ACCOUNT_TYPE)),
                             value(Account.CARD_TYPE, realAccount.get(CARD_TYPE)),
                             value(Account.POSITION_DATE, realAccount.get(POSITION_DATE)),
                             value(Account.NUMBER, realAccount.get(NUMBER)),
                             value(Account.NAME, realAccount.get(NAME)),
                             value(Account.LAST_IMPORT_POSITION, position),
                             value(Account.IS_IMPORTED_ACCOUNT, isImported),
                             value(Account.DIRECT_SYNCHRO, true),
                             value(Account.BANK, realAccount.get(BANK)));
  }

  public static void copy(GlobRepository repository, Glob from, Glob to) {
    if (from.get(RealAccount.POSITION_DATE).after(to.get(RealAccount.POSITION_DATE))) {
      repository.update(to.getKey(),
                        value(RealAccount.POSITION, from.get(RealAccount.POSITION)),
                        value(RealAccount.POSITION_DATE, from.get(RealAccount.POSITION_DATE)));
    }
  }

  public static class Serializer implements GlobSerializer {

    public int getWriteVersion() {
      return 4;
    }

    public boolean shouldBeSaved(GlobRepository repository, FieldValues fieldValues) {
      return true;
    }

    public byte[] serializeData(FieldValues fieldValues) {
      SerializedByteArrayOutput serializedByteArrayOutput = new SerializedByteArrayOutput();
      SerializedOutput output = serializedByteArrayOutput.getOutput();
      output.writeUtf8String(fieldValues.get(NAME));
      output.writeUtf8String(fieldValues.get(NUMBER));
      output.writeUtf8String(fieldValues.get(POSITION));
      output.writeDate(fieldValues.get(POSITION_DATE));
      output.writeInteger(fieldValues.get(BANK));
      output.writeInteger(fieldValues.get(BANK_ENTITY));
      output.writeUtf8String(fieldValues.get(BANK_ENTITY_LABEL));
      output.writeInteger(fieldValues.get(ACCOUNT_TYPE));
      output.writeInteger(fieldValues.get(ACCOUNT));
      output.writeInteger(fieldValues.get(CARD_TYPE));
      output.writeInteger(fieldValues.get(TRANSACTION_ID));
      output.writeInteger(fieldValues.get(PROVIDER));
      output.writeInteger(fieldValues.get(PROVIDER_ACCOUNT_ID));
      output.writeBoolean(fieldValues.get(ENABLED));
      return serializedByteArrayOutput.toByteArray();
    }

    public void deserializeData(int version, byte[] data, Integer id, FieldSetter fieldSetter) {
      if (version == 4) {
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

    private void deserializeDataV4(FieldSetter fieldSetter, byte[] data) {
      SerializedInput input = SerializedInputOutputFactory.init(data);
      fieldSetter.set(NAME, input.readUtf8String());
      fieldSetter.set(NUMBER, input.readUtf8String());
      fieldSetter.set(POSITION, input.readUtf8String());
      fieldSetter.set(POSITION_DATE, input.readDate());
      fieldSetter.set(BANK, input.readInteger());
      fieldSetter.set(BANK_ENTITY, input.readInteger());
      fieldSetter.set(BANK_ENTITY_LABEL, input.readUtf8String());
      fieldSetter.set(ACCOUNT_TYPE, input.readInteger());
      fieldSetter.set(ACCOUNT, input.readInteger());
      fieldSetter.set(CARD_TYPE, input.readInteger());
      fieldSetter.set(TRANSACTION_ID, input.readInteger());
      fieldSetter.set(PROVIDER, input.readInteger());
      fieldSetter.set(PROVIDER_ACCOUNT_ID, input.readInteger());
      fieldSetter.set(ENABLED, input.readBoolean());
    }

    private void deserializeDataV3(FieldSetter fieldSetter, byte[] data) {
      SerializedInput input = SerializedInputOutputFactory.init(data);
      input.readInteger(); // SYNCHRO
      fieldSetter.set(BANK, input.readInteger());
      input.readUtf8String(); // BANK_ID
      fieldSetter.set(BANK_ENTITY, input.readInteger());
      fieldSetter.set(BANK_ENTITY_LABEL, input.readUtf8String());
      input.readUtf8String(); // ACC_TYPE
      input.readUtf8String(); // URL
      input.readUtf8String(); // ORG
      input.readUtf8String(); // FID
      fieldSetter.set(NUMBER, input.readUtf8String());
      fieldSetter.set(POSITION, input.readUtf8String());
      fieldSetter.set(POSITION_DATE, input.readDate());
      fieldSetter.set(NAME, input.readUtf8String());
      fieldSetter.set(ACCOUNT_TYPE, input.readInteger());
      input.readBoolean(); // SAVINGS
      fieldSetter.set(ACCOUNT, input.readInteger());
      fieldSetter.set(CARD_TYPE, input.readInteger());
      fieldSetter.set(TRANSACTION_ID, input.readInteger());
      input.readBoolean(); // FROM_SYNCHRO
      fieldSetter.set(PROVIDER, input.readInteger());
      fieldSetter.set(PROVIDER_ACCOUNT_ID, input.readInteger());
    }

    private void deserializeDataV2(FieldSetter fieldSetter, byte[] data) {
      SerializedInput input = SerializedInputOutputFactory.init(data);
      input.readInteger(); // SYNCHRO
      fieldSetter.set(BANK, input.readInteger());
      input.readUtf8String(); // BANK_ID
      fieldSetter.set(BANK_ENTITY, input.readInteger());
      fieldSetter.set(BANK_ENTITY_LABEL, input.readUtf8String());
      input.readUtf8String(); // ACC_TYPE
      input.readUtf8String(); // URL
      input.readUtf8String(); // ORG
      input.readUtf8String(); // FID
      fieldSetter.set(NUMBER, input.readUtf8String());
      fieldSetter.set(POSITION, input.readUtf8String());
      fieldSetter.set(POSITION_DATE, input.readDate());
      fieldSetter.set(NAME, input.readUtf8String());
      fieldSetter.set(ACCOUNT_TYPE, input.readInteger());
      input.readBoolean(); // SAVINGS
      fieldSetter.set(ACCOUNT, input.readInteger());
      fieldSetter.set(CARD_TYPE, input.readInteger());
      fieldSetter.set(TRANSACTION_ID, input.readInteger());
      input.readBoolean(); // FROM_SYNCHRO
      fieldSetter.set(PROVIDER, Provider.FILE_IMPORT.getId());
      fieldSetter.set(PROVIDER_ACCOUNT_ID, null);
    }

    private void deserializeDataV1(FieldSetter fieldSetter, byte[] data) {
      SerializedInput input = SerializedInputOutputFactory.init(data);
      fieldSetter.set(BANK, input.readInteger());
      input.readUtf8String(); // BANK_ID
      fieldSetter.set(BANK_ENTITY, input.readInteger());
      fieldSetter.set(BANK_ENTITY_LABEL, input.readUtf8String());
      input.readUtf8String(); // ACC_TYPE
      input.readUtf8String(); // URL
      input.readUtf8String(); // ORG
      input.readUtf8String(); // FID
      fieldSetter.set(NUMBER, input.readUtf8String());
      fieldSetter.set(POSITION, input.readUtf8String());
      fieldSetter.set(POSITION_DATE, input.readDate());
      fieldSetter.set(NAME, input.readUtf8String());
      fieldSetter.set(ACCOUNT_TYPE, input.readInteger());
      input.readBoolean(); // SAVINGS
      fieldSetter.set(ACCOUNT, input.readInteger());
      fieldSetter.set(CARD_TYPE, input.readInteger());
      fieldSetter.set(TRANSACTION_ID, input.readInteger());
      input.readBoolean(); // FROM_SYNCHRO
      fieldSetter.set(PROVIDER, Provider.FILE_IMPORT.getId());
      fieldSetter.set(PROVIDER_ACCOUNT_ID, null);
    }
  }
}
