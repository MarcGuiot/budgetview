package com.designup.siteweaver.html.output;

import com.designup.siteweaver.html.HtmlWriter;
import com.designup.siteweaver.model.Page;
import com.designup.siteweaver.model.Site;
import com.designup.siteweaver.utils.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class FileOutput implements HtmlOutput {
  private String outputDir;
  private boolean publish;

  public FileOutput(String outputDir) {
    this.outputDir = outputDir;
  }

  public FileOutput(String outputDir, boolean publish) {
    this.outputDir = outputDir;
    this.publish = publish;
  }

  public HtmlWriter createWriter(Page page) throws IOException {
    File path = new File(outputDir, page.getOutputFilePath());
    File outputDir = new File(path.getParent());
    outputDir.mkdirs();
    return new HtmlWriter(FileUtils.createEncodedWriter(path.getAbsolutePath()));
  }

  public void copyFile(File inputFile, String filePath) throws IOException {
    File outputFile = new File(outputDir, filePath);
    if (inputFile.isDirectory()) {
      FileUtils.copyDirectory(inputFile, outputFile, false);
    }
    else {
      outputFile.getParentFile().mkdirs();
      if (inputFile.lastModified() >= outputFile.lastModified()) {
        FileUtils.copyStreamToFile(new FileInputStream(inputFile), outputFile);
      }
    }
  }

  public String getBaseUrl(Site site) {
    if (publish) {
      return site.getUrl();
    }
    return "file://" + new File(outputDir).getAbsolutePath() + "/";
  }

}
