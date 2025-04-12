package com.designup.siteweaver.generation.generators;

import com.designup.siteweaver.generation.Generator;
import com.designup.siteweaver.html.HtmlTag;
import com.designup.siteweaver.html.HtmlWriter;
import com.designup.siteweaver.html.output.HtmlOutput;
import com.designup.siteweaver.model.Page;
import com.designup.siteweaver.model.Site;

import java.io.IOException;

public class BookTourGenerator implements Generator {
  private Generator generator;
  private Formatter formatter;

  public interface Formatter {
    void writeStart(HtmlWriter writer);
    void writePath(Page nextPage, HtmlWriter writer);
    void writeLink(Page nextPage, HtmlWriter writer);
    void writeTitle(Page nextPage, HtmlWriter writer);
    void writeEnd(HtmlWriter writer);
  }

  public BookTourGenerator(HtmlTag tag, Formatter formatter) {
    this.formatter = formatter;
    this.generator = getGenerator(tag);
  }

  public void processPage(Site site, Page page, HtmlWriter writer, HtmlOutput output) throws IOException {
      generator.processPage(site, page, writer, output);
  }

  private Generator getGenerator(HtmlTag tag) {
    String contentType = tag.getAttributeValue("output", "link");
    if (contentType.equalsIgnoreCase("path")) {
      return new OutputGenerator() {
        protected void writeOutput(HtmlWriter writer, Page nextPage) {
          formatter.writePath(nextPage, writer);
        }
      };
    }
    else if (contentType.equalsIgnoreCase("link")) {
      return new OutputGenerator() {
        protected void writeOutput(HtmlWriter writer, Page nextPage) throws IOException {
          formatter.writeLink(nextPage, writer);
        }
      };
    }
    else if (contentType.equalsIgnoreCase("title")) {
      return new OutputGenerator() {
        protected void writeOutput(HtmlWriter writer, Page nextPage) {
          formatter.writeTitle(nextPage, writer);
        }
      };
    }
    else {
      throw new RuntimeException("Unexpected contentType: " + contentType);
    }
  }

  private abstract class OutputGenerator implements Generator {
    public void processPage(Site site, Page page, HtmlWriter writer, HtmlOutput htmlOutput) throws IOException {
      if (!BookMenuGenerator.isInMenu(page)) {
        return;
      }
      formatter.writeStart(writer);
      writeOutput(writer, page.getNextPageInDepthFirstTraversal());
      formatter.writeEnd(writer);
    }

    protected abstract void writeOutput(HtmlWriter output, Page nextPage) throws IOException;
  }
}
