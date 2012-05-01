package org.designup.picsou.functests.checkers;

import org.designup.picsou.functests.checkers.components.PopupButton;
import org.uispec4j.Panel;
import org.uispec4j.Trigger;

import static org.uispec4j.assertion.UISpecAssert.*;

public class BankChooserPanelChecker<T extends BankChooserPanelChecker> extends GuiChecker {
  protected final Panel panel;

  public BankChooserPanelChecker(Panel panel) {
    this.panel = panel;
  }

  public T selectBank(String bankName) {
    panel.getListBox("bankList").select(bankName);
    return (T)this;
  }

  public T checkBankListEquals(String... banks) {
    assertThat(panel.getListBox("bankList").contentEquals(banks));
    return (T)this;
  }

  public T checkContainsBanks(String... banks) {
    assertThat(panel.getListBox("bankList").contains(banks));
    return (T)this;
  }

  public T checkBankNotPresent(String bank) {
    assertFalse(panel.getListBox("bankList").contains(bank));
    return (T)this;
  }

  public T checkNoBankSelected() {
    assertThat(panel.getListBox("bankList").selectionIsEmpty());
    return (T)this;
  }

  public T checkSelectedBank(String bank) {
    assertThat(panel.getListBox("bankList").selectionEquals(bank));
    return (T)this;
  }

  public T setFilter(String filter) {
    panel.getTextBox("bankEditor").setText(filter, false);
    return (T)this;
  }

  public T checkListContent(String... banks) {
    assertThat(panel.getListBox("bankList").contentEquals(banks));
    return (T)this;
  }

  public BankEditionDialogChecker addNewBank() {
    return BankEditionDialogChecker.open(panel.getButton("addBank"));
  }

  public T addNewBank(String name, String url) {
    addNewBank().setName(name).setUrl(url).validate();
    return (T)this;
  }

  public BankEditionDialogChecker edit() {
    String menuItem = "Edit bank";
    PopupButton popupButton = new PopupButton(panel.getButton("bankActions"));
    Trigger trigger = popupButton.triggerClick(menuItem);
    return BankEditionDialogChecker.open(trigger);
  }

  public T checkEditDisabled() {
    PopupButton popupButton = new PopupButton(panel.getButton("bankActions"));
    popupButton.checkItemDisabled("Edit bank");
    return (T)this;
  }

  public T deleteAndCancel() {
    PopupButton popupButton = new PopupButton(panel.getButton("bankActions"));
    ConfirmationDialogChecker.open(popupButton.triggerClick("Delete bank"))
      .cancel();
    return (T)this;
  }

  public T delete() {
    return delete("Delete bank", "This bank is not used. Do you want to delete it?");
  }

  public T delete(String title, String message) {
    PopupButton popupButton = new PopupButton(panel.getButton("bankActions"));
    ConfirmationDialogChecker.open(popupButton.triggerClick("Delete bank"))
      .checkTitle(title)
      .checkMessageContains(message)
      .validate();
    return (T)this;
  }

  public T checkDeleteDisabled() {
    PopupButton popupButton = new PopupButton(panel.getButton("bankActions"));
    popupButton.checkItemDisabled("Delete bank");
    return (T)this;
  }

  public T checkDeleteRejected(String title, String message) {
    PopupButton popupButton = new PopupButton(panel.getButton("bankActions"));
    MessageDialogChecker.open(popupButton.triggerClick("Delete bank"))
      .checkTitle(title)
      .checkMessageContains(message)
      .close();
    return (T)this;
  }
}
