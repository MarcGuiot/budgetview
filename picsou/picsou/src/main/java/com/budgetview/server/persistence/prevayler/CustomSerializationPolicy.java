package com.budgetview.server.persistence.prevayler;

import org.globsframework.utils.serialization.SerializedInput;
import org.globsframework.utils.serialization.SerializedOutput;

import java.io.IOException;

public interface CustomSerializationPolicy {
  public void registerFactory(CustomSerializableFactory factory);

  CustomSerializable read(SerializedInput input) throws IOException, ClassNotFoundException;

  void write(SerializedOutput output, CustomSerializable object) throws IOException;
}
