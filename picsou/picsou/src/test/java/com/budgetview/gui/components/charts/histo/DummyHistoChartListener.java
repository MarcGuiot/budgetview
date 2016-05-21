package com.budgetview.gui.components.charts.histo;

import org.globsframework.model.Key;
import org.globsframework.xml.XmlTestLogger;

import java.awt.*;
import java.io.IOException;
import java.util.Set;

public class DummyHistoChartListener implements HistoChartListener {

  private XmlTestLogger logger = new XmlTestLogger();
  private boolean rolloverTracked = false;

  public DummyHistoChartListener() throws IOException {
  }

  public void processClick(HistoSelection selection, Set<Key> objectKeys) {
    try {
      logger.log("select").addAttribute("ids", selection.getColumnIds().toString()).end();
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public void processDoubleClick(Integer columnIndex, Set<Key> objectKeys) {
  }

  public void processRightClick(HistoSelection selection, Set<Key> objectKeys, Point mouseLocation) {
    try {
      logger.log("rightClick").addAttribute("ids", selection.getColumnIds().toString()).end();
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }
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
