package com.budgetview.gui.addons;

import com.budgetview.gui.addons.specific.*;

import java.util.ArrayList;
import java.util.List;

public class AddOnService {

  private List<AddOn> addOns = new ArrayList<AddOn>();

  public AddOnService() {
    addOns.add(new ExtraRangeAddOn());
    addOns.add(new ProjectsAddOn());
    addOns.add(new GroupsAddOn());
    addOns.add(new AnalysisAddOn());
    addOns.add(new MobileAddOn());
  }

  public List<AddOn> getAddOns() {
    return addOns;
  }
}
