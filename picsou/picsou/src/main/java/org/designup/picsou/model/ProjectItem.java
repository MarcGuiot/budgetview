package org.designup.picsou.model;

import com.budgetview.shared.utils.PicsouGlobSerializer;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.*;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.fields.*;
import org.globsframework.metamodel.index.UniqueIndex;
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
  @DoublePrecision(4)
  public static DoubleField PLANNED_AMOUNT;

  @DefaultBoolean(true)
  public static BooleanField ACTIVE;

  @Target(SubSeries.class)
  public static LinkField SUB_SERIES;

  @Target(Picture.class)
  public static LinkField PICTURE;

  public static StringField URL;

  public static StringField DESCRIPTION;
  
  public static UniqueIndex SUB_SERIES_INDEX;

  static {
    GlobTypeLoader loader = GlobTypeLoader.init(ProjectItem.class, "projectItem");
    loader.defineUniqueIndex(SUB_SERIES_INDEX, SUB_SERIES);
  }

  public static class Serializer implements PicsouGlobSerializer {

    public int getWriteVersion() {
      return 3;
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
      output.writeDouble(fieldValues.get(ProjectItem.PLANNED_AMOUNT));
      output.writeBoolean(fieldValues.get(ProjectItem.ACTIVE));
      output.writeInteger(fieldValues.get(ProjectItem.SUB_SERIES));
      output.writeInteger(fieldValues.get(ProjectItem.PICTURE));
      output.writeUtf8String(fieldValues.get(ProjectItem.URL));
      output.writeUtf8String(fieldValues.get(ProjectItem.DESCRIPTION));
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
        deserializeDataV1(fieldSetter, data);
      }
    }

    private void deserializeDataV3(FieldSetter fieldSetter, byte[] data) {
      SerializedInput input = SerializedInputOutputFactory.init(data);
      fieldSetter.set(ProjectItem.PROJECT, input.readInteger());
      fieldSetter.set(ProjectItem.LABEL, input.readUtf8String());
      fieldSetter.set(ProjectItem.MONTH, input.readInteger());
      fieldSetter.set(ProjectItem.PLANNED_AMOUNT, input.readDouble());
      fieldSetter.set(ProjectItem.ACTIVE, input.readBoolean());
      fieldSetter.set(ProjectItem.SUB_SERIES, input.readInteger());
      fieldSetter.set(ProjectItem.PICTURE, input.readInteger());
      fieldSetter.set(ProjectItem.URL, input.readUtf8String());
      fieldSetter.set(ProjectItem.DESCRIPTION, input.readUtf8String());
    }

    private void deserializeDataV2(FieldSetter fieldSetter, byte[] data) {
      SerializedInput input = SerializedInputOutputFactory.init(data);
      fieldSetter.set(ProjectItem.PROJECT, input.readInteger());
      fieldSetter.set(ProjectItem.LABEL, input.readUtf8String());
      fieldSetter.set(ProjectItem.MONTH, input.readInteger());
      fieldSetter.set(ProjectItem.PLANNED_AMOUNT, input.readDouble());
      fieldSetter.set(ProjectItem.ACTIVE, true);
      fieldSetter.set(ProjectItem.SUB_SERIES, input.readInteger());
      fieldSetter.set(ProjectItem.PICTURE, null);
      fieldSetter.set(ProjectItem.URL, null);
      fieldSetter.set(ProjectItem.DESCRIPTION, null);
    }

    private void deserializeDataV1(FieldSetter fieldSetter, byte[] data) {
      SerializedInput input = SerializedInputOutputFactory.init(data);
      fieldSetter.set(ProjectItem.PROJECT, input.readInteger());
      fieldSetter.set(ProjectItem.LABEL, input.readUtf8String());
      fieldSetter.set(ProjectItem.MONTH, input.readInteger());
      fieldSetter.set(ProjectItem.PLANNED_AMOUNT, input.readDouble());
      fieldSetter.set(ProjectItem.ACTIVE, true);
      fieldSetter.set(ProjectItem.SUB_SERIES, null);
      fieldSetter.set(ProjectItem.PICTURE, null);
      fieldSetter.set(ProjectItem.URL, null);
      fieldSetter.set(ProjectItem.DESCRIPTION, null);
    }
  }
}
