package org.designup.picsou.gui.addons;

import org.designup.picsou.gui.addons.specific.AnalysisAddOn;
import org.designup.picsou.gui.addons.specific.GroupsAddOn;
import org.designup.picsou.gui.addons.specific.ProjectsAddOn;

import java.util.ArrayList;
import java.util.List;

public class AddOnService {

  private List<AddOn> addOns = new ArrayList<AddOn>();

  public AddOnService() {
    addOns.add(new ProjectsAddOn());
    addOns.add(new GroupsAddOn());
    addOns.add(new AnalysisAddOn());
  }

  public List<AddOn> getAddOns() {
    return addOns;
  }
}
