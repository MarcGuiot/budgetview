package com.budgetview.bank.connectors.webcomponents;

import com.gargoylesoftware.htmlunit.html.*;
import com.budgetview.bank.connectors.webcomponents.utils.HtmlUnit;
import com.budgetview.bank.connectors.webcomponents.utils.WebParsingError;

import java.util.ArrayList;
import java.util.List;

public abstract class WebComponent<T extends HtmlElement> {
  protected final WebBrowser browser;
  protected final T node;

  protected WebComponent(WebBrowser browser, T node) {
    this.browser = browser;
    this.node = node;
  }

  public String dump(HtmlElement element) {
    return element.toString();
  }

  public String getUrl() {
    return browser.getUrl();
  }

  public String asText() {
    return node.asText();
  }

  public String asXml() {
    return node.asXml();
  }

  public String toString() {
    return HtmlUnit.dump(node);
  }

  public String getAttributeValue(String attributeName) {
    return node.getAttribute(attributeName);
  }

  public void removeAttribute(String attributeName) {
    node.removeAttribute(attributeName);
  }

  public void fireEvent(String event) {
    node.fireEvent(event);
  }

  public String getClassName() {
    return node.getAttribute("className");
  }

  public void mouseDown() {
    browser.setCurrentPage(node.mouseDown());
  }

  public void mouseUp() {
    browser.setCurrentPage(node.mouseUp());
  }

  public void mouseOut() {
    browser.setCurrentPage(node.mouseOut());
  }

  public void mouseOver() {
    browser.setCurrentPage(node.mouseOver());
  }

  public String getOnclick() {
    return node.getOnClickAttribute();
  }

  public T getNode() {
    return node;
  }

  public static class HtmlNavigate {
    private WebBrowser browser;
    private HtmlElement node;
    private final boolean optional;

    public HtmlNavigate(WebBrowser browser, HtmlElement node, boolean optional) {
      this.browser = browser;
      this.node = node;
      this.optional = optional;
    }

    public HtmlNavigate next() throws WebParsingError {
      if (optional && node == null) {
        return this;
      }
      node = (HtmlElement)findFirstHtmlElement(node.getNextSibling());
      return this;
    }

    private DomNode findFirstHtmlElement(DomNode sibling) throws WebParsingError {
      while (!(sibling instanceof HtmlElement) && sibling != null) {
        sibling = sibling.getNextSibling();
      }
      if (sibling == null) {
        throw new WebParsingError(node, "Can not find next");
      }
      return sibling;
    }

    public HtmlNavigate in() throws WebParsingError {
      if (optional && node == null) {
        return this;
      }
      node = (HtmlElement)findFirstHtmlElement(node.getFirstChild());
      return this;
    }

    public WebAnchor asAnchor() throws WebParsingError {
      if (optional && node == null) {
        return null;
      }
      if (node instanceof HtmlAnchor) {
        return new WebAnchor(browser, ((HtmlAnchor)node));
      }
      else {
        throw new WebParsingError(node, "not an anchor");
      }
    }

    public WebCheckBox asCheckBox() throws WebParsingError {
      if (optional && node == null) {
        return null;
      }
      if (node instanceof HtmlInput) {
        return new WebCheckBox(browser, ((HtmlInput)node));
      }
      else {
        throw new WebParsingError(node, "not an anchor");
      }
    }

    public WebInput asInput() throws WebParsingError {
      if (optional && node == null) {
        return null;
      }
      if (node instanceof HtmlInput) {
        return new WebInput(browser, ((HtmlInput)node));
      }
      else {
        throw new WebParsingError(node, "not an anchor");
      }
    }

    public WebSelect asSelect() throws WebParsingError {
      if (optional && node == null) {
        return null;
      }
      if (node instanceof HtmlSelect) {
        return new WebSelect(browser, ((HtmlSelect)node));
      }
      else {
        throw new WebParsingError(node, "not an anchor");
      }
    }

    public HtmlNavigate parent() {
      if (optional && node == null) {
        return this;
      }
      node = (HtmlElement)node.getParentNode();
      return this;
    }

    public HtmlNavigate parent(int count) {
      for (int i = 0; i < count; i++){
        parent();
      }
      return this;
    }


