package com.designup.siteweaver.main;

import com.designup.siteweaver.generation.SiteGenerator;
import com.designup.siteweaver.html.output.FileOutput;
import com.designup.siteweaver.model.Site;
import com.designup.siteweaver.utils.FileUtils;
import com.designup.siteweaver.xml.SiteParser;

import java.io.File;

public class SiteWeaver {

  public static void main(String args[]) throws Exception {

    checkUsage(args);
    checkJavaVersion();

    String configFile = args[0];
    String outputDir = args[1];
    boolean publication = (args.length == 3) && args[2].equals("pub");

    String inputDir = new File(configFile).getParent();
    File file = new File(configFile);
    String absolutePath = file.getAbsolutePath();

    Site site = SiteParser.parse(FileUtils.createEncodedReader(absolutePath), inputDir);

    System.out.println("=== Generating private site...");
    FileOutput output = new FileOutput(outputDir, publication);
    SiteGenerator.run(site, output);

    System.out.print("=== Generation completed (");

    if (publication) {
      System.out.print("publication - " + site.getUrl());
    }
    else {
      System.out.print("local");
    }

    System.out.println(") ===");
  }

  private static void checkUsage(String[] args) {
    if (args.length < 2) {
      System.out.println("Usage: java SiteWeaver <configFile> <outputDir> [pub]");
      System.exit(-1);
    }
  }

  private static void checkJavaVersion() {
    String javaVersion = System.getProperty("java.version");
    if (javaVersion.compareTo("1.1.2") < 0) {
      System.out.println("WARNING: Swing must be run with a " +
                         "1.1.2 or higher version VM!!!");
      System.exit(-1);
    }
  }

}



