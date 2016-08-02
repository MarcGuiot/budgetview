package com.budgetview.bank.connectors.sg;

import com.budgetview.bank.BankConnector;
import com.budgetview.bank.BankConnectorFactory;
import com.budgetview.bank.connectors.WebBankConnector;
import com.budgetview.bank.connectors.webcomponents.*;
import com.budgetview.bank.connectors.webcomponents.utils.WebCommandFailed;
import com.budgetview.desktop.browsing.BrowsingAction;
import com.budgetview.shared.utils.Amounts;
import com.gargoylesoftware.htmlunit.*;
import com.gargoylesoftware.htmlunit.html.*;
import com.gargoylesoftware.htmlunit.javascript.host.event.Event;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.varia.NullAppender;
import com.budgetview.bank.connectors.webcomponents.utils.HttpConnectionProvider;
import com.budgetview.bank.connectors.webcomponents.utils.WebConnectorLauncher;
import com.budgetview.bank.connectors.webcomponents.utils.WebParsingError;
import com.budgetview.model.RealAccount;
import com.budgetview.utils.Lang;
import org.globsframework.gui.splits.SplitsBuilder;
import org.globsframework.gui.splits.utils.Disposable;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.Dates;
import org.globsframework.utils.Log;
import org.globsframework.utils.Strings;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

public class SgConnector extends WebBankConnector implements HttpConnectionProvider {
  private static final String INDEX = "https://particuliers.secure.societegenerale.fr/index.html";
  private static final String URL_TELECHARGEMENT = "https://particuliers.secure.societegenerale.fr/restitution/tel_telechargement.html";
  //  private static final String INDEX = "file:index.html";
  //  private static final String URL_TELECHARGEMENT = "file:tel_telechargement.html";
  public static final Integer BANK_ID = 4;
  private JButton corriger;
  private SgKeyboardPanel keyboardPanel;
  private ValidateUserIdAction validateUserIdAction;
  private JButton validateCode;
  private JTextField userIdField;
  private JTextField passwordField;

  public static void main(String[] args) throws IOException {
    BasicConfigurator.configure(new NullAppender());
    Logger.getRootLogger().setLevel(Level.ERROR);
    WebConnectorLauncher.show(BANK_ID, new Factory());
  }

  public HttpWebConnection getHttpConnection(WebClient client) {
    return new HttpWebConnection(client) {
      public WebResponse getResponse(WebRequest request) throws IOException {
        String s = request.getUrl().toString();
//        System.out.println("SgConnector.getResponse " + s);
        if (s.startsWith("https://logs128.xiti.com") || s.startsWith("https://societegenerale.solution.weborama.fr")
            || s.startsWith("https://ssl.weborama.fr")) {
          return new StringWebResponse("", request.getUrl());
        }
        WebResponse response = super.getResponse(request);
//        System.out.println("SgConnector.getResponse " + response.getLoadTime() + " ms.");
        return response;
      }
    };
  }

  public static class Factory implements BankConnectorFactory {
    public BankConnector create(GlobRepository repository, Directory directory, boolean syncExistingAccount, Glob synchro) {
      return new SgConnector(syncExistingAccount, repository, directory, synchro);
    }
  }

  private SgConnector(boolean syncExistingAccount, GlobRepository repository, Directory directory, Glob synchro) {
    super(BANK_ID, syncExistingAccount, repository, directory, synchro);
    this.setBrowserVersion(BrowserVersion.INTERNET_EXPLORER_11);
  }

  protected JPanel createPanel() {
    final SplitsBuilder builder = SplitsBuilder.init(directory);
    builder.setSource(getClass(), "/layout/bank/connection/sgConnectorPanel.splits");
    initCardCode(builder);
    directory.get(ExecutorService.class)
      .submit(new Runnable() {
        public void run() {
          try {
            notifyInitialConnection();
            loadPage(INDEX);
            SwingUtilities.invokeLater(new Runnable() {
              public void run() {
                validateUserIdAction.setEnabled(true);
              }
            });
          }
          catch (Exception e) {
            notifyErrorFound(e);
          }
          finally {
            notifyWaitingForUser();
          }
        }
      });
    addToBeDisposed(new Disposable() {
      public void dispose() {
        builder.dispose();
      }
    });

    return builder.load();
  }

