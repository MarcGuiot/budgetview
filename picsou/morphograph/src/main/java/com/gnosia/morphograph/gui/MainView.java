package com.gnosia.morphograph.gui;

import com.gnosia.morphograph.model.Series;
import com.gnosia.morphograph.model.Topic;
import org.crossbowlabs.globs.gui.GlobSelection;
import org.crossbowlabs.globs.gui.GlobSelectionListener;
import org.crossbowlabs.globs.gui.SelectionService;
import org.crossbowlabs.globs.gui.views.GlobComboView;
import org.crossbowlabs.globs.model.GlobList;
import org.crossbowlabs.globs.model.GlobRepository;
import org.crossbowlabs.globs.model.utils.GlobFieldComparator;
import org.crossbowlabs.globs.model.utils.GlobMatcher;
import org.crossbowlabs.globs.model.utils.GlobMatchers;
import org.crossbowlabs.globs.utils.directory.Directory;
import org.crossbowlabs.splits.IconLocator;
import org.crossbowlabs.splits.SplitsBuilder;
import org.crossbowlabs.splits.color.ColorService;

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

    frame = (JFrame)splits.parse(MainView.class.getResourceAsStream("/layout/mainView.xml"), "UTF-8");
    seriesView.setFrame(frame);

//    ColorServiceEditor.showInFrame(directory.get(ColorService.class), frame);
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
