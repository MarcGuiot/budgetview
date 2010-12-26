package org.designup.picsou.model;

import org.designup.picsou.server.serialization.PicsouGlobSerializer;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.*;
import org.globsframework.metamodel.fields.DoubleField;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.fields.LinkField;
import org.globsframework.metamodel.fields.StringField;
import org.globsframework.metamodel.utils.GlobTypeLoader;
import org.globsframework.model.FieldSetter;
import org.globsframework.model.FieldValues;
import org.globsframework.utils.serialization.SerializedByteArrayOutput;
import org.globsframework.utils.serialization.SerializedInput;
import org.globsframework.utils.serialization.SerializedInputOutputFactory;
import org.globsframework.utils.serialization.SerializedOutput;

public class ProjectItem {
  public static GlobType TYPE;

  @Key
  public static IntegerField ID;

  @Target(Project.class)
  @Required
  public static LinkField PROJECT;

  @NamingField
  @Required
  @DefaultString("")
  public static StringField LABEL;

  @Target(Month.class)
  @Required
  public static IntegerField MONTH;

  @DefaultDouble(0.00)
  public static DoubleField AMOUNT;

  static {
    GlobTypeLoader.init(ProjectItem.class, "projectItem");
  }

  public static class Serializer implements PicsouGlobSerializer {

    public int getWriteVersion() {
      return 1;
    }

    public byte[] serializeData(FieldValues fieldValues) {
      SerializedByteArrayOutput serializedByteArrayOutput = new SerializedByteArrayOutput();
      SerializedOutput output = serializedByteArrayOutput.getOutput();
      output.writeInteger(fieldValues.get(ProjectItem.PROJECT));
      output.writeUtf8String(fieldValues.get(ProjectItem.LABEL));
      output.writeInteger(fieldValues.get(ProjectItem.MONTH));
      output.writeDouble(fieldValues.get(ProjectItem.AMOUNT));
      return serializedByteArrayOutput.toByteArray();
    }

    public void deserializeData(int version, FieldSetter fieldSetter, byte[] data, Integer id) {
      if (version == 1) {
        deserializeDataV1(fieldSetter, data);
      }
    }

    private void deserializeDataV1(FieldSetter fieldSetter, byte[] data) {
      SerializedInput input = SerializedInputOutputFactory.init(data);
      fieldSetter.set(ProjectItem.PROJECT, input.readInteger());
      fieldSetter.set(ProjectItem.LABEL, input.readUtf8String());
      fieldSetter.set(ProjectItem.MONTH, input.readInteger());
      fieldSetter.set(ProjectItem.AMOUNT, input.readDouble());
    }
  }
}
