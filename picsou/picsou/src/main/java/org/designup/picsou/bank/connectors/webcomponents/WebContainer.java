package org.designup.picsou.bank.connectors.webcomponents;

import com.gargoylesoftware.htmlunit.ElementNotFoundException;
import com.gargoylesoftware.htmlunit.html.*;
import org.designup.picsou.bank.connectors.webcomponents.filters.WebFilter;
import org.designup.picsou.bank.connectors.webcomponents.filters.WebFilters;
import org.designup.picsou.bank.connectors.webcomponents.utils.HtmlUnit;
import org.designup.picsou.bank.connectors.webcomponents.utils.WebParsingError;
import org.globsframework.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

public class WebContainer<T extends HtmlElement> extends WebComponent<T> {
  protected WebContainer(WebBrowser browser, T node) {
    super(browser, node);
  }

  public WebPanel getPanelById(String id) throws WebParsingError {
    return new WebPanel(browser, getElementById(id, HtmlElement.class));
  }

  public WebPanel findPanelById(String id) throws WebParsingError {
    try {
      return new WebPanel(browser, node.getElementById(id));
    }
    catch (ElementNotFoundException e) {
      return null;
    }
  }

  public WebPanel getPanelByAttribute(String tag, String attribute, String value) throws WebParsingError {
    return new WebPanel(browser, HtmlUnit.getElementWithAttribute(node, tag, attribute, value, HtmlElement.class));
  }

  public WebFrame getFrameByName(String name) throws WebParsingError {
    return new WebFrame(browser, getElementByName("frame", name, HtmlFrame.class));
  }

  public WebTextInput getTextInput() throws WebParsingError {
    return new WebTextInput(browser,
                            HtmlUnit.getElementWithAttribute(node, "input", "type", "text", HtmlTextInput.class));
  }

  public WebTextInput getTextInputById(String id) throws WebParsingError {
    return new WebTextInput(browser, getElementById(id, HtmlTextInput.class));
  }

  public WebTextInput getTextInputByName(String name) throws WebParsingError {
    return new WebTextInput(browser, getElementByName("input", name, HtmlTextInput.class));
  }

  public WebPasswordInput getPasswordInputById(String id) throws WebParsingError {
    return new WebPasswordInput(browser, getElementById(id, HtmlPasswordInput.class));
  }

  public WebPasswordInput getPasswordInputByName(String name) throws WebParsingError {
    return new WebPasswordInput(browser, getElementByName("input", name, HtmlPasswordInput.class));
  }

  public WebAnchor getSingleAnchor() throws WebParsingError {
    HtmlAnchor anchor = HtmlUnit.getSingleElementByTag(node, "a", HtmlAnchor.class);
    return new WebAnchor(browser, anchor);
  }

  public boolean containsAnchor() {
    return !node.getElementsByTagName("a").isEmpty();
  }

  public boolean containsAnchorWithHRef(String href) {
    return !node.getElementsByAttribute("a", "href", href).isEmpty();
  }

  public WebAnchor getAnchorById(String id) throws WebParsingError {
    return new WebAnchor(browser, getElementById(id, HtmlAnchor.class));
  }

  public WebAnchor getAnchorById(final String id, boolean updateToLevelWindow) throws WebParsingError {
    return browser.retry(new Callable<WebAnchor>() {
      public WebAnchor call() throws Exception {
        return new WebAnchor(browser, getElementById(id, HtmlAnchor.class));
      }
    });
  }

  public WebAnchor getAnchor(WebFilter filter) throws WebParsingError {
    HtmlElement element = HtmlUnit.getHtmlElement(node, WebFilters.and(WebFilters.tagEquals(HtmlAnchor.TAG_NAME), filter));
    return new WebAnchor(browser, (HtmlAnchor)element);
  }

  public WebAnchor getFirstAnchorWithText(String text) throws WebParsingError {
    List anchors = HtmlUnit.getElementsWithText(node, "a", text);
    return new WebAnchor(browser, (HtmlAnchor)anchors.get(0));
  }

  public WebAnchor getAnchorWithRef(String src) throws WebParsingError {
    return new WebAnchor(browser, HtmlUnit.getFirstElementWithAttribute(node, HtmlAnchor.class, HtmlAnchor.TAG_NAME, "href", src));
  }

  public WebAnchor getAnchorWithImage(String src) throws WebParsingError {
    HtmlElement img = HtmlUnit.getElementWithAttribute(node, "img", "src", src, HtmlElement.class);
    return new WebAnchor(browser, HtmlUnit.getFirstParent(img, "a", HtmlAnchor.class));
  }

