package org.designup.picsou.model;

import com.budgetview.shared.utils.PicsouGlobSerializer;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.DefaultDouble;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.fields.DoubleField;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.utils.GlobTypeLoader;
import org.globsframework.model.*;
import org.globsframework.utils.serialization.SerializedByteArrayOutput;
import org.globsframework.utils.serialization.SerializedInput;
import org.globsframework.utils.serialization.SerializedInputOutputFactory;
import org.globsframework.utils.serialization.SerializedOutput;

import java.awt.*;

import static org.globsframework.model.FieldValue.value;
import static org.globsframework.model.utils.GlobMatchers.*;

public class LayoutConfig {

  public static GlobType TYPE;

  @Key
  public static IntegerField ID;

  public static IntegerField SCREEN_WIDTH;
  public static IntegerField SCREEN_HEIGHT;

  public static IntegerField FRAME_WIDTH;
  public static IntegerField FRAME_HEIGHT;

  @DefaultDouble(0.5)
  public static DoubleField HOME_SUMMARY_PROJECTS;

  @DefaultDouble(0.34)
  public static DoubleField BUDGET_HORIZONTAL_1;

  @DefaultDouble(0.33)
  public static DoubleField BUDGET_HORIZONTAL_2;

  @DefaultDouble(0.5)
  public static DoubleField BUDGET_VERTICAL_LEFT;

  @DefaultDouble(0.7)
  public static DoubleField BUDGET_VERTICAL_CENTER;

  @DefaultDouble(0.5)
  public static DoubleField ACCOUNTS_VERTICAL_LEFT;

  @DefaultDouble(0.3)
  public static DoubleField ACCOUNTS_HORIZONTAL;

  @DefaultDouble(0.75)
  public static DoubleField ACCOUNTS_TRANSACTION_CHART;

  @DefaultDouble(0.5)
  public static DoubleField CATEGORIZATION_HORIZONTAL;

  @DefaultDouble(0.65)
  public static DoubleField ANALYSIS_TABLE;

  static {
    GlobTypeLoader.init(LayoutConfig.class, "layoutConfig");
  }

  public static Glob find(Dimension screenSize, Dimension targetFrameSize, GlobRepository repository, boolean createIfNeeded) {
    GlobList all = repository.getAll(LayoutConfig.TYPE,
                                     and(fieldEquals(SCREEN_WIDTH, screenSize.width),
                                         fieldEquals(SCREEN_HEIGHT, screenSize.height)));
    if (!all.isEmpty()) {
      return all.getFirst();
    }
    if (createIfNeeded) {
      if (repository.find(UserPreferences.KEY) != null) {
        return repository.create(TYPE,
                                 value(SCREEN_WIDTH, screenSize.width),
                                 value(SCREEN_HEIGHT, screenSize.height),
                                 value(FRAME_WIDTH, targetFrameSize.width),
                                 value(FRAME_HEIGHT, targetFrameSize.height));
      }
    }
    return null;
  }

  public static class Serializer implements PicsouGlobSerializer {

    public int getWriteVersion() {
      return 1;
    }

    public boolean shouldBeSaved(GlobRepository repository, FieldValues fieldValues) {
      return true;
    }

    public byte[] serializeData(FieldValues values) {
      SerializedByteArrayOutput serializedByteArrayOutput = new SerializedByteArrayOutput();
      SerializedOutput outputStream = serializedByteArrayOutput.getOutput();
      outputStream.writeInteger(values.get(SCREEN_WIDTH));
      outputStream.writeInteger(values.get(SCREEN_HEIGHT));
      outputStream.writeInteger(values.get(FRAME_WIDTH));
      outputStream.writeInteger(values.get(FRAME_HEIGHT));
      outputStream.writeDouble(values.get(HOME_SUMMARY_PROJECTS));
      outputStream.writeDouble(values.get(BUDGET_HORIZONTAL_1));
      outputStream.writeDouble(values.get(BUDGET_HORIZONTAL_2));
      outputStream.writeDouble(values.get(BUDGET_VERTICAL_LEFT));
      outputStream.writeDouble(values.get(BUDGET_VERTICAL_CENTER));
      outputStream.writeDouble(values.get(ACCOUNTS_HORIZONTAL));
      outputStream.writeDouble(values.get(ACCOUNTS_VERTICAL_LEFT));
      outputStream.writeDouble(values.get(ACCOUNTS_TRANSACTION_CHART));
      outputStream.writeDouble(values.get(CATEGORIZATION_HORIZONTAL));
      outputStream.writeDouble(values.get(ANALYSIS_TABLE));
      return serializedByteArrayOutput.toByteArray();
    }

    public void deserializeData(int version, FieldSetter fieldSetter, byte[] data, Integer id) {
      if (version == 1) {
        deserializeDataV1(fieldSetter, data);
      }
    }

    private void deserializeDataV1(FieldSetter fieldSetter, byte[] data) {
      SerializedInput input = SerializedInputOutputFactory.init(data);
      fieldSetter.set(SCREEN_WIDTH, input.readInteger());
      fieldSetter.set(SCREEN_HEIGHT, input.readInteger());
      fieldSetter.set(FRAME_WIDTH, input.readInteger());
      fieldSetter.set(FRAME_HEIGHT, input.readInteger());
      fieldSetter.set(HOME_SUMMARY_PROJECTS, input.readDouble());
      fieldSetter.set(BUDGET_HORIZONTAL_1, input.readDouble());
      fieldSetter.set(BUDGET_HORIZONTAL_2, input.readDouble());
      fieldSetter.set(BUDGET_VERTICAL_LEFT, input.readDouble());
      fieldSetter.set(BUDGET_VERTICAL_CENTER, input.readDouble());
      fieldSetter.set(ACCOUNTS_HORIZONTAL, input.readDouble());
      fieldSetter.set(ACCOUNTS_VERTICAL_LEFT, input.readDouble());
      fieldSetter.set(ACCOUNTS_TRANSACTION_CHART, input.readDouble());
      fieldSetter.set(CATEGORIZATION_HORIZONTAL, input.readDouble());
      fieldSetter.set(ANALYSIS_TABLE, input.readDouble());
    }
  }
}
