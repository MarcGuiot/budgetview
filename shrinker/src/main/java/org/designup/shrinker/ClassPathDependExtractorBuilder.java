package org.designup.shrinker;

import org.globsframework.utils.exceptions.InvalidData;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ClassPathDependExtractorBuilder {
  protected DependExtractor extractor;
  List<String> ressources = new ArrayList<String>();
  List<String> pathToIgnore = new ArrayList<String>();
  Set<String> jarToIgnore = new HashSet<String>();

  public static ClassPathDependExtractorBuilder init(boolean withDebug) {
    return new ClassPathDependExtractorBuilder(withDebug);
  }

  private ClassPathDependExtractorBuilder(boolean withDebug) {
    extractor = new DependExtractor(withDebug);
  }

  public void initStartPoint(Reader startPointRead) throws IOException {
    String className;
    BufferedReader reader = new BufferedReader(startPointRead);
    while ((className = reader.readLine()) != null) {
      className = className.trim();
      if (className.trim().equals("") || className.startsWith("#")) {
        continue;
      }
      if (className.startsWith("C ")) {
        extractor.addStartPoint(className.substring(2));
      }
      else if (className.startsWith("P ")) {
        String[] s = className.substring(2).split("  *");
        if (s.length < 2) {
          throw new InvalidData("after P (for directory or package name) true or false is expected for 'is recursive'");
        }
        extractor.addRootPackage(s[0], s[1].equalsIgnoreCase("true"));
      }
      else if (className.startsWith("R ")) {
        ressources.add(className.substring(2));
      }
      else {
        throw new InvalidData("C or R (class or ressource) expected instead of " + className);
      }
    }
  }

  public void initIgnore(Reader ignoredReader) throws IOException {
    BufferedReader reader = new BufferedReader(ignoredReader);
    String className;
    while ((className = reader.readLine()) != null) {
      className = className.trim();
      if (className.equals("") || className.startsWith("#")) {
        continue;
      }
      if (className.startsWith("C ")) {
        extractor.addClassToIgnore(className.substring(2));
      }
      else if (className.startsWith("M ")) {
        extractor.addMethodToIgnore(className.substring(2, className.indexOf(" ", 3)).trim(),
                                    className.substring(className.indexOf(" ", 3)).trim());
      }
      else if (className.startsWith("P ")) {
        addPathToIgnore(className.substring(2));
      }
      else if (className.startsWith("D ")) {
        addJarToIgnore(className.substring(2));
      }
      else {
        throw new InvalidData("C, M or P (class, method or Path (directory or jar)) expected instead of " + className);
      }
    }
  }

  private void addPathToIgnore(String path) {
    pathToIgnore.add(path);
  }

  private void addJarToIgnore(String path) {
    jarToIgnore.add(path);
  }

  public DependExtractor getExtractor() {
    return extractor;
  }
}
