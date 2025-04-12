package com.designup.siteweaver.generation;

import com.designup.siteweaver.html.HtmlTag;

public interface GeneratorFactory {
  public Generator createGenerator(HtmlTag tag);
}
