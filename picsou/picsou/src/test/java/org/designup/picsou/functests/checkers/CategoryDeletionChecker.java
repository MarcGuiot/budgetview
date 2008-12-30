package org.designup.picsou.functests.checkers;

import org.uispec4j.Button;
import org.uispec4j.Trigger;
import org.uispec4j.Window;
import org.uispec4j.assertion.UISpecAssert;
import org.uispec4j.interception.WindowHandler;
import org.uispec4j.interception.WindowInterceptor;

public class CategoryDeletionChecker extends GuiChecker {
  private Window window;

  public CategoryDeletionChecker(Window window) {
    this.window = window;
  }

  public void checkCategory(String name) {
    UISpecAssert.assertThat(window.getTextBox("categoryField").textEquals(name));
  }

  public void selectCategory(final String name, final String excluded) {
    WindowInterceptor.init(window.getButton("categoryChooser").triggerClick())
      .process(new WindowHandler() {
        public Trigger process(final Window window) throws Exception {
          return new Trigger() {
            public void run() throws Exception {
              CategoryChooserChecker categoryChooser = new CategoryChooserChecker(window);
              if (excluded != null) {
                categoryChooser.checkExcluded(excluded);
              }
              categoryChooser.selectCategory(name, true);
            }
          };
        }
      }).run();
  }

  public Trigger validate() {
    return getOkButton().triggerClick();
  }

  public void checkOkEnabled(boolean b) {
    UISpecAssert.assertEquals(b, getOkButton().isEnabled());
  }

  private Button getOkButton() {
    return window.getButton("OK");
  }
}
