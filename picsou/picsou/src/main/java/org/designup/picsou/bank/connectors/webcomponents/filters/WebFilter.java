package org.designup.picsou.bank.connectors.webcomponents.filters;

import com.gargoylesoftware.htmlunit.html.HtmlElement;

public interface WebFilter {
  boolean matches(HtmlElement element);
}
