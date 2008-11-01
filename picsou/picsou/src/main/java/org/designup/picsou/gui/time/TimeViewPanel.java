package org.designup.picsou.gui.time;

import org.designup.picsou.gui.TimeService;
import org.designup.picsou.gui.model.BalanceStat;
import org.designup.picsou.gui.time.selectable.*;
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

public class TimeViewPanel extends JPanel implements MouseListener, MouseMotionListener, KeyListener, FocusListener,
                                                     SelectableContainer,
                                                     ChangeSetListener, GlobSelectionListener, BalancesProvider {

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
  double translation;
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
  private VisibilityListener visibilityListener = new VisibilityListener() {
    public void change(Selectable first, Selectable last) {
    }
  };
  private TooltipsHandler tooltipsHandler;
  private Selectable selectableForTooltips = null;

  public TimeViewPanel(GlobRepository globRepository, Directory directory) {
    setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    this.repository = globRepository;
    timeService = directory.get(TimeService.class);
    Font monthFont = getFont();
    Font yearFont = monthFont.deriveFont((float)monthFont.getSize() - 2);
    colors = new MonthViewColors(directory, yearFont, monthFont);
    GlobList list = globRepository.getAll(Month.TYPE).sort(Month.ID);
    timeGraph = new TimeGraph(list, colors, timeService, getFontMetrics(yearFont),
                              getFontMetrics(monthFont), this);
    selectionService = directory.get(SelectionService.class);
    setName("MonthSelector");
    globRepository.addChangeListener(this);
    enableEvents(AWTEvent.KEY_EVENT_MASK);

    selectionService.addListener(this, Month.TYPE, UserPreferences.TYPE);

    setFocusable(true);
    addMouseListener(this);
    addMouseMotionListener(this);
    addKeyListener(this);
    Dimension dimension = new Dimension(50, timeGraph.getAbsoluteHeight());
    setMinimumSize(dimension);
    setPreferredSize(dimension);
    addFocusListener(this);
    setOpaque(false);
  }

  public void paintComponent(Graphics g) {
    boolean scroll = false;
    if (previousWidth > 0 && getWidth() > previousWidth && translation < 0) {
      translation += (getWidth() - previousWidth) / 2.;
      double tmp = timeGraph.getWidth() + translation - getWidth();
      if (tmp < 0) {
        translation = translation - tmp;
      }
      if (translation > 0) {
        translation = 0;
      }
      scroll = true;
    }
    if (getWidth() < previousWidth || previousWidth == -1) {
      if (getWidth() < timeGraph.getWidth()) {
        translation += (getWidth() - previousWidth) / 2.;
      }
      scroll = true;
    }
    if (previousWidth != getWidth()) {
      timeGraph.init(getWidth());
    }
    previousWidth = getWidth();
    Graphics2D d = (Graphics2D)g.create();
    d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    try {
      if (pendingOperation != null) {
        pendingOperation.run();
        pendingOperation = null;
      }
      TransformationAdapter transformationAdapter = new TransformationAdapter(d);
      transformationAdapter.translate(translation, 0);
      timeGraph.draw(d, transformationAdapter, getWidth(), getHeight());
    }
    finally {
      d.dispose();
    }
    if (scroll && scrollToLastVisible()) {
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

  public void registerTooltips(TooltipsHandler tooltipsHandler) {
    this.tooltipsHandler = tooltipsHandler;
  }

  public Double getAccountBalance(int monthId) {
    Glob balance = repository.find(Key.create(BalanceStat.TYPE, monthId));
    if (balance == null) {
      return 0.0;
    }
    return balance.get(BalanceStat.END_OF_MONTH_ACCOUNT_BALANCE);
  }

  public double getAccountBalanceLimit(int monthId) {
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
        setLastSeletected(selectable);
      }
    }
    selectionService.select(selectedGlob, Month.TYPE);
  }

  public void mouseEntered(MouseEvent e) {
    selectableForTooltips = timeGraph.getSelectableAt(e.getX(), e.getY());
    sendTooltipEvent();
  }

  private void sendTooltipEvent() {
    if (selectableForTooltips instanceof MonthGraph) {
      tooltipsHandler.enterMonth(((MonthGraph)selectableForTooltips).getMonth().get(Month.ID));
    }
    else if (selectableForTooltips instanceof YearGraph) {
      tooltipsHandler.enterYear(((YearGraph)selectableForTooltips).getYear());
    }
  }

  public void mouseExited(MouseEvent e) {
    tooltipsHandler.leave();
    selectableForTooltips = null;
  }

  public void mouseMoved(MouseEvent e) {
    if (selectableForTooltips == null) {
      return;
    }
    Selectable newSelectable = timeGraph.getSelectableAt(e.getX(), e.getY());
    if (newSelectable == selectableForTooltips) {
      return;
    }
    selectableForTooltips = newSelectable;
    sendTooltipEvent();
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

  private boolean scrollLeft(int shift) {
    translation += shift;
    if (translation > 0) {
      translation = 0;
      return false;
    }
    else {
      return true;
    }
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
    if (changeSet.containsChanges(Month.TYPE)) {
      reloadMonth();
      repaint();
      return;
    }
    if (changeSet.containsChanges(BalanceStat.TYPE) || changeSet.containsChanges(AccountBalanceLimit.TYPE)) {
      repaint();
    }
  }

  public void globsReset(GlobRepository globRepository, Set<GlobType> changedTypes) {
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

  private boolean scrollToLastVisible() {
    if (previousWidth == -1) {
      pendingOperation = new Runnable() {
        public void run() {
          scrollToLastVisible();
        }
      };
      return false;
    }
    if (currentlySelected.isEmpty()) {
      return false;
    }
    Selectable lastSelected = null;
    for (Selectable selected : currentlySelected) {
      lastSelected = selected;
    }
    if (lastSelected == null || lastSelected.getVisibility().equals(Selectable.Visibility.FULLY)) {
      return false;
    }

    Selectable tmp = timeGraph.getLastSelectable();
    boolean visibleOnRight = false;
    while (tmp != lastSelected) {
      if (tmp.getVisibility() != Selectable.Visibility.NOT_VISIBLE) {
        visibleOnRight = true;
        break;
      }
      tmp = tmp.getLeft();
    }

    if (getWidth() / timeGraph.getMonthWidth() < 2) {
      return false;
    }
    if (visibleOnRight) {
      Selectable left = lastSelected.getRight();
      int count = 1;
      while (left != null && left.getVisibility() == Selectable.Visibility.NOT_VISIBLE) {
        left = left.getRight();
        count++;
      }
      scrollLeft(count * timeGraph.getMonthWidth());
    }
    else {
      Selectable right = lastSelected.getLeft();
      int count = 1;
      while (right != null && right.getVisibility() == Selectable.Visibility.NOT_VISIBLE) {
        right = right.getLeft();
        count++;
      }
      scrollRigth(count * timeGraph.getMonthWidth());
    }
    return true;
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
    scrollLeft(timeGraph.getYearWeigth() / 2);
    repaint();
  }

  public void goToLast() {
    scrollRigth(timeGraph.getYearWeigth() / 2);
    repaint();
  }

  public void goToPrevious() {
    scrollLeft(timeGraph.getMonthWidth());
    repaint();
  }

  public void goToNext() {
    scrollRigth(timeGraph.getMonthWidth());
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
