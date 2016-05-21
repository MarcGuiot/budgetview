package com.budgetview.bank;

import com.budgetview.bank.connectors.SynchroMonitor;

import javax.swing.*;

public interface BankConnector {

  String getLabel();

  Icon getIcon();

  JPanel getPanel();

  void init(SynchroMonitor monitor);

  void panelShown();

  String getCurrentLocation();

  void stop();

  void reset();

  void release();
}
