package org.designup.picsou.bank.connectors;

import org.globsframework.model.GlobList;

import javax.swing.*;

public class SwingSynchroMonitor implements SynchroMonitor {
  private final SynchroMonitor synchroMonitor;

  public SwingSynchroMonitor(SynchroMonitor synchroMonitor) {
    this.synchroMonitor = synchroMonitor;
  }

  public void initialConnection() {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        synchroMonitor.initialConnection();
      }
    });
  }

  public void waitingForUser() {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        synchroMonitor.waitingForUser();
      }
    });
  }

  public void identificationInProgress() {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        synchroMonitor.identificationInProgress();
      }
    });

  }

  public void identificationFailed() {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        synchroMonitor.identificationFailed();
      }
    });
  }

  public void downloadInProgress() {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        synchroMonitor.downloadInProgress();
      }
    });
  }

  public void preparingAccount(final String accountName) {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        synchroMonitor.preparingAccount(accountName);
      }
    });
  }

  public void downloadingAccount(final String accountName) {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        synchroMonitor.downloadingAccount(accountName);
      }
    });

  }

  public void importCompleted(final GlobList realAccounts) {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        synchroMonitor.importCompleted(realAccounts);
      }
    });
  }

  public void errorFound(final String errorMessage) {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        synchroMonitor.errorFound(errorMessage);
      }
    });
  }

  public void errorFound(final Throwable exception) {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        synchroMonitor.errorFound(exception);
      }
    });

  }
}
