package com.budgetview.desktop.cloud.utils;

import com.budgetview.desktop.importer.ImportDisplay;
import org.globsframework.model.Glob;
import org.globsframework.model.Key;
import org.globsframework.utils.exceptions.NotSupported;

import java.io.File;
import java.util.List;
import java.util.Set;

public abstract class SilentImportDisplay implements ImportDisplay {
  public void preselectFiles(List<File> files) {
    throw new NotSupported();
  }

  public void acceptFiles() {
    throw new NotSupported();
  }

  public void showCloudSignup() {
    throw new NotSupported();
  }

  public void showModifyCloudEmail() {
    throw new NotSupported();
  }

  public void showCloudEdition() {
    throw new NotSupported();
  }

  public void showCloudAccounts(Glob cloudProviderConnection) {
    throw new NotSupported();
  }

  public void showCloudValidationForSignup(String email) {
    throw new NotSupported();
  }

  public void showCloudValidationForEmailModification(String email) {
    throw new NotSupported();
  }

  public void showCloudEmailModificationCompleted(String newEmail) {
    throw new NotSupported();
  }

  public void showCloudBankSelection() {
    throw new NotSupported();
  }

  public void showCloudBankConnection(Key bank) {
    throw new NotSupported();
  }

  public void showCloudFirstDownload(Glob providerConnection) {
    throw new NotSupported();
  }

  public void showCloudDownload() {
    throw new NotSupported();
  }

  public boolean askForSeriesImport(Set<Key> newSeries, Glob targetAccount) {
    throw new NotSupported();
  }

  public void showNoImport(Glob remove, boolean first) {
    throw new NotSupported();
  }

  public void updateForNextImport(String filePath, List<String> dateFormats, Glob importedAccount, Integer accountNumber, Integer accountCount) {
    throw new NotSupported();
  }

  public void showAccountPositionDialogsIfNeeded() {
    throw new NotSupported();
  }

  public void showCompleteMessage(Set<Integer> months, int importedTransactionCount, int ignoredTransactionCount, int autocategorizedTransactionCount) {
    throw new NotSupported();
  }

  public void showMessage(String message) {
    throw new NotSupported();
  }

  public void showMessage(String message, String details) {
    throw new NotSupported();
  }

  public void showLastImportedMonthAndClose(Set<Integer> months) {
    throw new NotSupported();
  }

  public void updatePassword(Glob cloudProviderConnection) {
    throw new NotSupported();
  }

  public void showCloudUnsubscription() {
    throw new NotSupported();
  }

  public void showCloudError(Exception e) {
    throw new NotSupported();
  }
}
