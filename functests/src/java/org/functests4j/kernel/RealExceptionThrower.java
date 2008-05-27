package org.functests4j.kernel;

public class RealExceptionThrower implements ExceptionThrower {
  private Exception e;

  public RealExceptionThrower(Exception e) {
    this.e = e;
  }

  public void throwme() throws Exception {
    throw e;
  }
}
