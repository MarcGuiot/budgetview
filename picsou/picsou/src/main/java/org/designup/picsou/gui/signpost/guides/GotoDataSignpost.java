package org.designup.picsou.gui.signpost.guides;

import org.designup.picsou.gui.model.Card;
import org.designup.picsou.gui.signpost.SimpleSignpost;
import org.designup.picsou.model.SignpostStatus;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.SelectionService;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

public class GotoDataSignpost extends SimpleSignpost implements GlobSelectionListener {
  public GotoDataSignpost(GlobRepository repository, Directory directory) {
    super(Lang.get("signpost.gotoData"), SignpostStatus.GOTO_DATA_DONE, SignpostStatus.WELCOME_SHOWN, repository, directory);
  }

  protected void init() {
    super.init();
    directory.get(SelectionService.class).addListener(this, Card.TYPE);
  }

  public void selectionUpdated(GlobSelection selection) {
    Glob card = selection.getAll(Card.TYPE).getFirst();
    if (isShowing() && (card != null && Card.get(card) == Card.DATA)) {
      dispose();
    }
  }
}
