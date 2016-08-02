package com.budgetview.desktop.components;

import com.budgetview.functests.utils.BalloonTipTesting;
import com.budgetview.desktop.components.tips.ShowDetailsTipAction;
import junit.framework.TestCase;
import com.budgetview.functests.checkers.components.GaugeChecker;
import com.budgetview.desktop.components.charts.Gauge;
import com.budgetview.utils.Lang;
import org.globsframework.gui.splits.color.ColorService;
import org.globsframework.utils.directory.DefaultDirectory;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.Locale;

public class GaugeTest extends TestCase {
  private Directory directory;

  protected void setUp() throws Exception {
    super.setUp();
    Locale.setDefault(Lang.ROOT);
    directory = new DefaultDirectory();
    directory.add(new ColorService());
  }

  public void testInit() throws Exception {
    init()
      .checkFill(0)
      .checkOverrun(0, false)
      .checkEmpty(1.0)
      .checkTooltip("No value defined");
  }

  public void testStandardCases() throws Exception {
    init()
      .set(0, 10)
      .checkFill(0)
      .checkOverrun(0, false)
      .checkEmpty(1.0)
      .checkTooltip("Expected: 10.00");

    init()
      .set(5, 10)
      .checkFill(0.5)
      .checkOverrun(0, false)
      .checkEmpty(0.5)
      .checkTooltip("Expected: 5.00");

    init()
      .set(10, 10)
      .checkFill(1.0)
      .checkOverrun(0, false)
      .checkEmpty(0)
      .checkTooltip("Complete");
  }

  public void testOverrun() throws Exception {
    init()
      .set(-15, -10)
      .checkFill(0.66)
      .checkOverrun(0.33, true)
      .checkEmpty(0)
      .checkTooltip("Overrun: 5.00");

    init()
      .set(15, 10)
      .checkFill(0.66)
      .checkOverrun(0.33, false)
      .checkEmpty(0)
      .checkTooltip("Extra: 5.00");
  }

  public void testInvertedSign() throws Exception {
    init()
      .set(-5, 10)
      .checkFill(0.0)
      .checkBegin(0.33)
      .checkEmpty(0.66)
      .checkTooltip("Expected: 15.00");

    init()
      .set(5, -10)
      .checkFill(0.33)
      .checkBegin(0)
      .checkEmpty(0.66)
      .checkTooltip("Remainder: 15.00");
  }

  public void testNoTarget() throws Exception {
    init()
      .set(-5, 0)
      .checkFill(0.0)
      .checkOverrun(1.0, true)
      .checkEmpty(0)
      .checkTooltip("Overrun: 5.00");

    init()
      .set(5, 0)
      .checkFill(0.0)
      .checkOverrun(1.0, false)
      .checkEmpty(0)
      .checkTooltip("Extra: 5.00");
  }

  public void testAmountsAreRoundedToTwoDecimals() throws Exception {
    init()
      .set(5.552, 5.551)
      .checkFill(1.0)
      .checkOverrun(0.0, false)
      .checkEmpty(0);
  }

  public void testPartialOverrun() throws Exception {
    init()
      .set(10.0, 20.0, 3.0, 10)
      .checkFill((10.0 - 3.0) / 20.0)
      .checkOverrun(3.0 / 20.0, false)
      .checkEmpty(10.0 / 20.0);

    init()
      .set(10.0, 20.0, 3.0, 10)
      .checkFill((10.0 - 3.0) / 20.0)
      .checkOverrun(3.0 / 20.0, false)
      .checkEmpty(10.0 / 20.0);
  }

  public void testOverrunAndRemaining() throws Exception {
    init().set(10, 20, 10, 0);
  }

  public void testPartialOverrunIsIgnoredIfActualGreaterThanTarget() throws Exception {
    init()
      .set(40.0, 20.0, 20.0, 0)
      .checkFill(0.5)
      .checkOverrun(0.5, false)
      .checkEmpty(0);
  }

  public void testDetailsTips() throws Exception {
    Gauge gauge = new Gauge();
    assertEquals("<html>No value defined</html>", gauge.getToolTipText());

    gauge.setActionListener(new ShowDetailsTipAction(gauge, directory));
    assertEquals("<html>No value defined</html>", gauge.getToolTipText());

    final JFrame frame = new JFrame();
    frame.add(gauge);
    frame.pack();

    click(gauge, gauge);
    BalloonTipTesting.checkBalloonTipVisible(frame, gauge, "No value defined");
  }

  private void click(final JComponent targetComponent, final JComponent container) throws InvocationTargetException, InterruptedException {
    SwingUtilities.invokeAndWait(new Runnable() {
      public void run() {
        MouseEvent event = new MouseEvent(targetComponent, MouseEvent.MOUSE_PRESSED, 0, 0, targetComponent.getX(), targetComponent.getY(), 1, false);
        container.dispatchEvent(event);
      }
    });
  }

  private GaugeChecker init() {
    Gauge gauge = new Gauge();
    return new GaugeChecker(gauge);
  }
}
