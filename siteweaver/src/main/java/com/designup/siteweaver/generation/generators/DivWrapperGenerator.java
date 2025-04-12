package com.designup.siteweaver.generation.generators;

import com.designup.siteweaver.generation.Generator;
import com.designup.siteweaver.html.HtmlWriter;
import com.designup.siteweaver.html.output.HtmlOutput;
import com.designup.siteweaver.model.Page;
import com.designup.siteweaver.model.Site;

import java.io.IOException;

public class DivWrapperGenerator implements Generator {

  private String classAttribute;
  private Generator generator;

  public DivWrapperGenerator(String classAttribute, Generator generator) {
    this.classAttribute = classAttribute.isEmpty() ? "" : " class=\"" + classAttribute + "\"";
    this.generator = generator;
  }

  public void processPage(Site site, Page page, HtmlWriter writer, HtmlOutput htmlOutput) throws IOException {
    writer.write("<div" + classAttribute + ">\n");
    generator.processPage(site, page, writer, htmlOutput);
    writer.write("</div>");
  }
}
