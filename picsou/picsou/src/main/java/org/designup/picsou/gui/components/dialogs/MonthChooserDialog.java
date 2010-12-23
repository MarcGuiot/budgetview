package org.designup.picsou.gui.components.dialogs;

import org.designup.picsou.gui.time.TimeService;
import org.designup.picsou.gui.components.MonthRangeBound;
import org.designup.picsou.model.Month;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.splits.SplitsBuilder;
import org.globsframework.gui.splits.color.ColorChangeListener;
import org.globsframework.gui.splits.color.ColorLocator;
import org.globsframework.gui.splits.color.ColorService;
import org.globsframework.gui.splits.repeat.RepeatCellBuilder;
import org.globsframework.gui.splits.repeat.RepeatComponentFactory;
import org.globsframework.gui.splits.utils.Disposable;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.*;
import java.util.List;

public class MonthChooserDialog implements ColorChangeListener, Disposable {

  private JLabel nextYearLabel = new JLabel();
  private JLabel previousYearLabel = new JLabel();
  private JLabel currentYearLabel = new JLabel();
  List<MonthsComponentFactory> monthsComponentFactories = new ArrayList<MonthsComponentFactory>();
  private PicsouDialog dialog;
  private int selectedYear;
  private int selectedMonth = -1;
  private int currentYear;
  private Directory directory;
  private MonthRangeBound bound;
  private int yearLowerLimit;
  private int monthLowerLimit;
  private int yearUpperLimit;
  private int monthUpperLimit;
  private int newMonth;
  private Color todayColor;
  private Color defaultForegroundColor;
  private Set<Integer> forceDesable = new HashSet<Integer>();
  private SplitsBuilder builder;

  public MonthChooserDialog(Window parent, final Directory directory) {
    this.directory = directory;
    this.directory.get(ColorService.class).addListener(this);
    JPanel panel = createPanel();
    this.dialog = PicsouDialog.createWithButton(parent, panel,
                                                new CancelAction(), directory);
  }

