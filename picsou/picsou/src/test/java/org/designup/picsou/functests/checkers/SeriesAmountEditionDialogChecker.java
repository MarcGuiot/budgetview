package org.designup.picsou.functests.checkers;

import org.uispec4j.Trigger;
import org.uispec4j.Window;
import static org.uispec4j.assertion.UISpecAssert.*;
import org.uispec4j.interception.WindowInterceptor;

public class SeriesAmountEditionDialogChecker extends SeriesAmountEditionChecker<SeriesAmountEditionDialogChecker> {

  public static SeriesAmountEditionDialogChecker open(Trigger trigger) {
    return new SeriesAmountEditionDialogChecker(WindowInterceptor.getModalDialog(trigger));
  }

  public SeriesAmountEditionDialogChecker(Window dialog) {
    super(dialog);
  }

  public SeriesAmountEditionDialogChecker checkAmountLabel(String text) {
    assertThat(dialog.getTextBox("dateLabel").textEquals(text));
    return this;
  }

  public SeriesAmountEditionDialogChecker checkPropagationDisabled() {
    assertFalse(dialog.getCheckBox().isSelected());
    return this;
  }

  public SeriesAmountEditionDialogChecker setPropagationEnabled() {
    dialog.getCheckBox().select();
    return this;
  }

  public void cancel() {
    dialog.getButton("Cancel").click();
  }

  public void validate() {
    dialog.getButton("OK").click();
  }

  public SeriesAmountEditionDialogChecker checkPeriodicity(String text) {
    assertThat(dialog.getButton("editSeries").textEquals(text));
    return this;
  }

  public SeriesEditionDialogChecker editSeries() {
    return SeriesEditionDialogChecker.open(dialog.getButton("editSeries"));
  }

  public SeriesAmountEditionDialogChecker checkSliderPosition(int percentage) {
    assertThat(dialog.getSlider().relativePositionEquals(percentage));
    return this;
  }

  public SeriesAmountEditionDialogChecker setSliderPosition(int percentage) {
    dialog.getSlider().setRelativePosition(percentage);
    return this;
  }

  public SeriesAmountEditionDialogChecker checkSliderLabels(String... labels) {
    assertThat(dialog.getSlider().labelsEqual(labels));
    return this;
  }
}
