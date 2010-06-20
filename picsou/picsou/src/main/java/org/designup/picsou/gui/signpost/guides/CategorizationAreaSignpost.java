package org.designup.picsou.gui.signpost.guides;

import net.java.balloontip.BalloonTip;
import org.designup.picsou.gui.signpost.SimpleSignpost;
import org.designup.picsou.model.SignpostStatus;
import org.designup.picsou.utils.Lang;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

public class CategorizationAreaSignpost extends SimpleSignpost {
  public CategorizationAreaSignpost(GlobRepository repository, Directory directory) {
    super(Lang.get("signpost.categorizationAreaSelection"),
          SignpostStatus.CATEGORIZATION_AREA_SHOWN,
          SignpostStatus.CATEGORIZATION_SELECTION_SHOWN,
          repository, directory);
    setLocation(BalloonTip.Orientation.RIGHT_ABOVE, BalloonTip.AttachLocation.NORTH);
  }
}