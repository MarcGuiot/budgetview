package org.functests4j.kernel;


public interface FuncTestEvent {
  String getName();

  boolean isEquivalent(FuncTestEvent eventToFind);

  String getDescription();

  void setHomoEvent(FuncTestEvent eventToFind);

  FuncTestEvent getHomoEvent();

  String toString();
}
