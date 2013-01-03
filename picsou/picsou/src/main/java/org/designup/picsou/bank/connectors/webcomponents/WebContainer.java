package org.designup.picsou.bank.connectors.webcomponents;

import com.gargoylesoftware.htmlunit.ElementNotFoundException;
import com.gargoylesoftware.htmlunit.html.*;
import org.designup.picsou.bank.connectors.webcomponents.utils.DomNodeFilter;
import org.designup.picsou.bank.connectors.webcomponents.utils.HtmlUnit;
import org.designup.picsou.bank.connectors.webcomponents.utils.WebParsingError;

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

  public WebTextField getTextField() throws WebParsingError {
    return new WebTextField(browser,
                            (HtmlInput)HtmlUnit.getElementWithAttribute(node, "input", "type", "text"));
  }

  public WebTextField getTextFieldById(String id) throws WebParsingError {
    return new WebTextField(browser, (HtmlInput)getElementById(id, HtmlInput.class));
  }

  public WebLink getLinkById(String id) throws WebParsingError {
    return new WebLink(browser, (HtmlAnchor)getElementById(id, HtmlAnchor.class));
  }

  public WebLink getFirstLinkWithText(String text) throws WebParsingError {
    List anchors = HtmlUnit.getElementsWithText(node, "a", text);
    return new WebLink(browser, (HtmlAnchor)anchors.get(0));
  }

  public WebLink getAnchorWithRef(String src) throws WebParsingError {
    return new WebLink(browser, HtmlUnit.getFirstElementWithAttribute(node, HtmlAnchor.class, HtmlAnchor.TAG_NAME, "href", src));
  }

  public WebForm getFormByName(String name) throws WebParsingError {
    return new WebForm(browser, (HtmlForm)getElementByName("form", name, HtmlForm.class));
  }

  public WebRadioButton getRadioButtonByValue(String value) throws WebParsingError {
    HtmlInput input =
      (HtmlInput)HtmlUnit.getElementWithAttribute(node, "input", "value", value);
    String actualType = input.getTypeAttribute();
    if (!"radio".equalsIgnoreCase(actualType)) {
      throw new WebParsingError(this, "Unexpected input type '" + actualType + "'");
    }
    return new WebRadioButton(browser, input);
  }

  public WebInput getInputById(String id) throws WebParsingError {
    HtmlInput input = HtmlUnit.getElementById(node, id, HtmlInput.class);
    return new WebInput(browser, input);
  }

  public WebInput getInputByValue(String value) throws WebParsingError {
    HtmlInput input =
      (HtmlInput)HtmlUnit.getElementWithAttribute(node, "input", "value", value);
    return new WebInput(browser, input);
  }

  public WebButton getButton() throws WebParsingError {
    return new WebButton(browser, (HtmlButton)getSingleElement("button", HtmlButton.class));
  }

  public WebButton getButtonById(String id) throws WebParsingError {
    return new WebButton(browser, (HtmlButton)getElementById(id, HtmlButton.class));
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

  public WebTextArea getTextArea() throws WebParsingError {
    return new WebTextArea(browser, (HtmlTextArea)getSingleElement("textarea", HtmlTextArea.class));
  }

  public WebTextArea getTextAreaById(String id)  throws WebParsingError {
    return new WebTextArea(browser, (HtmlTextArea)getElementById(id, HtmlTextArea.class));
  }

  public WebTextArea getTextAreaByLabel(String label) throws WebParsingError {
    return new WebTextArea(browser, (HtmlTextArea)getElementByLabel(label, HtmlTextArea.class));
  }

  public WebImage getImageById(String id) throws WebParsingError {
    return new WebImage(browser, getElementById(id, HtmlImage.class));
  }

  public WebMap getMapByName(String name) throws WebParsingError {
    return new WebMap(browser, (HtmlMap)HtmlUnit.getFirstElementWithAttribute(node, HtmlMap.TAG_NAME, "name", name));
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

  public WebLink getAnchorWithImage(String src) throws WebParsingError {
    HtmlElement img = HtmlUnit.getElementWithAttribute(node, "img", "src", src);
    return new WebLink(browser, (HtmlAnchor)HtmlUnit.getFirstParent(img, "a"));
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
}
