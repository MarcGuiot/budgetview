package org.globs.samples.swingdemo;

import org.globsframework.globs.gui.GlobsPanelBuilder;
import org.globsframework.globs.gui.SelectionService;
import org.globsframework.globs.gui.ComponentHolder;
import org.globsframework.globs.gui.views.GlobRepeatView;
import org.globsframework.globs.metamodel.GlobType;
import org.globsframework.globs.model.*;
import org.globsframework.globs.model.format.DescriptionService;
import org.globsframework.globs.model.format.Formats;
import org.globsframework.globs.model.format.utils.DefaultDescriptionService;
import org.globsframework.globs.model.utils.GlobFieldComparator;
import org.globsframework.globs.utils.Dates;
import org.globsframework.globs.utils.directory.DefaultDirectory;
import org.globsframework.globs.utils.directory.Directory;
import org.globsframework.gui.splits.IconLocator;
import org.globsframework.gui.splits.color.ColorService;
import org.globs.samples.swingdemo.model.Movie;

import javax.swing.*;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RepeatDemo {
  public static void main(String[] args) {
    GlobRepository repository = GlobRepositoryBuilder.createEmpty();

    createMovie(repository, "Shrek", Dates.parse("2005/12/25"));

    Directory directory = initDirectory();

    GlobsPanelBuilder builder = GlobsPanelBuilder.init(repository, directory);
    builder.addTable(Movie.TYPE, new GlobFieldComparator(Movie.TITLE))
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


    JFrame frame = (JFrame) builder.parse(RepeatDemo.class, "/repeatdemo.splits");
    frame.setVisible(true);

    createMovie(repository, "Bambi", Dates.parse("2005/12/26"));
  }

  private static void createMovie(GlobRepository repository, String title, Date date) {
    repository.create(Movie.TYPE,
                      FieldValuesBuilder.init()
                              .set(Movie.TITLE, title)
                              .set(Movie.DATE, date)
                              .set(Movie.DIRECTOR, "moi")
                              .get());
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

      GlobsPanelBuilder builder = GlobsPanelBuilder.init(repository, directory);
      builder.add("title", new JLabel(glob.get(Movie.TITLE)));
      builder.add("date", new JLabel(Dates.toString(glob.get(Movie.DATE))));
      builder.add("director", new JLabel(glob.get(Movie.DIRECTOR)));
      jPanel = (JPanel) builder.parse(RepeatDemo.class, "/moviepanel.splits");
    }

    public JComponent getComponent() {
      return jPanel;
    }

    public void dispose() {
      repository.removeChangeListener(this);
    }

    public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
      Glob glob = repository.find(key);
    }

    public void globsReset(GlobRepository repository, List<GlobType> changedTypes) {
    }
  }
}
