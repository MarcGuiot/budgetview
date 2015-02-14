package org.designup.picsou.model;

import com.budgetview.shared.utils.PicsouGlobSerializer;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.*;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.fields.*;
import org.globsframework.metamodel.utils.GlobTypeLoader;
import org.globsframework.model.*;
import org.globsframework.utils.serialization.SerializedByteArrayOutput;
import org.globsframework.utils.serialization.SerializedInput;
import org.globsframework.utils.serialization.SerializedInputOutputFactory;
import org.globsframework.utils.serialization.SerializedOutput;

public class ProjectTransfer {
  public static GlobType TYPE;

  @Key
  @Target(ProjectItem.class)
  public static LinkField PROJECT_ITEM;

  @Target(Account.class)
  @Required
  public static LinkField FROM_ACCOUNT;

  @Target(Account.class)
  @Required
  public static LinkField TO_ACCOUNT;

  static {
    GlobTypeLoader.init(ProjectTransfer.class, "projectTransfer");
  }

  public static Glob getTransferFromItem(FieldValues itemValues, GlobRepository repository) {
    return repository.get(org.globsframework.model.Key.create(ProjectTransfer.TYPE,
                                                              itemValues.get(ProjectItem.ID)));
  }

  public static Glob getItemFromTransfer(FieldValues transferValues, GlobRepository repository) {
    return repository.get(org.globsframework.model.Key.create(ProjectItem.TYPE,
                                                              transferValues.get(PROJECT_ITEM)));
  }

  public static boolean usesMainAccounts(FieldValues transferValues, GlobRepository repository) {
    AccountType fromType = getAccountType(transferValues, repository, FROM_ACCOUNT);
    AccountType toType = getAccountType(transferValues, repository, TO_ACCOUNT);
    return (fromType == AccountType.MAIN) || (toType == AccountType.MAIN);
  }

  public static boolean isFromAccountAMainAccount(Glob transfer, GlobRepository repository) {
    return getAccountType(transfer, repository, FROM_ACCOUNT) == AccountType.MAIN;
  }

  public static boolean usesSavingsAccounts(FieldValues transferValues, GlobRepository repository) {
    AccountType fromType = getAccountType(transferValues, repository, FROM_ACCOUNT);
    AccountType toType = getAccountType(transferValues, repository, TO_ACCOUNT);
    return (fromType == AccountType.SAVINGS) || (toType == AccountType.SAVINGS);
  }

  private static AccountType getAccountType(FieldValues transferValues, GlobRepository repository, LinkField field) {
    Integer accountId = transferValues.get(field);
    if (accountId == null) {
      return null;
    }
    return AccountType.get(repository.find(org.globsframework.model.Key.create(Account.TYPE, accountId)));
  }

  public static boolean isComplete(Glob transfer) {
    return transfer.get(FROM_ACCOUNT) != null && transfer.get(TO_ACCOUNT) != null;
  }

  public static class Serializer implements PicsouGlobSerializer {

    public int getWriteVersion() {
      return 1;
    }

    public boolean shouldBeSaved(GlobRepository repository, FieldValues fieldValues) {
      return true;
    }

    public byte[] serializeData(FieldValues fieldValues) {
      SerializedByteArrayOutput serializedByteArrayOutput = new SerializedByteArrayOutput();
      SerializedOutput output = serializedByteArrayOutput.getOutput();
      output.writeInteger(fieldValues.get(ProjectTransfer.PROJECT_ITEM));
      output.writeInteger(fieldValues.get(ProjectTransfer.FROM_ACCOUNT));
      output.writeInteger(fieldValues.get(ProjectTransfer.TO_ACCOUNT));
      return serializedByteArrayOutput.toByteArray();
    }

    public void deserializeData(int version, FieldSetter fieldSetter, byte[] data, Integer id) {
      if (version == 1) {
        deserializeDataV1(fieldSetter, data);
      }
    }

    private void deserializeDataV1(FieldSetter fieldSetter, byte[] data) {
      SerializedInput input = SerializedInputOutputFactory.init(data);
      fieldSetter.set(ProjectTransfer.PROJECT_ITEM, input.readInteger());
      fieldSetter.set(ProjectTransfer.FROM_ACCOUNT, input.readInteger());
      fieldSetter.set(ProjectTransfer.TO_ACCOUNT, input.readInteger());
    }
  }
}
