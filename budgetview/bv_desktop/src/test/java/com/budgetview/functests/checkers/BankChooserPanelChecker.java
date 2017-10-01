package com.budgetview.functests.checkers;

import com.budgetview.functests.checkers.components.PopupButton;
import org.uispec4j.ListBox;
import org.uispec4j.Trigger;
import org.uispec4j.Window;

import static org.uispec4j.assertion.UISpecAssert.assertFalse;
import static org.uispec4j.assertion.UISpecAssert.assertThat;

public class BankChooserPanelChecker<T extends BankChooserPanelChecker> extends GuiChecker {
  protected final Window window;

  public BankChooserPanelChecker(Window window) {
    this.window = window;
  }

  public T selectBank(String bankName) {
    window.getListBox("bankList").select(bankName);
    return (T) this;
  }

  public T checkBankListEquals(String... banks) {
    assertThat(window.getListBox("bankList").contentEquals(banks));
    return (T) this;
  }

  public T checkContainsBanks(String... banks) {
    assertThat(window.getListBox("bankList").contains(banks));
    return (T) this;
  }

  public T checkBanksNotPresent(String... banks) {
    ListBox bankList = window.getListBox("bankList");
    for (String bank : banks) {
      assertFalse("Bank unexpectedly found: " + bank, bankList.contains(bank));
    }
    return (T) this;
  }

  public T checkBankNotPresent(String bank) {
    assertFalse(window.getListBox("bankList").contains(bank));
    return (T) this;
  }

  public T checkNoBankSelected() {
    assertThat(window.getListBox("bankList").selectionIsEmpty());
    return (T) this;
  }

  public T checkSelectedBank(String bank) {
    assertThat(window.getListBox("bankList").selectionEquals(bank));
    return (T) this;
  }

  public T setFilter(String filter) {
    window.getTextBox("bankEditor").setText(filter, false);
    return (T) this;
  }

  public T checkListContent(String... banks) {
    assertThat(window.getListBox("bankList").contentEquals(banks));
    return (T) this;
  }

  public BankEditionDialogChecker addNewBank() {
    String menuItem = "Add a bank";
    return BankEditionDialogChecker.open(PopupButton.init(window, "bankActions").triggerClick(menuItem));
  }

  public T addNewBank(String name, String url) {
    addNewBank().setName(name).setUrl(url).validate();
    return (T) this;
  }

  public BankEditionDialogChecker edit() {
    String menuItem = "Edit bank";
    PopupButton popupButton = new PopupButton(window.getButton("bankActions"));
    Trigger trigger = popupButton.triggerClick(menuItem);
    return BankEditionDialogChecker.open(trigger);
  }

  public T checkEditDisabled() {
    PopupButton popupButton = new PopupButton(window.getButton("bankActions"));
    popupButton.checkItemDisabled("Edit bank");
    return (T) this;
  }

  public T deleteAndCancel() {
    PopupButton popupButton = new PopupButton(window.getButton("bankActions"));
    ConfirmationDialogChecker.open(popupButton.triggerClick("Delete bank"))
      .cancel();
    return (T) this;
  }

  public T delete() {
    return delete("Delete bank", "This bank is not used. Do you want to delete it?");
  }

  public T delete(String title, String message) {
    PopupButton popupButton = new PopupButton(window.getButton("bankActions"));
    ConfirmationDialogChecker.open(popupButton.triggerClick("Delete bank"))
      .checkTitle(title)
      .checkMessageContains(message)
      .validate();
    return (T) this;
  }

  public T checkDeleteDisabled() {
    PopupButton popupButton = new PopupButton(window.getButton("bankActions"));
    popupButton.checkItemDisabled("Delete bank");
    return (T) this;
  }

  public T checkDeleteRejected(String title, String message) {
    PopupButton popupButton = new PopupButton(window.getButton("bankActions"));
    MessageDialogChecker.open(popupButton.triggerClick("Delete bank"))
      .checkTitle(title)
      .checkInfoMessageContains(message)
      .close();
    return (T) this;
  }

  public T checkCountry(String country) {
    assertThat(window.getButton("countrySelector").textEquals(country));
    return (T) this;
  }

  public T selectCountry(String country) {
    PopupButton popupButton = new PopupButton(window.getButton("countrySelector"));
    popupButton.click(country);
    return (T) this;
  }
}
