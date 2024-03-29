package com.budgetview.desktop.series.subseries;

import com.budgetview.desktop.series.SeriesEditionDialog;
import com.budgetview.desktop.series.edition.DeleteSubSeriesAction;
import com.budgetview.desktop.series.edition.RenameSubSeriesAction;
import com.budgetview.model.Series;
import com.budgetview.model.SubSeries;
import com.budgetview.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.gui.utils.AbstractDocumentListener;
import org.globsframework.gui.views.GlobListView;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.Strings;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import java.awt.event.ActionEvent;

import static org.globsframework.model.FieldValue.value;

public class SubSeriesEditionPanel {

  private GlobRepository repository;
  private SelectionService selectionService;

  private Glob currentSeries;
  private JTextField nameField = new JTextField();
  private JLabel errorMessage = new JLabel();
  private SubSeriesEditionPanel.AddAction addAction = new AddAction();
  private GlobListView list;
  private JPanel panel;

  public SubSeriesEditionPanel(GlobRepository repository, Directory directory, JDialog dialog) {
    this.repository = repository;
    this.selectionService = directory.get(SelectionService.class);

    GlobsPanelBuilder builder = new GlobsPanelBuilder(SeriesEditionDialog.class,
                                                      "/layout/series/subSeriesEditionPanel.splits",
                                                      repository, directory);

    builder.add("subSeriesNameField", nameField).getComponent().addActionListener(addAction);
    builder.add("add", addAction);
    list = builder.addList("list", SubSeries.TYPE).setFilter(GlobMatchers.NONE);

    builder.add("subSeriesErrorMessage", errorMessage);
    errorMessage.setVisible(false);

    nameField.getDocument().addDocumentListener(new AbstractDocumentListener() {
      protected void documentChanged(DocumentEvent e) {
        processNameUpdate();
      }
    });
    nameField.requestFocus();

    builder.add("rename", new RenameSubSeriesAction(repository, directory, dialog));
    builder.add("delete", new DeleteSubSeriesAction(repository, directory, dialog));

    panel = builder.load();
  }

  public JPanel getPanel() {
    return panel;
  }

  public void setCurrentSeries(Glob series) {
    this.currentSeries = series;
    nameField.setEnabled(series != null);
    list.setFilter(GlobMatchers.linkedTo(series, SubSeries.SERIES));
    list.selectFirst();
  }

  private void processNameUpdate() {
    addAction.setEnabled(Strings.isNotEmpty(nameField.getText()));
    errorMessage.setVisible(false);
  }

  public void select() {
    GuiUtils.selectAndRequestFocus(nameField);
  }

  private class AddAction extends AbstractAction {

    private AddAction() {
      super(Lang.get("add"));
      setEnabled(false);
    }

    public void actionPerformed(ActionEvent e) {
      String name = nameField.getText();

      boolean nameAlreadyUsed =
        repository.findLinkedTo(currentSeries, SubSeries.SERIES)
          .getValueSet(SubSeries.NAME)
          .contains(name);
      errorMessage.setVisible(nameAlreadyUsed);
      if (nameAlreadyUsed) {
        errorMessage.setText(Lang.get("subseries.name.already.used"));
        return;
      }

      Glob subSeries = repository.create(SubSeries.TYPE,
                                         value(SubSeries.NAME, name),
                                         value(SubSeries.SERIES, currentSeries.get(Series.ID)));
      nameField.setText("");
      selectionService.select(subSeries);
    }
  }
}
