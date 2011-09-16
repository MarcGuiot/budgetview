package org.designup.picsou.model;

import org.designup.picsou.model.util.Amounts;
import org.designup.picsou.server.serialization.PicsouGlobSerializer;
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

public class RealAccount {

  public static GlobType TYPE;

  @Key
  public static IntegerField ID;

  @Target(Bank.class)
  public static LinkField BANK; // to protect if same name

  public static StringField BANK_ID;

  public static IntegerField BANK_ENTITY;

  public static StringField BANK_ENTITY_LABEL;

  public static StringField ACC_TYPE;

  public static StringField URL;

  public static StringField ORG;

  public static StringField FID;

  public static StringField NUMBER;

  public static DateField POSITION_DATE;

  public static StringField POSITION;

  public static IntegerField TRANSACTION_ID;

  public static StringField NAME;

  @Target(value = AccountCardType.class)
  @DefaultInteger(0)
  public static LinkField CARD_TYPE;

  @Target(value = AccountType.class)
  public static LinkField ACCOUNT_TYPE;

  @DefaultBoolean(false)
  public static BooleanField SAVINGS;

  @Target(Account.class)
  public static LinkField ACCOUNT;

  public static StringField FILE_NAME;

  static {
    GlobTypeLoader.init(RealAccount.class, "realAccount");
  }

  static public boolean areStriclyEquivalent(Glob account, Glob glob) {
    return (Strings.isNotEmpty(account.get(NAME)) || Strings.isNotEmpty(account.get(NUMBER)))
           &&
           account.get(BANK) != null &&
           Utils.equalIgnoreCase(account.get(NAME), glob.get(NAME)) &&
           Utils.equalIgnoreCase(account.get(NUMBER), glob.get(NUMBER)) &&
           Utils.equal(account.get(BANK), glob.get(BANK)) &&
           Utils.equal(account.get(BANK_ENTITY), glob.get(BANK_ENTITY)) &&
           Utils.equal(account.get(ACC_TYPE), glob.get(ACC_TYPE)) &&
           Utils.equal(account.get(BANK_ID), glob.get(BANK_ID)) &&
           Utils.equal(account.get(CARD_TYPE), glob.get(CARD_TYPE)) &&
           Utils.equal(account.get(SAVINGS), glob.get(SAVINGS)) &&
           !Utils.equal(account.get(ID), glob.get(ID));
  }

  static public boolean areEquivalent(Glob account, Glob glob) {
    return (Strings.isNotEmpty(account.get(NAME)) || Strings.isNotEmpty(account.get(NUMBER)))
           &&
           Utils.equalIgnoreCase(account.get(NAME), glob.get(NAME)) &&
           Utils.equalIgnoreCase(account.get(NUMBER), glob.get(NUMBER)) &&
           Utils.equal(account.get(SAVINGS), glob.get(SAVINGS)) &&
           !Utils.equal(account.get(ID), glob.get(ID));
  }

  static public boolean areNearEquivalent(Glob account, Glob glob) {
    return Utils.equalIgnoreCase(account.get(NAME), glob.get(NAME)) &&
           Utils.equalIgnoreCase(account.get(NUMBER), glob.get(NUMBER)) &&
           !Utils.equal(account.get(ID), glob.get(ID));
  }

  public static Glob createAccountFromImported(Glob importedAccount, GlobRepository repository, boolean isImported) {
    String amount = importedAccount.get(POSITION);
    Double position = amount != null ? Amounts.extractAmount(amount) : null;
    return repository.create(Account.TYPE,
                             FieldValue.value(Account.BANK_ENTITY, importedAccount.get(BANK_ENTITY)),
                             FieldValue.value(Account.BANK_ENTITY_LABEL, importedAccount.get(BANK_ENTITY_LABEL)),
                             FieldValue.value(Account.ACCOUNT_TYPE, importedAccount.get(ACCOUNT_TYPE)),
                             FieldValue.value(Account.CARD_TYPE, importedAccount.get(CARD_TYPE)),
                             FieldValue.value(Account.POSITION_DATE, importedAccount.get(POSITION_DATE)),
                             FieldValue.value(Account.NUMBER, importedAccount.get(NUMBER)),
                             FieldValue.value(Account.NAME, importedAccount.get(NAME)),
                             FieldValue.value(Account.POSITION, position),
                             FieldValue.value(Account.IS_IMPORTED_ACCOUNT, isImported),
                             FieldValue.value(Account.DIRECT_SYNCHRO, true),
                             FieldValue.value(Account.BANK, importedAccount.get(BANK)));
  }

