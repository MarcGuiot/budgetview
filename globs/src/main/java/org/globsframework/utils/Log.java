package org.globsframework.utils;

import org.globsframework.model.ChangeSet;
import org.globsframework.model.format.ChangeSetPrinter;

import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;

public class Log {

  private static boolean loggingEnabled = true;
  private static boolean debugEnabled = false;

  private static PrintStream stream = System.out;
  private static int indentation = 0;
  private static String BANNER =
    "=================================================================";

  public static void init(PrintStream stream) {
    Log.stream = stream;
    System.setOut(stream);
    System.setErr(stream);
  }

  public static void enableDebug() {
    debugEnabled = true;
  }

  public static void reset() {
    indentation = 0;
  }

  public static void space(int lineCount) {
    if (loggingEnabled) {
      for (int i = 0; i < lineCount; i++) {
        stream.append(Strings.LINE_SEPARATOR);
      }
    }
  }

  public static void enter(String text) {
    doWrite(">> ", text, true);
    indentation++;
  }

  public static void leave(String text) {
    indentation--;
    doWrite("<< ", text, true);
  }

  public static void write(String text) {
    doWrite(".. ", text, true);
  }

  public static void write(ChangeSet changeSet) {
    if (loggingEnabled) {
      ChangeSetPrinter.printStats(changeSet, new PrintWriter(stream), indentString());
    }
  }

  public static void writeStack(String text) {
    if (loggingEnabled) {
      new Exception("[Stack dump] " + text).printStackTrace(new IndentedPrintWriter(stream, indentString()));
    }
  }

  public static void write(String text, Throwable e) {
    if (loggingEnabled) {
      doWrite(".. ", text, true);
      e.printStackTrace(stream);
    }
  }

  private static void doWrite(String prefix, String text, boolean indent) {
    if (loggingEnabled && text != null) {
      for (String line : text.split("\n")) {
        if (indent) {
          indent();
        }
        if (indent && indentation > 0) {
          stream.append(prefix);
        }
        stream.append(line);
        stream.append(Strings.LINE_SEPARATOR);
      }
    }
  }

  private static void indent() {
    for (int i = 0; i < indentation; i++) {
      stream.append(".  ");
    }
  }

  private static String indentString() {
    StringBuilder builder = new StringBuilder();
    for (int i = 0; i < indentation; i++) {
      builder.append(".  ");
    }
    return builder.toString();
  }

  public static void banner(String text) {
    if (loggingEnabled) {
      space(3);
      doWrite("", BANNER, false);
      doWrite("==     ", text, false);
      doWrite("", BANNER, false);
      stream.flush();
    }
  }

  public static void enable() {
    setEnabled(true);
  }

  public static void disable() {
    setEnabled(false);
  }

  public static void setEnabled(boolean enabled) {
    loggingEnabled = enabled;
  }

  public static void debug(String text) {
    if (loggingEnabled && debugEnabled) {
      write(text);
    }
  }

  public static boolean debugEnabled() {
    return debugEnabled;
  }

  public static void setDebugEnabled(boolean debugEnabled) {
    Log.debugEnabled = debugEnabled;
  }

  public static class IndentedPrintWriter extends PrintWriter {

    private String indent;
    private StringBuilder line = new StringBuilder();

    public IndentedPrintWriter(OutputStream out, String indent) {
      super(out, true);
      this.indent = indent;
    }

    public void println() {
      line.insert(0, indent);
      super.write(line.toString(), 0, line.length());
      line = new StringBuilder();
      super.println();
    }

    public void write(int c) {
      line.append((char) c);
    }

    public void write(String s, int off, int len) {
      line.append(s, off, len);
    }

    public void write(char[] buf, int off, int len) {
      line.append(buf, off, len);
    }
  }
}
