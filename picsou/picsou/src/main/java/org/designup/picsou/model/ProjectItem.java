package org.designup.picsou.model;

import com.budgetview.shared.utils.PicsouGlobSerializer;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.*;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.fields.DoubleField;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.fields.LinkField;
import org.globsframework.metamodel.fields.StringField;
import org.globsframework.metamodel.utils.GlobTypeLoader;
import org.globsframework.model.*;
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

  @Target(SubSeries.class)
  public static LinkField SUB_SERIES;

  static {
    GlobTypeLoader.init(ProjectItem.class, "projectItem");
  }

  public static class Serializer implements PicsouGlobSerializer {

    public int getWriteVersion() {
      return 2;
    }

    public boolean shouldBeSaved(GlobRepository repository, FieldValues fieldValues) {
      return true;
    }

    public byte[] serializeData(FieldValues fieldValues) {
      SerializedByteArrayOutput serializedByteArrayOutput = new SerializedByteArrayOutput();
      SerializedOutput output = serializedByteArrayOutput.getOutput();
      output.writeInteger(fieldValues.get(ProjectItem.PROJECT));
      output.writeUtf8String(fieldValues.get(ProjectItem.LABEL));
      output.writeInteger(fieldValues.get(ProjectItem.MONTH));
      output.writeDouble(fieldValues.get(ProjectItem.AMOUNT));
      output.writeInteger(fieldValues.get(ProjectItem.SUB_SERIES));
      return serializedByteArrayOutput.toByteArray();
    }

    public void deserializeData(int version, FieldSetter fieldSetter, byte[] data, Integer id) {
      if (version == 2) {
        deserializeDataV2(fieldSetter, data);
      }
      else if (version == 1) {
        deserializeDataV1(fieldSetter, data);
      }
    }

    private void deserializeDataV2(FieldSetter fieldSetter, byte[] data) {
      SerializedInput input = SerializedInputOutputFactory.init(data);
      fieldSetter.set(ProjectItem.PROJECT, input.readInteger());
      fieldSetter.set(ProjectItem.LABEL, input.readUtf8String());
      fieldSetter.set(ProjectItem.MONTH, input.readInteger());
      fieldSetter.set(ProjectItem.AMOUNT, input.readDouble());
      fieldSetter.set(ProjectItem.SUB_SERIES, input.readInteger());
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
