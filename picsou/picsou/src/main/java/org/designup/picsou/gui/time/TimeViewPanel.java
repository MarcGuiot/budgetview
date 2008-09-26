package org.designup.picsou.gui.time;

import org.designup.picsou.gui.TimeService;
import org.designup.picsou.gui.time.selectable.*;
import org.designup.picsou.model.Month;
import org.designup.picsou.model.UserPreferences;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.SelectionService;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

public class TimeViewPanel extends JPanel implements MouseListener, MouseMotionListener, KeyListener, FocusListener,
                                                     SelectableContainer,
                                                     ChangeSetListener, GlobSelectionListener {

  private TimeGraph timeGraph;
  private Set<Selectable> currentlySelected = new TreeSet<Selectable>(new Comparator<Selectable>() {
    public int compare(Selectable o1, Selectable o2) {
      if (o1 instanceof YearGraph) {
        return 1;
      }
      if (o2 instanceof YearGraph) {
        return -1;
      }
      return ((MonthGraph)o1).compareTo(((MonthGraph)o2));
    }
  });
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
  private Runnable pendingOperation;
  private int previousWidth = -1;
  private int paintCount = 0;
  private int currentPaintCount = 0;
  private TimeService timeService;
  private boolean isRegisteredUser;

  public TimeViewPanel(GlobRepository globRepository, Directory directory) {
    this.repository = globRepository;
    timeService = directory.get(TimeService.class);
    Font monthFont = getFont();
    Font yearFont = monthFont.deriveFont((float)monthFont.getSize() - 2);
    colors = new MonthViewColors(directory, yearFont, monthFont);
    isRegisteredUser = repository.get(UserPreferences.KEY).get(UserPreferences.REGISTRED_USER);
    GlobList list = globRepository.getAll(Month.TYPE).sort(Month.ID);
    filterMonth(list);
    timeGraph = new TimeGraph(list, colors, timeService, getFontMetrics(yearFont), getFontMetrics(monthFont));
    selectionService = directory.get(SelectionService.class);
    setName("MonthSelector");
    globRepository.addChangeListener(this);
    enableEvents(AWTEvent.KEY_EVENT_MASK);

    selectionService.addListener(this, Month.TYPE, UserPreferences.TYPE);

    setFocusable(true);
    addMouseListener(this);
    addMouseMotionListener(this);
    addKeyListener(this);
    Dimension dimension = new Dimension(50, timeGraph.getTotalHeight());
    setMinimumSize(dimension);
    setPreferredSize(dimension);
    addFocusListener(this);
    setOpaque(false);
  }

  public void paintComponent(Graphics g) {
    if (previousWidth > 0 && getWidth() > previousWidth && translation < 0) {
      translation += getWidth() - previousWidth;
      if (translation > 0) {
        translation = 0;
      }
    }
    boolean shouldScroll = false;
    if (getWidth() < previousWidth) {
      shouldScroll = true;
    }
    previousWidth = getWidth();
    Graphics2D d = (Graphics2D)g.create();
    d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    try {
      timeGraph.init(getWidth());
      if (pendingOperation != null) {
        pendingOperation.run();
        pendingOperation = null;
      }
//      d.setPaint(new GradientPaint(0, 0, colors.pastBackgroundTop, 0,
//                                   timeGraph.getMonthHeight(), colors.pastBackgroundBottom));
      TransformationAdapter transformationAdapter = new TransformationAdapter(d);
      transformationAdapter.translate(translation, 0);
      timeGraph.draw(d, transformationAdapter, getWidth(), getHeight());
    }
    finally {
      d.dispose();
    }
    if (shouldScroll) {
      scrollToLastVisible();
      repaint();
    }
    synchronized (this) {
      paintCount++;
      this.notify();
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
    timer.stop();
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
      if (scrollLeft(timeGraph.getMonthWidth())) {
        scrollRunnable.set(id, e);
        timer.stop();
        timer.start();
      }
      else {
        timer.stop();
      }
    }
    else if (e.getPoint().getX() > getWidth()) {
      if (scrollRigth(timeGraph.getMonthWidth())) {
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

  private boolean scrollRigth(int shift) {
    Selectable selected = timeGraph.getLastSelectable();
    if (!selected.isVisible().equals(Selectable.Visibility.FULLY)) {
      translation -= shift;
      if (-translation + getWidth() > timeGraph.getWidth()) {
        translation = getWidth() - timeGraph.getWidth();
        if (translation > 0) {
          translation = 0;
        }
        return false;
      }
      else {
        return true;
      }
    }
    return false;
  }

  private boolean scrollLeft(int shift) {
    Selectable selected = timeGraph.getFirstSelectable();
    if (!selected.isVisible().equals(Selectable.Visibility.FULLY)) {
      translation += shift;
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
    if (y >= getWidth()) {
      return timeGraph.getSelectableAt(x, getWidth() + timeGraph.getMonthWidth());
    }
    if (y < 0) {
      return timeGraph.getSelectableAt(x, -timeGraph.getMonthWidth());
    }
    return timeGraph.getSelectableAt(x, y);
  }

  public Selectable getLastSelected() {
    return selected;
  }

  public void setLastSeletected(Selectable selectable) {
    selected = selectable;
  }

  public void globsChanged(ChangeSet changeSet, GlobRepository globRepository) {
    if (changeSet.containsChanges(UserPreferences.KEY)) {
      if (repository.get(UserPreferences.KEY).get(UserPreferences.REGISTRED_USER) != isRegisteredUser) {
        isRegisteredUser = !isRegisteredUser;
        reloadMonth();
        repaint();
        return;
      }
    }
    if (changeSet.containsChanges(Month.TYPE)) {
      reloadMonth();
      repaint();
    }
  }

  public void globsReset(GlobRepository globRepository, Set<GlobType> changedTypes) {
    isRegisteredUser = repository.get(UserPreferences.KEY).get(UserPreferences.REGISTRED_USER);
    reloadMonth();
    repaint();
  }

  private void reloadMonth() {
    GlobList list = repository.getAll(Month.TYPE).sort(Month.ID);
    filterMonth(list);
    timeGraph = new TimeGraph(list, colors, timeService, getFontMetrics(colors.getYearFont()),
                              getFontMetrics(colors.getMonthFont()));
  }

  private void filterMonth(GlobList list) {
    if (!isRegisteredUser) {
      list.filterSelf(GlobMatchers.fieldLesserOrEqual(Month.ID, timeService.getCurrentMonthId()), repository);
    }
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
    scrollToLastVisible();
    repaint();
  }

  private void scrollToLastVisible() {
    if (previousWidth == -1) {
      pendingOperation = new Runnable() {
        public void run() {
          scrollToLastVisible();
        }
      };
      return;
    }
    if (currentlySelected.isEmpty()) {
      return;
    }
    Selectable lastSelected = null;
    for (Selectable selected : currentlySelected) {
      lastSelected = selected;
    }
    if (lastSelected == null || lastSelected.isVisible().equals(Selectable.Visibility.FULLY)) {
      return;
    }
    Selectable mostLeftSelectable = timeGraph.getFirstSelectable();
    int count = 1;
    while (mostLeftSelectable != lastSelected) {
      mostLeftSelectable = mostLeftSelectable.getRight();
      count++;
    }
    scrollRigth(count * timeGraph.getMonthWidth());
  }

  public void selectMonth(int... indexes) {
    clearSelection();
    timeGraph.selectMonth(indexes, currentlySelected);
    sendSelectionEvent(true);
    repaint();
  }

  public void selectMonth(Set<Integer> monthIds) {
    clearSelection();
    timeGraph.selectMonth(monthIds, currentlySelected);
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
    do {
    }
    while (scrollLeft(timeGraph.getMonthWidth()));
    repaint();
  }

  public void goToLast() {
    do {
    }
    while (scrollRigth(timeGraph.getMonthWidth()));
    repaint();
  }

  public void goToPrevious() {
    scrollLeft(timeGraph.getYearWeigth());
    repaint();
  }

  public void goToNext() {
    scrollRigth(timeGraph.getYearWeigth());
    repaint();
  }

  public void focusGained(FocusEvent e) {
  }

  public void focusLost(FocusEvent e) {
  }

  public TimeGraph getTimeGraph() {
    return timeGraph;
  }

  public synchronized void savePaintPoint() {
    currentPaintCount = paintCount;
  }

  public synchronized void waitRepaint() {
    long mili = System.currentTimeMillis() + 100;
    while (currentPaintCount == paintCount) {
      try {
        long duration = mili - System.currentTimeMillis();
        if (duration > 0) {
          wait(duration);
        }
        else {
          return;
        }
      }
      catch (InterruptedException e) {
      }
    }
    currentPaintCount = paintCount;
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
