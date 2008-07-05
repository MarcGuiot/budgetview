package com.gnosia.morphograph.gui;

import com.gnosia.morphograph.model.Exercise;
import com.gnosia.morphograph.utils.TemplateProcessor;
import org.globsframework.gui.splits.IconLocator;
import org.globsframework.gui.splits.SplitsBuilder;
import org.globsframework.gui.splits.color.ColorService;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;

public abstract class AbstractExoView implements ExoView {
  protected Glob exercise;
  protected GlobRepository globRepository;
  protected Directory directory;
  protected ColorService colorService;
  protected String templateFile;
  protected SplitsBuilder builder;
  protected Icon okIcon;
  protected Icon failedIcon;

  public AbstractExoView(Glob exercise, GlobRepository globRepository, Directory directory, String velocityFile) {
    this.exercise = exercise;
    this.globRepository = globRepository;
    this.directory = directory;
    this.colorService = directory.get(ColorService.class);
    this.templateFile = velocityFile;
    this.builder = new SplitsBuilder(directory);

    IconLocator iconLocator = directory.get(IconLocator.class);
    okIcon = iconLocator.get("accept.png");
    failedIcon = iconLocator.get("exclamation.png");
  }

  public String getName() {
    return exercise.get(Exercise.NAME);
  }

  public String getTitle() {
    return exercise.get(Exercise.TITLE);
  }

  public String getDescription() {
    return exercise.get(Exercise.DESCRIPTION);
  }

  public String getExample() {
    String example = exercise.get(Exercise.EXAMPLE);
    if ("".equals(example)) {
      return null;
    }
    return example;
  }

  public String getComment() {
    return exercise.get(Exercise.COMMENT);
  }

  public JPanel getPanel() {
    final String xml = createLayout();
    return builder.setSource(xml).load();
  }

  public String createLayout() {
    return TemplateProcessor
      .init(templateFile)
      .add("exo", this)
      .run();
  }
}
