package org.designup.picsou.gui.time;

import junit.framework.TestCase;
import org.designup.picsou.gui.time.selectable.MouseState;
import org.designup.picsou.gui.time.selectable.ReleasedMouseState;
import org.designup.picsou.gui.time.selectable.Selectable;
import org.designup.picsou.gui.time.selectable.SelectableContainer;
import org.designup.picsou.gui.utils.PicsouColors;
import org.designup.picsou.model.Month;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.splits.color.ColorService;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.GlobRepositoryBuilder;
import org.globsframework.model.utils.GlobBuilder;
import org.globsframework.utils.directory.DefaultDirectory;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class TimeViewPanelTest extends TestCase {

  public void testInOut() throws Exception {
    DummySelectableContainer container = new DummySelectableContainer();
    MouseState mouseState = new ReleasedMouseState(container);
    mouseState.mousePressed(getMouseEvent(0, 0));
    container.checkSelected(0);
    mouseState = mouseState.mousePressed(getMouseEvent(2, 1));
    container.checkSelected(0, 1);
    mouseState = mouseState.mouseMoved(getMouseEvent(2, 1));
    container.checkSelected(0, 1);
    mouseState = mouseState.mouseMoved(getMouseEvent(3, 1));
    container.checkSelected(0, 1, 1);
    mouseState = mouseState.mouseMoved(getMouseEvent(3, 0));
    container.checkSelected(0, 1, 1);
    mouseState = mouseState.mouseMoved(getMouseEvent(5, 0));
    container.checkSelected(0, 1, 1);
    mouseState = mouseState.mouseMoved(getMouseEvent(5, 1));
    container.checkSelected(0, 1, 1, 1, 1);
    mouseState.mouseMoved(getMouseEvent(2, 1));
    container.checkSelected(0, 1);
  }

  public void testOutInBack() throws Exception {
    DummySelectableContainer container = new DummySelectableContainer();
    MouseState mouseState = new ReleasedMouseState(container);
    mouseState = mouseState.mousePressed(getMouseEvent(2, 1));
    container.checkSelected(0, 1);
    mouseState = mouseState.mouseMoved(getMouseEvent(5, 1));
    container.checkSelected(0, 1, 1, 1, 1);
    mouseState = mouseState.mouseMoved(getMouseEvent(5, 0));
    container.checkSelected(0, 1, 1, 1, 1);
    mouseState = mouseState.mouseMoved(getMouseEvent(3, 0));
    container.checkSelected(0, 1, 1, 1, 1);
    mouseState.mouseMoved(getMouseEvent(3, 1));
    container.checkSelected(0, 1, 1);
  }

  public void testInOutBetweenFirstClic() throws Exception {
    DummySelectableContainer container = new DummySelectableContainer();
    MouseState mouseState = new ReleasedMouseState(container);
    mouseState = mouseState.mousePressed(getMouseEvent(3, 1));
    container.checkSelected(0, 0, 1);
    mouseState = mouseState.mouseMoved(getMouseEvent(5, 1));
    container.checkSelected(0, 0, 1, 1, 1);
    mouseState = mouseState.mouseMoved(getMouseEvent(5, 0));
    container.checkSelected(0, 0, 1, 1, 1);
    mouseState = mouseState.mouseMoved(getMouseEvent(2, 0));
    container.checkSelected(0, 0, 1, 1, 1);
    mouseState.mouseMoved(getMouseEvent(2, 1));
    container.checkSelected(0, 1, 1);
  }

  public void testWithCtrlDownOnNotSelected() throws Exception {
    DummySelectableContainer container = new DummySelectableContainer();
    MouseState mouseState = new ReleasedMouseState(container);
    mouseState = mouseState.mousePressed(getMouseEvent(3, 1));
    mouseState = mouseState.mouseReleased(getMouseEvent(3, 1));
    container.checkSelected(0, 0, 1);
    mouseState = mouseState.mousePressed(getCtrlMouseEvent(5, 1));
    mouseState.mouseReleased(getMouseEvent(5, 1));
    container.checkSelected(0, 0, 1, 0, 1);
  }

  public void testWithShiftDownOnNotSelected() throws Exception {
    DummySelectableContainer container = new DummySelectableContainer();
    MouseState mouseState = new ReleasedMouseState(container);
    mouseState = mouseState.mousePressed(getMouseEvent(3, 1));
    mouseState = mouseState.mouseReleased(getMouseEvent(3, 1));
    container.checkSelected(0, 0, 1);
    mouseState = mouseState.mousePressed(getShiftMouseEvent(5, 1));
    mouseState = mouseState.mouseReleased(getShiftMouseEvent(5, 1));
    container.checkSelected(0, 0, 1, 1, 1);
    mouseState = mouseState.mousePressed(getShiftMouseEvent(2, 1));
    mouseState.mouseReleased(getShiftMouseEvent(2, 1));
    container.checkSelected(0, 1, 1, 1, 1);
  }

//  public void testReal() throws Exception {
//    DefaultDirectory defaultDirectory = new DefaultDirectory();
//    GlobRepository repository = GlobRepositoryBuilder.init().get();
//    final JFrame jFrame = initPanel(defaultDirectory, repository);
//
//    SelectionService service = defaultDirectory.get(SelectionService.class);
//    DummySelectionListener listener = new DummySelectionListener();
//    service.addListener(listener, Month.TYPE);
//    setAdapter(new UISpecAdapter() {
//      public Window getMainWindow() {
//        return WindowInterceptor.run(new Trigger() {
//          public void run() throws Exception {
//            jFrame.setVisible(true);
//          }
//        });
//      }
//    });
//    Window mainWindow = getMainWindow();
//    Panel panel = mainWindow.getPanel("MonthSelector");
//    mainWindow.getAwtComponent().repaint();
//    Thread.sleep(100);
//    Mouse.pressed(panel, 200, 5);
//    Mouse.released(panel, 200, 5);
//    GlobList list = listener.getReceived();
//    GlobList allMonth = repository.getAll(Month.TYPE).sort(Month.ID);
//    int i = allMonth.indexOf(list.get(0));
//    assertTrue(i != -1);
//    GlobList expected = allMonth.subList(i, allMonth.size() - 1);
//    Mouse.pressed(panel, 200, 5);
//    Mouse.drag(panel, 400, 5);
//    Thread.sleep(10000);
//    Mouse.released(panel, 200, 5);
//    GlobList received = listener.getReceived();
//    org.globsframework.utils.TestUtils.assertEquals(expected, received);
//  }

  private static JFrame initPanel(Directory directory, GlobRepository repository) {
    final JFrame jFrame = new JFrame();
    jFrame.setBounds(0, 0, 270, 40);
    directory.add(SelectionService.class, new SelectionService());
    directory.add(ColorService.class, PicsouColors.createColorService());
    GlobList months = new GlobList();
    for (int i = 4; i < 13; i++) {
      months.add(GlobBuilder.init(Month.TYPE).set(Month.ID, Month.toYyyyMm(2006, i)).get());
    }
    for (int i = 1; i < 13; i++) {
      months.add(GlobBuilder.init(Month.TYPE).set(Month.ID, Month.toYyyyMm(2007, i)).get());
    }
    for (int i = 1; i < 5; i++) {
      months.add(GlobBuilder.init(Month.TYPE).set(Month.ID, Month.toYyyyMm(2008, i)).get());
    }
    repository.reset(months, Month.TYPE);
    jFrame.add(new TimeViewPanel(repository, directory));
    return jFrame;
  }


  public void testInter() throws Exception {
    Rectangle r1 = new Rectangle(15, 15, 10, 10);
    Rectangle r2 = new Rectangle(10, 10, 10, 10);
    Rectangle rectangle = r1.intersection(r2);
    System.out.println("TimeViewPanelTest.testInter " + rectangle);
  }

  public static void main(String[] args) {
    initPanel(new DefaultDirectory(), GlobRepositoryBuilder.init().get()).setVisible(true);
  }

  private MouseEvent getMouseEvent(int x, int y) {
    return new MouseEvent(new JLabel(), 12, 0, 0, x * 10 + 3, y * 10 + 3, 0, false);
  }

  private MouseEvent getCtrlMouseEvent(int x, int y) {
    return new MouseEvent(new JLabel(), 12, 0, MouseEvent.CTRL_DOWN_MASK | MouseEvent.BUTTON1_DOWN_MASK,
                          x * 10 + 3, y * 10 + 3, 0, false);
  }

  private MouseEvent getShiftMouseEvent(int x, int y) {
    return new MouseEvent(new JLabel(), 12, 0, MouseEvent.SHIFT_DOWN_MASK | MouseEvent.BUTTON1_DOWN_MASK,
                          x * 10 + 3, y * 10 + 3, 0, false);
  }

  private static class DummySelectableContainer implements SelectableContainer {
    protected Set<Selectable> selectables = new HashSet<Selectable>();
    private Selectable[][] select;
    private Selectable selected;

    public DummySelectableContainer() {
      select = new Selectable[3][10];
      Selectable[] selectables1 = select[1];
      for (int i = 1; i < selectables1.length - 1; i++) {
        selectables1[i] = new DummySelectable(i);
      }
    }

    public Set<Selectable> getCurrentlySelectedToUpdate() {
      return selectables;
    }

    public Selectable getSelectable(int x, int y) {
      return select[y / 10][x / 10];
    }

    public void repaint() {

    }

    public Selectable getLastSelected() {
      return selected;
    }

    public void setLastSeletected(Selectable selectable) {
      selected = selectable;
    }

    private void checkSelected(int... selectedPos) {
      int i;
      for (i = 0; i < selectedPos.length; i++) {
        assertEquals("On " + i, ((DummySelectable)select[1][i + 1]).selected, selectedPos[i] != 0);
      }
      i++;
      while (i < select[1].length - 1) {
        assertEquals("on " + i, false, ((DummySelectable)select[1][i]).selected);
        i++;
      }
    }

    private class DummySelectable implements Selectable {
      boolean selected = false;
      private int pos;

      public DummySelectable(int pos) {
        this.pos = pos;
      }

      public void select() {
        selected = true;
      }

      public void unSelect() {
        selected = false;
      }

      public void inverseSelect() {
        selected = !selected;
      }

      public String getCommonParent() {
        return "unique";
      }

      public void getSelectedGlobs(Collection<Glob> selected) {
      }

      public Visibility isVisible() {
        return Visibility.FULLY;
      }

      public Selectable getLeft() {
        if (pos == 1) {
          return null;
        }
        return select[1][pos - 1];
      }

      public Selectable getRight() {
        if (pos == select[1].length - 1) {
          return null;
        }
        return select[1][pos + 1];
      }

      public boolean equals(Object o) {
        if (this == o) {
          return true;
        }
        if (o == null || getClass() != o.getClass()) {
          return false;
        }

        DummySelectable that = (DummySelectable)o;

        if (pos != that.pos) {
          return false;
        }

        return true;
      }

      public int hashCode() {
        return pos;
      }

      public String toString() {
        return "" + pos;
      }
    }
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
          wait(10000);
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
