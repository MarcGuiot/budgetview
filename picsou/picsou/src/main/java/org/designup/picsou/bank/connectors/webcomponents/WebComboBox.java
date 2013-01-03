package org.designup.picsou.bank.connectors.webcomponents;

import com.gargoylesoftware.htmlunit.html.HtmlOption;
import com.gargoylesoftware.htmlunit.html.HtmlSelect;
import org.designup.picsou.bank.connectors.webcomponents.utils.WebParsingError;
import org.globsframework.utils.Utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class WebComboBox extends WebComponent<HtmlSelect> {

  public WebComboBox(WebBrowser browser, HtmlSelect select) {
    super(browser, select);
  }

  public void select(String entry) throws WebParsingError {
    boolean found = false;
    for (Iterator iterator = node.getOptions().iterator(); iterator.hasNext(); ) {
      HtmlOption option = (HtmlOption)iterator.next();
      boolean selected = option.asText().trim().equalsIgnoreCase(entry);
      option.setSelected(selected);
      if (selected) {
        found = true;
      }
    }
    if (!found) {
      throw new WebParsingError(this,
                                "No option with value '" + entry + "' " +
                                "for combo: " + node.getAttribute("id") +
                                " - actual content: " + getEntryNames());
    }
  }

  public void checkSelectionEquals(String value) throws WebParsingError {
    List selection = node.getSelectedOptions();
    if (selection.isEmpty()) {
      throw new WebParsingError(this, "No selection - actual content:\n" + node);
    }
    if (selection.size() > 1) {
      throw new WebParsingError(this, "Several options are selected: " + selection +
                                      " - actual content:\n" + node);
    }
    HtmlOption selected = (HtmlOption)selection.get(0);
    if (!Utils.equal(value, selected.asText())) {
      throw new WebParsingError(this, "Invalid selection: " + selected.asText());
    }
  }

  public List<String> getEntryNames() {
    List actualEntries = new ArrayList();
    for (Iterator iterator = node.getOptions().iterator(); iterator.hasNext(); ) {
      HtmlOption option = (HtmlOption)iterator.next();
      actualEntries.add(option.asText());
    }
    return actualEntries;
  }
}
