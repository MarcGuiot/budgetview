package org.designup.shrinker;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class JarShrinker {

  public static boolean LOG_ENABLED = false;

  public static void main(String[] args) throws ClassNotFoundException, IOException {
    if (args.length < 3) {
      System.out.println("Usage: -Dstd.out.trace=[true|false] -Dwith.debug=[true|false] targetJar contentFileToFollow contentFileToIgnore");
      return;
    }
    boolean withDebug = isPropertySet("with.debug");
    LOG_ENABLED = isPropertySet("std.out.trace");
    ClassPathDependExtractorBuilder builder = ClassPathDependExtractorBuilder.init(withDebug);
    builder.initIgnore(new FileReader(new File(args[2])));
    builder.initStartPoint(new FileReader(new File(args[1])));
    DependExtractor extractor = builder.getExtractor();
    ClassPathClassRetriever pathClassRetriever = new ClassPathClassRetriever(args[0], withDebug);
    pathClassRetriever.init(builder.jarToIgnore,
                            System.getProperty("java.class.path"));
    extractor.add(pathClassRetriever);
    extractor.extract();
    pathClassRetriever.complete(builder.ressources, builder.pathToIgnore);
  }

  private static boolean isPropertySet(final String key) {
    return System.getProperty(key) != null &&
           System.getProperty(key).equalsIgnoreCase("true");
  }
}
