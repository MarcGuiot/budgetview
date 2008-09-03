package org.designup.picsou.gui.components;

import org.designup.picsou.model.Month;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.splits.SplitsBuilder;
import org.globsframework.gui.splits.repeat.RepeatCellBuilder;
import org.globsframework.gui.splits.repeat.RepeatComponentFactory;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MonthChooser {
  private JLabel nextYearLabel = new JLabel();
  private JLabel previousYearLabel = new JLabel();
  private JLabel currentYearLabel = new JLabel();
  List<MonthsComponentFactory> monthsComponentFactories = new ArrayList<MonthsComponentFactory>();
  private JPanel panel;
  private PicsouDialog dialog;
  private int selectedYear;
  private int selectedMonth = -1;
  private int currentYear;

  public MonthChooser(final Directory directory) {
    SplitsBuilder builder = new SplitsBuilder(directory);
    builder.setSource(MonthChooser.class, "/layout/monthChooser.splits");
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

    panel = builder.load();
  }

  public int show(Window parent, int year, Integer month) {
    selectedMonth = month == null ? -1 : month;
    selectedYear = year;
    currentYear = year;
    dialog = PicsouDialog.createWithButton(Lang.get("month.chooser.title"), parent, panel, new CancelAction());
    update();
    dialog.pack();
    dialog.setVisible(true);
    dialog = null;
    return selectedMonth;
  }

  private void update() {
    previousYearLabel.setText(Integer.toString(currentYear - 1));
    currentYearLabel.setText(Integer.toString(currentYear));
    nextYearLabel.setText(Integer.toString(currentYear + 1));
    for (MonthsComponentFactory factory : monthsComponentFactories) {
      factory.setCurrentYear(selectedYear, selectedMonth, currentYear);
    }
  }

  private void set(int monthId) {
    selectedMonth = monthId;
    dialog.setVisible(false);
  }

  private void addMonthsPanel(String name, SplitsBuilder builder, MonthChooser selection, int index) {
    MonthsComponentFactory monthsComponentFactory =
      new MonthsComponentFactory(selection, index);
    builder.addRepeat(name, Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12),
                      monthsComponentFactory);
    monthsComponentFactories.add(monthsComponentFactory);
  }

  private static class MonthsComponentFactory implements RepeatComponentFactory<Integer> {
    private MonthChooser selection;
    private int index;
    private int selectedYear;
    private int selectedMonth;
    private int currentYear;
    JToggleButton[] buttons = new JToggleButton[12];

    public MonthsComponentFactory(MonthChooser selection, int index) {
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

    public void setCurrentYear(int selectedYear, int selectedMonth, int currentYear) {
      this.selectedYear = selectedYear;
      this.selectedMonth = selectedMonth;
      this.currentYear = currentYear + index;
      updateButton();
    }

    public void updateButton() {
      for (int i = 0; i < buttons.length; i++) {
        buttons[i].setSelected(currentYear == selectedYear && selectedMonth == i + 1);
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
