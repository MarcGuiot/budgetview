package org.designup.picsou.bank.connectors.sg;

import com.budgetview.shared.utils.Amounts;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.html.*;
import com.gargoylesoftware.htmlunit.javascript.host.Event;
import org.designup.picsou.bank.BankConnector;
import org.designup.picsou.bank.BankConnectorFactory;
import org.designup.picsou.bank.connectors.WebBankConnector;
import org.designup.picsou.bank.connectors.webcomponents.utils.WebConnectorLauncher;
import org.designup.picsou.gui.browsing.BrowsingAction;
import org.designup.picsou.model.RealAccount;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.splits.SplitsBuilder;
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
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class SgConnector extends WebBankConnector {
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
    WebConnectorLauncher.show(new Factory());
  }

  public static class Factory implements BankConnectorFactory {
    public BankConnector create(GlobRepository repository, Directory directory) {
      return new SgConnector(repository, directory);
    }
  }

  private SgConnector(GlobRepository repository, Directory directory) {
    super(BANK_ID, repository, directory);
  }

  protected JPanel createPanel() {
    SplitsBuilder builder = SplitsBuilder.init(directory);
    builder.setSource(getClass(), "/layout/bank/connection/sgConnectorPanel.splits");
    initCardCode(builder);
    Thread thread = new Thread() {
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
    };
    thread.start();
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

  protected Double extractAmount(String position) {
    return Amounts.extractAmount(position.replace("EUR", ""));
  }

  private class ValidateUserIdAction extends AbstractAction {

    private ValidateUserIdAction() {
      super(Lang.get("bank.sg.code.valider"));
    }

    public void actionPerformed(ActionEvent event) {
      try {
        DomElement elementById = page.getElementById("codcli");
        ((HtmlInput)elementById).setValueAttribute(userIdField.getText());
        notifyIdentificationInProgress();
        Page newPage = ((HtmlElement)page.getElementById("button")).click();
        notifyWaitingForUser();
//        System.out.println("SG$ValiderActionListener.actionPerformed " + newPage);
//        page = (HtmlPage)newPage;
        if (hasError) {
          hasError = false;
          return;
        }

        HtmlElement zoneClavier = (HtmlElement)page.getElementById("tc_cvcs");
        HtmlInput password = (HtmlInput)zoneClavier.getElementById("tc_visu_saisie");
        HtmlImage htmlImageClavier = zoneClavier.getElementById("img_clavier");
        htmlImageClavier.fireEvent(Event.TYPE_LOAD);
        final BufferedImage imageClavier = getFirstImage(htmlImageClavier);
        keyboardPanel.setSize(imageClavier.getWidth(), imageClavier.getHeight());
        List<HtmlElement> attribute = zoneClavier.getElementsByAttribute(HtmlMap.TAG_NAME, "name", "tc_tclavier");
        if (attribute.size() != 1) {
          throw new RuntimeException("Can not find tc_tclavier in" + zoneClavier.asXml());
        }
        HtmlElement map = (HtmlElement)attribute.get(0);
        keyboardPanel.setImage(imageClavier, map, password);

        HtmlImage corrigerImg = zoneClavier.getElementById("tc_corriger");
        corriger.setAction(new CorrigerActionListener(corrigerImg, password));
        corriger.setEnabled(true);

        HtmlImage validerImg = zoneClavier.getElementById("tc_valider");
        validateCode.setAction(new ValiderPwdActionListener(validerImg));
        validateCode.setEnabled(true);
      }
      catch (Exception e) {
        Log.write(page.asXml());
        notifyErrorFound(e);
      }
    }

    private class CorrigerActionListener extends AbstractAction {
      private HtmlImage img;
      private HtmlInput passwordInput;

      private CorrigerActionListener(HtmlImage img, HtmlInput passwordInput) {
        super(Lang.get("bank.sg.corriger"));
        this.img = img;
        this.passwordInput = passwordInput;
      }

      public void actionPerformed(ActionEvent event) {
        try {
          page = (HtmlPage)img.click();
          passwordField.setText(passwordInput.getValueAttribute());
        }
        catch (IOException e) {
          notifyErrorFound(e);
        }
      }
    }

    private class ValiderPwdActionListener extends AbstractAction {
      private HtmlImage img;

      public ValiderPwdActionListener(HtmlImage img) {
        super(Lang.get("bank.sg.valider"));
        this.img = img;
      }

      public void actionPerformed(ActionEvent e) {
        try {
          notifyDownloadInProgress();
          page = (HtmlPage)img.click();
          getClient().waitForBackgroundJavaScript(10000);

          if (page.getTitleText().contains("Erreur")) {
            notifyWaitingForUser();
            return;
          }

          DomElement content = null;
          int count = 3;
          while (!hasError && content == null && count != 0) {
            content = page.getElementById("content");
            if (content == null) {
              page = (HtmlPage)img.click();
              getClient().waitForBackgroundJavaScript(10000);
            }
            count--;
          }

          if (!hasError) {
            content = page.getElementById("content");
            if (content == null) {
              notifyWaitingForUser();
              hasError = false;
              return;
            }

            notifyDownloadInProgress();
            List<HtmlTable> tables = ((HtmlElement)content).getElementsByAttribute(HtmlTable.TAG_NAME, "class", "LGNTableA ListePrestation");
            if (tables.size() != 1) {
              throw new RuntimeException("Find " + tables.size() + " table(s) in " + page.asXml());
            }
            HtmlTable table = tables.get(0);
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
                  name = cell.getTextContent();
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
            page = getClient().getPage(URL_TELECHARGEMENT);
            doImport();
          }
        }
        catch (Exception e1) {
          notifyErrorFound(e1);
        }
        finally {
          notifyWaitingForUser();
        }
      }
    }
  }

  public void downloadFile() throws Exception {
    HtmlSelect compte = getElementById("compte");
    List<HtmlOption> accountList = compte.getOptions();
    for (int i = 0, size = accountList.size(); i < size; i++) {
      HtmlOption option = accountList.get(i);
      Glob realAccount = find(option, this.accounts);
      if (realAccount != null) {
        page = (HtmlPage)compte.setSelectedAttribute(option, true);
        File file = downloadFor(realAccount);
        if (file != null) {
          repository.update(realAccount.getKey(), RealAccount.FILE_NAME, file.getAbsolutePath());
        }
        else {
          try {
//            DomElement error = ((HtmlPage)client.getCurrentWindow().getEnclosedPage()).getElementById("div_NET2G");
//            DomNodeList<HtmlElement> name = error.getElementsByTagName(HtmlAnchor.TAG_NAME);
//            if (name.size() == 1 && name.get(0).hasAttribute()){
//              page = name.get(0).click();
//            }
//            else {
            page = getClient().getPage(URL_TELECHARGEMENT);
            compte = getElementById("compte");
            accountList = compte.getOptions();
//            }
          }
          catch (Exception e) {
            Log.write("Can not go back", e);
            try {
              page = getClient().getPage(URL_TELECHARGEMENT);
              compte = getElementById("compte");
              accountList = compte.getOptions();
            }
            catch (IOException e1) {
              notifyErrorFound(e1);
              return;
            }
          }
        }
      }
    }
    getClient().closeAllWindows();
  }

  private Glob find(HtmlOption option, GlobList accounts) {
    for (Glob account : accounts) {
      String number = account.get(RealAccount.NUMBER);
      if (option.getTextContent().contains(number)) {
        return account;
      }
    }
    return null;
  }

  private void setDate(HtmlElement fromDate, final int monthBack, final int dayback) {
    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
    Date today = new Date();
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(today);
    calendar.add(Calendar.MONTH, monthBack);
    calendar.add(Calendar.DAY_OF_MONTH, dayback);
    ((HtmlInput)fromDate).setValueAttribute(dateFormat.format(calendar.getTime()));
  }

  private <T> T getFirst(List<T> htmlElements, final String attribute) {
    if (htmlElements.size() == 0) {
      throw new RuntimeException("no " + attribute + " in :\n" + page.asXml());
    }
    return htmlElements.get(0);
  }

  private File downloadFor(Glob realAccount) throws Exception {
    HtmlElement div = getElementById("logicielFull");
    String style = div.getAttribute("style");
    if (Strings.isNotEmpty(style)) {
      return null;
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
    List<HtmlElement> from90LastDays = periodes.getElementsByAttribute(HtmlInput.TAG_NAME, "value", "XXJOURS");
    if (!from90LastDays.isEmpty()) {
      ((HtmlInput)from90LastDays.get(0)).setChecked(true);
    }
    else {
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
            setDate(toDate, 0, -1);
          }
        }
        setDate(fromDate, -3, 4);
      }
      else {
        Log.write("SG : can not find periode");
      }
    }
    HtmlAnchor anchor = findLink(page.getAnchors(), "telecharger");

    return downloadQifFile(realAccount, anchor);
  }
}
