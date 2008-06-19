package org.designup.picsou.gui.time;

import org.designup.picsou.gui.time.selectable.*;
import org.designup.picsou.model.Month;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.SelectionService;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class TimeViewPanel extends JPanel implements MouseListener, MouseMotionListener, KeyListener, FocusListener,
                                                     SelectableContainer,
                                                     ChangeSetListener, GlobSelectionListener {

  private static final Dimension DIMENSION = new Dimension(100, 45);

  private TimeGraph timeGraph;
  private Set<Selectable> currentlySelected = new TreeSet<Selectable>();
  private MouseState currentState = new ReleasedMouseState(this);
  private SelectionService selectionService;
  private GlobRepository repository;
  private MonthViewColors colors;
  private Selectable selected;
  int translation;
  private long id = 0;
  private ScrollAndRepaint scrollRunnable = new ScrollAndRepaint();
  private Timer timer = new Timer(250, new ActionListener() {
    public void actionPerformed(ActionEvent e) {
      SwingUtilities.invokeLater(scrollRunnable);
    }
  });
  private int previousWidth = -1;

  public TimeViewPanel(GlobRepository globRepository, Directory directory) {
    this.repository = globRepository;
    colors = new MonthViewColors(directory);

    GlobList list = globRepository.getAll(Month.TYPE).sort(Month.ID);
    timeGraph = new TimeGraph(list, colors);
    selectionService = directory.get(SelectionService.class);
    setName("MonthSelector");
    globRepository.addChangeListener(this);
    enableEvents(AWTEvent.KEY_EVENT_MASK);
    selectionService.addListener(this, Month.TYPE);
    setFocusable(true);
    addMouseListener(this);
    addMouseMotionListener(this);
    addKeyListener(this);
    setPreferredSize(DIMENSION);
    setMinimumSize(DIMENSION);
    addFocusListener(this);
  }

  public void paintComponent(Graphics g) {
    System.out.println("TimeViewPanel.paintComponent h=" + getHeight() + " w= " + getWidth());
    if (previousWidth > 0 && getWidth() > previousWidth && translation < 0) {
      translation += getWidth() - previousWidth;
      if (translation > 0) {
        translation = 0;
      }
    }
    previousWidth = getWidth();
    Graphics2D d = (Graphics2D)g.create();
    try {
      d.setPaint(new GradientPaint(0, 0, colors.backgroundTop, 0, getHeight(), colors.backgroundBottom));
      d.fillRect(0, 0, getWidth(), getHeight());
      TransformationAdapter transformationAdapter = new TransformationAdapter(d);
      System.out.println("TimeViewPanel.paintComponent " + translation);
      transformationAdapter.translate(translation, 0);
      timeGraph.draw(d, transformationAdapter, getHeight(), getWidth());
    }
    finally {
      d.dispose();
    }
  }

  public void mouseClicked(MouseEvent e) {
  }

  public void mousePressed(MouseEvent e) {
    id++;
    currentState = currentState.mousePressed(e);
    repaint();
  }

  public void mouseReleased(MouseEvent e) {
    currentState = currentState.mouseReleased(e);
    sendSelectionEvent(false);
    repaint();
  }

  private void sendSelectionEvent(boolean updateLastSelected) {
    List<Glob> selectedGlob = new ArrayList<Glob>();
    for (Selectable selectable : currentlySelected) {
      selectable.getSelectedGlobs(selectedGlob);
      if (updateLastSelected) {
        setLastSeletected(selectable);
      }
    }
    selectionService.select(selectedGlob, Month.TYPE);
  }

  public void mouseEntered(MouseEvent e) {
  }

  public void mouseExited(MouseEvent e) {
  }

  public void mouseDragged(MouseEvent e) {
    currentState = currentState.mouseMoved(e);
    if (e.getPoint().getX() < 0) {
      if (scrollLeft()) {
        scrollRunnable.set(id, e);
        timer.stop();
        timer.start();
      }
      else {
        timer.stop();
      }
    }
    else if (e.getPoint().getX() > getWidth()) {
      if (scrollRigth()) {
        scrollRunnable.set(id, e);
        timer.stop();
        timer.start();
      }
      else {
        timer.stop();
      }

    }
    else {
      timer.stop();
      id++;
    }
    repaint();
  }

  private boolean scrollRigth() {
    Selectable selected = timeGraph.getLastSelectable();
    if (!selected.isVisible().equals(Selectable.Visibility.FULLY)) {
      translation -= 10;
      if (-translation + getWidth() > timeGraph.getWidth()) {
        translation = getWidth() - timeGraph.getWidth();
        return false;
      }
      else {
        return true;
      }
    }
    return false;
  }

  private boolean scrollLeft() {
    Selectable selected = timeGraph.getFirstSelectable();
    if (!selected.isVisible().equals(Selectable.Visibility.FULLY)) {
      translation += 10;
      if (translation > 0) {
        translation = 0;
        return false;
      }
      else {
        return true;
      }
    }
    return false;
  }

  public void mouseMoved(MouseEvent e) {
  }

  public Set<Selectable> getCurrentlySelectedToUpdate() {
    return currentlySelected;
  }

  public Selectable getSelectable(int x, int y) {
    return timeGraph.getSelectableAt(x, y);
  }

  public Selectable getLastSelected() {
    return selected;
  }

  public void setLastSeletected(Selectable selectable) {
    selected = selectable;
  }

  public void globsChanged(ChangeSet changeSet, GlobRepository globRepository) {
    if (changeSet.containsChanges(Month.TYPE)) {
      reloadMonth();
      repaint();
    }
  }

  private void reloadMonth() {
    GlobList list = repository.getAll(Month.TYPE).sort(Month.ID);
    timeGraph = new TimeGraph(list, colors);
    selectLastMonth();
  }

  public void globsReset(GlobRepository globRepository, List<GlobType> changedTypes) {
    reloadMonth();
    repaint();
  }

  public void selectionUpdated(GlobSelection selection) {
    clearSelection();
    timeGraph.selectMonth(selection.getAll(Month.TYPE), currentlySelected);
    repaint();
  }

  private void clearSelection() {
    for (Selectable selectable : currentlySelected) {
      selectable.unSelect();
    }
    currentlySelected.clear();
  }

  private void selectFirstMonth() {
    clearSelection();
    timeGraph.selectFirstMonth(currentlySelected);
    sendSelectionEvent(true);
    repaint();
  }

  public void selectLastMonth() {
    clearSelection();
    timeGraph.selectLastMonth(currentlySelected);
    sendSelectionEvent(true);
    repaint();
  }

  public void selectMonth(int... indexes) {
    clearSelection();
    timeGraph.selectMonth(indexes, currentlySelected);
    sendSelectionEvent(true);
    repaint();
  }

  public void selectAll() {
    clearSelection();
    timeGraph.selectAll(currentlySelected);
    sendSelectionEvent(true);
    repaint();
  }

  public GlobRepository getRepository() {
    return repository;
  }

  public void getAllSelectableMonth(GlobList globs) {
    timeGraph.getAllSelectableMonth(globs);
  }

  public void keyTyped(KeyEvent e) {
  }

  public void keyPressed(KeyEvent e) {
  }

  public void keyReleased(KeyEvent e) {
  }

  public void goToFirst() {
    selectFirstMonth();
  }

  public void goToLast() {
    selectLastMonth();
  }

  public void goToPrevious() {
    scrollLeft();
    repaint();
  }

  public void goToNext() {
    scrollRigth();
    repaint();
  }

  public void focusGained(FocusEvent e) {
  }

  public void focusLost(FocusEvent e) {
  }

  private class ScrollAndRepaint implements Runnable {
    private long id;
    private MouseEvent mouseEvent;

    public ScrollAndRepaint() {
    }

    public void run() {
      if (this.id == TimeViewPanel.this.id) {
        mouseDragged(mouseEvent);
      }
    }

    public void set(long id, MouseEvent e) {
      this.id = id;
      this.mouseEvent = e;
    }
  }
}
