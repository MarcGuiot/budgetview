package com.gnosia.morphograph.gui;

import javax.swing.*;

public interface ExoView {
  String getName();

  String getTitle();

  String getDescription();

  String getExample();

  JPanel getPanel();
}
