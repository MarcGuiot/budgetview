package org.designup.picsou.bank.connectors.webcomponents;

import com.gargoylesoftware.htmlunit.ElementNotFoundException;
import com.gargoylesoftware.htmlunit.html.*;
import org.designup.picsou.bank.connectors.webcomponents.utils.DomNodeFilter;
import org.designup.picsou.bank.connectors.webcomponents.utils.HtmlUnit;
import org.designup.picsou.bank.connectors.webcomponents.utils.WebParsingError;

import java.util.ArrayList;
import java.util.List;

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

  public WebFrame getFrameByName(String name) throws WebParsingError {
    return new WebFrame(browser,  (HtmlFrame)getElementByName("frame", name, HtmlFrame.class));
  }

  public WebTextInput getTextInput() throws WebParsingError {
    return new WebTextInput(browser,
                            (HtmlTextInput)HtmlUnit.getElementWithAttribute(node, "input", "type", "text"));
  }

  public WebTextInput getTextInputById(String id) throws WebParsingError {
    return new WebTextInput(browser, (HtmlTextInput)getElementById(id, HtmlTextInput.class));
  }

  public WebTextInput getTextInputByName(String name) throws WebParsingError {
    return new WebTextInput(browser, (HtmlTextInput)getElementByName("input", name, HtmlTextInput.class));
  }

  public WebPasswordInput getPasswordInputById(String id) throws WebParsingError {
    return new WebPasswordInput(browser, (HtmlPasswordInput)getElementById(id, HtmlPasswordInput.class));
  }

  public WebPasswordInput getPasswordInputByName(String name) throws WebParsingError {
    return new WebPasswordInput(browser, (HtmlPasswordInput)getElementByName("input", name, HtmlPasswordInput.class));
  }

  public WebAnchor getSingleAnchor() throws WebParsingError {
    HtmlAnchor anchor = (HtmlAnchor)HtmlUnit.getSingleElementByTag(node, "a", HtmlAnchor.class);
    return new WebAnchor(browser, anchor);
  }

  public boolean containsAnchor() {
    return !node.getElementsByTagName("a").isEmpty();
  }

  public WebAnchor getAnchorById(String id) throws WebParsingError {
    return new WebAnchor(browser, (HtmlAnchor)getElementById(id, HtmlAnchor.class));
  }

  public WebAnchor getFirstAnchorWithText(String text) throws WebParsingError {
    List anchors = HtmlUnit.getElementsWithText(node, "a", text);
    return new WebAnchor(browser, (HtmlAnchor)anchors.get(0));
  }

  public WebAnchor getAnchorWithRef(String src) throws WebParsingError {
    return new WebAnchor(browser, HtmlUnit.getFirstElementWithAttribute(node, HtmlAnchor.class, HtmlAnchor.TAG_NAME, "href", src));
  }

  public WebAnchor getAnchorWithImage(String src) throws WebParsingError {
    HtmlElement img = HtmlUnit.getElementWithAttribute(node, "img", "src", src);
    return new WebAnchor(browser, (HtmlAnchor)HtmlUnit.getFirstParent(img, "a"));
  }

  public WebImage getSingleImage() throws WebParsingError {
    HtmlImage image = (HtmlImage)HtmlUnit.getSingleElementByTag(node, "img", HtmlImage.class);
    return new WebImage(browser, image);
  }

  public WebImage getImageById(String id) throws WebParsingError {
    return new WebImage(browser, getElementById(id, HtmlImage.class));
  }

  public WebMap getMapByName(String name) throws WebParsingError {
    return new WebMap(browser, (HtmlMap)HtmlUnit.getFirstElementWithAttribute(node, HtmlMap.TAG_NAME, "name", name));
  }

  public WebForm getSingleForm() throws WebParsingError {
    return new WebForm(browser, (HtmlForm)getSingleElement("form", HtmlForm.class));
  }

  public WebForm getFormByName(String name) throws WebParsingError {
    return new WebForm(browser, (HtmlForm)getElementByName("form", name, HtmlForm.class));
  }

  public WebForm getFormByAction(String action) throws WebParsingError {
    return new WebForm(browser,
                       (HtmlForm)HtmlUnit.getElementWithAttribute(node, HtmlForm.class, "form", "action", action));
  }

  public WebRadioButton getRadioButtonById(String id) throws WebParsingError {
    return new WebRadioButton(browser, (HtmlRadioButtonInput)getElementById(id, HtmlRadioButtonInput.class));
  }

  public WebRadioButton getRadioButtonByValue(String value) throws WebParsingError {
    HtmlRadioButtonInput input =
      (HtmlRadioButtonInput)HtmlUnit.getElementWithAttribute(node, HtmlRadioButtonInput.class, "input", "value", value);
    return new WebRadioButton(browser, input);
  }

  public WebInput getInputById(String id) throws WebParsingError {
    HtmlInput input = HtmlUnit.getElementById(node, id, HtmlInput.class);
    return new WebInput(browser, input);
  }

  public WebInput getInputByValue(String value) throws WebParsingError {
    HtmlInput input = (HtmlInput)HtmlUnit.getElementWithAttribute(node, "input", "value", value);
    return new WebInput(browser, input);
  }

  public WebInput getInputByAttribute(String attribute, String value) throws WebParsingError {
    HtmlInput input = (HtmlInput)HtmlUnit.getElementWithAttribute(node, "input", attribute, value);
    return new WebInput(browser, input);
  }

  public WebInput getInputByNameAndValue(final String name, final String value) throws WebParsingError {
    HtmlInput input = (HtmlInput)HtmlUnit.getSingleElement(node, "input", new HtmlUnit.Filter() {
      public boolean matches(HtmlElement element) {
        return element.getAttribute("name").equals(name)
               && element.getAttribute("value").equals(value);
      }
    });
    return new WebInput(browser, input);
  }

  public WebButton getButton() throws WebParsingError {
    return new WebButton(browser, (HtmlButton)getSingleElement("button", HtmlButton.class));
  }

  public WebButton getButtonById(String id) throws WebParsingError {
    return new WebButton(browser, (HtmlButton)getElementById(id, HtmlButton.class));
  }

  public WebButton getButtonByName(String name) throws WebParsingError {
    return new WebButton(browser, (HtmlButton)getElementByName("input", name, HtmlButton.class));
  }

  public WebButton getButtonByValue(String value) throws WebParsingError {
    return new WebButton(browser, (HtmlButton)HtmlUnit.getElementWithAttribute(node, HtmlButton.class, "input", "value", value));
  }

  public WebCheckBox getCheckBox() throws WebParsingError {
    return new WebCheckBox(browser, (HtmlInput)getSingleElement("input", HtmlInput.class));
  }

  public WebCheckBox getCheckBoxById(String id) throws WebParsingError {
    return new WebCheckBox(browser, (HtmlInput)getElementById(id, HtmlInput.class));
  }

  public WebCheckBox getCheckBoxByName(String name) throws WebParsingError {
    return new WebCheckBox(browser, (HtmlInput)getElementById(name, HtmlInput.class));
  }

  public WebCheckBox getCheckBoxByLabel(String label) throws WebParsingError {
    return new WebCheckBox(browser, (HtmlInput)getElementByLabel(label, HtmlInput.class));
  }

  public WebComboBox getComboBox() throws WebParsingError {
    return new WebComboBox(browser, (HtmlSelect)getSingleElement("select", HtmlSelect.class));
  }

  public WebComboBox getComboBoxById(String id) throws WebParsingError {
    return new WebComboBox(browser, (HtmlSelect)getElementById(id, HtmlSelect.class));
  }

  public WebComboBox getComboBoxByName(String name) throws WebParsingError {
    return new WebComboBox(browser, (HtmlSelect)getElementByName("select", name, HtmlSelect.class));
  }

  public WebComboBox getComboBoxByLabel(String label) throws WebParsingError {
    return new WebComboBox(browser, (HtmlSelect)getElementByLabel(label, HtmlSelect.class));
  }

  public WebTable getTableById(String id) throws WebParsingError {
    return new WebTable(browser, (HtmlTable)getElementById(id, HtmlTable.class));
  }

  public WebTable getTableWithClass(String name) throws WebParsingError {
    return new WebTable(browser,
                        (HtmlTable)HtmlUnit.getFirstElementWithAttribute(node, HtmlTable.TAG_NAME, "class", name));
  }

  public WebTable getTableWithNamedInput(String name) throws WebParsingError {
    HtmlInput input = (HtmlInput)HtmlUnit.getFirstElementWithAttribute(node, "input", "name", name);
    return new WebTable(browser, (HtmlTable)HtmlUnit.getFirstParent(input, "table"));
  }

  public List<WebTableCell> getTableCellsWithClass(String className) {
    List<WebTableCell> result = new ArrayList<WebTableCell>();
    for (HtmlElement element : HtmlUnit.getVisibleElementsWithAttribute(node, "td", "class", className)) {
      result.add(new WebTableCell(browser, (HtmlTableCell)element));
    }
    return result;
  }

  public WebTextArea getTextArea() throws WebParsingError {
    return new WebTextArea(browser, (HtmlTextArea)getSingleElement("textarea", HtmlTextArea.class));
  }

  public WebTextArea getTextAreaById(String id)  throws WebParsingError {
    return new WebTextArea(browser, (HtmlTextArea)getElementById(id, HtmlTextArea.class));
  }

  public WebTextArea getTextAreaByLabel(String label) throws WebParsingError {
    return new WebTextArea(browser, (HtmlTextArea)getElementByLabel(label, HtmlTextArea.class));
  }

  public WebSpan getSingleSpan() throws WebParsingError {
    return new WebSpan(browser, (HtmlSpan)getSingleElement("span", HtmlSpan.class));
  }

  public WebImageMap getImageMapByName(String name) throws WebParsingError {
    return new WebImageMap(browser, (HtmlMap)getElementByName("map", name, HtmlMap.class));
  }

  private HtmlElement getSingleElement(String tagName, Class expectedClass) throws WebParsingError {
    return HtmlUnit.getSingleElementByTag(node, tagName, expectedClass);
  }

  protected <T extends HtmlElement> T getElementById(String id, Class<T> expectedClass) throws WebParsingError {
    return HtmlUnit.getElementById(node, id, expectedClass);
  }

  private HtmlElement getElementByLabel(String label, Class expectedClass) throws WebParsingError {
    return HtmlUnit.getElementByLabel(label, node, expectedClass);
  }

  /**
   * @deprecated To make protected by using WebComponents instead of HtmlUnit components
   */
  public HtmlElement getElementByName(String tagName, String name) throws WebParsingError {
    return HtmlUnit.getElementWithAttribute(node, tagName, "name", name);
  }

  protected HtmlElement getElementByName(String tagName, String name, Class expectedClass) throws WebParsingError {
    HtmlElement element = HtmlUnit.getElementWithAttribute(this.node, tagName, "name", name);
    if (!expectedClass.isInstance(element)) {
      throw new WebParsingError(this, "Unexpected class for element '" + tagName +
                                      "' with name '" + name + "' - expected: " + expectedClass.getSimpleName() +
                                      " but was: " + element.getClass().getName());
    }
    return element;
  }

  public DomNode findElement(DomNode parent, DomNodeFilter filter, String failureMessage) throws WebParsingError {
    DomNode element = getElement(this.node, filter);
    if (element == null) {
      throw new WebParsingError(this.node,
                                failureMessage + "<br/><br/><h1>page content follows</h1>\n" +
                                HtmlUnit.dump(parent));
    }
    return element;
  }

  private DomNode getElement(DomNode parent, DomNodeFilter filter) throws WebParsingError {
    for (DomNode child : parent.getChildren()) {
      if (filter.accept(child)) {
        return child;
      }
    }
    return null;
  }

  public boolean containsTagWithId(String tag, String id) {
    List list = HtmlUnit.getVisibleElementsWithAttribute(node, tag, "id", id);
    return !list.isEmpty();
  }

  public boolean containsText(String text) {
    return node.asXml().contains(text);
  }
}
