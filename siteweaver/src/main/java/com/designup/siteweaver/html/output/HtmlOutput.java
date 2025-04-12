package com.designup.siteweaver.html.output;

import com.designup.siteweaver.html.HtmlWriter;
import com.designup.siteweaver.model.Page;
import com.designup.siteweaver.model.Site;

import java.io.File;
import java.io.IOException;

public interface HtmlOutput {

  HtmlWriter createWriter(Page page) throws IOException;

  void copyFile(File inputFile, String filePath) throws IOException;

  String getBaseUrl(Site site);
}
