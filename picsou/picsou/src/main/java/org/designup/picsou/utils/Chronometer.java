package org.designup.picsou.utils;

import java.util.Date;

public class Chronometer {

  private Date startDate;

  public static Chronometer start() {
    return new Chronometer();
  }

  private Chronometer() {
    startDate = new Date();
  }

  public long getElapsedTime() {
    Date currentDate = new Date();
    return currentDate.getTime() - startDate.getTime();
  }

  public String toString() {
    return "Elapsed: " + (getElapsedTime() / 1000);
  }
}
