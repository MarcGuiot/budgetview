package org.designup.picsou.bank.importer;

import org.designup.picsou.bank.BankSynchroService;
import org.designup.picsou.model.RealAccount;
import org.designup.picsou.model.util.Amounts;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.views.GlobTableView;
import org.globsframework.model.*;
import org.globsframework.model.utils.GlobFieldComparator;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.model.utils.GlobFunctor;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

public class OtherBank extends WebBankPage {
  public static int ID = -123456;
  private Map<Key, String> files = new HashMap<Key, String>();

  public static class Init implements BankSynchroService.BankSynchro {

    public GlobList show(Directory directory, GlobRepository repository) {
      OtherBank bank = new OtherBank(directory, repository);
      bank.init();
      return bank.show();
    }
  }

  public JPanel getPanel() {
    final SelectionService selectionService = directory.get(SelectionService.class);
    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/bank/connection/otherPanel.splits", repository, directory);

//    builder.add("occupedPanel", accupedPanel);
    
    builder.addEditor("type", RealAccount.NUMBER);
    builder.addEditor("name", RealAccount.NAME);
    builder.addEditor("position", RealAccount.POSITION);
    builder.addCheckBox("isSavings", RealAccount.SAVINGS);
    builder.addEditor("file", RealAccount.FILE_NAME);

    JButton addRealAccount = new JButton("add");
    builder.add("add", addRealAccount);
    addRealAccount.addActionListener(new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        Glob glob = repository.create(RealAccount.TYPE, FieldValue.value(RealAccount.BANK, ID),
                                      FieldValue.value(RealAccount.FROM_SYNCHRO, true));
        selectionService.select(glob);
      }
    });
    final GlobTableView table = builder.addTable("table", RealAccount.TYPE, new GlobFieldComparator(RealAccount.ID))
      .setFilter(GlobMatchers.fieldEquals(RealAccount.BANK, ID))
      .addColumn(RealAccount.NUMBER)
      .addColumn(RealAccount.NAME)
      .addColumn(RealAccount.POSITION)
      .addColumn(RealAccount.FILE_NAME);

    JButton update = new JButton("update");
    builder.add("update", update);
    update.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent e) {
        startOccuped();
        try {
          GlobList globList = table.getGlobs();
          for (Glob glob : globList) {
            files.put(glob.getKey(), glob.get(RealAccount.FILE_NAME));
          }
          accounts.addAll(globList);
          doImport();
        }
        finally {
          endOccuped();
        }
      }
    });
    repository.getAll(RealAccount.TYPE, GlobMatchers.fieldEquals(RealAccount.BANK,
                                                                 OtherBank.ID))
      .safeApply(new GlobFunctor() {
      public void run(Glob glob, GlobRepository repository) throws Exception {
        repository.update(glob.getKey(), RealAccount.FILE_NAME, null);
      }
    }, repository);
    return builder.load();
  }

  public OtherBank(Directory directory, GlobRepository repository) {
    super(directory, repository, ID);
  }

  public void loadFile() {
    for (Map.Entry<Key, String> entry : files.entrySet()) {
      repository.update(entry.getKey(), RealAccount.FILE_NAME, entry.getValue());
    }
  }

  protected Double extractAmount(String position) {
    return Amounts.extractAmount(position);
  }
}
