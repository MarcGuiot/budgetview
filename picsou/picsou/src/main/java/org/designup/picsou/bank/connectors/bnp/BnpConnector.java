package org.designup.picsou.bank.connectors.bnp;

import com.budgetview.shared.utils.Amounts;
import com.gargoylesoftware.htmlunit.DownloadedContent;
import com.gargoylesoftware.htmlunit.HttpWebConnection;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.*;
import com.gargoylesoftware.htmlunit.javascript.host.Event;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.designup.picsou.bank.BankConnector;
import org.designup.picsou.bank.BankConnectorFactory;
import org.designup.picsou.bank.connectors.WebBankConnector;
import org.designup.picsou.bank.connectors.webcomponents.utils.HttpConnectionProvider;
import org.designup.picsou.bank.connectors.webcomponents.utils.WebConnectorLauncher;
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
import org.globsframework.utils.stream.ReplacementInputStreamBuilder;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class BnpConnector extends WebBankConnector implements HttpConnectionProvider {
  private static final int BANK_ID = 5;

  private static final String INDEX = "https://www.secure.bnpparibas.net/banque/portail/particulier/HomeConnexion?type=homeconnex";
  private static final String URL_TELECHARGEMENT
    = "https://particuliers.secure.societegenerale.fr/restitution/tel_telechargement.html";
  //  private static final String INDEX = "file:index.html";
  //  private static final String URL_TELECHARGEMENT = "file:tel_telechargement.html";
  private JButton corriger;
  private BnpKeyboardPanel keyboardPanel;
  private JButton valider;
  private JTextField code;
  private JTextField passwordField;
  private HtmlElement input;
  private BufferedImage clavier;

  public static void main(String[] args) throws IOException {
    WebConnectorLauncher.show(new Factory());
  }

  public static class Factory implements BankConnectorFactory {
    public BankConnector create(GlobRepository repository, Directory directory) {
      return new BnpConnector(repository, directory);
    }
  }

  private BnpConnector(GlobRepository repository, Directory directory) {
    super(BANK_ID, repository, directory);
    browser.setHttpConnectionProvider(this);
  }

  public HttpWebConnection getHttpConnection(WebClient client) {
    return new HttpWebConnection(client) {
      protected DownloadedContent downloadResponseBody(final HttpResponse httpResponse) throws IOException {
        final DownloadedContent content = super.downloadResponseBody(httpResponse);
        Header type = httpResponse.getEntity().getContentType();
        if (type.getValue() != null && type.getValue().contains("text/html")) {
          return new DownloadedContent() {
            public InputStream getInputStream() throws IOException {
              ReplacementInputStreamBuilder builder = new ReplacementInputStreamBuilder();
              builder.replace("maxlength=\"10\" value=\"\" name=\"ch1\" type=\"text\"&gt;".getBytes(),
                              "<INPUT size=\"10\" maxlength=\"6\" name=\"ch1\" value=\"\" type=\"text\" > ".getBytes());
              builder.replace("maxlength=\"6\" name=\"ch2\" value=\"\" type=\"password\" disabled &gt;>".getBytes(),
                              "<INPUT size=\"10\" maxlength=\"6\" name=\"ch2\" value=\"\" type=\"password\" disabled > ".getBytes());
              builder.replace("document.write('<INPUT size=\"10\" ');".getBytes(), " ".getBytes());
              builder.replace("document.write('<INPUT size=\"5\" ');".getBytes(), " ".getBytes());
              return builder.create(content.getInputStream());
            }
          };
        }
        else {
          return content;
        }
      }
    };
  }

  public JPanel getPanel() {
    SplitsBuilder builder = SplitsBuilder.init(directory);
    builder.setSource(getClass(), "/layout/bank/connection/bnpConnectorPanel.splits");
    initCardCode(builder);
    Thread thread = new Thread() {
      public void run() {
        try {
          notifyInitialConnection();
          loadPage(INDEX);
          System.out.println("BnpSync.run " + browser.dumpCurrentPage());
          notifyWaitingForUser();
          SwingUtilities.invokeLater(new Runnable() {
            public void run() {
              initImg();
            }
          });
        }
        catch (Exception e) {
          notifyWaitingForUser();
          e.printStackTrace();
        }
      }

    };
    thread.start();
    return builder.load();
  }

  private void initCardCode(SplitsBuilder builder) {
    code = new JTextField();
    code.setName("code");
    builder.add(code);

    passwordField = new JTextField();
    passwordField.setEditable(false);
    builder.add("password", passwordField);

    keyboardPanel = new BnpKeyboardPanel(passwordField);
    keyboardPanel.setName("imageClavier");
    builder.add(keyboardPanel);

    corriger = new JButton(Lang.get("bank.sg.corriger"));
    corriger.setName("corriger");
    builder.add(corriger);

    valider = new JButton(Lang.get("bank.sg.valider"));
    valider.setName("valider");
    builder.add(valider);
    valider.addActionListener(new ValiderActionListener());

    corriger.setEnabled(false);
    valider.setEnabled(false);
  }

  private void initImg() {
    HtmlElement body = page.getBody();
    getClient().waitForBackgroundJavaScript(10000);
    List<HtmlElement> attribute = body.getElementsByAttribute(HtmlInput.TAG_NAME, "name", "ch1");
    if (attribute.size() != 1) {
      throw new RuntimeException("Fail to find input name='ch1' (" + attribute.size() + " element ) in " + page.asXml());
    }
    input = attribute.get(0);

    List<HtmlElement> usemap = body.getElementsByAttribute(HtmlImage.TAG_NAME, "usemap", "#MapGril");
    if (usemap.size() != 1) {
      throw new RuntimeException("Can not find image " + usemap.size() + " in " + page.asXml());
    }
    HtmlImage image = (HtmlImage)usemap.get(0);
    image.fireEvent(Event.TYPE_LOAD);
    clavier = getFirstImage(image);
    keyboardPanel.setSize(clavier.getWidth(), clavier.getHeight());
    List<DomElement> name = (List)page.getElementsByName("MapGril");
    if (name.size() == 0) {
      throw new RuntimeException("Can not find MapGril" + " in " + page.asXml());
    }
    List<HtmlElement> password = body.getElementsByAttribute(HtmlInput.TAG_NAME, "name", "ch2");
    if (password.size() == 0) {
      throw new RuntimeException("Can not find input name='ch2'" + " in " + page.asXml());
    }
    keyboardPanel.setImage(clavier, (HtmlElement)name.get(0), (HtmlInput)password.get(0));
    corriger.setEnabled(true);
    valider.setEnabled(true);
  }

  protected Double extractAmount(String position) {
    return Amounts.extractAmount(position.replace("EUR", ""));
  }

  private class ValiderActionListener implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      String text = code.getText();
      System.out.println(text);
      input.setTextContent(text);
      String s = page.asXml();
      System.out.println("BnpSync$ValiderActionListener.actionPerformed " + s);
    }

    private class CorrigerActionListener extends AbstractAction {
      private HtmlImage img;
      private HtmlInput password;

      private CorrigerActionListener(HtmlImage img, HtmlInput password) {
        super(Lang.get("bank.sg.corriger"));
        this.img = img;
        this.password = password;
      }

      public void actionPerformed(ActionEvent e) {
        try {
          page = (HtmlPage)img.click();
          passwordField.setText(password.getValueAttribute());
        }
        catch (IOException e1) {
          e1.printStackTrace();
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
          notifyIdentification();
          page = (HtmlPage)img.click();
          getClient().waitForBackgroundJavaScript(10000);

          if (page.getTitleText().contains("Erreur")) {
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

          notifyDownloadInProgress();
          if (!hasError) {
            content = page.getElementById("content");
            if (content == null) {
              hasError = false;
              return;
            }

            List<HtmlTable> tables = ((HtmlElement)content).getElementsByAttribute(HtmlTable.TAG_NAME, "class", "LGNTableA");
            if (tables.size() != 1) {
              throw new RuntimeException("Found " + tables.size() + " table(s) in " + page.asXml());
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
        catch (IOException e1) {
          e1.printStackTrace();
        }
        finally {
          notifyWaitingForUser();
        }
      }
    }
  }

  public void downloadFile() {
    HtmlSelect compte = getElementById("compte");
    List<HtmlOption> accountList = compte.getOptions();
    for (HtmlOption option : accountList) {
      Glob realAccount = find(option, this.accounts);
      if (realAccount != null) {
        page = (HtmlPage)compte.setSelectedAttribute(option, true);
        File file = downloadFor(realAccount);
        if (file != null) {
          repository.update(realAccount.getKey(), RealAccount.FILE_NAME, file.getAbsolutePath());
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

  private File downloadFor(Glob realAccount) {
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

    return downloadFile(realAccount, anchor);
  }

  public static BufferedImage getFirstImage(HtmlImage img) {
    return WebBankConnector.getFirstImage(img);
  }
}
