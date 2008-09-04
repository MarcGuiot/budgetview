package org.designup.picsou.gui.components;

import org.designup.picsou.gui.TimeService;
import org.designup.picsou.model.Month;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.splits.SplitsBuilder;
import org.globsframework.gui.splits.SplitsEditor;
import org.globsframework.gui.splits.color.ColorChangeListener;
import org.globsframework.gui.splits.color.ColorLocator;
import org.globsframework.gui.splits.color.ColorService;
import org.globsframework.gui.splits.repeat.RepeatCellBuilder;
import org.globsframework.gui.splits.repeat.RepeatComponentFactory;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MonthChooserDialog implements ColorChangeListener {
  private JLabel nextYearLabel = new JLabel();
  private JLabel previousYearLabel = new JLabel();
  private JLabel currentYearLabel = new JLabel();
  List<MonthsComponentFactory> monthsComponentFactories = new ArrayList<MonthsComponentFactory>();
  private JPanel panel;
  private PicsouDialog dialog;
  private int selectedYear;
  private int selectedMonth = -1;
  private int currentYear;
  private Directory directory;
  private int sens;
  private int yearLimit;
  private int monthLimit;
  private int newMonth;
  private Color todayColor;
  private Color defaultForegroundColor;

  public MonthChooserDialog(final Directory directory) {
    this.directory = directory;
    ColorService colorService = directory.get(ColorService.class);
    colorService.addListener(this);
    SplitsBuilder builder = new SplitsBuilder(directory);
    builder.setSource(MonthChooserDialog.class, "/layout/monthChooserDialog.splits");
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

    panel = builder.load();
  }

  public int show(Window parent, int selectedMonthId, int sens, int limitMonthId) {
    newMonth = -1;
    this.sens = sens;
    this.yearLimit = Month.toYear(limitMonthId);
    this.monthLimit = Month.toMonth(limitMonthId);
    selectedMonth = Month.toMonth(selectedMonthId);
    selectedYear = Month.toYear(selectedMonthId);
    currentYear = sens == 0 ? selectedYear : sens == -1 ? yearLimit - 1 : yearLimit + 1;
    dialog = PicsouDialog.createWithButton(Lang.get("month.chooser.title"), parent, panel, new CancelAction(), directory);
    update();
    dialog.pack();
    GuiUtils.showCentered(dialog);
    dialog = null;
    return newMonth;
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
    todayColor = colorLocator.get("month.chooser.today.color");
    defaultForegroundColor = colorLocator.get("toggle.selector.normal");
    update();
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
      AbstractAction action = new AbstractAction(Month.getMediumSizeLetterLabel(item)) {
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
        if (sens < 0) {
          boolean b = currentMonthId <= Month.toMonthId(yearLimit, monthLimit);
          buttons[i].setEnabled(b);
        }
        else if (sens > 0) {
          buttons[i].setEnabled(currentMonthId >= Month.toMonthId(yearLimit, monthLimit));
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
