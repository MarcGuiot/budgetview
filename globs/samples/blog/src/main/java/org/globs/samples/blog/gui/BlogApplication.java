package org.globs.samples.blog.gui;

import org.globsframework.globs.gui.SelectionService;
import org.globsframework.globs.gui.GlobSelectionListener;
import org.globsframework.globs.gui.GlobSelection;
import org.globsframework.globs.gui.actions.CreateGlobAction;
import org.globsframework.globs.gui.editors.GlobTextEditor;
import org.globsframework.globs.gui.editors.GlobMultiLineTextEditor;
import org.globsframework.globs.gui.views.GlobTableView;
import org.globsframework.globs.metamodel.GlobType;
import org.globsframework.globs.metamodel.fields.StringField;
import org.globsframework.globs.model.Glob;
import org.globsframework.globs.model.GlobRepository;
import org.globsframework.globs.model.GlobRepositoryBuilder;
import org.globsframework.globs.model.GlobList;
import org.globsframework.globs.model.format.DescriptionService;
import org.globsframework.globs.model.format.Formats;
import org.globsframework.globs.model.format.utils.DefaultDescriptionService;
import org.globsframework.globs.model.utils.GlobFieldComparator;
import org.globsframework.globs.model.utils.GlobMatchers;
import org.globsframework.globs.utils.directory.DefaultDirectory;
import org.globsframework.globs.utils.directory.Directory;
import org.globsframework.globs.utils.logging.Debug;
import org.globsframework.gui.splits.IconLocator;
import org.globsframework.gui.splits.SplitsBuilder;
import org.globsframework.gui.splits.color.ColorService;
import org.globsframework.gui.splits.utils.JarIconLocator;
import org.globs.samples.blog.model.Post;

import javax.swing.*;
import java.util.Locale;
import java.util.ResourceBundle;
import java.awt.event.ActionEvent;

public class BlogApplication {

  public static void main(String[] args) {
    GlobRepository repository = GlobRepositoryBuilder.createEmpty();

    Directory directory = new DefaultDirectory();
    final SelectionService selectionService = new SelectionService();
    directory.add(selectionService);
    directory.add(DescriptionService.class,
                  new DefaultDescriptionService(Formats.DEFAULT, "lang", Locale.ENGLISH, BlogApplication.class.getClassLoader()));
    ColorService colorService = new ColorService();
    directory.add(colorService);
    directory.add(IconLocator.class, new JarIconLocator(BlogApplication.class, ""));

    SplitsBuilder splits = new SplitsBuilder(colorService);
    addTable("draftPosts", false, splits, repository, directory);
    addTable("publishedPosts", true, splits, repository, directory);

    splits.add("newPost", new CreateGlobAction("New Post", Post.TYPE, repository, directory) {
      protected Glob doCreate(GlobType type, StringField namingField, String name, GlobRepository repository) {
        Glob newPost = super.doCreate(type, namingField, name, repository);
        selectionService.select(newPost);
        return newPost;
      }
    });

    splits.add("titleField", GlobTextEditor.init(Post.TITLE, repository, directory).getComponent());
    splits.add("contentField", GlobMultiLineTextEditor.init(Post.CONTENT, repository, directory).getComponent());
    splits.add("publish", new PublishAction(repository, directory));

    JFrame frame = (JFrame) splits.parse(BlogApplication.class, "/blogapplication.splits");
    frame.setVisible(true);
  }

  private static void addTable(String name, boolean published, SplitsBuilder splits, GlobRepository repository, Directory directory) {
    GlobTableView view = GlobTableView.init(Post.TYPE, repository, new GlobFieldComparator(Post.TITLE), directory)
      .addColumn(Post.TITLE)
      .addColumn(Post.CATEGORY);
    splits.add(name, view.getComponent());
    view.setFilter(GlobMatchers.fieldEquals(Post.PUBLISHED, published));
  }

  private static class PublishAction extends AbstractAction implements GlobSelectionListener {
    private GlobList selectedPosts;
    private GlobRepository repository;

    private PublishAction(GlobRepository repository, Directory directory) {
      super("Publish");
      this.repository = repository;
      directory.get(SelectionService.class).addListener(this, Post.TYPE);
      setEnabled(false);
    }

    public void selectionUpdated(GlobSelection selection) {
      selectedPosts = selection.getAll(Post.TYPE);
      setEnabled(!selectedPosts.isEmpty());
    }

    public void actionPerformed(ActionEvent e) {
      for (Glob post : selectedPosts) {
        repository.update(post.getKey(), Post.PUBLISHED, true);
      }
    }
  }
}
