package org.designup.picsou.bank.connectors;

import org.globsframework.model.GlobList;

public interface SynchroMonitor {

  void initialConnection();

  void waitingForUser();

  void identificationInProgress();

  void identificationFailed();

  void downloadInProgress();

  void preparingAccount(String accountName);

  void downloadingAccount(String accountName);

  void importCompleted(GlobList realAccounts);

  void errorFound(String errorMessage);

  void errorFound(Throwable exception);

  public static SynchroMonitor SILENT = new SynchroMonitor() {
    public void initialConnection() {
    }

    public void identificationInProgress() {
    }

    public void identificationFailed() {
    }

    public void waitingForUser() {
    }

    public void preparingAccount(String accountName) {
    }

    public void downloadInProgress() {
    }

    public void downloadingAccount(String accountName) {
    }

    public void errorFound(String errorMessage) {
    }

    public void errorFound(Throwable exception) {
    }

    public void importCompleted(GlobList realAccounts) {
    }
  };

}