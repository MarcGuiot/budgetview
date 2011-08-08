package org.designup.picsou.gui.components.charts.histo;

import org.globsframework.model.Key;
import org.globsframework.xml.XmlTestLogger;

import java.io.IOException;

public class DummyHistoChartListener implements HistoChartListener {

  private XmlTestLogger logger = new XmlTestLogger();
  private boolean rolloverTracked = false;

  public DummyHistoChartListener() throws IOException {
  }

  public void columnsClicked(HistoSelection selection) {
    try {
      logger.log("select").addAttribute("ids", selection.getColumnIds().toString()).end();
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public void doubleClick(Integer columnIndex, Key objectKey) {
  }

  public void scroll(int count) {
  }

  public void setRolloverTracked(boolean tracked) {
    this.rolloverTracked = tracked;
  }

  public void rolloverUpdated(HistoRollover rollover) {
    try {
      if (rolloverTracked) {
        logger.log("rolloverUpdated").end();
      }
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public void check(String expectedLog) throws IOException {
    logger.assertEquals("<log>\n" + expectedLog + "\n</log>");
  }

  public void checkEmpty() {
    logger.assertEmpty();
  }
}
