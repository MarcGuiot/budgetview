package com.budgetview.desktop.series.edition;

import com.budgetview.model.Month;
import com.budgetview.model.ProfileType;
import com.budgetview.model.Series;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.SelectionService;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.fields.BooleanField;
import org.globsframework.model.*;

import javax.swing.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Set;

public class MonthCheckBoxUpdater implements GlobSelectionListener, ItemListener, ChangeSetListener {
  private JCheckBox checkBox;
  private Integer monthIndex;
  private boolean updateInProgress;
  private boolean forceDisable;
  private Glob currentSeries;
  private GlobRepository repository;

  public MonthCheckBoxUpdater(Integer monthIndex,
                              GlobRepository repository,
                              SelectionService selectionService) {
    this.checkBox = new JCheckBox();
    this.monthIndex = monthIndex;
    selectionService.addListener(this, Series.TYPE);
    checkBox.addItemListener(this);
    this.repository = repository;
    repository.addChangeListener(new ChangeSetListener() {
      public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
        if ((currentSeries != null) && changeSet.isDeleted(currentSeries.getKey())) {
          currentSeries = null;
        }
      }

      public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
        currentSeries = null;
      }
    });
  }

  public JCheckBox getCheckBox() {
    return checkBox;
  }

  public void selectionUpdated(GlobSelection selection) {
    GlobList seriesList = selection.getAll(Series.TYPE);
    currentSeries = seriesList.size() == 1 ? seriesList.get(0) : null;
    try {
      updateInProgress = true;
      updateCheckBox();
    }
    finally {
      updateInProgress = false;
    }
  }

  public void itemStateChanged(ItemEvent e) {
    if (updateInProgress) {
      return;
    }
    repository.startChangeSet();
    try {
      if (currentSeries != null) {
        BooleanField field = Series.getMonthField(monthIndex);
        boolean selected = e.getStateChange() == ItemEvent.SELECTED;
        repository.update(currentSeries.getKey(), field, selected);
        ProfileType profileType = ProfileType.get(currentSeries.get(Series.PROFILE_TYPE));
        if (selected && profileType.getMonthStep() != -1 && profileType != ProfileType.CUSTOM) {
          BooleanField[] months = Series.getMonths();
          for (BooleanField month : months) {
            if (month != field) {
              repository.update(currentSeries.getKey(), month, false);
            }
          }
        }
      }
    }
    finally {
      repository.completeChangeSet();
    }
  }

  public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
    updateCheckBox();
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
    updateCheckBox();
  }

  private void updateCheckBox() {
    if (currentSeries != null) {
      Integer firstMonth = currentSeries.get(Series.FIRST_MONTH);
      Integer lastMonth = currentSeries.get(Series.LAST_MONTH);
      if (firstMonth == null || lastMonth == null) {
        forceDisable = false;
      }
      else {
        forceDisable = true;
        for (int month = firstMonth; month <= lastMonth; month = Month.next(month)) {
          if (Month.toMonth(month) == monthIndex) {
            forceDisable = false;
            break;
          }
        }
      }
    }
    checkBox.setEnabled(!forceDisable && currentSeries != null);
    checkBox.setSelected((currentSeries != null) && currentSeries.isTrue(Series.getMonthField(monthIndex)));
  }
}
