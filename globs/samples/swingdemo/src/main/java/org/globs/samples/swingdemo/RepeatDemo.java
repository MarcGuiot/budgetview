package org.globs.samples.swingdemo;

import org.globs.samples.swingdemo.model.Movie;
import org.globsframework.gui.ComponentHolder;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.splits.IconLocator;
import org.globsframework.gui.splits.color.ColorService;
import org.globsframework.gui.views.GlobRepeatView;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import org.globsframework.model.format.DescriptionService;
import org.globsframework.model.format.Formats;
import org.globsframework.model.format.utils.DefaultDescriptionService;
import org.globsframework.model.utils.GlobFieldComparator;
import org.globsframework.utils.Dates;
import org.globsframework.utils.directory.DefaultDirectory;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class RepeatDemo {
  public static void main(String[] args) {
    GlobRepository repository = GlobRepositoryBuilder.createEmpty();

    createMovie(repository, "Shrek", Dates.parse("2005/12/25"));

    Directory directory = initDirectory();

    GlobsPanelBuilder builder = new GlobsPanelBuilder(RepeatDemo.class, "/repeatdemo.splits", repository, directory);
    builder.addTable("movie", Movie.TYPE, new GlobFieldComparator(Movie.TITLE))
      .addColumn(Movie.TITLE)
      .addColumn(Movie.DATE)
      .addColumn(Movie.DIRECTOR);
    builder.addCreateAction("+", "newMovie", Movie.TYPE);
    builder.addDeleteAction("-", "deleteMovie", Movie.TYPE);
    builder.addEditor(Movie.TITLE);
    builder.addEditor(Movie.DIRECTOR);


    builder.add("repeat", GlobRepeatView.init(Movie.TYPE, repository, directory, new GlobFieldComparator(Movie.TITLE),
                                              new GlobRepeatView.Factory() {
                                                public ComponentHolder getComponent(final Glob glob, GlobRepository repository, Directory directory) {
                                                  return new MoviePanel(glob, repository, directory);
                                                }
                                              }).getComponent());


    JFrame frame = (JFrame)builder.load();
    frame.setVisible(true);

    createMovie(repository, "Bambi", Dates.parse("2005/12/26"));
  }

  private static void createMovie(GlobRepository repository, String title, Date date) {
    repository.create(Movie.TYPE,
                      FieldValue.value(Movie.TITLE, title),
                      FieldValue.value(Movie.DATE, date),
                      FieldValue.value(Movie.DIRECTOR, "moi"));
  }

  private static Directory initDirectory() {
    Directory directory = new DefaultDirectory();
    directory.add(new ColorService());
    directory.add(IconLocator.class, IconLocator.NULL);
    directory.add(new SelectionService());
    directory.add(DescriptionService.class,
                  new DefaultDescriptionService(Formats.DEFAULT, "lang", Locale.ENGLISH, RepeatDemo.class.getClassLoader()));
    return directory;
  }

  private static class MoviePanel implements ComponentHolder, ChangeSetListener {
    private GlobRepository repository;
    private Key key;
    private JPanel jPanel;

    public MoviePanel(Glob glob, GlobRepository repository, Directory directory) {
      this.key = glob.getKey();
      this.repository = repository;
      repository.addChangeListener(this);

      GlobsPanelBuilder builder = new GlobsPanelBuilder(RepeatDemo.class, "/moviepanel.splits", repository, directory);
      builder.add("title", new JLabel(glob.get(Movie.TITLE)));
      builder.add("date", new JLabel(Dates.toString(glob.get(Movie.DATE))));
      builder.add("director", new JLabel(glob.get(Movie.DIRECTOR)));
      jPanel = (JPanel)builder.load();
    }

    public JComponent getComponent() {
      return jPanel;
    }

    public void dispose() {
      repository.removeChangeListener(this);
    }

    public ComponentHolder setName(String name) {
      jPanel.setName(name);
      return this;
    }

    public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
      Glob glob = repository.find(key);
    }

    public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
    }

    public void globsReset(GlobRepository repository, List<GlobType> changedTypes) {
    }
  }
}
