package org.designup.picsou.functests.general;

import com.jidesoft.swing.JideSplitPane;
import junit.framework.Assert;
import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.globsframework.utils.TestUtils;
import org.uispec4j.Window;

import java.awt.*;
import java.awt.event.ComponentListener;

public class LayoutConfigTest extends LoggedInFunctionalTestCase {
  protected void setUp() throws Exception {
    resetWindow();
    setInMemory(false);
    setDeleteLocalPrevayler(true);
    super.setUp();
    setDeleteLocalPrevayler(false);
    addOns.activateAnalysis();
    addOns.activateProjects();
  }

  protected void tearDown() throws Exception {
    resetWindow();
    super.tearDown();
  }

  public void test() throws Exception {

    resize(mainWindow, 900, 700);
    assertThat(mainWindow.sizeEquals(900, 700));

    views.selectData();
    transactions.showGraph();
    checkSplitPane("transactionChartSplit", 0.75);
    setSplitPane("transactionChartSplit", 0.65);

    views.selectBudget();
    checkSplitPane("horizontalSplit", 0.5);
    setSplitPane("horizontalSplit", 0.25);
    checkSplitPane("verticalSplit1", 0.2, 0.5);
    setSplitPane("verticalSplit1", 0.3, 0.4);
    checkSplitPane("verticalSplit2", 0.6);
    setSplitPane("verticalSplit2", 0.5);

    // -- Restart --
    restartApplication();

    assertThat(mainWindow.sizeEquals(900, 700));

    views.selectData();
    checkSplitPane("transactionChartSplit", 0.65);

    views.selectBudget();
    checkSplitPane("horizontalSplit", 0.25);
    checkSplitPane("verticalSplit1", 0.3, 0.4);
    checkSplitPane("verticalSplit2", 0.5);
  }

  private void resize(Window mainWindow, int width, int height) {
    mainWindow.getAwtComponent().setSize(width, height);
    for (ComponentListener listener : mainWindow.getAwtComponent().getComponentListeners()) {
      listener.componentResized(null);
    }
  }

  private void setSplitPane(String componentName, double... proportions) {
    JideSplitPane splitPane = getSplitPanel(componentName);
    splitPane.setProportions(proportions);
  }

  private void checkSplitPane(String componentName, double... proportions) {
    JideSplitPane splitPane = getSplitPanel(componentName);
    TestUtils.assertEquals(splitPane.getProportions(), proportions);
  }

  private JideSplitPane getSplitPanel(String componentName) {
    Component[] swingComponents = mainWindow.getSwingComponents(JideSplitPane.class, componentName);
    if (swingComponents.length != 1) {
      Assert.fail(componentName + " not found - content: \n" + mainWindow.getDescription());
    }
    return (JideSplitPane)swingComponents[0];
  }
}
