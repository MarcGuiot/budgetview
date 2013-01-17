package org.designup.picsou.bank.connectors.webcomponents;

import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.html.HtmlOption;
import com.gargoylesoftware.htmlunit.html.HtmlSelect;
import org.designup.picsou.bank.connectors.webcomponents.utils.WebParsingError;

import java.util.ArrayList;
import java.util.List;

public class WebSelect extends WebComponent<HtmlSelect> {

  public WebSelect(WebBrowser browser, HtmlSelect select) {
    super(browser, select);
  }

  public WebPage select(String text) throws WebParsingError {
    for (HtmlOption option : node.getOptions()) {
      boolean selected = option.asText().trim().equalsIgnoreCase(text);
      if (selected) {
        Page page = option.setSelected(true);
        browser.setCurrentPage(page);
        return browser.getCurrentPage();
      }
      else if (option.isSelected()) {
        option.setSelected(false);
      }
    }
    throw new WebParsingError(this,
                              "No option with text '" + text + "' " +
                              "for combo: " + node.getAttribute("id") +
                              " - actual content: " + getEntryNames());
  }

  public WebPage selectByValue(String value) throws WebParsingError {
    for (HtmlOption option : node.getOptions()) {
      if (option.getValueAttribute().trim().equalsIgnoreCase(value)) {
        Page page = option.setSelected(true);
        browser.setCurrentPage(page);
        return browser.getCurrentPage();
      }
      else if (option.isSelected()) {
        option.setSelected(false);
      }
    }
    throw new WebParsingError(this,
                              "No option with value '" + value + "' " +
                              "for combo: " + node.getAttribute("id") +
                              " - actual content: " + getEntryNames());
  }

  public List<String> getEntryNames() {
    List<String> result = new ArrayList<String>();
    for (HtmlOption option : node.getOptions()) {
      result.add(option.asText());
    }
    return result;
  }

  public List<String> getValues() {
    List<String> result = new ArrayList<String>();
    for (HtmlOption option : node.getOptions()) {
      result.add(option.getValueAttribute());
    }
    return result;
  }

  public String getSelectedValue() throws WebParsingError {
    List<String> result = new ArrayList<String>();
    for (HtmlOption option : node.getOptions()) {
      if (option.isSelected())
      result.add(option.getText());
    }
    if (result.size() == 0) {
      return null;
    }
    if (result.size() > 1) {
      throw new WebParsingError(this, "More than one selected value: " + result);
    }
    return result.get(0);

  }
}