  public static void copy(GlobRepository repository, Glob from, Glob to) {
    if (from.get(RealAccount.POSITION_DATE).after(to.get(RealAccount.POSITION_DATE))) {
      repository.update(to.getKey(),
                        FieldValue.value(RealAccount.POSITION, from.get(RealAccount.POSITION)),
                        FieldValue.value(RealAccount.POSITION_DATE, from.get(RealAccount.POSITION_DATE)));
    }
  }

  public static class Serializer implements PicsouGlobSerializer {

    public int getWriteVersion() {
      return 1;
    }

    public byte[] serializeData(FieldValues fieldValues) {
      SerializedByteArrayOutput serializedByteArrayOutput = new SerializedByteArrayOutput();
      SerializedOutput output = serializedByteArrayOutput.getOutput();
      output.writeInteger(fieldValues.get(BANK));
      output.writeUtf8String(fieldValues.get(BANK_ID));
      output.writeInteger(fieldValues.get(BANK_ENTITY));
      output.writeUtf8String(fieldValues.get(BANK_ENTITY_LABEL));
      output.writeUtf8String(fieldValues.get(ACC_TYPE));
      output.writeUtf8String(fieldValues.get(URL));
      output.writeUtf8String(fieldValues.get(ORG));
      output.writeUtf8String(fieldValues.get(FID));
      output.writeUtf8String(fieldValues.get(NUMBER));
      output.writeUtf8String(fieldValues.get(POSITION));
      output.writeDate(fieldValues.get(POSITION_DATE));
      output.writeUtf8String(fieldValues.get(NAME));
      output.writeInteger(fieldValues.get(ACCOUNT_TYPE));
      output.writeBoolean(fieldValues.get(SAVINGS));
      output.writeInteger(fieldValues.get(ACCOUNT));
      output.writeInteger(fieldValues.get(CARD_TYPE));
      output.writeInteger(fieldValues.get(TRANSACTION_ID));

      return serializedByteArrayOutput.toByteArray();
    }

    public void deserializeData(int version, FieldSetter fieldSetter, byte[] data, Integer id) {
      if (version == 1) {
        deserializeDataV1(fieldSetter, data);
      }
    }

    private void deserializeDataV1(FieldSetter fieldSetter, byte[] data) {
      SerializedInput input = SerializedInputOutputFactory.init(data);
      fieldSetter.set(BANK, input.readInteger());
      fieldSetter.set(BANK_ID, input.readUtf8String());
      fieldSetter.set(BANK_ENTITY, input.readInteger());
      fieldSetter.set(BANK_ENTITY_LABEL, input.readUtf8String());
      fieldSetter.set(ACC_TYPE, input.readUtf8String());
      fieldSetter.set(URL, input.readUtf8String());
      fieldSetter.set(ORG, input.readUtf8String());
      fieldSetter.set(FID, input.readUtf8String());
      fieldSetter.set(NUMBER, input.readUtf8String());
      fieldSetter.set(POSITION, input.readUtf8String());
      fieldSetter.set(POSITION_DATE, input.readDate());
      fieldSetter.set(NAME, input.readUtf8String());
      fieldSetter.set(ACCOUNT_TYPE, input.readInteger());
      fieldSetter.set(SAVINGS, input.readBoolean());
      fieldSetter.set(ACCOUNT, input.readInteger());
      fieldSetter.set(CARD_TYPE, input.readInteger());
      fieldSetter.set(TRANSACTION_ID, input.readInteger());
    }
  }

}
