package org.designup.picsou.functests.checkers;

import org.uispec4j.*;
import static org.uispec4j.assertion.UISpecAssert.*;
import org.uispec4j.interception.WindowHandler;
import org.uispec4j.interception.WindowInterceptor;

public class MainAccountViewChecker extends AccountViewChecker {
  public MainAccountViewChecker(Panel panel) {
    super(panel, "mainAccountView");
  }

  public EstimatedPositionDetailsChecker openEstimatedPositionDetails() {
    Window window = WindowInterceptor.getModalDialog(panel.getButton("estimatedPosition").triggerClick());
    return new EstimatedPositionDetailsChecker(window);
  }

  public MainAccountViewChecker checkNoEstimatedPositionDetails() {
    assertFalse(panel.getButton("estimatedPosition").isEnabled());
    return this;
  }

  public MainAccountViewChecker checkEstimatedPosition(double amount) {
    Button totalButton = panel.getButton("estimatedPosition");
    assertThat(totalButton.isVisible());
    assertThat(totalButton.textEquals(toString(amount)));
    return this;
  }

  public MainAccountViewChecker checkEstimatedPositionDate(String expected) {
    TextBox textBox = panel.getTextBox("estimatedPositionDate");
    assertThat(textBox.textEquals("on " + expected));
    return this;
  }

  public MainAccountViewChecker checkNoEstimatedPosition() {
    Button totalButton = panel.getButton("estimatedPosition");
    assertFalse(totalButton.isVisible());
    return this;
  }

  public MainAccountViewChecker checkEstimatedPositionColor(String color) {
    Button button = panel.getButton("estimatedPosition");
    assertThat(button.foregroundNear(color));
    return this;
  }

  public MainAccountViewChecker setLimit(final double amount, final boolean validateThroughTextField) {
    WindowInterceptor.init(panel.getButton("accountPositionThreshold").triggerClick())
      .process(new WindowHandler() {
        public Trigger process(Window window) throws Exception {
          final TextBox textField = window.getInputTextBox("editor");
          final String text = MainAccountViewChecker.this.toString(amount);

          if (validateThroughTextField) {
            return new Trigger() {
              public void run() throws Exception {
                textField.setText(text);
              }
            };
          }
          else {
            textField.clear();
            textField.appendText(text);
            return window.getButton("OK").triggerClick();
          }
        }
      })
      .run();
    return this;
  }

  public MainAccountViewChecker checkLimit(double amount) {
    Button button = panel.getButton("accountPositionThreshold");
    assertThat(button.textEquals("Limit: " + toString(amount)));
    return this;
  }
}
