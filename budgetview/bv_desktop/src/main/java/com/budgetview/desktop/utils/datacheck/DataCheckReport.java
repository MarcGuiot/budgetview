package com.budgetview.desktop.utils.datacheck;

import com.budgetview.desktop.time.TimeService;
import org.globsframework.model.Glob;
import org.globsframework.utils.Dates;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Date;

public class DataCheckReport {

  private int errorCount;
  private int fixCount;
  private Date date = TimeService.getCurrentDate();
  private PrintWriter writer;

  public DataCheckReport(Writer writer) {
    this.writer = new PrintWriter(writer, true);
  }

  public DataCheckReport(OutputStream stream) {
    this.writer = new PrintWriter(stream, true);
  }

  public void addError(String message) {
    errorCount++;
    writer.append("[ERR] ").append(message).append('\n').flush();
  }

  public void addError(String message, String source) {
    errorCount++;
    writer.append("[ERR] ")
      .append(message)
      .append("\n          ")
      .append(source)
      .append('\n')
      .flush();
  }

  public void addFix(String message) {
    errorCount++;
    fixCount++;
    writer.append("[FIX] ").append(message).append('\n').flush();
  }

  public void addFix(String message, String source) {
    errorCount++;
    fixCount++;
    writer.append("[FIX] ")
      .append(message)
      .append("\n          ")
      .append(source)
      .append('\n')
      .flush();
  }

  public boolean hasErrors() {
    return errorCount > 0;
  }

  public boolean hasFixes() {
    return fixCount > 0;
  }

  public String toString() {
    if (!hasErrors()) {
      return "No errors";
    }
    return "On " + Dates.toString(date) + ": " + errorCount + " errors";
  }

  public void addError(Throwable ex) {
    ex.printStackTrace(writer);
  }

  public void reset() {
    errorCount = 0;
  }

  public int errorCount() {
    return errorCount;
  }

  public int fixCount() {
    return fixCount;
  }
}
