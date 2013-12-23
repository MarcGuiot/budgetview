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
  }

  protected void tearDown() throws Exception {
    resetWindow();
    super.tearDown();
  }

  public void test() throws Exception {
    resize(mainWindow, 900, 700);
    assertThat(mainWindow.sizeEquals(900, 700));
    views.selectHome();
    checkSplitPane("summaryProjectSplit", 0.5);
    setSplitPane("summaryProjectSplit", 0.46);

    views.selectData();
    checkSplitPane("accountSplit", 0.5);
    setSplitPane("accountSplit", 0.35);
    checkSplitPane("transactionChartSplit", 0.75);
    setSplitPane("transactionChartSplit", 0.65);

    views.selectBudget();
    checkSplitPane("horizontalSplit", 0.34, 0.33);
    setSplitPane("horizontalSplit", 0.25, 0.35);
    checkSplitPane("verticalSplit1", 0.5);
    setSplitPane("verticalSplit1", 0.55);
    checkSplitPane("verticalSplit2", 0.7);
    setSplitPane("verticalSplit2", 0.6);

    views.selectCategorization();
    checkSplitPane("categorizationSplit", 0.5);
    setSplitPane("categorizationSplit", 0.66);

    views.selectAnalysis();
    seriesAnalysis.toggleTable();
    checkSplitPane("analysisTableSplit", 0.65);
    setSplitPane("analysisTableSplit", 0.52);

    // -- Restart --
    restartApplication();

    assertThat(mainWindow.sizeEquals(900, 700));

    views.selectHome();
    checkSplitPane("summaryProjectSplit", 0.46);

    views.selectData();
    checkSplitPane("accountSplit", 0.35);
    checkSplitPane("transactionChartSplit", 0.65);

    views.selectBudget();
    checkSplitPane("horizontalSplit", 0.25, 0.35);
    checkSplitPane("verticalSplit1", 0.55);
    checkSplitPane("verticalSplit2", 0.6);

    views.selectCategorization();
    checkSplitPane("categorizationSplit", 0.66);

    views.selectAnalysis();
    seriesAnalysis.toggleTable();
    checkSplitPane("analysisTableSplit", 0.52);
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
