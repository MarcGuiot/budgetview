package org.designup.picsou.gui.importer.additionalactions;

import org.designup.picsou.gui.components.tips.ErrorTip;
import org.designup.picsou.gui.importer.AdditionalImportPanel;
import org.designup.picsou.model.Account;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.editors.GlobLinkComboEditor;
import org.globsframework.gui.splits.repeat.Repeat;
import org.globsframework.gui.splits.repeat.RepeatCellBuilder;
import org.globsframework.gui.splits.repeat.RepeatComponentFactory;
import org.globsframework.gui.splits.utils.AutoDispose;
import org.globsframework.gui.splits.utils.Disposable;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.format.DescriptionService;
import org.globsframework.model.format.GlobStringifier;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;

import java.util.Collections;

import static org.globsframework.model.utils.GlobMatchers.*;

public class AccountTypeSelectionPanel implements AdditionalImportPanel {
  private GlobRepository repository;
  private Directory directory;

  private JPanel panel;
  private ErrorTip errorTip;
  private Repeat<Glob> repeat;
  private boolean showErrors;
  private GlobStringifier accountStringifier;

  public AccountTypeSelectionPanel(GlobRepository repository, Directory directory) {
    this.repository = repository;
    this.directory = directory;
    DescriptionService descriptionService = directory.get(DescriptionService.class);
    accountStringifier = descriptionService.getStringifier(Account.TYPE);
    createPanel();
  }

  public boolean shouldBeDisplayed(boolean showErrors) {
    if (errorTip != null) {
      errorTip.dispose();
      errorTip = null;
    }
    this.showErrors = showErrors;

    GlobList accounts = repository.getAll(Account.TYPE,
                                          and(isNull(Account.ACCOUNT_TYPE),
                                              isTrue(Account.IS_VALIDATED),
                                              not(fieldEquals(Account.ID, Account.EXTERNAL_ACCOUNT_ID))
                                              // pour ne pas avoir en meme temps
                                              // AccountTypeSelection et ChooseOrCreateAccout
                                          ));
    return !accounts.isEmpty();
  }

  public JPanel getPanel() {
    this.repeat.set(repository
      .getAll(Account.TYPE, and(isNull(Account.ACCOUNT_TYPE), not(fieldEquals(Account.ID, Account.EXTERNAL_ACCOUNT_ID))))
      .sort(accountStringifier.getComparator(repository)));
    return panel;
  }

  private void createPanel() {
    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/accounts/accountTypeSelectionPanel.splits",
                                                      repository, directory);


    repeat = builder.addRepeat("accountTypeRepeat", Collections.<Glob>emptyList(), new RepeatComponentFactory<Glob>() {
      public void registerComponents(RepeatCellBuilder cellBuilder, Glob account) {
        String accountName =
          accountStringifier.toString(account, repository);

        cellBuilder.add("accountName", new JLabel(accountName));

        final GlobLinkComboEditor typeEditor =
          GlobLinkComboEditor.init(Account.ACCOUNT_TYPE, repository, directory)
            .setShowEmptyOption(false)
            .forceSelection(account.getKey())
            .setName("Combo:" + accountName);

        JComboBox combo = typeEditor.getComponent();
        cellBuilder.add("accountType", combo);

        if (showErrors && (errorTip == null)) {
          errorTip = ErrorTip.showRight(combo, Lang.get("import.accountTypeSelectionPanel.errorTip"), directory);
          AutoDispose.registerComboSelection(combo, errorTip);
        }

        cellBuilder.addDisposeListener(new Disposable() {
          public void dispose() {
            typeEditor.dispose();
            if (errorTip != null) {
              errorTip.dispose();
            }
          }
        });
      }
    });

    panel =  builder.load();
  }
}