  public void panelShown() {
    userIdField.requestFocus();
  }

  public void reset() {
    // TODO
  }

  private void initCardCode(SplitsBuilder builder) {
    validateUserIdAction = new ValidateUserIdAction();
    builder.add("validateUserId", validateUserIdAction);

    userIdField = new JTextField();
    userIdField.addActionListener(validateUserIdAction);

    userIdField.setText(getSyncCode());
    builder.add("userIdField", userIdField);

    builder.add("userIdHelp", new BrowsingAction("Aide Société Générale", directory) {
      protected String getUrl() {
        return "https://particuliers.societegenerale.fr/faq.html";
      }
    });

    passwordField = new JTextField();
    passwordField.setEditable(false);
    builder.add("password", passwordField);

    keyboardPanel = new SgKeyboardPanel(passwordField);
    builder.add("imageClavier", keyboardPanel);

    corriger = new JButton(Lang.get("bank.sg.corriger"));
    builder.add("corriger", corriger);

    validateCode = new JButton(Lang.get("bank.sg.valider"));
    builder.add("validateCode", validateCode);

    validateUserIdAction.setEnabled(false);
    corriger.setEnabled(false);
    validateCode.setEnabled(false);
  }

  protected Double extractAmount(WebTableCell cell) {
    return Amounts.extractAmount(cell.asText().replace("EUR", ""));
  }

  public String getCode() {
    return userIdField.getText();
  }

  private class ValidateUserIdAction extends AbstractAction {

    private ValidateUserIdAction() {
      super(Lang.get("bank.sg.code.valider"));
    }

    public void actionPerformed(ActionEvent event) {
      try {
        WebTextInput codcli = browser.getCurrentPage().getTextInputById("codcli");
        codcli.setText(userIdField.getText());
        notifyIdentificationInProgress();
        browser.getCurrentPage().getInputById("button").click();
        notifyWaitingForUser();
        if (hasError) {
          hasError = false;
          return;
        }

        WebPanel zoneClavier = browser.getCurrentPage().getPanelById("tc_cvcs");
        WebTextInput password = zoneClavier.getTextInputById("tc_visu_saisie");
        WebImage htmlImageClavier = zoneClavier.getImageById("img_clavier");
        htmlImageClavier.fireEvent(Event.TYPE_LOAD);
        final BufferedImage imageClavier = htmlImageClavier.getFirstImage();
        keyboardPanel.setSize(imageClavier.getWidth(), imageClavier.getHeight());
        WebMap map = zoneClavier.getMapByName("tc_tclavier");
        keyboardPanel.setImage(imageClavier, map, password, zoneClavier);

        WebImage corrigerImg = zoneClavier.getImageById("tc_corriger");
        corriger.setAction(new CorrigerActionListener(corrigerImg, password));
        corriger.setEnabled(true);

        WebImage validerImg = zoneClavier.getImageById("tc_valider");
        validateCode.setAction(new ValiderPwdActionListener(validerImg));
        validateCode.setEnabled(true);
      }
      catch (Exception e) {
        notifyErrorFound(e);
      }
    }

    private class CorrigerActionListener extends AbstractAction {
      private WebImage img;
      private WebTextInput password;
      private HtmlInput passwordInput;

      private CorrigerActionListener(WebImage img, WebTextInput password) {
        super(Lang.get("bank.sg.corriger"));
        this.img = img;
        this.password = password;
      }

