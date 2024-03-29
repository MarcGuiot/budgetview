package com.budgetview.model;

import com.budgetview.desktop.utils.FrameSize;
import com.budgetview.model.util.TypeLoader;
import com.budgetview.shared.utils.GlobSerializer;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.DefaultDouble;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.fields.DoubleField;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.model.*;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.serialization.SerializedByteArrayOutput;
import org.globsframework.utils.serialization.SerializedInput;
import org.globsframework.utils.serialization.SerializedInputOutputFactory;
import org.globsframework.utils.serialization.SerializedOutput;

import javax.swing.*;

import static org.globsframework.model.FieldValue.value;
import static org.globsframework.model.utils.GlobMatchers.and;
import static org.globsframework.model.utils.GlobMatchers.fieldEquals;

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

  @DefaultDouble(0.5)
  public static DoubleField BUDGET_HORIZONTAL_1;

  @DefaultDouble(0.20)
  public static DoubleField BUDGET_VERTICAL_LEFT_1;

  @DefaultDouble(0.5)
  public static DoubleField BUDGET_VERTICAL_LEFT_2;

  @DefaultDouble(0.6)
  public static DoubleField BUDGET_VERTICAL_RIGHT_1;

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
    TypeLoader.init(LayoutConfig.class, "layoutConfig");
  }

  public static void init(GlobRepository repository, Directory directory) {
    JFrame frame = directory.get(JFrame.class);
    FrameSize frameSize = FrameSize.init(frame);
    LayoutConfig.find(frameSize, repository, true);
  }

  public static Glob find(FrameSize frameSize, GlobRepository repository, boolean createIfNeeded) {
    GlobList all = repository.getAll(LayoutConfig.TYPE,
                                     and(fieldEquals(SCREEN_WIDTH, frameSize.screenSize.width),
                                         fieldEquals(SCREEN_HEIGHT, frameSize.screenSize.height)));
    if (!all.isEmpty()) {
      return all.getFirst();
    }
    if (createIfNeeded) {
      if (repository.find(UserPreferences.KEY) != null) {
        return repository.create(TYPE,
                                 value(SCREEN_WIDTH, frameSize.screenSize.width),
                                 value(SCREEN_HEIGHT, frameSize.screenSize.height),
                                 value(FRAME_WIDTH, frameSize.targetFrameSize.width),
                                 value(FRAME_HEIGHT, frameSize.targetFrameSize.height));
      }
    }
    return null;
  }

  public static class Serializer implements GlobSerializer {

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
      outputStream.writeDouble(values.get(BUDGET_VERTICAL_LEFT_1));
      outputStream.writeDouble(values.get(BUDGET_VERTICAL_LEFT_2));
      outputStream.writeDouble(values.get(BUDGET_VERTICAL_RIGHT_1));
      outputStream.writeDouble(values.get(ACCOUNTS_HORIZONTAL));
      outputStream.writeDouble(values.get(ACCOUNTS_VERTICAL_LEFT));
      outputStream.writeDouble(values.get(ACCOUNTS_TRANSACTION_CHART));
      outputStream.writeDouble(values.get(CATEGORIZATION_HORIZONTAL));
      outputStream.writeDouble(values.get(ANALYSIS_TABLE));
      return serializedByteArrayOutput.toByteArray();
    }

    public void deserializeData(int version, byte[] data, Integer id, FieldSetter fieldSetter) {
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
      fieldSetter.set(BUDGET_VERTICAL_LEFT_1, input.readDouble());
      fieldSetter.set(BUDGET_VERTICAL_LEFT_2, input.readDouble());
      fieldSetter.set(BUDGET_VERTICAL_RIGHT_1, input.readDouble());
      fieldSetter.set(ACCOUNTS_HORIZONTAL, input.readDouble());
      fieldSetter.set(ACCOUNTS_VERTICAL_LEFT, input.readDouble());
      fieldSetter.set(ACCOUNTS_TRANSACTION_CHART, input.readDouble());
      fieldSetter.set(CATEGORIZATION_HORIZONTAL, input.readDouble());
      fieldSetter.set(ANALYSIS_TABLE, input.readDouble());
    }
  }
}
