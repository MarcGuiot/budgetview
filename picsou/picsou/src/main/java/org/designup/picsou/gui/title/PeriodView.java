package org.designup.picsou.gui.title;

import org.designup.picsou.gui.View;
import org.designup.picsou.gui.description.stringifiers.AbstractMonthRangeFormatter;
import org.designup.picsou.gui.description.stringifiers.MonthListStringifier;
import org.designup.picsou.gui.description.stringifiers.MonthRangeFormatter;
import org.designup.picsou.model.Month;
import org.designup.picsou.model.Transaction;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.SelectionService;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.util.Collections;
import java.util.Set;

public class PeriodView extends View implements GlobSelectionListener {

  private JLabel periodTitle = new JLabel();
  private Set<Integer> months = Collections.emptySet();

  public PeriodView(GlobRepository repository, Directory directory) {
    super(repository, directory);
    directory.get(SelectionService.class).addListener(this, Month.TYPE);
  }

  public void registerComponents(GlobsPanelBuilder builder) {
    builder.add("periodTitle", periodTitle);
    updateLabel();
  }

  public void selectionUpdated(GlobSelection selection) {
    if (selection.isRelevantForType(Month.TYPE)) {
      months = selection.getAll(Month.TYPE).getValueSet(Month.ID);
    }
    updateLabel();
  }

  private void updateLabel() {
    if (!repository.contains(Transaction.TYPE)) {
      periodTitle.setText("");
      return;
    }
    if (months.isEmpty()) {
      periodTitle.setText("");
      return;
    }
    periodTitle.setText(MonthListStringifier.toString(months, MonthRangeFormatter.STANDARD));
    periodTitle.setText(MonthListStringifier.toString(months, new AbstractMonthRangeFormatter() {
      public String monthRangeInYear(int firstMonthId, int lastMonthId, int year) {
        if (firstMonthId == lastMonthId) {
          return "<html><b>" + Month.getFullMonthLabel(firstMonthId) + "</b> " + Integer.toString(year) + "</html>";
        }
        return "<html><b>" + Month.getFullMonthLabel(firstMonthId) + " - " + Month.getFullMonthLabel(lastMonthId) +
               "</b> " + Integer.toString(year) + "</html>";
      }

      public String monthRangeAcrossYears(int firstMonthId, int lastMonthId) {
        return Month.getFullLabel(firstMonthId) + " - " + Month.getFullLabel(lastMonthId);
      }
    }));
  }

}