  public List<WebAnchor> getAnchorsWithClass(final String targetClass) {
    List<HtmlElement> elements = HtmlUnit.getElements(node, "a", new WebFilter() {
      public boolean matches(HtmlElement element) {
        String[] classes = element.getAttribute("class").split(" ");
        for (String aClass : classes) {
          if (Utils.equal(aClass, targetClass)) {
            return true;
          }
        }
        return false;
      }
    });
    List<WebAnchor> result = new ArrayList<WebAnchor>();
    for (HtmlElement element : elements) {
      result.add(new WebAnchor(browser, (HtmlAnchor)element));
    }
    return result;
  }

  public WebImage getSingleImage() throws WebParsingError {
    HtmlImage image = HtmlUnit.getSingleElementByTag(node, "img", HtmlImage.class);
    return new WebImage(browser, image);
  }

  public WebImage getImageById(String id) throws WebParsingError {
    return new WebImage(browser, getElementById(id, HtmlImage.class));
  }

  public WebMap getMapByName(String name) throws WebParsingError {
    return new WebMap(browser, HtmlUnit.getFirstElementWithAttribute(node, HtmlMap.class, HtmlMap.TAG_NAME, "name", name));
  }

  public WebForm getSingleForm() throws WebParsingError {
    return new WebForm(browser, getSingleElement("form", HtmlForm.class));
  }

  public WebForm getFormByName(String name) throws WebParsingError {
    return new WebForm(browser, getElementByName("form", name, HtmlForm.class));
  }

  public WebForm getFormById(String id) throws WebParsingError {
    return new WebForm(browser, getElementById(id, HtmlForm.class));
  }

  public WebForm getFormByAction(String action) throws WebParsingError {
    return new WebForm(browser,
                       (HtmlForm)(HtmlForm)HtmlUnit.getElementWithAttribute(node, "form", "action", action, HtmlForm.class));
  }

  public WebRadioButton getRadioButtonById(String id) throws WebParsingError {
    return new WebRadioButton(browser, getElementById(id, HtmlRadioButtonInput.class));
  }

  public WebRadioButton getRadioButtonByValue(String value) throws WebParsingError {
    HtmlRadioButtonInput input =
      HtmlUnit.getElementWithAttribute(node, "input", "value", value, HtmlRadioButtonInput.class);
    return new WebRadioButton(browser, input);
  }

  public WebInput getInputById(String id) throws WebParsingError {
    HtmlInput input = HtmlUnit.getElementById(node, id, HtmlInput.class);
    return new WebInput(browser, input);
  }

  public WebInput getInputByValue(String value) throws WebParsingError {
    HtmlInput input = HtmlUnit.getElementWithAttribute(node, "input", "value", value, HtmlInput.class);
    return new WebInput(browser, input);
  }

  public WebInput getInputByAttribute(String attribute, String value) throws WebParsingError {
    HtmlInput input = HtmlUnit.getElementWithAttribute(node, "input", attribute, value, HtmlInput.class);
    return new WebInput(browser, input);
  }

  public WebInput getInputByNameAndValue(final String name, final String value) throws WebParsingError {
    HtmlInput input = (HtmlInput)HtmlUnit.getSingleElement(node, "input", new WebFilter() {
      public boolean matches(HtmlElement element) {
        return element.getAttribute("name").equals(name)
               && element.getAttribute("value").equals(value);
      }
    });
    return new WebInput(browser, input);
  }

  public WebButton getButton() throws WebParsingError {
    return new WebButton(browser, getSingleElement("button", HtmlButton.class));
  }

  public WebButton getButtonById(String id) throws WebParsingError {
    return new WebButton(browser, getElementById(id, HtmlButton.class));
  }

  public WebButton getButtonByName(String name) throws WebParsingError {
    return new WebButton(browser, getElementByName("input", name, HtmlButton.class));
  }

  public WebButton getButtonByValue(String value) throws WebParsingError {
    return new WebButton(browser, HtmlUnit.getElementWithAttribute(node, "input", "value", value, HtmlButton.class));
  }

  public WebButtonInput getButtonInputByValue(String value) throws WebParsingError {
    return new WebButtonInput(browser, HtmlUnit.getElementWithAttribute(node, "input", "value", value, HtmlButtonInput.class));
  }

  public WebCheckBox getCheckBox() throws WebParsingError {
    return new WebCheckBox(browser, getSingleElement("input", HtmlInput.class));
  }

  public WebCheckBox getCheckBoxById(String id) throws WebParsingError {
    return new WebCheckBox(browser, getElementById(id, HtmlInput.class));
  }

  public WebCheckBox getCheckBoxByName(String name) throws WebParsingError {
    return new WebCheckBox(browser, getElementById(name, HtmlInput.class));
  }

  public WebCheckBox getCheckBoxByLabel(String label) throws WebParsingError {
    return new WebCheckBox(browser, getElementByLabel(label, HtmlInput.class));
  }

