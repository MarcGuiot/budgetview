package org.designup.picsou.bank.connectors.webcomponents;

import com.gargoylesoftware.htmlunit.html.*;
import org.designup.picsou.bank.connectors.webcomponents.utils.DomNodeFilter;
import org.designup.picsou.bank.connectors.webcomponents.utils.HtmlUnit;
import org.designup.picsou.bank.connectors.webcomponents.utils.WebParsingError;

import java.util.List;

public class WebPanel extends WebComponent<HtmlElement> {

  public WebPanel(WebBrowser browser, HtmlElement element) {
    super(browser, element);
  }

  public WebPanel getPanelById(String id) {
    return new WebPanel(browser, getElementById(id, HtmlElement.class));
  }

  public WebPanel findPanelById(String id) {
    return new WebPanel(browser, node.getElementById(id));
  }

  public WebTextField getTextField() {
    return new WebTextField(browser,
                            (HtmlInput)HtmlUnit.getElementWithAttribute(node, "input", "type", "text"));
  }

  public WebTextField getTextFieldById(String id) {
    return new WebTextField(browser, (HtmlInput)getElementById(id, HtmlInput.class));
  }

  public WebLink getLinkById(String id) {
    return new WebLink(browser, (HtmlAnchor)getElementById(id, HtmlAnchor.class));
  }

  public WebLink getFirstLinkWithText(String text) {
    List anchors = HtmlUnit.getElementsWithText(node, "a", text);
    return new WebLink(browser, (HtmlAnchor)anchors.get(0));
  }

  public WebForm getFormByName(String name) {
    return new WebForm(browser, (HtmlForm)getElementByName("form", name, HtmlForm.class));
  }

  public WebRadioButton getRadioButtonByValue(String value) {
    HtmlInput input =
      (HtmlInput)HtmlUnit.getElementWithAttribute(node, "input", "value", value);
    String actualType = input.getTypeAttribute();
    if (!"radio".equalsIgnoreCase(actualType)) {
      throw new WebParsingError(this, "Unexpected input type '" + actualType + "'");
    }
    return new WebRadioButton(browser, input);
  }

  public WebInput getInputByValue(String value) {
    HtmlInput input =
      (HtmlInput)HtmlUnit.getElementWithAttribute(node, "input", "value", value);
    return new WebInput(browser, input);
  }

  public WebButton getButton() {
    return new WebButton(browser, (HtmlButton)getSingleElement("button", HtmlButton.class));
  }

  public WebButton getButtonById(String id) {
    return new WebButton(browser, (HtmlButton)getElementById(id, HtmlButton.class));
  }

  public WebCheckBox getCheckBox() {
    return new WebCheckBox(browser, (HtmlInput)getSingleElement("input", HtmlInput.class));
  }

  public WebCheckBox getCheckBoxById(String id) {
    return new WebCheckBox(browser, (HtmlInput)getElementById(id, HtmlInput.class));
  }

  public WebCheckBox getCheckBoxByName(String name) {
    return new WebCheckBox(browser, (HtmlInput)getElementById(name, HtmlInput.class));
  }

  public WebCheckBox getCheckBoxByLabel(String label) throws Exception {
    return new WebCheckBox(browser, (HtmlInput)getElementByLabel(label, HtmlInput.class));
  }

  public WebComboBox getComboBox() {
    return new WebComboBox(browser, (HtmlSelect)getSingleElement("select", HtmlSelect.class));
  }

  public WebComboBox getComboBoxById(String id) {
    return new WebComboBox(browser, (HtmlSelect)getElementById(id, HtmlSelect.class));
  }

  public WebComboBox getComboBoxByLabel(String label) throws Exception {
    return new WebComboBox(browser, (HtmlSelect)getElementByLabel(label, HtmlSelect.class));
  }

  public WebTable getTableById(String id) {
    return new WebTable(browser, (HtmlTable)getElementById(id, HtmlTable.class));
  }

  public WebTable getTableWithNamedInput(String name) {
    HtmlInput input = (HtmlInput)HtmlUnit.getFirstElementWithAttribute(node, "input", "name", name);
    return new WebTable(browser, (HtmlTable)HtmlUnit.getFirstParent(input, "table"));
  }

  public WebTextArea getTextArea() {
    return new WebTextArea(browser, (HtmlTextArea)getSingleElement("textarea", HtmlTextArea.class));
  }

  public WebTextArea getTextAreaById(String id) {
    return new WebTextArea(browser, (HtmlTextArea)getElementById(id, HtmlTextArea.class));
  }

  public WebTextArea getTextAreaByLabel(String label) throws Exception {
    return new WebTextArea(browser, (HtmlTextArea)getElementByLabel(label, HtmlTextArea.class));
  }

  private HtmlElement getSingleElement(String tagName, Class expectedClass) {
    return HtmlUnit.getSingleElementByTag(node, tagName, expectedClass);
  }

  protected HtmlElement getElementById(String id, Class expectedClass) {
    return HtmlUnit.getElementById(node, id, expectedClass);
  }

  private HtmlElement getElementByLabel(String label, Class expectedClass) throws Exception {
    return HtmlUnit.getElementByLabel(label, node, expectedClass);
  }

  /**
   * @deprecated To make protected by using WebComponents instead of HtmlUnit components
   */
  public HtmlElement getElementByName(String tagName, String name) {
    return HtmlUnit.getElementWithAttribute(node, tagName, "name", name);
  }

  protected HtmlElement getElementByName(String tagName, String name, Class expectedClass) {
    HtmlElement element = HtmlUnit.getElementWithAttribute(this.node, tagName, "name", name);
    if (!expectedClass.isInstance(element)) {
      throw new WebParsingError(this, "Unexpected class for element '" + tagName +
                                      "' with name '" + name + "' - expected: " + expectedClass.getSimpleName() +
                                      " but was: " + element.getClass().getName());
    }
    return element;
  }

  public WebLink getAnchorWithImage(String src) {
    HtmlElement img = HtmlUnit.getElementWithAttribute(node, "img", "src", src);
    return new WebLink(browser, (HtmlAnchor)HtmlUnit.getFirstParent(img, "a"));
  }

  public DomNode findElement(DomNode parent, DomNodeFilter filter, String failureMessage) {
    DomNode element = getElement(this.node, filter);
    if (element == null) {
      throw new WebParsingError(this.node,
                                failureMessage + "<br/><br/><h1>page content follows</h1>\n" +
                                HtmlUnit.dump(parent));
    }
    return element;
  }

  private DomNode getElement(DomNode parent, DomNodeFilter filter) {
    for (DomNode child : parent.getChildren()) {
      if (filter.accept(child)) {
        return child;
      }
    }
    return null;
  }
}
