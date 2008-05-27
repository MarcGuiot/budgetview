package org.designup.picsou.server.persistence.prevayler;

import org.crossbowlabs.globs.utils.serialization.SerializedInput;
import org.crossbowlabs.globs.utils.serialization.SerializedOutput;

import java.io.IOException;

public interface CustomSerializationPolicy {
  public void registerFactory(CustomSerializableFactory factory);

  CustomSerializable read(SerializedInput input) throws IOException, ClassNotFoundException;

  void write(SerializedOutput output, CustomSerializable object) throws IOException;
}
