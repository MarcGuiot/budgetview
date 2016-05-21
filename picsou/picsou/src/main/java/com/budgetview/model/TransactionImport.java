package com.budgetview.model;

import com.budgetview.shared.utils.PicsouGlobSerializer;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.annotations.DefaultBoolean;
import org.globsframework.metamodel.fields.*;
import org.globsframework.metamodel.utils.GlobTypeLoader;
import org.globsframework.model.FieldSetter;
import org.globsframework.model.FieldValues;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.serialization.SerializedByteArrayOutput;
import org.globsframework.utils.serialization.SerializedInput;
import org.globsframework.utils.serialization.SerializedInputOutputFactory;
import org.globsframework.utils.serialization.SerializedOutput;

public class TransactionImport {
  public static GlobType TYPE;

  @Key
  public static IntegerField ID;

  public static StringField SOURCE;

  public static DateField IMPORT_DATE;

  @DefaultBoolean(false)
  public static BooleanField IS_WITH_SERIES;

  public static BlobField FILE_CONTENT;

  static {
    GlobTypeLoader.init(TransactionImport.class, "transactionImport");
  }

  public static class Serializer implements PicsouGlobSerializer {

    public byte[] serializeData(FieldValues values) {
      SerializedByteArrayOutput serializedByteArrayOutput = new SerializedByteArrayOutput();
      SerializedOutput stream = serializedByteArrayOutput.getOutput();
      stream.writeUtf8String(values.get(TransactionImport.SOURCE));
      stream.writeDate(values.get(TransactionImport.IMPORT_DATE));
      stream.writeBytes(values.get(TransactionImport.FILE_CONTENT));
      stream.writeBoolean(values.get(TransactionImport.IS_WITH_SERIES));
      return serializedByteArrayOutput.toByteArray();
    }

    public int getWriteVersion() {
      return 4;
    }

    public boolean shouldBeSaved(GlobRepository repository, FieldValues fieldValues) {
      return true;
    }

    public void deserializeData(int version, FieldSetter fieldSetter, byte[] data, Integer id) {
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
      fieldSetter.set(TransactionImport.SOURCE, input.readUtf8String());
      fieldSetter.set(TransactionImport.IMPORT_DATE, input.readDate());
      fieldSetter.set(TransactionImport.FILE_CONTENT, input.readBytes());
      fieldSetter.set(TransactionImport.IS_WITH_SERIES, input.readBoolean());
    }

    private void deserializeDataV3(FieldSetter fieldSetter, byte[] data) {
      SerializedInput input = SerializedInputOutputFactory.init(data);
      fieldSetter.set(TransactionImport.SOURCE, input.readUtf8String());
      fieldSetter.set(TransactionImport.IMPORT_DATE, input.readDate());
      fieldSetter.set(TransactionImport.FILE_CONTENT, input.readBytes());
    }

    private void deserializeDataV2(FieldSetter fieldSetter, byte[] data) {
      SerializedInput input = SerializedInputOutputFactory.init(data);
      fieldSetter.set(TransactionImport.SOURCE, input.readUtf8String());
      fieldSetter.set(TransactionImport.IMPORT_DATE, input.readDate());
    }

    private void deserializeDataV1(FieldSetter fieldSetter, byte[] data) {
      SerializedInput input = SerializedInputOutputFactory.init(data);
      fieldSetter.set(TransactionImport.SOURCE, input.readJavaString());
      fieldSetter.set(TransactionImport.IMPORT_DATE, input.readDate());
    }
  }
}
