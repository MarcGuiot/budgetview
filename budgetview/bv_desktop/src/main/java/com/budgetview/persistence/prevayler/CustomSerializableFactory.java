package com.budgetview.persistence.prevayler;

public interface CustomSerializableFactory {
  String getSerializationName();

  CustomSerializable create();

}
