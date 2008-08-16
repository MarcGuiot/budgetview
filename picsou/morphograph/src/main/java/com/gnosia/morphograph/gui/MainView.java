package com.gnosia.morphograph.gui;

import com.gnosia.morphograph.model.Series;
import com.gnosia.morphograph.model.Topic;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.splits.SplitsBuilder;
import org.globsframework.gui.splits.SplitsEditor;
import org.globsframework.gui.views.GlobComboView;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.GlobFieldComparator;
import org.globsframework.model.utils.GlobMatcher;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;

public class MainView {
  private JFrame frame;
  private GlobComboView topicsCombo;
  private GlobComboView seriesCombo;

  public MainView(GlobRepository globRepository, Directory directory) throws Exception {

    setNativeLookAndFeel();

    SplitsBuilder builder = new SplitsBuilder(directory);

    SeriesView seriesView = new SeriesView(globRepository, directory);
    builder.add("seriesPanel", seriesView.createPanel());

    topicsCombo = GlobComboView.init(Topic.TYPE, globRepository, directory)
      .setComparator(new GlobFieldComparator(Topic.ID));
    builder.add(topicsCombo.getComponent());

    seriesCombo = GlobComboView.init(Series.TYPE, globRepository, directory)
      .setComparator(new GlobFieldComparator(Series.ID));
    builder.add(seriesCombo.getComponent());

    directory.get(SelectionService.class).addListener(new GlobSelectionListener() {
      public void selectionUpdated(GlobSelection selection) {
        GlobList topics = selection.getAll(Topic.TYPE);
        GlobMatcher matcher = topics.isEmpty() ? GlobMatchers.NONE : GlobMatchers.linkedTo(topics.get(0), Series.TOPIC);
        seriesCombo.setFilter(matcher);
      }
    }, Topic.TYPE);

    builder.setSource(MainView.class, "/layout/mainView.xml", "UTF-8");
    frame = builder.load();
    seriesView.setFrame(frame);

    SplitsEditor.show(builder, frame);
  }

  private static void setNativeLookAndFeel() throws Exception {
    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
  }

  public void show() {
    topicsCombo.selectFirst();
    seriesCombo.selectFirst();
    frame.setVisible(true);
  }
}
