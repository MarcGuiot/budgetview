package org.designup.picsou.gui.printing.reports;

import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.utils.Lang;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SeriesTable {

  List<SeriesRow> seriesRows = new ArrayList<SeriesRow>();

  public SeriesTable(BudgetArea variable, GlobRepository repository, Directory directory) {

    for (int i = 0; i < 22; i++) {
      seriesRows.add(new SeriesRow(i));
    }
  }

  public String getTitle() {
    return "Variable";
  }

  public int getColumnCount() {
    return 14;
  }

  public String getColumnTitle(int column) {
    if (column == 0) {
      return Lang.get("print.seriesTable.series");
    }
    return Integer.toString(column);
  }

  public List<SeriesRow> rows() {
    return Collections.unmodifiableList(seriesRows);
  }

  public int getRowCount() {
    return rows().size();
  }

  public class SeriesRow {
    private int i;

    public SeriesRow(int i) {
      this.i = i;
    }

    public String getLabel() {
      return "Series " + i;
    }

    public String getValue(int column) {
      return Integer.toString(10000 + column) + ".00";
    }
  }
}
