package org.designup.picsou.functests.checkers;

import org.uispec4j.Button;
import org.uispec4j.Trigger;
import org.uispec4j.Window;
import org.uispec4j.assertion.UISpecAssert;
import org.uispec4j.interception.WindowHandler;
import org.uispec4j.interception.WindowInterceptor;

public class DeleteCategoryChecker extends DataChecker {
  private Window window;

  public DeleteCategoryChecker(Window window) {
    this.window = window;
  }

  public void checkCategory(String name) {
    UISpecAssert.assertThat(window.getTextBox("categoryLabel").textEquals(name));
  }

  public void selectCategory(final String name) {
    WindowInterceptor.init(window.getButton("categoryChooser").triggerClick())
      .process(new WindowHandler() {
        public Trigger process(final Window window) throws Exception {
          return new Trigger() {
            public void run() throws Exception {
              CategoryChooserChecker categoryChooser = new CategoryChooserChecker(window);
              categoryChooser.selectCategory(name, true);
            }
          };
        }
      }).run();
  }

  public Trigger validate() {
    return getOkButton().triggerClick();
  }

  public Button getOkButton() {
    return window.getButton("OK");
  }


}
