package org.designup.picsou.gui.bank;

import org.designup.picsou.model.Bank;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.views.GlobListView;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.GlobMatcher;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class BankChooserPanel {
  private JTextField bankEditor;
  private JPanel panel;
  private SelectionService selectionService;

  public BankChooserPanel(GlobRepository repository, Directory directory) {
    selectionService = directory.get(SelectionService.class);
    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/bank/bankChooserPanel.splits",
                                                      repository, directory);
    bankEditor = new JTextField();
    builder.add("bankEditor", bankEditor);

    GlobListView bankListView = builder.addList("bankList", Bank.TYPE);
    bankEditor.getDocument().addDocumentListener(new OnKeyActionListener(bankListView));
    panel = builder.load();
  }

  public JPanel getPanel() {
    return panel;
  }

  private class OnKeyActionListener implements GlobMatcher, DocumentListener {
    private GlobListView bankListView;
    private String[] filters;

    public OnKeyActionListener(GlobListView bankListView) {
      this.bankListView = bankListView;
    }

    public void update() {
      String filter = bankEditor.getText();
      filters = filter.split(" ");
      for (int i = 0; i < filters.length; i++) {
        filters[i] = filters[i].toUpperCase();
      }
      bankListView.setFilter(this);
      if (bankListView.getSize() == 1) {
        selectionService.select(bankListView.getGlobAt(0));
      }
    }

    public boolean matches(Glob item, GlobRepository repository) {
      String name = item.get(Bank.NAME).toUpperCase();
      for (String filter : filters) {
        if (name.indexOf(filter) < 0) {
          return false;
        }
      }
      return true;
    }

    public void insertUpdate(DocumentEvent e) {
      update();
    }

    public void removeUpdate(DocumentEvent e) {
      update();
    }

    public void changedUpdate(DocumentEvent e) {
      update();
    }
  }

}
