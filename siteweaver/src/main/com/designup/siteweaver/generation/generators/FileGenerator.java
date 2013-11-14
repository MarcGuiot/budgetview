package com.designup.siteweaver.generation.generators;

import com.designup.siteweaver.custom.dup.DupGeneratorFactory;
import com.designup.siteweaver.generation.Generator;
import com.designup.siteweaver.generation.GeneratorFactory;
import com.designup.siteweaver.html.HtmlParser;
import com.designup.siteweaver.html.HtmlTag;
import com.designup.siteweaver.html.HtmlWriter;
import com.designup.siteweaver.html.output.HtmlOutput;
import com.designup.siteweaver.model.Page;
import com.designup.siteweaver.model.Site;
import com.designup.siteweaver.utils.FileUtils;

import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

public class FileGenerator {
  private GeneratorFactory factory = new DupGeneratorFactory();
  private List<Generator> generators = new ArrayList<Generator>();

  public FileGenerator(String templateFileName) throws IOException {
    createGeneratorsList(templateFileName);
  }

  private void createGeneratorsList(String fileName)
    throws IOException {

    Reader reader = FileUtils.createEncodedReader(fileName);
    HtmlParser parser = new HtmlParser(reader);

    HtmlTag tag;
    do {
      StringWriter writer = new StringWriter();
      tag = parser.findNextTag("gen", writer);
      String precedingString = writer.toString();
      if (precedingString.length() != 0) {
        generators.add(new StaticTextGenerator(precedingString));
      }
      if (tag != null) {
        Generator generator = factory.createGenerator(tag);
        generators.add(generator);
      }
    }
    while (tag != null);

    parser.close();
  }

  public void generatePage(Page page, Site site, HtmlWriter writer, HtmlOutput output) throws IOException {
      if (page.isTemplateGenerationEnabled()) {
        for (Generator generator : generators) {
          generator.processPage(site, page, writer, output);
        }
      }
      else {
        FileUtils.copyFile(site.getInputFilePath(page), writer);
      }
  }
}
