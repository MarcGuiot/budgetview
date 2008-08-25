package org.designup.picsou.gui.time;

import org.designup.picsou.gui.TimeService;
import org.designup.picsou.gui.time.selectable.Selectable;
import org.designup.picsou.gui.utils.PicsouColors;
import org.designup.picsou.model.Month;
import org.designup.picsou.model.UserPreferences;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.splits.color.ColorService;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.GlobRepositoryBuilder;
import org.globsframework.model.utils.GlobBuilder;
import org.globsframework.utils.directory.DefaultDirectory;
import org.globsframework.utils.directory.Directory;
import org.uispec4j.*;
import org.uispec4j.interception.WindowInterceptor;

import javax.swing.*;
import java.util.Iterator;
import java.util.Set;

public class TimeViewPanelUISpecTest extends UISpecTestCase {
  private TimeViewPanel timeViewPanel;
  private JFrame frame;

  protected void tearDown() throws Exception {
    super.tearDown();
    timeViewPanel = null;
    frame = null;
  }

  public void DISABLED_testMouseMove() throws Exception {
    DefaultDirectory defaultDirectory = new DefaultDirectory();
    defaultDirectory.add(new TimeService());
    GlobRepository repository = GlobRepositoryBuilder.init().get();
    final JFrame jFrame = initPanel(defaultDirectory, repository, 270);

    SelectionService service = defaultDirectory.get(SelectionService.class);
    DummySelectionListener listener = new DummySelectionListener();
    service.addListener(listener, Month.TYPE);
    setAdapter(new UISpecAdapter() {
      public Window getMainWindow() {
        return WindowInterceptor.run(new Trigger() {
          public void run() throws Exception {
            jFrame.setVisible(true);
          }
        });
      }
    });
    Window mainWindow = getMainWindow();
    Panel panel = mainWindow.getPanel("MonthSelector");
    paintAndWait();
    Mouse.pressed(panel, 200, 5);
    Mouse.released(panel, 200, 5);
    GlobList list = listener.getReceived();
    GlobList allMonth = repository.getAll(Month.TYPE).sort(Month.ID);
    int i = allMonth.indexOf(list.get(0));
    assertTrue(i != -1);
    GlobList expected = allMonth.subList(i, allMonth.size() - 1);
    Mouse.pressed(panel, 200, 5);
    timeViewPanel.savePaintPoint();
    Mouse.drag(panel, 400, 5);
    timeViewPanel.waitRepaint();
    Mouse.released(panel, 400, 5);
    timeViewPanel.waitRepaint();
    GlobList received = listener.getReceived();
    org.globsframework.utils.TestUtils.assertEquals(expected, received);
  }

  private void paintAndWait() {
    timeViewPanel.savePaintPoint();
    timeViewPanel.selectLastMonth();
    timeViewPanel.repaint();
    timeViewPanel.waitRepaint();
  }

  public void testScrollToVisibleDoNotScrollToMuch_2() throws Exception {
    init(170);
    Selectable selectable = getLastSelected();
    assertTrue(selectable.isVisible() == Selectable.Visibility.FULLY);
    assertNotNull(timeViewPanel.getSelectable(1, 1));
  }

  public void testScrollToVisibleDoNotScrollToMuch_1() throws Exception {
    init(270);
    Selectable selectable = getLastSelected();
    assertTrue(selectable.isVisible() == Selectable.Visibility.FULLY);
    assertNotNull(timeViewPanel.getSelectable(1, 1));
  }

  public void testScrollToVisibleDoNotScrollToMuch_3() throws Exception {
    init(470);
    Selectable selectable = getLastSelected();
    assertTrue(selectable.isVisible() == Selectable.Visibility.FULLY);
    assertNotNull(timeViewPanel.getSelectable(1, 1));
  }

  public void DISABLED_testScrollReduceSize() throws Exception {
    init(470);
    timeViewPanel.waitRepaint();
    timeViewPanel.savePaintPoint();
    timeViewPanel.setSize(80, 40);
    timeViewPanel.waitRepaint();
    Selectable selectable = getLastSelected();
    assertTrue(selectable.isVisible() == Selectable.Visibility.FULLY);
  }

  public void testReduceSizeLetSelectedVisible() throws Exception {
    init(170);
    timeViewPanel.savePaintPoint();
    timeViewPanel.setSize(80, 40);
    timeViewPanel.waitRepaint();
    timeViewPanel.waitRepaint();
    Selectable selectable = getLastSelected();
    assertTrue(selectable.isVisible() == Selectable.Visibility.FULLY);
  }

  private Selectable getLastSelected() {
    Set<Selectable> selected = timeViewPanel.getCurrentlySelectedToUpdate();
    assertFalse(selected.isEmpty());
    Selectable selectable;
    Iterator<Selectable> selectableIterator = selected.iterator();
    selectable = selectableIterator.next();
    while (selectable != null) {
      if (!selectableIterator.hasNext()) {
        break;
      }
      selectable = selectableIterator.next();
    }
    return selectable;
  }

  private void init(int width) {
    DefaultDirectory defaultDirectory = new DefaultDirectory();
    defaultDirectory.add(new TimeService());
    GlobRepository repository = GlobRepositoryBuilder.init().get();
    repository.create(UserPreferences.KEY);
    frame = initPanel(defaultDirectory, repository, width);

    SelectionService service = defaultDirectory.get(SelectionService.class);
    DummySelectionListener listener = new DummySelectionListener();
    service.addListener(listener, Month.TYPE);
    setAdapter(new UISpecAdapter() {
      public Window getMainWindow() {
        return WindowInterceptor.run(new Trigger() {
          public void run() throws Exception {
            frame.setVisible(true);
          }
        });
      }
    });
    Window mainWindow = getMainWindow();
    TimeGraph timeGraph = timeViewPanel.getTimeGraph();
    int monthWidth = timeGraph.getMonthWidth();
    paintAndWait();
  }

  private JFrame initPanel(Directory directory, GlobRepository repository, int width) {
    final JFrame jFrame = new JFrame();
    jFrame.setBounds(0, 0, width, 40);
    directory.add(SelectionService.class, new SelectionService());
    directory.add(ColorService.class, PicsouColors.createColorService());
    GlobList months = new GlobList();
    for (int i = 1; i < 13; i++) {
      months.add(GlobBuilder.init(Month.TYPE).set(Month.ID, Month.toYyyyMm(2007, i)).get());
    }
    repository.reset(months, Month.TYPE);
    timeViewPanel = new TimeViewPanel(repository, directory);
    jFrame.add(timeViewPanel);
    return jFrame;
  }

  private static class DummySelectionListener implements GlobSelectionListener {
    private GlobList received;

    public void selectionUpdated(GlobSelection selection) {
      synchronized (this) {
        received = selection.getAll();
        notify();
      }
    }

    public GlobList getReceived() throws InterruptedException {
      synchronized (this) {
        if (received == null) {
          wait(500);
        }
        try {
          return received;
        }
        finally {
          received = null;
        }
      }
    }
  }

}
