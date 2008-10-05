package org.designup.shrinker;

import org.globsframework.utils.MultiMap;
import org.globsframework.utils.exceptions.UnexpectedApplicationState;
import org.objectweb.asm.ClassReader;

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
    if (JarShrinker.LOG_ENABLED) {
      for (Map.Entry<String, List<String>> entry : dependencies.entries()) {
        System.out.println("Key " + entry.getKey());
        for (String s : entry.getValue()) {
          System.out.println("     " + s);
        }
      }
    }

    for (String s : classToJar) {
      File sourceFile = new File(root, s + ".class");
      File output = new File(target, s + ".class");
      try {
        if (sourceFile.exists() && sourceFile.isFile()) {
          output.getParentFile().mkdirs();
          FileInputStream inputStream = new FileInputStream(sourceFile);
          ClassReader classReader = new ClassReader(inputStream);
          FilterWriter classWriter = new FilterWriter();
          classReader.accept(classWriter, ClassReader.SKIP_DEBUG);
          FileOutputStream outputStream = new FileOutputStream(output);
          outputStream.write(classWriter.toByteArray());
          outputStream.close();
          inputStream.close();
        }
      }
      catch (IOException e) {
        throw new UnexpectedApplicationState("Unable to copy " + sourceFile.getAbsolutePath() + " on " +
                                             output.getAbsolutePath(), e);
      }
    }
  }

}
