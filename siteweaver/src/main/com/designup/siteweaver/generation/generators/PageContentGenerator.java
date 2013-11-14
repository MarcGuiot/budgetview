package com.designup.siteweaver.generation.generators;

import com.designup.siteweaver.generation.Generator;
import com.designup.siteweaver.html.HtmlParser;
import com.designup.siteweaver.html.HtmlTag;
import com.designup.siteweaver.html.HtmlWriter;
import com.designup.siteweaver.html.output.HtmlOutput;
import com.designup.siteweaver.model.Page;
import com.designup.siteweaver.model.Site;
import com.designup.siteweaver.utils.FileUtils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;

public class PageContentGenerator implements Generator {

  public void processPage(Site site, Page page, HtmlWriter writer, HtmlOutput output) throws IOException {

    String inputFilePath = site.getInputFilePath(page);
    FileGenerator newGenerator = new FileGenerator(inputFilePath);
    newGenerator.generatePage(page, site, writer, output);
  }
}
