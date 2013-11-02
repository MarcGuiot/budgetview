package com.designup.siteweaver.generation;

import com.designup.siteweaver.html.output.HtmlOutput;
import com.designup.siteweaver.model.CopySet;
import com.designup.siteweaver.model.Page;
import com.designup.siteweaver.model.Site;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * High-level class which monitors the site building process.
 * The generation starts as follows : the builder parses the current
 * template, and builds a generator for each variable or fixed part.
 * Variable parts are represented as "gen" tags in the template, and fixed
 * parts are simply the HTML code found between two "gen" tags.<br>
 * The generation itself consists in navigating through the site model
 * and invoking the generators list for each page.
 */
public class SiteGenerator {

  private Map<String, FileGenerator> fileGenerators = new HashMap<String, FileGenerator>();

  public static void run(Site site, HtmlOutput output) throws IOException {
    SiteGenerator generator = new SiteGenerator();
    generator.generateSite(site, output);
  }

  public static void run(Site site, Page page, HtmlOutput output) throws IOException {
    SiteGenerator generator = new SiteGenerator();
    generator.generatePage(site, page, output);
  }

  private void generateSite(Site site, HtmlOutput output) throws IOException {
    generateSubtree(site, site.getRootPage(), output);
    copyFiles(site, output);
  }

  private void generateSubtree(Site site, Page page, HtmlOutput output) throws IOException {
    generatePage(site, page, output);
    for (Page subPage : page.getSubPages()) {
      generateSubtree(site, subPage, output);
    }
  }

  private void generatePage(Site site, Page page, HtmlOutput output) throws IOException {
    FileGenerator fileGenerator =
      getFileGenerator(site.getInputDirectory() + "/" + page.getTemplateFile());
    fileGenerator.generatePage(page, site, output);
  }

  private FileGenerator getFileGenerator(String templateFile) throws IOException {
    FileGenerator existingGenerator = fileGenerators.get(templateFile);
    if (existingGenerator != null) {
      return existingGenerator;
    }
    FileGenerator newGenerator = new FileGenerator(templateFile);
    fileGenerators.put(templateFile, newGenerator);
    return newGenerator;
  }

  private void copyFiles(Site currentSite, HtmlOutput output) throws IOException {
    List<CopySet> copySetToProcess = currentSite.getCopySets();
    for (CopySet copySet : copySetToProcess) {
      for (String filePath : copySet.getPaths()) {
        File inputFile = currentSite.getInputFilePath(copySet.getBaseDir(), filePath);
        output.copyFile(inputFile, filePath);
      }
    }
  }
}