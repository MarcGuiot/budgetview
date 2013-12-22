package org.designup.picsou.functests.general;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.uispec4j.Window;

import java.awt.event.ComponentListener;

public class LayoutConfigTest extends LoggedInFunctionalTestCase {
  protected void setUp() throws Exception {
    resetWindow();
    setInMemory(false);
    setDeleteLocalPrevayler(true);
    super.setUp();
    setDeleteLocalPrevayler(false);
  }

  protected void tearDown() throws Exception {
    resetWindow();
    super.tearDown();
  }

  public void test() throws Exception {
    resize(mainWindow, 900, 700);
    assertThat(mainWindow.sizeEquals(900, 700));

    restartApplication();

    assertThat(mainWindow.sizeEquals(900, 700));
  }

  private void resize(Window mainWindow, int width, int height) {
    mainWindow.getAwtComponent().setSize(width, height);
    for (ComponentListener listener : mainWindow.getAwtComponent().getComponentListeners()) {
      listener.componentResized(null);
    }

  }
}
