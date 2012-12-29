package org.designup.picsou.bank.connectors.webcomponents.utils;

import com.gargoylesoftware.htmlunit.html.DomNode;

public interface DomNodeFilter {
  boolean accept(DomNode node);
}
