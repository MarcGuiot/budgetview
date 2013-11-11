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

public class ContentGenerator implements Generator {

  public void processPage(Site site, Page page, HtmlWriter writer, HtmlOutput htmlOutput)
    throws IOException {

    String inputFilePath = site.getInputFilePath(page);
    dumpFileContent(inputFilePath, writer);
  }

  public static void dumpFileContent(String inputFileName, HtmlWriter output) throws IOException {
    BufferedReader inputFile = new BufferedReader(new FileReader(inputFileName));

    HtmlParser parser = new HtmlParser(inputFile);
    HtmlTag tag = parser.findNextTag("body", null);
    if (tag != null) {
      parser.findNextTag("/body", output);
    }
    else {
      FileUtils.copyFile(inputFileName, output);
    }
  }
}
