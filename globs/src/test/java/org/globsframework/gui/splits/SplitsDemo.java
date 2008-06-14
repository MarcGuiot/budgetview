package org.globsframework.gui.splits;


import org.globsframework.gui.splits.color.ColorService;

import javax.swing.*;

public class SplitsDemo {

  public static void main(String[] args) {
    loginPanel();
    samplePanel();
  }

  private static void samplePanel() {
    JTable table = new JTable(
      new Object[][]{
        {"sdfqsdf", "sdfs", "dfgwdfg", "wdfgwdfg"},
        {"ghj", "", "qsgfqdfh", "wfgqwfg"},
        {"rdtestg", "dfgsgsdfg", "dtgf", "wfdgh"},
        {"qstgsdfg", "sdfgsdfg", "hetykdghj", "etklmuykj"},
      },
      new String[]{
        "Nom", "Prenom", "Taille", "Autres"
      }
    );

    JList list = new JList(
      new String[]{"blah", "lkjml", "ojmlkj", "klhiuhyo"}
    );

    SplitsBuilder builder =
      SplitsBuilder.init(new ColorService(), IconLocator.NULL)
        .add("myTable", table)
        .add("myList", list)
        .add("button1", new JButton("button 1"))
        .add("button2", new JButton("button 2"))
        .add("button3", new JButton("button 3"));

    show(builder, "editor.xml");
  }

  private static void loginPanel() {
    SplitsBuilder builder =
      SplitsBuilder.init(new ColorService(), IconLocator.NULL)
        .add("userField", new JTextField())
        .add("passwordField", new JPasswordField())
        .add("createAccountCheckBox", new JCheckBox("Create account"))
        .add("loginButton", new JButton("Login"));

    show(builder, "login.xml");
  }

  private static void show(SplitsBuilder splits, String xmlFileName) {
    JFrame frame = (JFrame)splits.parse(SplitsDemo.class.getResourceAsStream("/demo/" + xmlFileName));
    frame.setVisible(true);
  }
}
