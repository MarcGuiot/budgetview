package org.globs.samples.blog.gui;

import org.crossbowlabs.globs.gui.SelectionService;
import org.crossbowlabs.globs.gui.GlobSelectionListener;
import org.crossbowlabs.globs.gui.GlobSelection;
import org.crossbowlabs.globs.gui.actions.CreateGlobAction;
import org.crossbowlabs.globs.gui.editors.GlobTextEditor;
import org.crossbowlabs.globs.gui.editors.GlobMultiLineTextEditor;
import org.crossbowlabs.globs.gui.views.GlobTableView;
import org.crossbowlabs.globs.metamodel.GlobType;
import org.crossbowlabs.globs.metamodel.fields.StringField;
import org.crossbowlabs.globs.model.Glob;
import org.crossbowlabs.globs.model.GlobRepository;
import org.crossbowlabs.globs.model.GlobRepositoryBuilder;
import org.crossbowlabs.globs.model.GlobList;
import org.crossbowlabs.globs.model.format.DescriptionService;
import org.crossbowlabs.globs.model.format.Formats;
import org.crossbowlabs.globs.model.format.utils.DefaultDescriptionService;
import org.crossbowlabs.globs.model.utils.GlobFieldComparator;
import org.crossbowlabs.globs.model.utils.GlobMatchers;
import org.crossbowlabs.globs.utils.directory.DefaultDirectory;
import org.crossbowlabs.globs.utils.directory.Directory;
import org.crossbowlabs.globs.utils.logging.Debug;
import org.crossbowlabs.splits.IconLocator;
import org.crossbowlabs.splits.SplitsBuilder;
import org.crossbowlabs.splits.color.ColorService;
import org.crossbowlabs.splits.utils.JarIconLocator;
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
