package org.designup.picsou.gui.actions;

import org.designup.picsou.client.ServerAccess;
import org.designup.picsou.model.*;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.delta.DefaultChangeSet;
import org.globsframework.utils.MultiMap;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.List;

public class SynchronizeWithServer extends AbstractAction {
  private ServerAccess localAccess;
  private ServerAccess remoteAccess;
  private GlobRepository repository;

  public SynchronizeWithServer(GlobRepository repository, Directory directory) {
    this.repository = repository;
  }

  public void actionPerformed(ActionEvent e) {
    DefaultChangeSet upgradeChangeSet = new DefaultChangeSet();
    GlobList serverData = remoteAccess.getUserData(upgradeChangeSet);
    GlobType globType[] = {Account.TYPE,
                           Bank.TYPE,
                           Category.TYPE,
                           Month.TYPE,
                           Transaction.TYPE,
                           TransactionType.TYPE,
                           TransactionToCategory.TYPE,
                           LabelToCategory.TYPE,
                           TransactionTypeMatcher.TYPE,
                           TransactionImport.TYPE};
    MultiMap<GlobType, Glob> data = new MultiMap<GlobType, Glob>();
    for (Glob glob : serverData) {
      data.put(glob.getType(), glob);
    }
    List<Glob> transactions = data.get(Transaction.TYPE);
    repository.getAll(Transaction.TYPE);
  }
}
