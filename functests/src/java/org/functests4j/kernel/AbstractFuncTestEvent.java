package org.functests4j.kernel;



public abstract class AbstractFuncTestEvent implements FuncTestEvent {
  private FuncTestEvent homoEvent;

  public void setHomoEvent(FuncTestEvent eventToFind) {
    homoEvent = eventToFind;
  }

  public FuncTestEvent getHomoEvent() {
    return homoEvent;
  }

}
