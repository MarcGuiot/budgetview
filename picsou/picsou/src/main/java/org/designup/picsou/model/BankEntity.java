package org.designup.picsou.model;

import org.designup.picsou.server.serialization.PicsouGlobSerializer;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.annotations.NoObfuscation;
import org.globsframework.metamodel.annotations.Target;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.fields.LinkField;
import org.globsframework.metamodel.index.NotUniqueIndex;
import org.globsframework.metamodel.utils.GlobTypeLoader;
import org.globsframework.model.FieldSetter;
import org.globsframework.model.FieldValues;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.format.GlobPrinter;
import org.globsframework.utils.exceptions.ItemNotFound;
import org.globsframework.utils.serialization.SerializedByteArrayOutput;
import org.globsframework.utils.serialization.SerializedInput;
import org.globsframework.utils.serialization.SerializedInputOutputFactory;
import org.globsframework.utils.serialization.SerializedOutput;

public class BankEntity {
  public static GlobType TYPE;

  @NoObfuscation
  @Key
  public static IntegerField ID;

  @NoObfuscation
  @Target(Bank.class)
  public static LinkField BANK;

  public static NotUniqueIndex BANK_INDEX;

  public static Glob getBank(Glob bankEntity, GlobRepository repository) {
    Glob bank = repository.findLinkTarget(bankEntity, BANK);
    if (bank == null) {
      throw new ItemNotFound("BankEntity with no bank: " + GlobPrinter.dump(bankEntity));
    }
    return bank;
  }

  static {
    GlobTypeLoader.init(BankEntity.class, "bankEntity")
      .defineNotUniqueIndex(BANK_INDEX, BANK);
  }

  public static class Serialization implements PicsouGlobSerializer {

    public byte[] serializeData(FieldValues values) {
      SerializedByteArrayOutput serializedByteArrayOutput = new SerializedByteArrayOutput();
      SerializedOutput outputStream = serializedByteArrayOutput.getOutput();
      outputStream.writeInteger(values.get(BANK));
      return serializedByteArrayOutput.toByteArray();
    }

    public void deserializeData(int version, FieldSetter fieldSetter, byte[] data) {
      if (version == 1) {
        deserializeDataV1(fieldSetter, data);
      }
    }

    private void deserializeDataV1(FieldSetter fieldSetter, byte[] data) {
      SerializedInput input = SerializedInputOutputFactory.init(data);
      fieldSetter.set(BANK, input.readInteger());
    }

    public int getWriteVersion() {
      return 1;
    }
  }

}
