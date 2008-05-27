package org.functests4j.kernel;


public interface FuncTestCmd {

  void call() throws Exception;

  String getDescription();
}
