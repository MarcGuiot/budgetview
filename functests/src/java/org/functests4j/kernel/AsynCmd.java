package org.functests4j.kernel;



public class AsynCmd {
  private FuncTestCmd funcTestCmd;
  private boolean done = false;
  private ExceptionThrower exceptionThrower = ExceptionThrower.NULL;

  public AsynCmd(FuncTestCmd funcTestCmd) {
    this.funcTestCmd = funcTestCmd;
  }

  public FuncTestCmd getCmd() {
    return funcTestCmd;
  }

  protected AsynCmd start() {
    Thread thread = new Thread() {
      public void run() {
        try {
          funcTestCmd.call();
        } catch (Exception e) {
          exceptionThrower = new RealExceptionThrower(e);
        } finally {
          synchronized (AsynCmd.this) {
            done = true;
            AsynCmd.this.notify();
          }
        }
      }
    };
    thread.setDaemon(true);
    thread.start();
    return this;
  }

  public boolean isDone() throws Exception {
    exceptionThrower.throwme();
    return done;
  }

  public String getDescription() {
    return funcTestCmd.getDescription();
  }
}
