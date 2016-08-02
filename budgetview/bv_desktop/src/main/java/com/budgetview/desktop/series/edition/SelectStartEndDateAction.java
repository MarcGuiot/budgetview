package com.budgetview.desktop.series.edition;

import com.budgetview.desktop.components.MonthRangeBound;
import com.budgetview.desktop.components.dialogs.MonthChooserDialog;
import com.budgetview.desktop.components.dialogs.PicsouDialog;
import com.budgetview.desktop.time.TimeService;
import com.budgetview.utils.Lang;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.exceptions.UnexpectedValue;

import javax.swing.*;
import java.awt.event.ActionEvent;

public abstract class SelectStartEndDateAction extends AbstractAction {
  private IntegerField dateField;
  private Glob currentSeries;
  private MonthRangeBound bound;
  private PicsouDialog dialog;
  private GlobRepository repository;
  private Directory directory;

  protected SelectStartEndDateAction(IntegerField dateField,
                                     MonthRangeBound bound,
                                     PicsouDialog dialog,
                                     GlobRepository repository,
                                     Directory directory) {
    this.dateField = dateField;
    this.bound = bound;
    this.dialog = dialog;
    this.repository = repository;
    this.directory = directory;
  }

  public void setCurrentSeries(Glob currentSeries) {
    this.currentSeries = currentSeries;
    setEnabled(currentSeries != null);
    processValue(currentSeries == null ? null : currentSeries.get(dateField));
  }

  protected abstract Integer getMonthLimit();

  public void actionPerformed(ActionEvent e) {
    MonthRangeBound bound = this.bound;
    MonthChooserDialog chooser = new MonthChooserDialog(getTitle(bound), dialog, directory);
    chooser.setNoneOptionShown(true);
    Integer monthId = currentSeries.get(dateField);
    Integer limit = getMonthLimit();
    if (monthId == null) {
      monthId = limit == null ? directory.get(TimeService.class).getCurrentMonthId() : limit;
    }
    if (limit == null) {
      limit = 0;
      bound = MonthRangeBound.NONE;
    }
    chooser.show(monthId, bound, limit, new MonthChooserDialog.Callback() {
      public void processSelection(int monthId) {
        setDate(monthId);
      }

      public void processNoneSelected() {
        setDate(null);
      }
    });
  }

  public void setDate(Integer monthId) {
    repository.update(currentSeries.getKey(), dateField, monthId);
    processValue(currentSeries.get(dateField));
  }

  private String getTitle(MonthRangeBound bound) {
    switch (bound) {
      case NONE:
        return Lang.get("seriesEdition.selectMonth.none");
      case LOWER:
        return Lang.get("seriesEdition.selectMonth.start");
      case UPPER:
        return Lang.get("seriesEdition.selectMonth.end");
      case BOTH:
        return Lang.get("seriesEdition.selectMonth.single");
    }
    throw new UnexpectedValue("Unknown bound: " + bound);
  }

  protected abstract void processValue(Integer monthId);
}
