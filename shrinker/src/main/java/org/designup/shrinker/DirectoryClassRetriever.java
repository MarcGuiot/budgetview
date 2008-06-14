package org.designup.shrinker;

import org.globsframework.utils.MultiMap;
import org.globsframework.utils.exceptions.UnexpectedApplicationState;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DirectoryClassRetriever implements DependExtractor.ClassRetreiver {
  private String root;
  private String target;
  private List<String> classToJar = new ArrayList<String>();
  private MultiMap<String, String> dependencies = new MultiMap<String, String>();

  public DirectoryClassRetriever(String root, String target) {
    this.root = root;
    this.target = target;
  }

  public InputStream getCode(String className) {
    String s = root + "/" + DependExtractor.undotte(className) + ".class";
    FileInputStream fileOutputStream;
    try {
      fileOutputStream = new FileInputStream(s);
    }
    catch (FileNotFoundException e) {
      return null;
    }
    return fileOutputStream;
  }

  public void add(String dependClassName, String className) {
    classToJar.add(className);
    dependencies.put(dependClassName, className);
  }

  public void addPathContent(String path, Boolean isRecursive) {
  }

  void complete() {
    for (Map.Entry<String, List<String>> entry : dependencies.values()) {
      System.out.println("Key " + entry.getKey());
      for (String s : entry.getValue()) {
        System.out.println("     " + s);
      }
    }

    for (String s : classToJar) {
      File sourceFile = new File(root, s + ".class");
      File output = new File(target, s + ".class");
      try {
        if (sourceFile.exists() && sourceFile.isFile()) {
          output.getParentFile().mkdirs();
          new FileInputStream(sourceFile).getChannel().transferTo(0, sourceFile.length(),
                                                                  new FileOutputStream(output).getChannel());
        }
      }
      catch (IOException e) {
        throw new UnexpectedApplicationState("Unable to copy " + sourceFile.getAbsolutePath() + " on " +
                                             output.getAbsolutePath(), e);
      }
    }
  }

}
