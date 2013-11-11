package com.designup.siteweaver.custom.dup;

import com.designup.siteweaver.generation.Generator;
import com.designup.siteweaver.generation.GeneratorFactory;
import com.designup.siteweaver.generation.generators.*;
import com.designup.siteweaver.html.HtmlTag;

public class DupGeneratorFactory implements GeneratorFactory {

  public Generator createGenerator(HtmlTag tag) {
    if (!tag.hasAttribute("type")) {
      throw new java.lang.IllegalArgumentException("Missing XML attribute 'type'");
    }

    String generatorType = tag.getAttributeValue("type").toLowerCase();
    if (generatorType.equals("content")) {
      return new ContentGenerator();
    }
    else if (generatorType.equals("title")) {
      return new TitleGenerator(new DupTitleFormatter(tag));
    }
    else if (generatorType.equals("base")) {
      return new BaseTagGenerator();
    }
    else if (generatorType.equals("pagedate")) {
      return new PageDateGenerator();
    }
    else if (generatorType.equals("boxes")) {
      return new StaticBoxGenerator();
    }
    else if (generatorType.equals("value")) {
      return new KeyValueGenerator(tag);
    }
    else if (generatorType.equals("filepath")) {
      return new FilePathGenerator();
    }
    else if (generatorType.equals("url")) {
      return new UrlGenerator(tag);
    }
    else if (generatorType.equals("path")) {
      return new BreadcrumbGenerator(new DupBreadcrumbFormatter(tag));
    }
    else if (generatorType.equals("navbar")) {
      return new NavBarGenerator(new DupNavBarFormatter(tag));
    }
    else if (generatorType.equals("booktour")) {
      return new BookTourGenerator(tag, new DupBookTourFormatter());
    }
    else if (generatorType.equals("bookmenu")) {
      return new BookMenuGenerator(new DupBookMenuFormatter());
    }

    throw new RuntimeException("Unknow generator type '" + generatorType + "'");
  }
}
