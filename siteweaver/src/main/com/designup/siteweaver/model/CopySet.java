package com.designup.siteweaver.model;

import java.lang.String;
import java.util.List;
import java.util.ArrayList;

public class CopySet {

  private String baseDir;
  private List<String> paths = new ArrayList<String>();

  public CopySet(String baseDir) {
    this.baseDir = baseDir;
  }

  public String getBaseDir() {
    return baseDir;
  }

  public CopySet add(String path) {
    this.paths.add(path);
    return this;
  }

  public List<String> getPaths() {
    return paths;
  }

  public String toString() {
    return baseDir + " / " + paths;
  }
}
