package org.functests4j.kernel;

public interface ExceptionThrower {
  void throwme() throws Exception;

  ExceptionThrower NULL = new ExceptionThrower() {
    public void throwme() throws Exception {
    }
  };

}
