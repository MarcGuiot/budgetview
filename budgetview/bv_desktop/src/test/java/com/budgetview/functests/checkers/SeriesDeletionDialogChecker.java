package com.budgetview.functests.checkers;

import org.uispec4j.TextBox;
import org.uispec4j.Trigger;
import org.uispec4j.Window;
import org.uispec4j.interception.WindowInterceptor;

import static org.uispec4j.assertion.UISpecAssert.assertFalse;
import static org.uispec4j.assertion.UISpecAssert.assertThat;

public class SeriesDeletionDialogChecker extends GuiChecker {
  private Window dialog;

  public static SeriesDeletionDialogChecker init(Trigger trigger) {
    return new SeriesDeletionDialogChecker(trigger);
  }

  private SeriesDeletionDialogChecker(Trigger trigger) {
    this(WindowInterceptor.getModalDialog(trigger));
  }

  private SeriesDeletionDialogChecker(Window dialog) {
    this.dialog = dialog;
  }

  public SeriesDeletionDialogChecker checkExistingTransactionsMessage(String seriesName) {
    TextBox introMessage = dialog.getTextBox("introMessage");
    assertThat(introMessage.textContains(seriesName));
    assertThat(introMessage.textContains("Some transactions have been associated with the envelope"));
    return this;
  }

  public SeriesDeletionDialogChecker checkExistingTransactionsMessage() {
    assertThat(dialog.getTextBox("introMessage").textContains("Some transactions have been associated with the envelope"));
    return this;
  }

  public SeriesDeletionDialogChecker checkTransferSeries(String... seriesNames) {
    assertThat(dialog.getListBox("seriesList").contentEquals(seriesNames));
    return this;
  }

  public SeriesDeletionDialogChecker setTransferSeriesFilter(String text) {
    dialog.getTextBox("seriesFilter").setText(text);
    return this;
  }

  public SeriesDeletionDialogChecker selectTransferSeries(String seriesName) {
    dialog.getListBox("seriesList").select(seriesName);
    return this;
  }

  public SeriesDeletionDialogChecker checkTransferEnabled() {
    assertThat(dialog.getButton("transfer").isEnabled());
    return this;
  }

  public SeriesDeletionDialogChecker checkTransferDisabled() {
    assertFalse(dialog.getButton("transfer").isEnabled());
    return this;
  }

  public void transfer() {
    dialog.getButton("transfer").click();
    assertFalse(dialog.isVisible());
  }

  public SeriesDeletionDialogChecker checkEndDateMessageContains(String text) {
    assertThat(dialog.getTextBox("setEndDateMessage").textContains(text));
    return this;
  }

  public void setEndDate() {
    dialog.getButton("setEndDate").click();
    assertFalse(dialog.isVisible());
  }

  public void uncategorize() {
    dialog.getButton("uncategorize").click();
    assertFalse(dialog.isVisible());
  }

  public void cancel() {
    dialog.getButton("cancel").click();
    assertFalse(dialog.isVisible());
  }

}
