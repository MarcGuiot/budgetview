package org.designup.picsou.bank.importer;

import org.designup.picsou.bank.BankSynchroService;
import org.designup.picsou.importer.BankFileType;
import org.designup.picsou.importer.utils.TypedInputStream;
import org.designup.picsou.model.RealAccount;
import org.designup.picsou.model.util.Amounts;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.views.GlobTableView;
import org.globsframework.model.*;
import org.globsframework.model.utils.GlobFieldComparator;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OtherBank extends BankPage {

  public static int ID = -123456;
  private Map<Key, String> files = new HashMap<Key, String>();

  public static class Init implements BankSynchroService.BankSynchro {

    public void show(Directory directory, GlobRepository repository) {
      OtherBank bank = new OtherBank(directory, repository);
      bank.init();
      bank.show();
    }
  }

  public JPanel getPanel() {
    final SelectionService selectionService = directory.get(SelectionService.class);
    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/connection/otherPanel.splits", repository, directory);

    builder.addEditor("type", RealAccount.NAME);
    builder.addEditor("name", RealAccount.TYPE_NAME);
    builder.addEditor("position", RealAccount.POSITION);
    builder.addCheckBox("isSavings", RealAccount.SAVINGS);
    builder.addEditor("file", RealAccount.FILE_NAME);

    JButton addRealAccount = new JButton("add");
    builder.add("add", addRealAccount);
    addRealAccount.addActionListener(new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        Glob glob = repository.create(RealAccount.TYPE, FieldValue.value(RealAccount.BANK, ID));
        selectionService.select(glob);
      }
    });
    GlobTableView table = builder.addTable("table", RealAccount.TYPE, new GlobFieldComparator(RealAccount.ID))
      .setFilter(GlobMatchers.fieldEquals(RealAccount.BANK, ID))
      .addColumn(RealAccount.NAME)
      .addColumn(RealAccount.TYPE_NAME)
      .addColumn(RealAccount.POSITION)
      .addColumn(RealAccount.FILE_NAME);

    JButton update = new JButton("update");
    builder.add("update", update);
    update.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent e) {
        GlobList globList = repository.getAll(RealAccount.TYPE,
                                              GlobMatchers.and(GlobMatchers.fieldEquals(RealAccount.BANK, ID),
                                                               GlobMatchers.not(GlobMatchers.isNullOrEmpty(RealAccount.FILE_NAME))));
        for (Glob glob : globList) {
          files.put(glob.getKey(), glob.get(RealAccount.FILE_NAME));
        }
        accountsInPage.addAll(globList);
        showAccounts();
      }
    });
    return builder.load();
  }

  public OtherBank(Directory directory, GlobRepository repository) {
    super(directory, repository, ID);
  }

  public List<File> loadFile() {
    List<File> downloadedFiles = new ArrayList<File>();
    for (Map.Entry<Key, String> entry : files.entrySet()) {
      try {
        TypedInputStream inputStream = new TypedInputStream(new File(entry.getValue()));
        if (inputStream.getType() == BankFileType.QIF) {
          downloadedFiles.add(createQifLocalFile(repository.get(entry.getKey()),
                                                 new FileInputStream(entry.getValue())));
        }
        else {
          downloadedFiles.add(new File(entry.getValue()));
        }
      }
      catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
    return downloadedFiles;
  }

  protected Double extractAmount(String position) {
    return Amounts.extractAmount(position);
  }
}
