package com.designup.siteweaver.generation.generators;

import com.designup.siteweaver.generation.Generator;
import com.designup.siteweaver.html.HtmlTag;
import com.designup.siteweaver.html.HtmlWriter;
import com.designup.siteweaver.html.output.HtmlOutput;
import com.designup.siteweaver.model.Page;
import com.designup.siteweaver.model.Site;

import java.io.IOException;

public class NextInTourGenerator implements Generator {
  private Generator generator;

  public NextInTourGenerator(HtmlTag tag) {
    generator = getGenerator(tag);
  }

  public void processPage(Site site, Page page, HtmlWriter writer, HtmlOutput output) throws IOException {
    generator.processPage(site, page, writer, output);
  }

  private Generator getGenerator(HtmlTag tag) {
    String contentType = tag.getAttributeValue("output", "path");
    if (contentType.equalsIgnoreCase("path")) {
      return new OutputGenerator() {
        protected void writeOutput(HtmlWriter output, Page nextPage) {
          output.write(nextPage.getOutputFileName());
        }
      };
    }
    else if (contentType.equalsIgnoreCase("link")) {
      return new OutputGenerator() {
        protected void writeOutput(HtmlWriter output, Page nextPage) throws IOException {
          output.writeLink(nextPage.getTitle(), nextPage.getOutputFileName());
        }
      };
    }
    else if (contentType.equalsIgnoreCase("title")) {
      return new OutputGenerator() {
        protected void writeOutput(HtmlWriter output, Page nextPage) {
          output.write(nextPage.getTitle());
        }
      };
    }
    else {
      throw new RuntimeException("Unexpected contentType: " + contentType);
    }
  }

  private abstract class OutputGenerator implements Generator {
    public void processPage(Site site, Page page, HtmlWriter writer, HtmlOutput htmlOutput) throws IOException {
      Page nextPage = page.getNextPageInDepthFirstTraversal();
      writeOutput(writer, nextPage);
    }

    protected abstract void writeOutput(HtmlWriter output, Page nextPage) throws IOException;
  }
}
