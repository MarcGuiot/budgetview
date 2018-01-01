package com.budgetview.desktop.importer.utils;

import com.budgetview.model.Month;
import org.globsframework.gui.SelectionService;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import java.util.Set;

import static org.globsframework.model.utils.GlobMatchers.fieldIn;

public class ImportSelection {
  public static void selectLastMonth(Set<Integer> months, GlobRepository repository, Directory directory) {
    GlobList monthsToSelect =
      repository.getAll(Month.TYPE, fieldIn(Month.ID, months)).sort(Month.ID);
    if (!monthsToSelect.isEmpty()) {
      SelectionService selectionService = directory.get(SelectionService.class);
      selectionService.select(monthsToSelect.getLast());
    }
  }
}
