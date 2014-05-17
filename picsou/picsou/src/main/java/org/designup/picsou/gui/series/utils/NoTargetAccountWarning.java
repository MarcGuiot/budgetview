package org.designup.picsou.gui.series.utils;

import org.designup.picsou.model.Account;
import org.designup.picsou.model.Series;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.splits.utils.Disposable;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.util.Set;

public class NoTargetAccountWarning implements GlobSelectionListener, ChangeSetListener, Disposable {

  private GlobRepository repository;
  private Directory directory;
  private JLabel label = new JLabel(Lang.get("seriesEdition.noTargetAccount"));
  private Key currentSeriesKey;

  public static void register(String labelName, GlobsPanelBuilder builder, GlobRepository repository, Directory directory) {
    NoTargetAccountWarning message = new NoTargetAccountWarning(repository, directory);
    builder.add(labelName, message.label);
    builder.addDisposable(message);
  }

  private NoTargetAccountWarning(GlobRepository repository, Directory directory) {
    this.repository = repository;
    this.directory = directory;
    this.repository.addChangeListener(this);
    this.directory.get(SelectionService.class).addListener(this, Series.TYPE);
  }

  public void selectionUpdated(GlobSelection selection) {
    GlobList selectedSeries = selection.getAll(Series.TYPE);
    if (selectedSeries.size() != 1) {
      currentSeriesKey = null;
    }
    else {
      currentSeriesKey = selectedSeries.getFirst().getKey();
    }
    update();
  }

  public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
    if (changeSet.containsChanges(Series.TYPE)) {
      update();
    }
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
    if (changedTypes.contains(Series.TYPE)) {
      update();
    }
  }

  private void update() {
    if (currentSeriesKey == null) {
      return;
    }

    Glob series = repository.find(currentSeriesKey);
    if (series == null) {
      currentSeriesKey = null;
      return;
    }

    label.setVisible(Account.needsTargetAccount(series));
  }

  public void dispose() {
    repository.removeChangeListener(this);
    repository = null;
    directory.get(SelectionService.class).removeListener(this);
    directory = null;
  }
}
