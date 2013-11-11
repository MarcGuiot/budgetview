package com.designup.siteweaver.model;

import java.io.IOException;

public interface PageFunctor {
  void process(Page page) throws Exception;
}
