package com.budgetview.importer.ofx;

import org.globsframework.utils.exceptions.InvalidParameter;

import java.io.*;

public class OfxBeautifier implements OfxFunctor {

  private Writer writer;
  private int indentLevel = 0;

  public OfxBeautifier(Writer writer) {
    this.writer = writer;
  }

  public void processHeader(String key, String value) {
    append(key);
    append(":");
    append(value);
    append("\n");
  }

  public void enterTag(String tag) {
    writeIndent();
    append("<");
    append(tag);
    append(">\n");
    indentLevel++;
  }

  public void leaveTag(String tag) {
    indentLevel--;
    writeIndent();
    append("</");
    append(tag);
    append(">\n");
  }

  public void processTag(String tag, String content) {
    writeIndent();
    append("<");
    append(tag);
    append(">");
    append(content);
    append("\n");
  }

  public void end() {
    try {
      writer.close();
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void append(String text) {
    try {
      writer.append(text);
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void writeIndent() {
    for (int i = 0; i < indentLevel; i++) {
      append("  ");
    }
  }

  public static void main(String[] args) throws Exception {
    if (args.length != 1) {
      throw new InvalidParameter("You must suppy the path of an OFX file");
    }
    OfxParser parser = new OfxParser();
    FileReader reader = new FileReader(new File(args[0]));
    parser.parse(reader, new OfxBeautifier(new BufferedWriter(new OutputStreamWriter(System.out))));
  }
}