      public void actionPerformed(ActionEvent event) {
        try {
          img.click();
          passwordField.setText(password.getValue());
          passwordField.setText(passwordInput.getValueAttribute());
        }
        catch (WebParsingError error) {
          notifyErrorFound(error);
        }
      }
    }
  }

  private class ValiderPwdActionListener extends AbstractAction {
    private WebImage img;

    public ValiderPwdActionListener(WebImage img) {
      super(Lang.get("bank.sg.valider"));
      this.img = img;
    }

    public void actionPerformed(ActionEvent e) {
      ExecutorService executorService = directory.get(ExecutorService.class);
      executorService.submit(new Callable<Object>() {
        public Object call() throws Exception {
          try {
            notifyDownloadInProgress();
            img.click();
            browser.waitForBackgroundJavaScript(10000);

            if (browser.getCurrentPage().getTitle().contains("Erreur")) {
              notifyErrorFound(browser.getCurrentPage().asText());
              return null;
            }

            WebPanel content = null;
            int count = 3;
            while (!hasError && content == null && count != 0) {
              content = browser.getCurrentPage().findPanelById("content");
              if (content == null) {
                img.click();
                browser.waitForBackgroundJavaScript(10000);
              }
              count--;
            }

            if (!hasError) {
              content = browser.getCurrentPage().findPanelById("content");
              if (content == null) {
                notifyWaitingForUser();
                hasError = false;
                return null;
              }

              notifyDownloadInProgress();
              WebTable tables = content.getTableWithClass("LGNTableA ListePrestation");
              HtmlTable table = tables.getTable();
              for (HtmlTableRow row : table.getRows()) {

                String type = null;
                String name = null;
                String position = null;
                Date date = null;
                for (HtmlTableCell cell : row.getCells()) {
                  if (cell.getTagName().equals(HtmlTableHeaderCell.TAG_NAME)) {
                    continue;
                  }
                  String columnName = cell.getAttribute("headers");
                  if (columnName.equalsIgnoreCase("TypeCompte")) {
                    DomNodeList<HtmlElement> htmlElements = cell.getElementsByTagName(HtmlAnchor.TAG_NAME);
                    if (htmlElements.isEmpty()) {
                      continue;
                    }
                    HtmlElement element = htmlElements.get(0);
                    type = element.getTextContent();
                  }
                  else if (columnName.equalsIgnoreCase("NumeroCompte")) {
                    name = Strings.replaceSpace(cell.getTextContent());
                  }
                  else if (columnName.equalsIgnoreCase("solde")) {
                    List<HtmlElement> htmlElements = cell.getElementsByAttribute(HtmlDivision.TAG_NAME, "class", "Solde");
                    if (htmlElements.size() > 0) {
                      HtmlDivision element = (HtmlDivision)htmlElements.get(0);
                      String title = element.getAttribute("title");
                      if (Strings.isNotEmpty(title)) {
                        date = Dates.extractDateDDMMYYYY(title);
                      }
                      position = element.getTextContent();
                    }
                    else {
                      position = cell.getTextContent();
                    }
                  }
                }
                createOrUpdateRealAccount(type, name, position, date, BANK_ID);
              }
              browser.load(URL_TELECHARGEMENT);
              doImport();
            }
          }
          catch (final WebCommandFailed failed) {
            notifyErrorFound(failed);
          }
          catch (final Exception e1) {
            notifyErrorFound(e1);
          }
          finally {
            notifyWaitingForUser();
          }
          return null;
        }
      });
    }
  }

  public void downloadFile() throws Exception {
    notifyInitialConnection();
    WebSelect compte = browser.getCurrentPage().getSelectById("compte");
    List<String> accountList = compte.getEntryNames();
    for (int i = 0, size = accountList.size(); i < size; i++) {
      try {
        String option = accountList.get(i);
        Glob realAccount = find(option, this.accounts);
        if (realAccount != null) {
          compte.select(option);
          notifyDownloadForAccount(getAccountName(realAccount));
          String content = downloadFor(realAccount);
          if (Strings.isNotEmpty(content)) {
            localRepository.update(realAccount.getKey(), RealAccount.FILE_CONTENT, content);
          }
        }
      }
      catch (Exception e) {
        Log.write("Can not go back", e);
        browser.load(URL_TELECHARGEMENT);
        compte = browser.getCurrentPage().getSelectById("compte");
        accountList = compte.getEntryNames();
      }
    }
    getClient().close();
  }

