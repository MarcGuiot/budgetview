package org.globsframework.utils.logging;

import org.globsframework.utils.Dates;
import org.globsframework.utils.Files;
import org.globsframework.utils.exceptions.IOFailure;
import org.saxstack.utils.XmlUtils;

import java.io.*;
import java.util.Date;
import java.util.Stack;

class HtmlLogger {

  private PrintWriter writer;
  private Stack<Integer> blockIdStack = new Stack<Integer>();
  private int currentBlockId = 0;

  public HtmlLogger() {
    File file = new File("log.html");
    try {
      this.writer =
        new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8")));
    }
    catch (IOException e) {
      throw new IOFailure(e);
    }
    write("<html>\n" +
          "<head>\n" +
          "  <meta HTTP-EQUIV='Content-Type' content='text/html; charset=UTF-8'>" +
          "  <title>" +
          "    Log " + Dates.toTimestampString(new Date()) + " - " + file.getAbsolutePath() +
          "  </title>\n" +
          "  <script>\n" +
          Files.loadStreamToString(HtmlLogger.class.getResourceAsStream("/logging/htmlLogger.js"), "UTF-8") +
          Files.loadStreamToString(HtmlLogger.class.getResourceAsStream("/logging/tabber.js"), "UTF-8") +
          "  </script>\n" +
          "  <style type='text/css'>\n" +
          Files.loadStreamToString(HtmlLogger.class.getResourceAsStream("/logging/htmlLogger.css"), "UTF-8") +
          Files.loadStreamToString(HtmlLogger.class.getResourceAsStream("/logging/tabber.css"), "UTF-8") +
          "  </style>\n" +
          "</head>\n" +
          "<body>\n");
  }

  public void startBlock(String htmlTitle) {
    currentBlockId++;
    blockIdStack.push(currentBlockId);
    write("\n<div id='" + currentBlockId + "' class='block'>");

    write("<div id='title_" + currentBlockId + "' class='block_title'>" + htmlTitle);
    write("<script type='text/javascript'>setupToggle('block_content_" + currentBlockId
          + "','[hide]','[show]')</script>\n");
    write("<script type='text/javascript'>setupToggle('block_stack_" + currentBlockId
          + "','[hide stack]','[stack]')</script>\n");
    writeThreadInfo();
    write("</div>\n");

    writeStackDiv("block_stack_" + currentBlockId);

    write("<div id='block_content_" + currentBlockId + "' class='block_content'>\n");
    write("<script type='text/javascript'>setToggled('block_content_" + currentBlockId
          + "',true)</script>\n");

    writer.flush();
  }

  private void writeStackDiv(String id) {
    write("<div id='" + id + "' class='block_stack'>\n");
    writeStack();
    write("</div>\n");
    write("<script type='text/javascript'>setToggled('" + id + "',false)</script>\n");
  }

  private void writeStack() {
    write("<table class='stack'>");
    StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
    for (int i = 0; i < stackTrace.length; i++) {
      StackTraceElement element = stackTrace[i];
      if ((i < 2) || element.getClassName().startsWith("org.globsframework.globs.utils.logging")) {
        continue;
      }
      write("<tr>");
      write("<td class='stack_class'>" + element.getClassName() + "</td>");
      write("<td class='stack_method'>" +
            "<span class='stack_method'>" + element.getMethodName() + "</span>" +
            " <span class='stack_line'>(" + element.getLineNumber() + ")</span>" +
            "</td>");
      write("</tr>");
    }
    write("</table>");
  }

  public void endBlock() {
    write("</div>\n");
    write("</div>\n");
    blockIdStack.pop();
    writer.flush();
  }

  public void writeBlock(String html) {
    currentBlockId++;
    blockIdStack.push(currentBlockId);
    write("\n<div id='" + currentBlockId + "' class='block'>");
    write("<div id='title_" + currentBlockId + "' class='block_title'>" + html);
    write("<script type='text/javascript'>setupToggle('block_stack_" + currentBlockId
          + "','[hide stack]','[stack]')</script>\n");
    writeThreadInfo();
    write("</div>");
    writeStackDiv("block_stack_" + currentBlockId);
    write("</div>\n");
    writer.flush();
  }

  public void write(String html) {
    writer.print(html);
  }

  public void close() {
    write("</body></html>");
    writer.flush();
  }

  public void writeFile(String fileName) {
    startBlock(fileName);
    write("<pre>");
    write(XmlUtils.convertEntities(Files.loadFileToString(fileName)));
    write("</pre>");
    endBlock();
  }

  private void writeThreadInfo() {
    write("<span class='threadInfo'>" + Dates.toTimestampString(new Date()) +
          " | " + Thread.currentThread().getName() +
          "</span>");
  }
}
