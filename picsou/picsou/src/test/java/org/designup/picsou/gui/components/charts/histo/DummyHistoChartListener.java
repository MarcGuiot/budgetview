package org.designup.picsou.gui.components.charts.histo;

import org.globsframework.xml.XmlTestLogger;

import java.io.IOException;
import java.util.Set;

public class DummyHistoChartListener implements HistoChartListener {

  private XmlTestLogger logger = new XmlTestLogger();

  public DummyHistoChartListener() throws IOException {
  }

  public void columnsClicked(Set<Integer> ids) {
    try {
      logger.log("select").addAttribute("ids", ids.toString()).end();
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public void doubleClick() {
  }

  public void scroll(int count) {
  }

  public void check(String expectedLog) throws IOException {
    logger.assertEquals("<log>\n" + expectedLog + "\n</log>");
  }

  public void checkEmpty() {
    logger.assertEmpty();
  }
}
