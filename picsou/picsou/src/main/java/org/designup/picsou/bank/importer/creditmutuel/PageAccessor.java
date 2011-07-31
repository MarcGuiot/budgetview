package org.designup.picsou.bank.importer.creditmutuel;

import com.gargoylesoftware.htmlunit.html.HtmlPage;

public interface PageAccessor {

  HtmlPage getPage();

  void setPage(HtmlPage page);
}
