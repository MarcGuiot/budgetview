package org.designup.picsou.gui.utils;

import org.globsframework.model.GlobRepository;

import javax.swing.*;

public class PicsouFrame extends JFrame {
  private GlobRepository repository;

  public PicsouFrame(String title) {
    super(title);
  }

  public GlobRepository getRepository() {
    return repository;
  }

  public void setRepository(GlobRepository repository) {
    this.repository = repository;
  }
}
