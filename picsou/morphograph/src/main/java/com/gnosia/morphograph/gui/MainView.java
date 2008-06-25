package com.gnosia.morphograph.gui;

import com.gnosia.morphograph.model.Series;
import com.gnosia.morphograph.model.Topic;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.splits.IconLocator;
import org.globsframework.gui.splits.SplitsBuilder;
import org.globsframework.gui.splits.color.ColorService;
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

    SplitsBuilder splits = new SplitsBuilder(directory.get(ColorService.class), directory.get(IconLocator.class));

    SeriesView seriesView = new SeriesView(globRepository, directory);
    splits.add("seriesPanel", seriesView.createPanel());

    topicsCombo = GlobComboView.init(Topic.TYPE, globRepository, directory)
      .setComparator(new GlobFieldComparator(Topic.ID));
    splits.add(topicsCombo.getComponent());

    seriesCombo = GlobComboView.init(Series.TYPE, globRepository, directory)
      .setComparator(new GlobFieldComparator(Series.ID));
    splits.add(seriesCombo.getComponent());

    directory.get(SelectionService.class).addListener(new GlobSelectionListener() {
      public void selectionUpdated(GlobSelection selection) {
        GlobList topics = selection.getAll(Topic.TYPE);
        GlobMatcher matcher = topics.isEmpty() ? GlobMatchers.NONE : GlobMatchers.linkedTo(topics.get(0), Series.TOPIC);
        seriesCombo.setFilter(matcher);
      }
    }, Topic.TYPE);

    splits.setSource(MainView.class, "/layout/mainView.xml", "UTF-8");
    frame = splits.load();
    seriesView.setFrame(frame);

//    SplitsEditor.showInFrame(directory.get(ColorService.class), frame);
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
