package com.budgetview.gui.plaf;

import com.budgetview.gui.utils.Gui;
import org.globsframework.gui.splits.ImageLocator;
import org.globsframework.gui.splits.SplitsBuilder;
import org.globsframework.gui.splits.color.ColorService;
import org.globsframework.gui.splits.ui.UIService;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.utils.directory.DefaultDirectory;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class ButtonPanelItemUIDemo {
  public static void main(String[] args) {
    DefaultDirectory directory = new DefaultDirectory();
    directory.add(new ColorService());
    directory.add(new UIService());
    directory.add(ImageLocator.class, Gui.IMAGE_LOCATOR);

    SplitsBuilder builder = new SplitsBuilder(directory);
    builder.setSource("<splits>" +
                      "  <styles>" +
                      "    <ui name='ui' class='" + ButtonPanelItemUI.class.getName() + "'/>" +
                      "    <style selector='button' ui='ui' text='' fill='none' weightX='0'/>" +
                      "  </styles>" +
                      "  <row margin='15'>" +
                      "    <row>" +
                      "      <button action='add' icon='button_plus.png' disabledIcon='button_plus_disabled.png'/>" +
                      "      <button action='remove' icon='button_minus.png' disabledIcon='button_minus_disabled.png'/>" +
                      "      <button action='rename' icon='button_rename.png' disabledIcon='button_rename_disabled.png'/>" +
                      "      <button action='up' icon='button_up.png' disabledIcon='button_up_disabled.png'/>" +
                      "      <button action='down' icon='button_down.png' disabledIcon='button_down_disabled.png'/>" +
                      "      <filler fill='horizontal'/>" +
                      "    </row>" +
                      "  </row>" +
                      "</splits>");
    builder.add("add", new MyAction("Add"));
    builder.add("remove", new MyAction("Remove"));
    builder.add("rename", new MyAction("Rename"));
    builder.add("up", new MyAction("Up"));
    builder.add("down", new MyAction("Down"));

    JPanel panel = builder.load();
    GuiUtils.showCentered(panel);
  }

  private static class MyAction extends AbstractAction {
    private String text;

    public MyAction(String text) {
      super("");
      setEnabled(!text.contains("n"));
      this.text = text;
    }

    public void actionPerformed(ActionEvent e) {
      System.out.println(text);
    }
  }
}
