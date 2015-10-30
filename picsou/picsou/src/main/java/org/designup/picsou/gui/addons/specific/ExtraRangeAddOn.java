package org.designup.picsou.gui.addons.specific;

import org.designup.picsou.gui.addons.AddOn;
import org.designup.picsou.gui.card.CardView;
import org.designup.picsou.gui.components.tips.TipAnchor;
import org.designup.picsou.gui.components.tips.TipPosition;
import org.designup.picsou.gui.model.Card;
import org.designup.picsou.gui.signpost.SignpostService;
import org.designup.picsou.model.AddOns;
import org.designup.picsou.utils.Lang;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

public class ExtraRangeAddOn extends AddOn {
  public ExtraRangeAddOn() {
    super(AddOns.EXTRA_RANGE, "addons/projects.png");
  }

  protected void processPostActivation(GlobRepository repository, Directory directory) {
    directory.get(SignpostService.class).show(CardView.getSignpostId(Card.PROJECTS),
                                              Lang.get("addons.extrarange.signpost"),
                                              TipPosition.BOTTOM_RIGHT,
                                              TipAnchor.SOUTH);
  }
}
