package com.budgetview.desktop.importer;

import com.budgetview.shared.cloud.CloudSubscriptionStatus;
import org.globsframework.model.Glob;
import org.globsframework.model.Key;

import java.awt.*;
import java.io.File;
import java.util.List;
import java.util.Set;

public interface ImportDisplay {
  void showNoImport(Glob remove, boolean first);

  void showPreview();

  void updateForNextImport(String filePath, List<String> dateFormats, Glob importedAccount, Integer accountNumber, Integer accountCount);

  void showAccountPositionDialogsIfNeeded();

  void showCompleteMessage(Set<Integer> months, int importedTransactionCount, int ignoredTransactionCount, int autocategorizedTransactionCount);

  void showMessage(String message);

  void showMessage(String message, String details);

  void showLastImportedMonthAndClose(Set<Integer> months);

  void preselectFiles(List<File> files);

  void acceptFiles();

  void showCloudSignup();

  void showModifyCloudEmail();

  void showCloudEdition();

  void showCloudAccounts(Glob cloudProviderConnection);

  void showCloudValidationForSignup(String email);

  void showCloudValidationForEmailModification(String email);

  void showCloudEmailModificationCompleted(String newEmail);

  void showCloudBankSelection();

  void showCloudBankConnection(Key bank);

  void updatePassword(Glob cloudProviderConnection);

  void showCloudUnsubscription();

  void showCloudError(Exception e);

  void showCloudSubscriptionError(String email, CloudSubscriptionStatus status);

  void showCloudFirstDownload(Glob providerConnection);

  void showCloudDownload();

  Window getParentWindow();

  boolean askForSeriesImport(Set<Key> newSeries, Glob targetAccount);

  void closeDialog();
}
