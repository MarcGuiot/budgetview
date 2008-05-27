package org.functests4j.corba;

import functests4j.ClientOperations;
import org.functests4j.DefaultFuncTestEvent;
import org.functests4j.kernel.FuncTestCase;
import org.omg.CORBA.StringHolder;

public class Client_i implements ClientOperations {
  private FuncTestCase funcTestCase;

  public Client_i(FuncTestCase funcTestCase) {
    this.funcTestCase = funcTestCase;
  }

  public boolean callToClient(String arg1, StringHolder arg2) {
    DefaultFuncTestEvent event = new DefaultFuncTestEvent("Client.callToClient");
    event.setAttributes("arg1", arg1);
    try {
      funcTestCase.actual(event);
      arg2.value = (String) ((DefaultFuncTestEvent) event.getHomoEvent()).getResult("arg2");
    } catch (Exception e) {
    }
    return ((Boolean) ((DefaultFuncTestEvent) event.getHomoEvent()).getResult("return")).booleanValue();
  }
}
