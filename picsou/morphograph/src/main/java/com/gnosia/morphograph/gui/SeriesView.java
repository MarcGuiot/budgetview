package com.gnosia.morphograph.gui;

import com.gnosia.morphograph.model.Exercise;
import com.gnosia.morphograph.model.ExerciseType;
import com.gnosia.morphograph.model.Series;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.splits.SplitsBuilder;
import org.globsframework.gui.splits.layout.SingleComponentPanels;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class SeriesView implements GlobSelectionListener {
  private GlobRepository repository;
  private Directory directory;
  private JPanel exoPanelContainer = new JPanel();
  private JPanel panel;
  private GlobList exercises;
  private int currentExerciseCount;
  private NextAction nextAction;
  private JFrame frame;

  public SeriesView(GlobRepository globRepository, Directory directory) {
    this.repository = globRepository;
    this.directory = directory;
    directory.get(SelectionService.class).addListener(this, Series.TYPE, Exercise.TYPE);

    SplitsBuilder builder = new SplitsBuilder(directory);
    builder.add("exoPanel", exoPanelContainer);
    nextAction = new NextAction();
    builder.add("next", nextAction);
    builder.setSource(SeriesView.class, "/layout/series.xml");
    panel = (JPanel)builder.load();
    panel.setOpaque(true);
    exoPanelContainer.setOpaque(true);
  }

  public Container createPanel() {
    return panel;
  }

  public void selectionUpdated(GlobSelection selection) {
    if (selection.isRelevantForType(Series.TYPE)) {
      GlobList series = selection.getAll(Series.TYPE);
      if (series.isEmpty()) {
        return;
      }
      clearPanel();
      exercises = repository.findLinkedTo(series.get(0), Exercise.SERIES).sort(Exercise.NAME);
      currentExerciseCount = 0;
      nextAction.reset();
      updateExoPanel();
    }
  }

  private void updateExoPanel() {
    if (exercises.size() <= currentExerciseCount) {
      SingleComponentPanels.install(exoPanelContainer, new JLabel("Aucun exercice")).setOpaque(true);
      return;
    }

    Glob exo = exercises.get(currentExerciseCount);

    ExoView exoView = null;
    switch (ExerciseType.get(exo)) {
      case INPUT:
        exoView = new InputExoView(exo, repository, directory);
        break;
      case SELECT:
        exoView = new SelectExoView(exo, repository, directory);
        break;
    }

    SingleComponentPanels.install(exoPanelContainer, exoView.getPanel()).setOpaque(true);
    frame.validate();
    frame.repaint();
  }

  private Component parseSplitsFile(SplitsBuilder builder, String fileName) {
    builder.setSource(getClass(), fileName);
    return builder.load();
  }

  private void clearPanel() {
    exoPanelContainer.removeAll();
  }

  public void setFrame(JFrame frame) {
    this.frame = frame;
  }

  private class NextAction extends AbstractAction {
    public NextAction() {
      super("Suivant");
    }

    public void actionPerformed(ActionEvent e) {
      currentExerciseCount++;
      updateExoPanel();
      if (currentExerciseCount >= exercises.size() - 1) {
        setEnabled(false);
      }
    }

    public void reset() {
      setEnabled(currentExerciseCount < exercises.size() - 1);
    }
  }
}
