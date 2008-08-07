package org.designup.shrinker;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class JarShrinker {
  public static void main(String[] args) throws ClassNotFoundException, IOException {
    if (args.length < 3) {
      System.out.println("Usage: targetJar contentFileToFollow contentFileToIgnore");
      return;
    }
    ClassPathDependExtractorBuilder builder = ClassPathDependExtractorBuilder.init();
    builder.initIgnore(new FileReader(new File(args[2])));
    builder.initStartPoint(new FileReader(new File(args[1])));
    DependExtractor extractor = builder.getExtractor();
    ClassPathClassRetriever pathClassRetriever = new ClassPathClassRetriever(args[0]);
    pathClassRetriever.init(builder.jarToIgnore,
                            System.getProperty("java.class.path"));
    extractor.add(pathClassRetriever);
    extractor.extract();
    pathClassRetriever.complete(builder.ressources, builder.pathToIgnore);
  }
}
