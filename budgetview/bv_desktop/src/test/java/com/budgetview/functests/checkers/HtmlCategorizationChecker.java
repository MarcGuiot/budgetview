package com.budgetview.functests.checkers;

import org.uispec4j.Panel;

public class HtmlCategorizationChecker extends SpecialCaseCategorizationChecker<HtmlCategorizationChecker> {
  public HtmlCategorizationChecker(Panel panel) {
    super(panel);
  }

  public SeriesEditionDialogChecker clickAndOpenSeriesEdition(String hyperlinkText) {
    return SeriesEditionDialogChecker.open(panel.getTextBox("message").triggerClickOnHyperlink(hyperlinkText));
  }
}
