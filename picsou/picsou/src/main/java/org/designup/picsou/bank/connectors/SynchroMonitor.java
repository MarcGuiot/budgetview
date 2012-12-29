package org.designup.picsou.bank.connectors;

import org.globsframework.model.GlobList;

public interface SynchroMonitor {

  void initialConnection();

  void identification();

  void downloadInProgress();

  void waitingForUser();

  void errorFound(String errorMessage);

  void importCompleted(GlobList realAccounts);

  public static SynchroMonitor SILENT = new SynchroMonitor() {
    public void initialConnection() {
    }

    public void identification() {
    }

    public void downloadInProgress() {
    }

    public void waitingForUser() {
    }

    public void errorFound(String errorMessage) {
    }

    public void importCompleted(GlobList realAccounts) {
    }
  };
}
