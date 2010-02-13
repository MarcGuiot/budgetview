package org.designup.picsou.functests.checkers;

import org.uispec4j.*;
import static org.uispec4j.assertion.UISpecAssert.assertThat;
import org.uispec4j.interception.WindowHandler;
import org.uispec4j.interception.WindowInterceptor;

public class MainAccountViewChecker extends AccountViewChecker<MainAccountViewChecker> {

  public MainAccountViewChecker(Panel window) {
    super(window, "mainAccountView");
  }

  public MainAccountViewChecker setThreshold(final double amount) {
    return setThreshold(amount, false);
  }

  public MainAccountViewChecker setThreshold(final double amount, final boolean validateThroughTextField) {
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

  public MainAccountViewChecker checkThreshold(double amount) {
    Button button = panel.getButton("accountPositionThreshold");
    assertThat(button.textEquals("Limit: " + toString(amount)));
    return this;
  }
}
