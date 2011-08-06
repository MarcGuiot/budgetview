package org.designup.picsou.gui.components.charts.histo;

public interface HistoChartListener {
  void columnsClicked(HistoSelection selection);

  void doubleClick();

  void scroll(int count);
}
