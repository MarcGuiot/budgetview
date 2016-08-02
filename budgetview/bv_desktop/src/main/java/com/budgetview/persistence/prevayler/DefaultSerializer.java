package com.budgetview.persistence.prevayler;

import org.globsframework.utils.serialization.SerializedInputOutputFactory;
import org.prevayler.foundation.serialization.Serializer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class DefaultSerializer implements Serializer {
  private CustomSerializationPolicy policy;

  public DefaultSerializer(CustomSerializationPolicy policy) {
    this.policy = policy;
  }

  public void writeObject(OutputStream stream, Object object) throws IOException {
    policy.write(SerializedInputOutputFactory.init(stream), (CustomSerializable)object);
  }

  public Object readObject(InputStream stream) throws IOException, ClassNotFoundException {
    return policy.read(SerializedInputOutputFactory.init(stream));
  }
}
