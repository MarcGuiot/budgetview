package org.designup.picsou.bank.importer.webcomponents.utils;

import com.gargoylesoftware.htmlunit.html.DomNode;

public interface DomNodeFilter {
  boolean accept(DomNode node);
}
