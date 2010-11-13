package org.designup.picsou.gui.feedback;

import org.designup.picsou.gui.components.dialogs.PicsouDialog;
import org.designup.picsou.model.User;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.editors.GlobTextEditor;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.LocalGlobRepository;
import org.globsframework.model.utils.LocalGlobRepositoryBuilder;
import org.globsframework.utils.directory.DefaultDirectory;
import org.globsframework.utils.directory.Directory;

import java.awt.*;

public class FeedbackDialog {
  private Window parent;
  private GlobRepository repository;
  private LocalGlobRepository localRepository;
  private PicsouDialog dialog;
  private DefaultDirectory localDirectory;
  private SelectionService selectionService;
  private GlobsPanelBuilder builder;

  public FeedbackDialog(Window parent, GlobRepository repository, Directory directory) {
    this.parent = parent;
    this.repository = repository;
    this.localDirectory = new DefaultDirectory(directory);
    this.selectionService = new SelectionService();
    this.localDirectory.add(selectionService);

    this.localRepository =
      LocalGlobRepositoryBuilder.init(repository)
        .copy(User.TYPE)
        .get();

    dialog = PicsouDialog.create(parent, directory);

    builder = new GlobsPanelBuilder(getClass(), "/layout/general/feedbackDialog.splits",
                                    localRepository, this.localDirectory);

    builder.add("mail", GlobTextEditor.init(User.MAIL, localRepository, directory)
      .forceSelection(User.KEY));
  }
}
