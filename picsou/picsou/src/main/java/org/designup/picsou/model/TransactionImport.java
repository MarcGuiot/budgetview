package org.designup.picsou.model;

import org.designup.picsou.server.serialization.PicsouGlobSerializer;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.fields.DateField;
import org.globsframework.metamodel.fields.DoubleField;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.fields.StringField;
import org.globsframework.metamodel.utils.GlobTypeLoader;
import org.globsframework.model.FieldSetter;
import org.globsframework.model.FieldValues;
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
  public static DateField LAST_TRANSACTION_DATE;
  public static DoubleField BALANCE;

  static {
    GlobTypeLoader.init(TransactionImport.class, "transactionImport");
  }

  public static class Serializer implements PicsouGlobSerializer {

    public byte[] serializeData(FieldValues values) {
      SerializedByteArrayOutput serializedByteArrayOutput = new SerializedByteArrayOutput();
      SerializedOutput outputStream = serializedByteArrayOutput.getOutput();
      outputStream.writeString(values.get(TransactionImport.SOURCE));
      outputStream.writeDate(values.get(TransactionImport.IMPORT_DATE));
      outputStream.writeDate(values.get(TransactionImport.LAST_TRANSACTION_DATE));
      outputStream.writeDouble(values.get(TransactionImport.BALANCE));
      return serializedByteArrayOutput.toByteArray();
    }

    public void deserializeData(int version, FieldSetter fieldSetter, byte[] data) {
      if (version == 1) {
        deserializeDataV1(fieldSetter, data);
      }
    }

    private void deserializeDataV1(FieldSetter fieldSetter, byte[] data) {
      SerializedInput input = SerializedInputOutputFactory.init(data);
      fieldSetter.set(TransactionImport.SOURCE, input.readString());
      fieldSetter.set(TransactionImport.IMPORT_DATE, input.readDate());
      fieldSetter.set(TransactionImport.LAST_TRANSACTION_DATE, input.readDate());
      fieldSetter.set(TransactionImport.BALANCE, input.readDouble());
    }

    public int getWriteVersion() {
      return 1;
    }
  }

}
