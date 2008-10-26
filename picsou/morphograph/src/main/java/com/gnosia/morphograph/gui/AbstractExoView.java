package com.gnosia.morphograph.gui;

import com.gnosia.morphograph.model.Exercise;
import com.gnosia.morphograph.utils.TemplateProcessor;
import org.globsframework.gui.splits.ImageLocator;
import org.globsframework.gui.splits.SplitsBuilder;
import org.globsframework.gui.splits.color.ColorService;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;

public abstract class AbstractExoView implements ExoView {
  protected Glob exercise;
  protected GlobRepository repository;
  protected Directory directory;
  protected ColorService colorService;
  protected String templateFile;
  protected SplitsBuilder builder;
  protected Icon okIcon;
  protected Icon failedIcon;

  public AbstractExoView(Glob exercise, GlobRepository repository, Directory directory, String velocityFile) {
    this.exercise = exercise;
    this.repository = repository;
    this.directory = directory;
    this.colorService = directory.get(ColorService.class);
    this.templateFile = velocityFile;
    this.builder = new SplitsBuilder(directory);

    ImageLocator imageLocator = directory.get(ImageLocator.class);
    okIcon = imageLocator.get("accept.png");
    failedIcon = imageLocator.get("exclamation.png");
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