  private Glob find(String option, GlobList accounts) {
    for (Glob account : accounts) {
      String number = account.get(RealAccount.NUMBER);
      if (Strings.replaceSpace(option).contains(number)) {
        return account;
      }
    }
    return null;
  }

  private <T> T getFirst(List<T> htmlElements, final String attribute) {
    if (htmlElements.size() == 0) {
      throw new RuntimeException("no " + attribute + " in :\n" + page.asXml());
    }
    return htmlElements.get(0);
  }

  private String downloadFor(Glob realAccount) throws Exception {
    HtmlElement div = getElementById("logicielFull");
    String style = div.getAttribute("style");
    if (Strings.isNotEmpty(style)) {
      browser.waitForBackgroundJavaScript(1000);
      style = div.getAttribute("style");
      if (Strings.isNotEmpty(style)) {
        return null;
      }
    }
    List<HtmlSelect> htmlSelectLogicielFull =
      div.getElementsByAttribute(HtmlSelect.TAG_NAME, "id", "logicielFull");
    HtmlSelect logiciel = getFirst(htmlSelectLogicielFull, "logicelFull");

    List<HtmlOption> softwareList = logiciel.getOptions();
    boolean found = false;
    for (HtmlOption option : softwareList) {
      if (option.getValueAttribute().equals("MONEY")) {
        logiciel.setSelectedAttribute(option, true);
        found = true;
        break;
      }
    }
    if (!found) {
      Log.write("MONEY not found.");
      return null;
    }
    HtmlElement periodes = getElementById("Periode");
//    List<HtmlElement> from90LastDays = periodes.getElementsByAttribute(HtmlInput.TAG_NAME, "value", "XXJOURS");
//    if (!from90LastDays.isEmpty()) {
//      ((HtmlInput)from90LastDays.get(0)).setChecked(true);
//    }
//    else {
    List<HtmlElement> periodesFromTo = periodes.getElementsByAttribute(HtmlInput.TAG_NAME, "value", "INTERVALLE");
    if (periodesFromTo.isEmpty()) {
      periodesFromTo = periodes.getElementsByAttribute(HtmlInput.TAG_NAME, "value", "PREMIER");
    }
    if (!periodesFromTo.isEmpty()) {
      ((HtmlInput)periodesFromTo.get(0)).setChecked(true);
      List<HtmlElement> fromDates = periodes.getElementsByAttribute(HtmlInput.TAG_NAME, "name", "datedu");
      HtmlElement fromDate = getFirst(fromDates, "datedu");
      List<HtmlElement> toDates = periodes.getElementsByAttribute(HtmlInput.TAG_NAME, "name", "dateau");
      if (!toDates.isEmpty()) {
        HtmlElement toDate = getFirst(toDates, "dateau");
        String value = ((HtmlInput)toDate).getValueAttribute();
        if (Strings.isNullOrEmpty(value)) {
          final int dayback = -1;
          String date = shiftDateddMMyyy(0, dayback);
          ((HtmlInput)toDate).setValueAttribute(date);
        }
      }
      final int monthBack = -3;
      String date = shiftDateddMMyyy(monthBack, 4);
      ((HtmlInput)fromDate).setValueAttribute(date);
    }
    else {
      Log.write("SG : can not find periode");
    }
//    }
    WebAnchor link = browser.getCurrentPage().getAnchorWithRef("javascript:telecharger(this)");
    return link.clickAndDownload().readAsQif(realAccount);
  }
}
