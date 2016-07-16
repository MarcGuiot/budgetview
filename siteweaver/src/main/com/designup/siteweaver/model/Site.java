package com.designup.siteweaver.model;

import org.eclipse.jetty.util.URIUtil;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class Site {

  private Page rootPage;
  private String inputDir;
  private String pagesDir;
  private String filesDir;
  private String remoteUrl;
  private List<CopySet> filesToCopy;
  private List<String> targetPathsToIgnore;
  private File configFile;

  public Site(File configFile,
              Page rootPage,
              String inputDir,
              String contentSubDir,
              String filesSubDir,
              String remoteUrl,
              List<CopySet> filesToCopy,
              List<String> targetPathsToIgnore) {
    this.configFile = configFile;
    this.targetPathsToIgnore = targetPathsToIgnore;
    if (rootPage.getTemplateFilePath() == null) {
      throw new RuntimeException("A template must be provided for the root page");
    }
    this.inputDir = inputDir;
    this.pagesDir = contentSubDir.isEmpty() ? inputDir : inputDir + "/" + contentSubDir;
    this.filesDir = filesSubDir.isEmpty() ? inputDir : inputDir + "/" + filesSubDir;
    this.remoteUrl = remoteUrl;
    this.rootPage = rootPage;
    this.filesToCopy = filesToCopy;
  }

  public String getUrl() {
    return remoteUrl;
  }

  public Page getRootPage() {
    return rootPage;
  }

  public Page getPageForFile(String pageFilePath) {
    return rootPage.getPageForFile(pageFilePath);
  }

  public void processPages(PageFunctor functor) throws IOException {
    doProcessPages(rootPage, functor);
  }

  private void doProcessPages(Page page, PageFunctor functor) throws IOException {
    try {
      functor.process(page);
    }
    catch (IOException e) {
      throw new IOException("Error processing page: " + page.getUrl(), e);
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
    for (Page subPage : page.getSubPages()) {
      doProcessPages(subPage, functor);
    }
  }

  public String getInputFilePath(Page page) {
    return pagesDir + "/" + page.getFilePath();
  }

  public File getInputFile(Page page) {
    return new File(getInputFilePath(page));
  }

  public String getInputDirectory() {
    return inputDir;
  }

  public String getInputDirectory(CopySet copySet) {
    String subDir = copySet.getBaseDir();
    String result;
    if (subDir == null || subDir.isEmpty()) {
      result = inputDir;
    }
    else {
      result = inputDir + "/" + subDir;
    }
    return URIUtil.canonicalPath(new File(result).getAbsolutePath());
  }

  public File getInputFile(CopySet copySet, String filePath) {
    String baseDir = copySet.getBaseDir();
    String base = filesDir;
    if (baseDir != null && !baseDir.isEmpty()) {
      base += "/" + baseDir;
    }
    return new File(base, filePath);
  }

  public String getTargetPath(Page page) {
    String targetUrl = page.getUrl();
    if (targetUrl.equals("/")) {
      return "/index.html";
    }
    return targetUrl + ".html";
  }

  public void processFiles(FileFunctor functor) throws IOException {
    for (CopySet copySet : filesToCopy) {
      for (String targetPath : copySet.getPaths()) {
        File inputFile = getInputFile(copySet, targetPath);
        if (!targetPath.startsWith("/")) {
          targetPath = "/" + targetPath;
        }
        recurseProcessFile(inputFile, targetPath, functor);
      }
    }
  }

  private void recurseProcessFile(File inputFile, String targetPath, FileFunctor functor) throws IOException {
    if (!inputFile.exists()) {
      System.out.println("Warning: file " + inputFile + " (target: " + targetPath + ") does not exist");
    }
    else if (!inputFile.isDirectory()) {
      functor.process(inputFile, targetPath);
    }
    else {
      for (File subFile : inputFile.listFiles()) {
        recurseProcessFile(subFile, targetPath + "/" + subFile.getName(), functor);
      }
    }
  }

  public List<CopySet> getCopySets() {
    return filesToCopy;
  }

  public File getTemplateFile(Page page) {
    return new File(getInputDirectory() + "/" + page.getTemplateFilePath());
  }

  public long getLastModified(Page page) {
    return Math.max(getInputFile(page).lastModified(),
                    Math.max(configFile.lastModified(),
                             getTemplateFile(page).lastModified()));
  }

  public boolean ignoreTargetPath(String path) {
    for (String targetPath : targetPathsToIgnore) {
      if (path.startsWith(targetPath)) {
        return true;
      }
    }
    return false;
  }
}
