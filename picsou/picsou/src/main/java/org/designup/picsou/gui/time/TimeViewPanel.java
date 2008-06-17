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
    currentState = currentState.mousePressed(e);
    repaint();
  }

  public void mouseReleased(MouseEvent e) {
    currentState = currentState.mouseReleased(e);
    sendSelectionEvent(false);
    repaint();
  }

  private void sendSelectionEvent(boolean updateLastSelected) {
    List<Glob> selected = new ArrayList<Glob>();
    for (Selectable selectable : currentlySelected) {
      selectable.getSelectedGlobs(selected);
      if (updateLastSelected) {
        setLastSeletected(selectable);
      }
    }
    selectionService.select(selected, Month.TYPE);
  }

  public void mouseEntered(MouseEvent e) {
  }

  public void mouseExited(MouseEvent e) {
  }

  public void mouseDragged(MouseEvent e) {
    currentState = currentState.mouseMoved(e);
    Selectable selected = getLastSelected();
    if (selected != null && !selected.isVisible().equals(Selectable.Visibility.FULLY)) {
      int leftCount = findLeftFirstVisible(selected);
      int rightCount = findRightFirstVisible(selected);
//      if (leftCount != rightCount) {
      if (leftCount == 0) {
        translation += 10;
      }
      else {
        translation -= 10;
      }
//      }
    }
    repaint();
  }

  private int findLeftFirstVisible(Selectable lastSelected) {
    int count = 1;
    Selectable selectable = lastSelected.getLeft();
    while (selectable != null && !selectable.isVisible().equals(Selectable.Visibility.FULLY)) {
      selectable = selectable.getLeft();
      count++;
    }
    if (selectable != null) {
      return count;
    }
    return 0;
  }

  private int findRightFirstVisible(Selectable lastSelected) {
    int count = 0;
    Selectable selectable = lastSelected.getRight();
    while (selectable != null && !selectable.isVisible().equals(Selectable.Visibility.FULLY)) {
      selectable = selectable.getRight();
      count++;
    }
    if (selectable != null) {
      return count;
    }
    return 0;
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
    Selectable selectable = timeGraph.getLastSelectable();
    if (selectable != null && !selectable.isVisible().equals(Selectable.Visibility.FULLY)) {
      translation += 10;
      repaint();
    }
  }

  private void scrollToVisible(Selectable next) {
    int left = findLeftFirstVisible(next);
    int right = findRightFirstVisible(next);
    if (left != 0) {
      translation += left * 10;
    }
    if (right != 0) {
      translation -= right * 10;
    }
    repaint();
  }

  public void goToNext() {
    Selectable selectable = timeGraph.getFirstSelectable();
    if (selectable != null && !selectable.isVisible().equals(Selectable.Visibility.FULLY)) {
      translation -= 10;
      repaint();
    }
  }

  public void focusGained(FocusEvent e) {
  }

  public void focusLost(FocusEvent e) {
  }
}
