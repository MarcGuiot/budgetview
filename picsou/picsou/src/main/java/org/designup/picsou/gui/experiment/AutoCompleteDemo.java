package org.designup.picsou.gui.experiment;

import com.jidesoft.swing.AutoCompletion;
import org.globsframework.gui.splits.utils.GuiUtils;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class AutoCompleteDemo {
  public static void main(String[] args) {
    JTextField textField = new JTextField();

    List<String> list = new ArrayList<String>();
    list.add("aaa");
    list.add("bbb ccc");
    list.add("ddd eee fff aaa");

    AutoCompletion autoCompletion = new AutoCompletion(textField, list);
    autoCompletion.setStrict(false);

    GuiUtils.show(textField);
  }
}
