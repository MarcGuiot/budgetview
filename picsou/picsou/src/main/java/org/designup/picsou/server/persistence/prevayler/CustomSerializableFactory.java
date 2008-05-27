package org.designup.picsou.server.persistence.prevayler;

public interface CustomSerializableFactory {
  String getSerializationName();

  CustomSerializable create();

}
