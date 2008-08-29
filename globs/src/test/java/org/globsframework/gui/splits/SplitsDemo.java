package org.globsframework.gui.splits;

import org.globsframework.utils.directory.DefaultDirectory;

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

    SplitsBuilder builder = SplitsBuilder.init(new DefaultDirectory());
    builder.add("myTable", table);
    builder.add("myList", list);
    builder.add("button1", new JButton("button 1"));
    builder.add("button2", new JButton("button 2"));
    builder.add("button3", new JButton("button 3"));

    show(builder, "editor.xml");
  }

  private static void loginPanel() {
    SplitsBuilder builder =
      SplitsBuilder.init(new DefaultDirectory());
    builder.add("userField", new JTextField());
    builder.add("passwordField", new JPasswordField());
    builder.add("createAccountCheckBox", new JCheckBox("Create account"));
    builder.add("loginButton", new JButton("Login"));

    show(builder, "login.xml");
  }

  private static void show(SplitsBuilder splits, String xmlFileName) {
    JFrame frame = (JFrame)splits.setSource(SplitsDemo.class, "/demo/" + xmlFileName).load();
    frame.setVisible(true);
  }
}
