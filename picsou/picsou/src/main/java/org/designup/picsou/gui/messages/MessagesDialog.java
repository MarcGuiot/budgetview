package org.designup.picsou.gui.messages;

import org.designup.picsou.gui.components.dialogs.PicsouDialog;
import org.designup.picsou.gui.description.Formatting;
import org.designup.picsou.gui.model.AccountPositionError;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.splits.SplitsBuilder;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.repository.LocalGlobRepository;
import org.globsframework.model.repository.LocalGlobRepositoryBuilder;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

public class MessagesDialog {
  private Directory directory;
  private GlobRepository repository;

  public MessagesDialog(Directory directory, GlobRepository repository) {
    this.directory = directory;
    this.repository = repository;
  }

  public void show() {
    LocalGlobRepository localRepository =
      LocalGlobRepositoryBuilder.init(repository)
        .copy(AccountPositionError.TYPE)
        .get();

    List<MessageDisplay> displays = new ArrayList<MessageDisplay>();
    GlobList all = localRepository.getAll(AccountPositionError.TYPE);
    int i =0;
    for (Glob glob : all) {
      Integer fullDate = glob.get(AccountPositionError.LAST_PREVIOUS_IMPORT_DATE);
      String date = fullDate != null ? Formatting.toString(fullDate) : null;
      displays.add(new AbstractMsg(localRepository, ++i, glob, AccountPositionError.UPDATE_DATE, AccountPositionError.CLEARED,
                                   Lang.get("messages.account.position.error.msg" + (date != null ? ".date" : ""),
                                            glob.get(AccountPositionError.ACCOUNT_NAME),
                                            glob.get(AccountPositionError.LAST_REAL_OPERATION_POSITION),
                                            glob.get(AccountPositionError.IMPORTED_POSITION),
                                            date)));
    }
    MessagesPanel messagesPanel = new MessagesPanel(directory, displays);
    PicsouDialog dialog = PicsouDialog.create(directory.get(JFrame.class), directory);
    SplitsBuilder splitsBuilder = SplitsBuilder.init(directory);
    splitsBuilder.setSource(getClass(), "/layout/messages/messagesDialog.splits");
    splitsBuilder.add("messagesPanel", messagesPanel.create());

    dialog.addPanelWithButtons(splitsBuilder.<JPanel>load(), new ValidateAction(dialog, localRepository), new CancelAction(dialog));
    dialog.pack();
    dialog.showCentered();
  }

  private static class ValidateAction extends AbstractAction {
    private final PicsouDialog dialog;
    private final LocalGlobRepository repository;

    public ValidateAction(PicsouDialog dialog, LocalGlobRepository repository) {
      super(Lang.get("ok"));
      this.dialog = dialog;
      this.repository = repository;
    }

    public void actionPerformed(ActionEvent e) {
      repository.commitChanges(true);
      dialog.setVisible(false);
    }
  }

  private static class CancelAction extends AbstractAction {
    private PicsouDialog dialog;

    public CancelAction(PicsouDialog dialog) {
      super(Lang.get("cancel"));
      this.dialog = dialog;
    }

    public void actionPerformed(ActionEvent e) {
      dialog.setVisible(false);
    }
  }
}
