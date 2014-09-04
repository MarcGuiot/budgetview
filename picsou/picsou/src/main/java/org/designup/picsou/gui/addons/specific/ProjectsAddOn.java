package org.designup.picsou.gui.addons.specific;

import org.designup.picsou.gui.addons.AddOn;
import org.designup.picsou.gui.card.CardView;
import org.designup.picsou.gui.components.tips.TipAnchor;
import org.designup.picsou.gui.components.tips.TipPosition;
import org.designup.picsou.gui.model.Card;
import org.designup.picsou.gui.signpost.SignpostService;
import org.designup.picsou.model.AddOns;
import org.designup.picsou.utils.Lang;
import org.globsframework.utils.directory.Directory;

public class ProjectsAddOn extends AddOn {
  public ProjectsAddOn() {
    super(AddOns.PROJECTS, "addons/projects.png");
  }

  protected void processPostActivation(Directory directory) {
    directory.get(SignpostService.class).show(CardView.getSignpostId(Card.PROJECTS),
                                              Lang.get("addons.projects.signpost"),
                                              TipPosition.BOTTOM_RIGHT,
                                              TipAnchor.SOUTH);
  }
}
