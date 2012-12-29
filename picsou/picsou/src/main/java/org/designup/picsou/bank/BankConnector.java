package org.designup.picsou.bank;

import org.designup.picsou.bank.connectors.SynchroMonitor;

import javax.swing.*;

public interface BankConnector {
  JPanel getPanel();

  void init(SynchroMonitor monitor);

  void stop();
}
