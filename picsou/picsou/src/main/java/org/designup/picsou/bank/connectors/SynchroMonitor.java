package org.designup.picsou.bank.connectors;

import org.globsframework.model.GlobList;

public interface SynchroMonitor {

  void initialConnection();

  void identificationInProgress();

  void identificationFailed();

  void downloadInProgress();

  void waitingForUser();

  void errorFound(String errorMessage);

  void errorFound(Throwable exception);

  void importCompleted(GlobList realAccounts);

  void notifyDownload(String description);

  public static SynchroMonitor SILENT = new SynchroMonitor() {
    public void initialConnection() {
    }

    public void identificationInProgress() {
    }

    public void identificationFailed() {
    }

    public void downloadInProgress() {
    }

    public void waitingForUser() {
    }

    public void errorFound(String errorMessage) {
    }

    public void errorFound(Throwable exception) {
    }

    public void importCompleted(GlobList realAccounts) {
    }

    public void notifyDownload(String description) {
    }
  };

}
