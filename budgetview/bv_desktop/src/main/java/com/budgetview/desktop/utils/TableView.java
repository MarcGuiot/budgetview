package com.budgetview.desktop.utils;

import org.globsframework.model.GlobList;

import javax.swing.event.TableModelListener;

public interface TableView {
  void addTableListener(TableModelListener listener);

  GlobList getDisplayedGlobs();
}
