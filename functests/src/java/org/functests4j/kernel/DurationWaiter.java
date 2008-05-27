package org.functests4j.kernel;


// use only we the pattern :
// while (duration.shouldContinue())
//   ... process here is not taken in account.
// wait(duration.getDuration());

public class DurationWaiter {
  private long now;
  private long endAt;

  public static DurationWaiter init(int timeOutInSecond) {
    return new DurationWaiter(timeOutInSecond * 1000);
  }

  private DurationWaiter(int timeOut) {
    endAt = System.currentTimeMillis() + timeOut;
  }

  public boolean shouldContinue() {
    now = System.currentTimeMillis();
    return (endAt - now) > 0;
  }

  public int getDuration() {
    return (int) (endAt - now);
  }
}
