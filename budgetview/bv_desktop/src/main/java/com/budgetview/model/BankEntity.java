package com.budgetview.model;

import com.budgetview.shared.utils.PicsouGlobSerializer;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.annotations.NamingField;
import org.globsframework.metamodel.annotations.NoObfuscation;
import org.globsframework.metamodel.annotations.Target;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.fields.LinkField;
import org.globsframework.metamodel.fields.StringField;
import org.globsframework.metamodel.utils.GlobTypeLoader;
import org.globsframework.model.*;
import org.globsframework.utils.exceptions.ItemAmbiguity;
import org.globsframework.utils.exceptions.ItemNotFound;
import org.globsframework.utils.serialization.SerializedByteArrayOutput;
import org.globsframework.utils.serialization.SerializedInput;
import org.globsframework.utils.serialization.SerializedInputOutputFactory;
import org.globsframework.utils.serialization.SerializedOutput;

import static org.globsframework.model.utils.GlobMatchers.fieldEquals;

public class BankEntity {

  public static final Integer GENERIC_BANK_ENTITY_ID = -1;

  public static GlobType TYPE;

  @Key
  @NoObfuscation
  public static IntegerField ID;

  @NamingField
  @NoObfuscation
  public static StringField LABEL;

  @Target(Bank.class)
  @NoObfuscation
  public static LinkField BANK;

  public static StringField URL;

  public static Glob getBank(Glob bankEntity, GlobRepository repository) {
    Glob bank = repository.findLinkTarget(bankEntity, BANK);
    if (bank == null) {
      throw new ItemNotFound("BankEntity with no bank: " + bankEntity);
    }
    return bank;
  }

  static {
    GlobTypeLoader.init(BankEntity.class, "bankEntity");
  }

  public static Integer find(String bankEntityLabel, GlobRepository repository) throws ItemAmbiguity {
    GlobList entities = repository.getAll(BankEntity.TYPE, fieldEquals(BankEntity.LABEL, bankEntityLabel));
    if (entities.size() > 1) {
      throw new ItemAmbiguity("Several bank entities found with label " + bankEntityLabel);
    }
    if (entities.size() == 1) {
      return entities.getFirst().get(BankEntity.ID);
    }
    return null;
  }

  public static void setLabelIfNeeded(Glob bankEntity, GlobRepository repository) {
    if (bankEntity.get(LABEL) == null) {
      repository.update(bankEntity.getKey(), LABEL, Integer.toString(bankEntity.get(ID)));
    }
  }

  public static class Serializer implements PicsouGlobSerializer {

    public int getWriteVersion() {
      return 2;
    }

    public boolean shouldBeSaved(GlobRepository repository, FieldValues fieldValues) {
      Glob glob = repository.find(org.globsframework.model.Key.create(Bank.TYPE, fieldValues.get(BANK)));
      if (glob != null){
        return glob.isTrue(Bank.USER_CREATED);
      }
      return false;
    }

    public byte[] serializeData(FieldValues values) {
      SerializedByteArrayOutput serializedByteArrayOutput = new SerializedByteArrayOutput();
      SerializedOutput outputStream = serializedByteArrayOutput.getOutput();
      outputStream.writeInteger(values.get(BANK));
      outputStream.writeUtf8String(values.get(LABEL));
      outputStream.writeUtf8String(values.get(URL));
      return serializedByteArrayOutput.toByteArray();
    }

    public void deserializeData(int version, FieldSetter fieldSetter, byte[] data, Integer id) {
      if (version == 3) {
        deserializeDataV3(fieldSetter, data);
      }
      else if (version == 2) {
        deserializeDataV2(fieldSetter, data);
      }
      else if (version == 1) {
        deserializeDataV1(fieldSetter, data, id);
      }
    }

    private void deserializeDataV3(FieldSetter fieldSetter, byte[] data) {
      SerializedInput input = SerializedInputOutputFactory.init(data);
      fieldSetter.set(BANK, input.readInteger());
      fieldSetter.set(LABEL, input.readUtf8String());
      fieldSetter.set(URL, input.readUtf8String());
    }

    private void deserializeDataV2(FieldSetter fieldSetter, byte[] data) {
      SerializedInput input = SerializedInputOutputFactory.init(data);
      fieldSetter.set(BANK, input.readInteger());
      fieldSetter.set(LABEL, input.readUtf8String());
    }

    private void deserializeDataV1(FieldSetter fieldSetter, byte[] data, Integer id) {
      SerializedInput input = SerializedInputOutputFactory.init(data);
      fieldSetter.set(BANK, input.readInteger());
      fieldSetter.set(LABEL, Integer.toString(id));
    }
  }

}
