package org.designup.picsou.functests.checkers;

import org.uispec4j.Window;
import org.uispec4j.Table;
import org.uispec4j.assertion.UISpecAssert;
import org.designup.picsou.gui.series.view.SeriesView;

public class SeriesViewChecker extends DataChecker {
  private Window mainWindow;

  public SeriesViewChecker(Window mainWindow) {
    this.mainWindow = mainWindow;
  }

  public void checkContains(String... items) {
    UISpecAssert.assertThat(getTable().columnEquals(SeriesView.LABEL_COLUMN_INDEX, items));
  }

  private Table getTable() {
    return mainWindow.getTable("seriesView");
  }
}