    public WebPanel asPanel() throws WebParsingError {
      if (optional && node == null) {
        return null;
      }
      if (node instanceof HtmlDivision) {
        return new WebPanel(browser, node);
      }
      else {
        throw new WebParsingError(node, "not an anchor");
      }
    }

    public WebTextInput asTextInput() throws WebParsingError {
      if (optional && node == null) {
        return null;
      }
      if (node instanceof HtmlInput) {
        return new WebTextInput(browser, ((HtmlTextInput)node));
      }
      else {
        throw new WebParsingError(node, "not an anchor");
      }
    }

    public WebButton asButton() throws WebParsingError {
      if (optional && node == null) {
        return null;
      }
      if (node instanceof HtmlButton) {
        return new WebButton(browser, ((HtmlButton)node));
      }
      else {
        throw new WebParsingError(node, "not an button but " + node.getTagName());
      }
    }
  }

  public HtmlNavigate navigate() {
    return new HtmlNavigate(browser, node, false);
  }


  public static class HtmlNavigates {
    private WebBrowser browser;
    private List<HtmlElement> nodes;
    private final boolean optional;

    public HtmlNavigates(WebBrowser browser, List<HtmlElement> nodes, boolean optional) {
      this.browser = browser;
      this.nodes = nodes;
      this.optional = optional;
    }

    public HtmlNavigates next() throws WebParsingError {
      if (optional && nodes.isEmpty()) {
        return this;
      }
      List<HtmlElement> tmp = new ArrayList<HtmlElement>();
      for (HtmlElement node : nodes) {
        tmp.add((HtmlElement)findFirstHtmlElement(node.getNextSibling()));
      }
      nodes = tmp;
      return this;
    }

    private DomNode findFirstHtmlElement(DomNode sibling) throws WebParsingError {
      while (!(sibling instanceof HtmlElement) && sibling != null) {
        sibling = sibling.getNextSibling();
      }
      if (sibling == null) {
        throw new WebParsingError(nodes.get(0), "Can not find next");
      }
      return sibling;
    }

    public HtmlNavigates in() throws WebParsingError {
      if (optional && nodes.isEmpty()) {
        return this;
      }
      List<HtmlElement> tmp = new ArrayList<HtmlElement>();
      for (HtmlElement node : nodes) {
        tmp.add((HtmlElement)findFirstHtmlElement(node.getFirstChild()));
      }
      nodes = tmp;

      return this;
    }

    public List<WebAnchor> asAnchor() throws WebParsingError {
      if (optional && nodes.isEmpty()) {
        return null;
      }
      List<WebAnchor> anchors = new ArrayList<WebAnchor>();
      for (HtmlElement node : nodes) {
        if (node instanceof HtmlAnchor) {
          anchors.add(new WebAnchor(browser, ((HtmlAnchor)node)));
        }
      }
      return anchors;
    }

    public List<WebCheckBox> asCheckBox() throws WebParsingError {
      if (optional && nodes.isEmpty()) {
        return null;
      }
      List<WebCheckBox> checkBoxes = new ArrayList<WebCheckBox>();
      for (HtmlElement node : nodes) {
        if (node instanceof HtmlInput) {
          checkBoxes.add(new WebCheckBox(browser, ((HtmlInput)node)));
        }
      }
      return checkBoxes;

    }

    public List<WebSelect> asSelect() throws WebParsingError {
      if (optional && nodes.isEmpty()) {
        return null;
      }
      List<WebSelect> webSelects = new ArrayList<WebSelect>();
      for (HtmlElement node : nodes) {
        if (node instanceof HtmlSelect) {
          webSelects.add(new WebSelect(browser, ((HtmlSelect)node)));
        }
      }
      return webSelects;

    }

    public List<WebTable> asTables() throws WebParsingError {
      if (optional && nodes.isEmpty()) {
        return null;
      }
      List<WebTable> webSelects = new ArrayList<WebTable>();
      for (HtmlElement node : nodes) {
        if (node instanceof HtmlTable) {
          webSelects.add(new WebTable(browser, ((HtmlTable)node)));
        }
      }
      return webSelects;
    }

    public HtmlNavigates parent() {
      if (optional && nodes.isEmpty()) {
        return this;
      }
      List<HtmlElement> tmp = new ArrayList<HtmlElement>();
      for (HtmlElement node : nodes) {
        tmp.add((HtmlElement)node.getParentNode());
      }
      nodes = tmp;
      return this;
    }
  }
}
