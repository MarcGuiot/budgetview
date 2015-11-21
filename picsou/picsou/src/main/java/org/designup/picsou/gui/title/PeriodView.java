package org.designup.picsou.gui.title;

import org.designup.picsou.gui.View;
import org.designup.picsou.gui.components.JPopupButton;
import org.designup.picsou.gui.description.stringifiers.AbstractMonthRangeFormatter;
import org.designup.picsou.gui.description.stringifiers.MonthListStringifier;
import org.designup.picsou.gui.notes.ShowNotesAction;
import org.designup.picsou.gui.time.actions.*;
import org.designup.picsou.gui.transactions.reconciliation.annotations.ShowReconciliationAction;
import org.designup.picsou.model.Month;
import org.designup.picsou.model.Transaction;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.utils.PopupMenuFactory;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.util.Collections;
import java.util.Set;

public class PeriodView extends View implements GlobSelectionListener {

  private JButton periodTitle;
  private Set<Integer> months = Collections.emptySet();

  public PeriodView(GlobRepository repository, Directory directory) {
    super(repository, directory);
    directory.get(SelectionService.class).addListener(this, Month.TYPE);
  }

  public void registerComponents(GlobsPanelBuilder builder) {
    builder.add("periodTitle", getButton());
    updateLabel();
  }

  public JButton getButton() {
    if (periodTitle == null) {
      periodTitle = new JPopupButton("", createMenu());
    }
    return periodTitle;
  }

  private PopupMenuFactory createMenu() {
    return new PopupMenuFactory() {
      public JPopupMenu createPopup() {
        JPopupMenu menu = new JPopupMenu();
        menu.add(new SelectCurrentMonthAction(repository, directory));
        menu.addSeparator();
        menu.add(new SelectMonthAction(repository, directory));
        menu.addSeparator();
        menu.add(new SelectCurrentYearAction(repository, directory));
        menu.add(new SelectLast12MonthsAction(repository, directory));
        menu.add(new SelectSinceLastJanuaryAction(repository, directory));
        return menu;
      }
    };
  }

  public void selectionUpdated(GlobSelection selection) {
    if (selection.isRelevantForType(Month.TYPE)) {
      months = selection.getAll(Month.TYPE).getValueSet(Month.ID);
    }
    updateLabel();
  }

  private void updateLabel() {
    if (!repository.contains(Transaction.TYPE)) {
      periodTitle.setText(Lang.get("title.nodata"));
      return;
    }
    if (months.isEmpty()) {
      periodTitle.setText(Lang.get("title.noperiod"));
      return;
    }
    periodTitle.setText(MonthListStringifier.toString(months, new AbstractMonthRangeFormatter() {
      public String year(int year) {
        return Integer.toString(year);
      }

      public String yearRange(int firstYear, int lastYear) {
        return Integer.toString(firstYear) + " - " + Integer.toString(lastYear);
      }

      public String monthRangeInYear(int firstMonthId, int lastMonthId, int year) {
        if (firstMonthId == lastMonthId) {
          return Month.getFullMonthLabel(firstMonthId, false) + " " + Integer.toString(year);
        }
        return Month.getFullMonthLabel(firstMonthId, false) + " - " +
               Month.getFullMonthLabel(lastMonthId, false) + " " + Integer.toString(year);
      }

      public String monthRangeAcrossYears(int firstMonthId, int lastMonthId) {
        return getFullLabel(firstMonthId) + " - " + getFullLabel(lastMonthId);
      }
    }));
  }

  private String getFullLabel(Integer monthId) {
    if (monthId == null) {
      return "";
    }
    int month = Month.toMonth(monthId);
    int year = Month.toYear(monthId);
    return "<b>" + Month.getFullMonthLabel(Month.toMonth(month), false) + "</b> " + year;
  }
}
