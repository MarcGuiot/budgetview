package org.designup.picsou.gui.series.subseries;

import org.designup.picsou.gui.series.SeriesEditionDialog;
import org.designup.picsou.model.Series;
import org.designup.picsou.model.SubSeries;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.utils.AbstractDocumentListener;
import org.globsframework.gui.views.GlobListView;
import static org.globsframework.model.FieldValue.value;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.Strings;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import java.awt.event.ActionEvent;
import java.util.Set;

public class SubSeriesEditionPanel {

  private GlobRepository repository;
  private Glob currentSeries;

  private JTextField nameField = new JTextField();
  private SubSeriesEditionPanel.AddAction addAction = new AddAction();
  private GlobListView list;
  private JPanel panel;

  public SubSeriesEditionPanel(GlobRepository repository, Directory directory) {
    this.repository = repository;

    GlobsPanelBuilder builder = new GlobsPanelBuilder(SeriesEditionDialog.class,
                                                      "/layout/subSeriesEditionPanel.splits",
                                                      repository, directory);

    builder.add("subSeriesNameField", nameField).addActionListener(addAction);
    builder.add("add", addAction);
    list = builder.addList("list", SubSeries.TYPE).setFilter(GlobMatchers.NONE);

    nameField.getDocument().addDocumentListener(new AbstractDocumentListener() {
      protected void documentChanged(DocumentEvent e) {
        processNameUpdate();
      }
    });

    nameField.requestFocus();

    panel = builder.load();
  }

  public JPanel getPanel() {
    return panel;
  }

  public void setCurrentSeries(Glob series) {
    this.currentSeries = series;
    list.setFilter(GlobMatchers.linkedTo(series, SubSeries.SERIES));
  }

  private void processNameUpdate() {
    if (currentSeries == null) {
      return;
    }

    String enteredText = nameField.getText();
    if (Strings.isNullOrEmpty(enteredText)) {
      addAction.setEnabled(false);
      return;
    }

    Set<String> currentNames =
      repository
        .getAll(SubSeries.TYPE, GlobMatchers.linkedTo(currentSeries, SubSeries.SERIES))
        .getValueSet(SubSeries.NAME);

    addAction.setEnabled(!currentNames.contains(enteredText));
  }

  private class AddAction extends AbstractAction {

    private AddAction() {
      super(Lang.get("add"));
      setEnabled(false);
    }

    public void actionPerformed(ActionEvent e) {
      String name = nameField.getText();
      repository.create(SubSeries.TYPE,
                        value(SubSeries.NAME, name),
                        value(SubSeries.SERIES, currentSeries.get(Series.ID)));
      nameField.setText("");
    }
  }
}
