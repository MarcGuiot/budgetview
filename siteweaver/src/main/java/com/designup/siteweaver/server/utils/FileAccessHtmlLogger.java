package com.designup.siteweaver.server.utils;

import com.designup.siteweaver.server.upload.FileAccessListener;

import java.io.File;
import java.io.PrintWriter;

public class FileAccessHtmlLogger implements FileAccessListener {

  private PrintWriter writer;

  public FileAccessHtmlLogger(PrintWriter writer) {
    this.writer = writer;
    writer.write("<ul>\n");
  }

  public void processUploadText(String path, String content) {
    writer.write("<li>Upload text <code>"+  path + "</code></li>\n");
  }

  public void processUploadFile(String path, File file) {
    writer.write("<li>Upload file <code>"+  path + "</code></li>\n");
  }

  public void processDelete(String path) {
    writer.write("<li>Delete <code>"+  path + "</code></li>\n");
  }

  public void complete() {
    writer.write("</ul>\n");
  }
}
