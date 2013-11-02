package com.designup.siteweaver.model;

import java.io.File;
import java.util.List;

public class Site {

  private Page rootPage;
  private String inputDir;
  private String pagesDir;
  private String filesDir;
  private String remoteUrl;
  private List<CopySet> filesToCopy;

  public Site(Page rootPage, String inputDir, String contentSubDir, String filesSubDir,
              String remoteUrl, List<CopySet> filesToCopy) {
    this.inputDir = inputDir;
    this.pagesDir = contentSubDir.isEmpty() ? inputDir : inputDir + "/" + contentSubDir;
    this.filesDir = filesSubDir.isEmpty() ? inputDir : inputDir + "/" + filesSubDir;
    this.remoteUrl = remoteUrl;
    this.rootPage = rootPage;
    this.filesToCopy = filesToCopy;
  }

  public Page getRootPage() {
    return rootPage;
  }

  public String getAbsoluteFileName(Page page) {
    return pagesDir + "/" + page.getFileName();
  }

  public String getInputDirectory() {
    return inputDir;
  }

  public String getInputDirectory(String subDir) {
    if (subDir == null || subDir.isEmpty()) {
      return inputDir;
    }
    return inputDir + "/" + subDir;
  }


  public File getInputFilePath(String baseDir, String filePath) {
    String base = filesDir;
    if (baseDir != null && !baseDir.isEmpty()) {
      base += "/" + baseDir;
    }
    return new File(base, filePath);
  }

  public String getUrl() {
    return remoteUrl;
  }

  public List<CopySet> getCopySets() {
    return filesToCopy;
  }
}
