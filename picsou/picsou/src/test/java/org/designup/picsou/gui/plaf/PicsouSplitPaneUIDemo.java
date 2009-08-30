package org.designup.picsou.gui.plaf;

import org.designup.picsou.gui.utils.Gui;
import org.globsframework.gui.splits.ImageLocator;
import org.globsframework.gui.splits.SplitsBuilder;
import org.globsframework.gui.splits.color.ColorService;
import org.globsframework.gui.splits.ui.UIService;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.utils.directory.DefaultDirectory;

import javax.swing.*;

public class PicsouSplitPaneUIDemo {
  public static void main(String[] args) {
    DefaultDirectory directory = new DefaultDirectory();
    directory.add(new ColorService());
    directory.add(new UIService());
    directory.add(ImageLocator.class, Gui.IMAGE_LOCATOR);

    SplitsBuilder builder = new SplitsBuilder(directory);
    builder.setSource("<splits>" +
                      "  <styles>" +
                      "    <ui name='ui' class='" + PicsouSplitPaneUI.class.getName() + "'" +
                      "        handleColor='#FF0000'/>" +
                      "    <style selector='horizontalSplit' ui='ui'/>" +
"  </styles>" +
"<panel background='#1e64c2' opaque='true'>" +
"  <row margin='15' preferredSize='(500,500)'>" +
"    <horizontalSplit dividerSize='30'>" +
"      <list/>" +
"      <table/>" +
"    </horizontalSplit>" +
"  </row>" +
"</panel>" +
"</splits>");

    JPanel panel = builder.load();
    GuiUtils.show(panel);
  }

}
