package org.designup.picsou.gui.components.charts.histo;

import java.io.IOException;
import java.util.Set;

public interface HistoChartListener {
  void columnsClicked(Set<Integer> ids);

  void doubleClick();

  void scroll(int count);
}
