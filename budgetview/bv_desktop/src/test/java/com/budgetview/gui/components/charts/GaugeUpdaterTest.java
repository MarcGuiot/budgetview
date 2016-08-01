package com.budgetview.gui.components.charts;

import junit.framework.TestCase;
import com.budgetview.model.BudgetArea;

public class GaugeUpdaterTest extends TestCase {

  public void testPast() throws Exception {

    assertEquals("<p>Vous avez dépensé <b>20.00</b> de plus que prévu</p>",
                 GaugeUpdater.computeTooltips(0., 0., -80., -100., -200., false, BudgetArea.VARIABLE));

    assertEquals("<p>Vous avez dépensé <b>20.00</b> de moins que prévu</p>",
                 GaugeUpdater.computeTooltips(0., 0., -100., -80., -200., false, BudgetArea.VARIABLE));

    assertEquals("<p>Vous avez reçu <b>20.00</b> de moins que prévu</p>",
                 GaugeUpdater.computeTooltips(0., 0., 100., 80., 200., false, BudgetArea.INCOME));

    assertEquals("<p>Vous avez reçu <b>20.00</b> de plus que prévu</p>",
                 GaugeUpdater.computeTooltips(0., 0., 80., 100., 200., false, BudgetArea.INCOME));

    assertEquals("<p>Vous avez reçu <b>100.00</b> de moins que prévu</p>",
                 GaugeUpdater.computeTooltips(0., 0., 100., 0., 200., false, BudgetArea.INCOME));

  }

  public void testPastAndFuture() throws Exception {
    assertEquals("<p>Vous avez reçu <b>100.00</b> de moins que prévu</p>" +
                 "<p>Il vous reste <b>70.00</b> à recevoir</p>",
                 GaugeUpdater.computeTooltips(70., 0., 100., 0., 200., false, BudgetArea.INCOME));

    assertEquals("<p>Il vous reste <b>70.00</b> à recevoir</p>" +
                 "<p>Vous avez reçu <b>100.00</b> de plus que prévu</p>",
                 GaugeUpdater.computeTooltips(70., 0., 100., 200., 200., false, BudgetArea.INCOME));

    assertEquals("<p>Il vous reste <b>70.00</b> à recevoir</p>" +
                 "<p>Vous avez reçu <b>100.00</b> de plus que prévu</p>",
                 GaugeUpdater.computeTooltips(70., 0., 100., 200., 200., false, BudgetArea.INCOME));

    assertEquals("<p>Vous avez reçu <b>270.00</b> de plus que prévu</p>",
                 GaugeUpdater.computeTooltips(0., 70., 0., 200., 200., false, BudgetArea.INCOME));

    assertEquals("<p>Il vous reste <b>10.00</b> à recevoir</p>" +
                 "<p>Vous avez reçu <b>270.00</b> de plus que prévu</p>",
                 GaugeUpdater.computeTooltips(10., 70., 0., 200., 200., false, BudgetArea.INCOME));

    assertEquals("<p>Il vous reste <b>70.00</b> à recevoir</p>" +
                 "<p>Vous avez reçu <b>180.00</b> de plus que prévu</p>",
                 GaugeUpdater.computeTooltips(70., -20., 0., 200., 200., false, BudgetArea.INCOME));

  }
}
