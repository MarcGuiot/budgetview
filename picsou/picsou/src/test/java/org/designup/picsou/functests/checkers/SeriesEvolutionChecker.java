package org.designup.picsou.functests.checkers;

import org.uispec4j.Table;
import org.uispec4j.Window;
import org.uispec4j.assertion.UISpecAssert;

public class SeriesEvolutionChecker extends DataChecker {
  private Table table;
  private Window mainWindow;

  public SeriesEvolutionChecker(Window mainWindow) {
    this.mainWindow = mainWindow;
  }

  public SeriesEvolutionChecker checkRowLabels(String... labels) {
    UISpecAssert.assertThat(getTable().columnEquals(1, labels));
    return this;
  }

  private Table getTable() {
    if (table == null) {
      table = mainWindow.getTable("seriesEvolutionTable");
    }
    return table;
  }
}
