package com.budgetview.server.persistence.prevayler;

public interface CustomSerializableFactory {
  String getSerializationName();

  CustomSerializable create();

}
