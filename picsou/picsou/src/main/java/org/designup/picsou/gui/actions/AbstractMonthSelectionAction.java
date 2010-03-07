package org.designup.picsou.gui.actions;

import org.designup.picsou.model.Month;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.SelectionService;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.SortedSet;

public abstract class AbstractMonthSelectionAction extends AbstractAction implements GlobSelectionListener {

  protected final GlobRepository repository;
  protected final Directory directory;
  protected SelectionService selectionService;
  protected Integer firstMonth;
  protected Integer lastMonth;

  public AbstractMonthSelectionAction(String name, String tooltip, GlobRepository repository, Directory directory) {
    super(name);
    this.repository = repository;
    this.directory = directory;
    this.selectionService = directory.get(SelectionService.class);
    setEnabled(false);
    selectionService.addListener(this, Month.TYPE);
    putValue(Action.SHORT_DESCRIPTION, tooltip);
  }

  protected abstract int getTargetMonth();

  public void selectionUpdated(GlobSelection selection) {
    final SortedSet<Integer> months = selection.getAll(Month.TYPE).getSortedSet(Month.ID);

    if (months.isEmpty()) {
      setEnabled(false);
      return;
    }

    firstMonth = months.first();
    lastMonth = months.last();
    setEnabled(containsMonth(getTargetMonth()));
  }

  protected boolean containsMonth(int month) {
    return repository.contains(Key.create(Month.TYPE, month));
  }

  public void actionPerformed(ActionEvent e) {
    selectionService.select(repository.find(Key.create(Month.TYPE, getTargetMonth())));
  }
}
