package org.designup.picsou.gui.time;

import org.designup.picsou.gui.TimeService;
import org.designup.picsou.gui.model.BalanceStat;
import org.designup.picsou.gui.time.mousestates.MouseState;
import org.designup.picsou.gui.time.mousestates.ReleasedMouseState;
import org.designup.picsou.gui.time.selectable.Selectable;
import org.designup.picsou.gui.time.selectable.SelectableContainer;
import org.designup.picsou.gui.time.selectable.TransformationAdapter;
import org.designup.picsou.gui.time.tooltip.TimeViewMouseHandler;
import org.designup.picsou.gui.time.tooltip.TimeViewTooltipHandler;
import org.designup.picsou.gui.time.utils.TimeViewColors;
import org.designup.picsou.model.AccountBalanceLimit;
import org.designup.picsou.model.Month;
import org.designup.picsou.model.UserPreferences;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.SelectionService;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

public class TimeViewPanel extends JPanel implements MouseListener, MouseMotionListener,
                                                     SelectableContainer,
                                                     ChangeSetListener, GlobSelectionListener, PositionProvider {

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
  private TimeViewColors colors;
  private Selectable selected;
  private int translation;
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
  private VisibilityListener visibilityListener;
  private TimeViewMouseHandler mouseOverHandler;
  private Selectable lastMouseOverSelectable = null;

  public TimeViewPanel(GlobRepository repository, Directory directory) {
    setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    this.repository = repository;
    timeService = directory.get(TimeService.class);
    Font monthFont = getFont();
    Font yearFont = monthFont.deriveFont((float)monthFont.getSize() - 2);
    colors = new TimeViewColors(directory, yearFont, monthFont);
    timeGraph = new TimeGraph(repository.getAll(Month.TYPE).sort(Month.ID),
                              colors, timeService,
                              getFontMetrics(yearFont), getFontMetrics(monthFont), this);
    selectionService = directory.get(SelectionService.class);
    setName("MonthSelector");
    repository.addChangeListener(this);
    enableEvents(AWTEvent.KEY_EVENT_MASK);

    selectionService.addListener(this, Month.TYPE, UserPreferences.TYPE);

    setFocusable(true);
    addMouseListener(this);
    addMouseMotionListener(this);
    Dimension dimension = new Dimension(50, timeGraph.getAbsoluteHeight());
    setMinimumSize(dimension);
    setPreferredSize(dimension);
    setOpaque(false);

    mouseOverHandler = new TimeViewTooltipHandler(this, this.repository, colors);
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
    if (previousWidth != getWidth()) {
      timeGraph.init(getWidth());
    }
    previousWidth = getWidth();

    Graphics2D g2d = (Graphics2D)g.create();
    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    try {
      if (pendingOperation != null) {
        pendingOperation.run();
        pendingOperation = null;
      }
      TransformationAdapter transformationAdapter = new TransformationAdapter(g2d);
      transformationAdapter.translate(translation, 0);
      timeGraph.draw(g2d, transformationAdapter, getWidth(), getHeight());
    }
    finally {
      g2d.dispose();
    }

    if (shouldScroll) {
      scrollToLastVisible();
      repaint();
    }
    synchronized (this) {
      paintCount++;
      this.notify();
    }
    visibilityListener.change(timeGraph.getFirstSelectable(), timeGraph.getLastSelectable());
  }

  public void register(VisibilityListener visibilityListener) {
    this.visibilityListener = visibilityListener;
  }

  public Double getPosition(int monthId) {
    Glob balance = repository.find(Key.create(BalanceStat.TYPE, monthId));
    if (balance == null) {
      return 0.0;
    }
    return balance.get(BalanceStat.END_OF_MONTH_ACCOUNT_BALANCE);
  }

  public double getPositionLimit(int monthId) {
    return AccountBalanceLimit.getLimit(repository);
  }

  public interface VisibilityListener {
    void change(Selectable first, Selectable last);
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
        setLastSelected(selectable);
      }
    }
    selectionService.select(selectedGlob, Month.TYPE);
  }

  public void mouseEntered(MouseEvent e) {
    lastMouseOverSelectable = timeGraph.getSelectableAt(e.getX(), e.getY());
    sendMouseOverEvent();
  }

  public void mouseExited(MouseEvent e) {
    mouseOverHandler.leave();
    lastMouseOverSelectable = null;
  }

  public void mouseMoved(MouseEvent e) {
    if (lastMouseOverSelectable == null) {
      return;
    }
    Selectable newSelectable = timeGraph.getSelectableAt(e.getX(), e.getY());
    if (newSelectable == lastMouseOverSelectable) {
      return;
    }
    lastMouseOverSelectable = newSelectable;
    sendMouseOverEvent();
  }

  private void sendMouseOverEvent() {
    if (lastMouseOverSelectable instanceof MonthGraph) {
      mouseOverHandler.enterMonth(((MonthGraph)lastMouseOverSelectable).getMonth().get(Month.ID));
    }
    else if (lastMouseOverSelectable instanceof YearGraph) {
      mouseOverHandler.enterYear(((YearGraph)lastMouseOverSelectable).getYear());
    }
  }

  // For tests only
  public TimeViewMouseHandler getMouseOverHandler() {
    return mouseOverHandler;
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
      if (scrollRight(timeGraph.getMonthWidth())) {
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

  private boolean scrollRight(int shift) {
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

  public void setLastSelected(Selectable selectable) {
    selected = selectable;
  }

  public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
    if (changeSet.containsChanges(Month.TYPE)) {
      reloadMonth();
      repaint();
      return;
    }
    if (changeSet.containsChanges(BalanceStat.TYPE) || changeSet.containsChanges(AccountBalanceLimit.TYPE)) {
      repaint();
    }
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
    reloadMonth();
    repaint();
  }

  private void reloadMonth() {
    GlobList list = repository.getAll(Month.TYPE).sort(Month.ID);
    timeGraph = new TimeGraph(list, colors, timeService, getFontMetrics(colors.getYearFont()),
                              getFontMetrics(colors.getMonthFont()), this);
    timeGraph.init(getWidth());
    GlobList selectedMonth = selectionService.getSelection(Month.TYPE);
    GlobList stillThere = new GlobList();
    for (Glob glob : list) {
      if (selectedMonth.remove(glob)) {
        stillThere.add(glob);
      }
    }
    if (stillThere.isEmpty()) {
      selectionService.clear(Month.TYPE);
    }
    else {
      selectionService.select(stillThere, Month.TYPE);
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
    scrollRight(count * timeGraph.getMonthWidth());
  }

  public void selectMonth(int index) {
    clearSelection();
    timeGraph.selectMonth(new int[]{index}, currentlySelected);
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

  public void gotoFirst() {
    do {
    }
    while (scrollLeft(timeGraph.getMonthWidth()));
    repaint();
  }

  public void gotoLast() {
    do {
    }
    while (scrollRight(timeGraph.getMonthWidth()));
    repaint();
  }

  public void gotoPrevious() {
    scrollLeft(timeGraph.getYearWeigth());
    repaint();
  }

  public void gotoNext() {
    scrollRight(timeGraph.getYearWeigth());
    repaint();
  }

  public TimeGraph getTimeGraph() {
    return timeGraph;
  }

  public synchronized void savePaintCount() {
    currentPaintCount = paintCount;
  }

  public synchronized void waitRepaint() {
    long milli = System.currentTimeMillis() + 100;
    while (currentPaintCount == paintCount) {
      try {
        long duration = milli - System.currentTimeMillis();
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