  public WebSelect getSelect() throws WebParsingError {
    return new WebSelect(browser, getSingleElement("select", HtmlSelect.class));
  }

  public WebSelect getSelectById(String id) throws WebParsingError {
    return new WebSelect(browser, getElementById(id, HtmlSelect.class));
  }

  public WebSelect getSelectByName(String name) throws WebParsingError {
    return new WebSelect(browser, getElementByName("select", name, HtmlSelect.class));
  }

  public WebSelect getSelectByLabel(String label) throws WebParsingError {
    return new WebSelect(browser, getElementByLabel(label, HtmlSelect.class));
  }

  public WebTable getTableById(String id) throws WebParsingError {
    return new WebTable(browser, getElementById(id, HtmlTable.class));
  }

  public WebTable getTableContaining(WebFilter filter) throws WebParsingError {
    DomNode element = HtmlUnit.findFirstHtmlElement(node, filter);
    while (element != null) {
      if (element instanceof HtmlTable) {
        return new WebTable(browser, (HtmlTable)element);
      }
      element = element.getParentNode();
    }
    throw new WebParsingError(this, "Can not find table as parent of " + node.getTagName() + " for " + filter);
  }

  public WebTable getTableWithClass(String name) throws WebParsingError {
    return new WebTable(browser, HtmlUnit.getFirstElementWithAttribute(node, HtmlTable.class, HtmlTable.TAG_NAME, "class", name));
  }

  public WebTable getTableWithNamedInput(String name) throws WebParsingError {
    HtmlInput input = HtmlUnit.getFirstElementWithAttribute(node, HtmlInput.class, "input", "name", name);
    return new WebTable(browser, HtmlUnit.getFirstParent(input, "table", HtmlTable.class));
  }

  public List<WebTableCell> getTableCellsWithClass(String className) {
    List<WebTableCell> result = new ArrayList<WebTableCell>();
    for (HtmlElement element : HtmlUnit.getVisibleElementsWithAttribute(node, "td", "class", className)) {
      result.add(new WebTableCell(browser, (HtmlTableCell)element));
    }
    return result;
  }

  public WebTextArea getTextArea() throws WebParsingError {
    return new WebTextArea(browser, getSingleElement("textarea", HtmlTextArea.class));
  }

  public WebTextArea getTextAreaById(String id) throws WebParsingError {
    return new WebTextArea(browser, getElementById(id, HtmlTextArea.class));
  }

  public WebTextArea getTextAreaByLabel(String label) throws WebParsingError {
    return new WebTextArea(browser, getElementByLabel(label, HtmlTextArea.class));
  }

  public WebSpan getSingleSpan() throws WebParsingError {
    return new WebSpan(browser, getSingleElement("span", HtmlSpan.class));
  }

  public WebImageMap getImageMapByName(String name) throws WebParsingError {
    return new WebImageMap(browser, getElementByName("map", name, HtmlMap.class));
  }

  public WebImageMap getImageMapById(String id) throws WebParsingError {
    return new WebImageMap(browser, getElementById(id, HtmlMap.class));
  }

  private <T extends HtmlElement> T getSingleElement(String tagName, Class<T> expectedClass) throws WebParsingError {
    return HtmlUnit.getSingleElementByTag(node, tagName, expectedClass);
  }

  protected <T extends HtmlElement> T getElementById(String id, Class<T> expectedClass) throws WebParsingError {
    return HtmlUnit.getElementById(node, id, expectedClass);
  }

  private <T extends HtmlElement> T getElementByLabel(String label, Class<T> expectedClass) throws WebParsingError {
    return HtmlUnit.getElementByLabel(label, node, expectedClass);
  }

  protected <T extends HtmlElement> T getElementByName(String tagName, String name, Class<T> expectedClass) throws WebParsingError {
    return HtmlUnit.getElementWithAttribute(this.node, tagName, "name", name, expectedClass);
  }

  public boolean containsTagWithId(String tag, String id) {
    List list = HtmlUnit.getVisibleElementsWithAttribute(node, tag, "id", id);
    return !list.isEmpty();
  }

  public HtmlElement get(WebFilter filter) throws WebParsingError {
    return HtmlUnit.findHtmlElement(node, filter);
  }

  public HtmlNavigate findFirst(WebFilter filter) throws WebParsingError {
    return new HtmlNavigate(browser, HtmlUnit.findFirstHtmlElement(node, filter), true);
  }

  public HtmlNavigates findAll(WebFilter filter) throws WebParsingError {
    return new HtmlNavigates(browser, HtmlUnit.findAllHtmlElement(node, filter), true);
  }

  public boolean hasId(String id) {
    return node.hasHtmlElementWithId(id);
  }
}
