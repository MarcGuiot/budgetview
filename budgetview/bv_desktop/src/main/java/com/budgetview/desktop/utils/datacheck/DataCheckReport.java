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
  private Date date = TimeService.getCurrentDate();
  private Glob currentSeries;
  private PrintWriter writer;

  public DataCheckReport(Writer writer) {
    this.writer = new PrintWriter(writer, true);
  }

  public DataCheckReport(OutputStream stream) {
    this.writer = new PrintWriter(stream, true);
  }

  public void setCurrentSeries(Glob series) {
    this.currentSeries = series;
  }

  public void clearCurrentSeries() {
    this.currentSeries = null;
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
    writer.append("[FIX] ").append(message).append('\n').flush();
  }

  public void addFix(String message, String source) {
    errorCount++;
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

  public String toString() {
    if (!hasErrors()) {
      return "No errors";
    }
    return "On " + Dates.toString(date) + ": " + errorCount + " errors";
  }

  public void addError(Throwable ex) {
    ex.printStackTrace(writer);
  }
}
