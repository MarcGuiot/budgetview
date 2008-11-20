package org.designup.picsou.model;

import org.designup.picsou.server.serialization.PicsouGlobSerializer;
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
  public static final Set<Integer> SUMMARY_ACCOUNT = new HashSet<Integer>();
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

  public static IntegerField TRANSACTION_ID;

  public static DateField BALANCE_DATE;

  public static DateField CLOSED_DATE;

  public static BooleanField IS_CARD_ACCOUNT;

  @Target(AccountType.class)
  @DefaultInteger(1)
  public static LinkField ACCOUNT_TYPE;

  @DefaultBoolean(true)
  public static BooleanField IS_IMPORTED_ACCOUNT;

  static {
    GlobTypeLoader.init(Account.class, "account");
    MAIN_SUMMARY_KEY = org.globsframework.model.Key.create(TYPE, MAIN_SUMMARY_ACCOUNT_ID);
    SAVINGS_SUMMARY_KEY = org.globsframework.model.Key.create(TYPE, SAVINGS_SUMMARY_ACCOUNT_ID);
    ALL_SUMMARY_KEY = org.globsframework.model.Key.create(TYPE, ALL_SUMMARY_ACCOUNT_ID);
    SUMMARY_ACCOUNT.add(MAIN_SUMMARY_ACCOUNT_ID);
    SUMMARY_ACCOUNT.add(SAVINGS_SUMMARY_ACCOUNT_ID);
    SUMMARY_ACCOUNT.add(ALL_SUMMARY_ACCOUNT_ID);
  }

  public static void createSummary(GlobRepository repository) {
    repository.create(TYPE,
                      value(ID, MAIN_SUMMARY_ACCOUNT_ID),
                      value(NUMBER, SUMMARY_ACCOUNT_NUMBER));
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

  public static class Serializer implements PicsouGlobSerializer {

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
      outputStream.writeBoolean(values.get(IS_IMPORTED_ACCOUNT));
      return serializedByteArrayOutput.toByteArray();
    }

    public void deserializeData(int version, FieldSetter fieldSetter, byte[] data) {
      if (version == 1) {
        deserializeDataV1(fieldSetter, data);
      }
      else if (version == 2) {
        deserializeDataV2(fieldSetter, data);
      }
      else if (version == 3) {
        deserializeDataV3(fieldSetter, data);
      }
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
      fieldSetter.set(IS_IMPORTED_ACCOUNT, true);
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
      fieldSetter.set(IS_IMPORTED_ACCOUNT, true);
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
      fieldSetter.set(IS_IMPORTED_ACCOUNT, input.readBoolean());
    }

    public int getWriteVersion() {
      return 3;
    }
  }

}
