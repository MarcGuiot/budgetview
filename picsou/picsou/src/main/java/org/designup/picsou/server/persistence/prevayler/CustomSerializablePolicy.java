package org.designup.picsou.server.persistence.prevayler;

import org.crossbowlabs.globs.utils.directory.Directory;
import org.crossbowlabs.globs.utils.serialization.SerializedInput;
import org.crossbowlabs.globs.utils.serialization.SerializedOutput;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class CustomSerializablePolicy implements CustomSerializationPolicy {
  private Map<String, CustomSerializableFactory> factories = new HashMap<String, CustomSerializableFactory>();
  private Directory directory;

  public CustomSerializablePolicy(Directory directory) {
    this.directory = directory;
  }

  public void registerFactory(CustomSerializableFactory factory) {
    factories.put(factory.getSerializationName(), factory);
  }

  public CustomSerializable read(SerializedInput input) throws IOException, ClassNotFoundException {
    String serializationName = input.readString();
    CustomSerializable object = create(serializationName);
    object.read(input, directory);
    return object;
  }

  protected CustomSerializable create(String serializationName) {
    CustomSerializableFactory factory = factories.get(serializationName);
    if (null == factory) {
      throw new RuntimeException("Trying to read a prevayler stream with unhandled object: " + serializationName);
    }
    return factory.create();
  }

  public void write(SerializedOutput output, CustomSerializable object) throws IOException {
    output.writeString(object.getSerializationName());
    object.write(output, directory);
  }

}