  private JPanel createPanel() {
    builder = new SplitsBuilder(directory);
    builder.setSource(MonthChooserDialog.class, "/layout/utils/monthChooserDialog.splits");
    builder.add("previousYearLabel", previousYearLabel);
    builder.add("currentYearLabel", currentYearLabel);
    builder.add("nextYearLabel", nextYearLabel);
    addMonthsPanel("previousYearMonths", builder, this, -1);
    addMonthsPanel("currentYearMonths", builder, this, 0);
    addMonthsPanel("nextYearMonths", builder, this, 1);

    builder.add("previousYearAction", new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        currentYear--;
        update();
      }
    });
    builder.add("nextYearAction", new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        currentYear++;
        update();
      }
    });
    builder.add("previousPageAction", new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        currentYear -= 3;
        update();
      }
    });
    builder.add("nextPageAction", new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        currentYear += 3;
        update();
      }
    });
    builder.add("homeYearAction", new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        currentYear = selectedYear;
        update();
      }
    });

    builder.addDisposable(this);
    return builder.load();
  }

  public int show(int selectedMonthId, int lowerLimit, int upperLimit, Collection<Integer> forceDesables) {
    this.newMonth = -1;
    bound = MonthRangeBound.BOTH;
    this.selectedMonth = Month.toMonth(selectedMonthId);
    this.selectedYear = Month.toYear(selectedMonthId);
    initBoundLimit(MonthRangeBound.UPPER, lowerLimit);
    initBoundLimit(MonthRangeBound.LOWER, upperLimit);
    currentYear = selectedYear;
    this.forceDesable.addAll(forceDesables);
    return show();
  }

  public int show(int selectedMonthId, MonthRangeBound bound, int limitMonthId) {
    this.newMonth = -1;
    this.bound = bound;
    this.selectedMonth = Month.toMonth(selectedMonthId);
    this.selectedYear = Month.toYear(selectedMonthId);
    initBoundLimit(bound, limitMonthId);
    return show();
  }

  private int show() {
    update();
    dialog.pack();
    dialog.showCentered();
    builder.dispose();
    dialog = null;
    return newMonth;
  }

  private void initBoundLimit(MonthRangeBound bound, int limitMonthId) {
    switch (bound) {
      case NONE:
        this.currentYear = selectedYear;
        break;
      case LOWER:
        this.yearLowerLimit = Month.toYear(limitMonthId);
        this.monthLowerLimit = Month.toMonth(limitMonthId);
        this.currentYear = yearLowerLimit - 1;
        break;
      case UPPER:
        this.yearUpperLimit = Month.toYear(limitMonthId);
        this.monthUpperLimit = Month.toMonth(limitMonthId);
        this.currentYear = yearUpperLimit + 1;
        break;
    }
  }

  private void update() {
    previousYearLabel.setText(Integer.toString(currentYear - 1));
    currentYearLabel.setText(Integer.toString(currentYear));
    nextYearLabel.setText(Integer.toString(currentYear + 1));
    for (MonthsComponentFactory factory : monthsComponentFactories) {
      factory.setCurrentYear(currentYear);
    }
  }

  private void set(int monthId) {
    newMonth = monthId;
    dialog.setVisible(false);
  }

  private void addMonthsPanel(String name, SplitsBuilder builder, MonthChooserDialog selection, int index) {
    MonthsComponentFactory monthsComponentFactory =
      new MonthsComponentFactory(selection, index);
    builder.addRepeat(name, Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12),
                      monthsComponentFactory);
    monthsComponentFactories.add(monthsComponentFactory);
  }

  public void colorsChanged(ColorLocator colorLocator) {
    todayColor = colorLocator.get("monthChooser.today");
    defaultForegroundColor = colorLocator.get("monthChooser.text");
    update();
  }

  public void dispose() {
    this.directory.get(ColorService.class).removeListener(this);
  }

  private class MonthsComponentFactory implements RepeatComponentFactory<Integer> {
    private MonthChooserDialog selection;
    private int index;
    private int currentYear;
    JToggleButton[] buttons = new JToggleButton[12];

    public MonthsComponentFactory(MonthChooserDialog selection, int index) {
      this.selection = selection;
      this.index = index;
    }

    public void registerComponents(RepeatCellBuilder cellBuilder, final Integer item) {
      AbstractAction action = new AbstractAction(Month.getShortMonthLabel(item)) {
        public void actionPerformed(ActionEvent e) {
          selection.set(Month.toMonthId(currentYear, item));
        }
      };
      buttons[item - 1] = new JToggleButton(action);
      buttons[item - 1].setName(Integer.toString(item));
      cellBuilder.add("month", buttons[item - 1]);
    }

    public void setCurrentYear(int currentYear) {
      this.currentYear = currentYear + index;
      updateButton();
    }

    public void updateButton() {
      int todayId = directory.get(TimeService.class).getCurrentMonthId();
      for (int i = 0; i < buttons.length; i++) {
        buttons[i].setSelected(currentYear == selectedYear && selectedMonth == i + 1);
        int currentMonthId = Month.toMonthId(currentYear, i + 1);
        if (currentYear != selectedYear || selectedMonth != i + 1) {
          switch (bound) {
            case LOWER:
              buttons[i].setEnabled(currentMonthId <= Month.toMonthId(yearLowerLimit, monthLowerLimit));
              break;
            case UPPER:
              buttons[i].setEnabled(currentMonthId >= Month.toMonthId(yearUpperLimit, monthUpperLimit));
              break;
            case BOTH:
              buttons[i].setEnabled(currentMonthId >= Month.toMonthId(yearUpperLimit, monthUpperLimit) &&
                                    currentMonthId <= Month.toMonthId(yearLowerLimit, monthLowerLimit));
          }
          if (forceDesable.contains(currentMonthId)) {
            buttons[i].setEnabled(false);
          }
        }
        if (todayId == currentMonthId) {
          buttons[i].setForeground(todayColor);
        }
        else {
          buttons[i].setForeground(defaultForegroundColor);
        }
      }
    }
  }

  private class CancelAction extends AbstractAction {
    private CancelAction() {
      super(Lang.get("cancel"));
    }

    public void actionPerformed(ActionEvent e) {
      selectedMonth = -1;
      dialog.setVisible(false);
    }
  }
}
