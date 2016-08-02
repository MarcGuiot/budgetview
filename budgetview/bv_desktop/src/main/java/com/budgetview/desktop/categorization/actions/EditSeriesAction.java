package com.budgetview.desktop.categorization.actions;

import com.budgetview.desktop.series.SeriesEditor;
import com.budgetview.model.Month;
import com.budgetview.utils.Lang;
import org.globsframework.gui.SelectionService;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Set;

public class EditSeriesAction extends AbstractAction {
  private Key seriesKey;
  private Set<Integer> monthIds;
  private GlobRepository repository;
  private Directory directory;

  public EditSeriesAction(Key seriesKey, GlobRepository repository, Directory directory) {
    super(Lang.get("series.edit"));
    this.seriesKey = seriesKey;
    this.repository = repository;
    this.directory = directory;
  }

  public EditSeriesAction(Key seriesKey, Set<Integer> monthIds, GlobRepository repository, Directory directory) {
    super(Lang.get("series.edit"));
    this.seriesKey = seriesKey;
    this.monthIds = monthIds;
    this.repository = repository;
    this.directory = directory;
  }

  public void actionPerformed(ActionEvent e) {
    Glob series = repository.get(seriesKey);
    SelectionService selectionService = directory.get(SelectionService.class);
    Set<Integer> monthIds = this.monthIds;
    if (monthIds == null) {
      monthIds = selectionService.getSelection(Month.TYPE).getValueSet(Month.ID);
    }
    SeriesEditor.get(directory).showSeries(series, monthIds);
  }
}
