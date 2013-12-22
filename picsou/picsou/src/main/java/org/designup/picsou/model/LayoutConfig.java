package org.designup.picsou.model;

import com.budgetview.shared.utils.PicsouGlobSerializer;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.utils.GlobTypeLoader;
import org.globsframework.model.*;
import org.globsframework.utils.exceptions.ItemAmbiguity;
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

  static {
    GlobTypeLoader.init(LayoutConfig.class, "layoutConfig");
  }

  public static void init(Dimension screenSize, Dimension targetFrameSize, GlobRepository repository) {
    GlobList all = repository.getAll(LayoutConfig.TYPE,
                                     and(fieldEquals(SCREEN_WIDTH, screenSize.width),
                                         fieldEquals(SCREEN_HEIGHT, screenSize.height)));
    if (all.isEmpty()) {
      repository.create(TYPE,
                        value(SCREEN_WIDTH, screenSize.width),
                        value(SCREEN_HEIGHT, screenSize.height),
                        value(FRAME_WIDTH, targetFrameSize.width),
                        value(FRAME_HEIGHT, targetFrameSize.height));
    }
  }

  public static Glob find(Dimension screenSize, GlobRepository repository) {
    GlobList all = repository.getAll(LayoutConfig.TYPE,
                                     and(fieldEquals(SCREEN_WIDTH, screenSize.width),
                                         fieldEquals(SCREEN_HEIGHT, screenSize.height)));
    if (all.isEmpty()) {
      return null;
    }
    else if (all.size() > 1) {
      throw new ItemAmbiguity("Found several LayoutConfig for: " + screenSize);
    }
    else {
      return all.getFirst();
    }
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
    }
  }
}
