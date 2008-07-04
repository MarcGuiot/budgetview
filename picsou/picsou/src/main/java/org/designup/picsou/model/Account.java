package org.designup.picsou.model;

import org.designup.picsou.server.serialization.PicsouGlobSerializer;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.annotations.Target;
import org.globsframework.metamodel.fields.*;
import org.globsframework.metamodel.utils.GlobTypeLoader;
import org.globsframework.model.FieldSetter;
import static org.globsframework.model.FieldValue.value;
import org.globsframework.model.FieldValues;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Glob;
import org.globsframework.model.utils.GlobUtils;
import org.globsframework.utils.serialization.SerializedByteArrayOutput;
import org.globsframework.utils.serialization.SerializedInput;
import org.globsframework.utils.serialization.SerializedInputOutputFactory;
import org.globsframework.utils.serialization.SerializedOutput;
import org.globsframework.utils.exceptions.ItemNotFound;

public class Account {
  public static final String SUMMARY_ACCOUNT_NUMBER = null;
  public static final int SUMMARY_ACCOUNT_ID = -1;

  public static GlobType TYPE;

  @Key
  public static IntegerField ID;

  public static StringField NUMBER;

  @Target(BankEntity.class)
  public static LinkField BANK_ENTITY;

  public static IntegerField BRANCH_ID;

  public static StringField NAME;
  public static DoubleField BALANCE;
  public static DateField UPDATE_DATE;
  public static BooleanField IS_CARD_ACCOUNT;

  static {
    GlobTypeLoader.init(Account.class);
  }

  public static void createSummary(GlobRepository repository) {
    repository.create(TYPE,
                      value(ID, SUMMARY_ACCOUNT_ID),
                      value(NUMBER, SUMMARY_ACCOUNT_NUMBER));
  }

  public static Glob getBank(Glob account, GlobRepository repository) {
    Glob bankEntity = repository.findLinkTarget(account, Account.BANK_ENTITY);
    if (bankEntity == null) {
      throw new ItemNotFound("Account with no bank entity: " + GlobUtils.dump(account));
    }
    return BankEntity.getBank(bankEntity, repository);
  }

  public static class Serialization implements PicsouGlobSerializer {

    public byte[] serializeData(FieldValues values) {
      SerializedByteArrayOutput serializedByteArrayOutput = new SerializedByteArrayOutput();
      SerializedOutput outputStream = serializedByteArrayOutput.getOutput();
      outputStream.writeString(values.get(NUMBER));
      outputStream.writeInteger(values.get(BANK_ENTITY));
      outputStream.writeInteger(values.get(BRANCH_ID));
      outputStream.writeString(values.get(NAME));
      outputStream.writeDouble(values.get(BALANCE));
      outputStream.writeDate(values.get(UPDATE_DATE));
      outputStream.writeBoolean(values.get(IS_CARD_ACCOUNT));
      return serializedByteArrayOutput.toByteArray();
    }

    public void deserializeData(int version, FieldSetter fieldSetter, byte[] data) {
      if (version == 1) {
        deserializeDataV1(fieldSetter, data);
      }
    }

    private void deserializeDataV1(FieldSetter fieldSetter, byte[] data) {
      SerializedInput input = SerializedInputOutputFactory.init(data);
      fieldSetter.set(NUMBER, input.readString());
      fieldSetter.set(BANK_ENTITY, input.readInteger());
      fieldSetter.set(BRANCH_ID, input.readInteger());
      fieldSetter.set(NAME, input.readString());
      fieldSetter.set(BALANCE, input.readDouble());
      fieldSetter.set(UPDATE_DATE, input.readDate());
      fieldSetter.set(IS_CARD_ACCOUNT, input.readBoolean());
    }

    public int getWriteVersion() {
      return 1;
    }
  }

}
