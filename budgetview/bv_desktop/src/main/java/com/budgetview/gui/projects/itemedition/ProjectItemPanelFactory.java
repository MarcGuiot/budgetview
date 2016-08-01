package com.budgetview.gui.projects.itemedition;

import com.budgetview.gui.projects.ProjectItemPanel;
import com.budgetview.model.ProjectItemType;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.exceptions.InvalidParameter;

import javax.swing.*;

public class ProjectItemPanelFactory {

  private JScrollPane scrollPane;
  private final GlobRepository repository;
  private final Directory directory;

  public ProjectItemPanelFactory(JScrollPane scrollPane, GlobRepository repository, Directory directory) {
    this.scrollPane = scrollPane;
    this.repository = repository;
    this.directory = directory;
  }

  public ProjectItemPanel create(Glob item) {
    switch (ProjectItemType.get(item)) {
      case EXPENSE:
        return new ProjectItemExpensePanel(item, scrollPane, repository, directory);
      case TRANSFER:
        return new ProjectItemTransferPanel(item, scrollPane, repository, directory);
      default:
        throw new InvalidParameter("Unexpected project item type for " + item);
    }
  }
}
