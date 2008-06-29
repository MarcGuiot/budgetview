package org.designup.picsou.functests.checkers;

import org.designup.picsou.gui.description.PicsouDescriptionService;
import static org.designup.picsou.model.Account.*;
import org.globsframework.model.Glob;
import org.globsframework.utils.Dates;
import org.uispec4j.ListBoxCellValueConverter;
import org.uispec4j.Panel;
import org.uispec4j.TextBox;
import org.uispec4j.assertion.UISpecAssert;

import java.awt.*;
import java.util.ArrayList;
import java.util.Date;

public class AccountChecker extends DataChecker {
  private TextBox accountLabel;

  public AccountChecker(Panel panel) {
//    accountLabel = panel.getTextBox("accountView");
  }

  public void assertDisplayEquals(String accountName, double balance, String updateDate) {
// TODO: correction necessaire dans UISpec ?
//    JComboBox comboBox = (JComboBox)accountCombo.getAwtComponent();
//    System.out.println("AccountChecker.assertDisplayEquals: " + comboBox.getSelectedIndex());
//    System.out.println("AccountChecker.assertDisplayEquals: " + comboBox.getSelectedItem());
//    UISpecAssert.assertTrue(accountCombo.selectionEquals(stringify(accountName, balance, updateDate)));
    UISpecAssert.assertTrue(accountLabel.textContains(Double.toString(balance)));
    Date date = Dates.parse(updateDate);
    UISpecAssert.assertTrue(accountLabel.textContains(PicsouDescriptionService.toString(date)));
  }

  public void assertDisplayEquals(String accountName) {
// TODO: correction necessaire dans UISpec ?
//    UISpecAssert.assertTrue(accountCombo.selectionEquals(stringify(accountName, balance, updateDate)));
    UISpecAssert.assertTrue(accountLabel.textIsEmpty());
    UISpecAssert.assertTrue(accountLabel.textIsEmpty());
  }

  private static String stringify(String accountName, double balance, String updateDate) {
    return accountName + " - " + balance + " - " + updateDate;
  }

  public void select(String accountName) {
//    accountCombo.select(accountName);
  }

  private static class Converter implements ListBoxCellValueConverter {
    public String getValue(int index, Component renderedComponent, Object modelObject) {
      Glob account = (Glob)modelObject;
      if (account == null) {
        return "(null)";
      }
      return stringify(account.get(NAME),
                       account.get(BALANCE),
                       Dates.toString(account.get(UPDATE_DATE)));
    }
  }

  public ContentChecker initContent() {
    return new ContentChecker();
  }

  public class ContentChecker {
    private java.util.List<String> expectedItems = new ArrayList<String>();

    private ContentChecker() {
    }

    public ContentChecker add(String accountName, double balance, String updateDate) {
      expectedItems.add(stringify(accountName, balance, updateDate));
      return this;
    }

    public void check() {
      String[] expectedArray = expectedItems.toArray(new String[expectedItems.size()]);
//      assertTrue(accountCombo.contentEquals(expectedArray));
    }
  }

}
