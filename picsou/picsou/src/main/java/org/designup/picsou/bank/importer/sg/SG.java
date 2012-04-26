package org.designup.picsou.bank.importer.sg;

import com.gargoylesoftware.htmlunit.html.*;
import com.gargoylesoftware.htmlunit.javascript.host.Event;
import com.jidesoft.swing.InfiniteProgressPanel;
import org.designup.picsou.bank.BankSynchroService;
import org.designup.picsou.bank.importer.WebBankPage;
import org.designup.picsou.gui.description.PicsouDescriptionService;
import org.designup.picsou.gui.startup.components.OpenRequestManager;
import org.designup.picsou.gui.utils.ApplicationColors;
import org.designup.picsou.model.RealAccount;
import org.designup.picsou.model.util.Amounts;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.splits.SplitsBuilder;
import org.globsframework.gui.splits.TextLocator;
import org.globsframework.gui.splits.ui.UIService;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.format.DescriptionService;
import org.globsframework.model.repository.DefaultGlobIdGenerator;
import org.globsframework.model.repository.DefaultGlobRepository;
import org.globsframework.utils.Dates;
import org.globsframework.utils.Log;
import org.globsframework.utils.Strings;
import org.globsframework.utils.directory.DefaultDirectory;
import org.globsframework.utils.directory.Directory;

import javax.imageio.ImageReader;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class SG extends WebBankPage {
  private static final String INDEX = "https://particuliers.secure.societegenerale.fr/index.html";
  private static final String URL_TELECHARGEMENT = "https://particuliers.secure.societegenerale.fr/restitution/tel_telechargement.html";
  //  private static final String INDEX = "file:index.html";
  //  private static final String URL_TELECHARGEMENT = "file:tel_telechargement.html";
  public static final Integer SG_ID = 4;
  private JButton corriger;
  private SgKeyboardPanel keyboardPanel;
  private JButton valider;
  private JButton validerCode;
  private JTextField code;
  private JTextField passwordField;
  private InfiniteProgressPanel progressPanel = new InfiniteProgressPanel();


  public static void main(String[] args) throws IOException {
    DefaultDirectory defaultDirectory = new DefaultDirectory();
    defaultDirectory.add(TextLocator.class, Lang.TEXT_LOCATOR);
    defaultDirectory.add(SelectionService.class, new SelectionService());
    defaultDirectory.add(DescriptionService.class, new PicsouDescriptionService());
    OpenRequestManager openRequestManager = new OpenRequestManager();
    defaultDirectory.add(OpenRequestManager.class, openRequestManager);
    defaultDirectory.add(new UIService());
    ApplicationColors.registerColorService(defaultDirectory);
    openRequestManager.pushCallback(new OpenRequestManager.Callback() {
      public boolean accept() {
        return true;
      }

      public void openFiles(List<File> files) {
        System.out.println("read " + files.size());
      }
    });

    JFrame frame = new JFrame("test SG");
    defaultDirectory.add(JFrame.class, frame);
    frame.setSize(100, 100);
    frame.setVisible(true);
    SG sg = new SG(frame, defaultDirectory, new DefaultGlobRepository(new DefaultGlobIdGenerator()));
    sg.init();
    sg.show();
  }

  public static class Init implements BankSynchroService.BankSynchro {

    public GlobList show(Window parent, Directory directory, GlobRepository repository) {
      SG sg = SG.init(parent, directory, repository);
      sg.init();
      return sg.show();
    }
  }

  public SG(Window parent, final Directory directory, GlobRepository repository) {
    super(parent, directory, repository, SG_ID);
  }

  public static SG init(Window parent, final Directory directory, GlobRepository repository) {
    return new SG(parent, directory, repository);
  }

  public JPanel getPanel() {
    SplitsBuilder builder = SplitsBuilder.init(directory);
    builder.setSource(getClass(), "/layout/bank/connection/sgPanel.splits");
//    builder.add("occupedPanel", accupedPanel);
    initCardCode(builder);
    progressPanel.setVisible(true);
    progressPanel.start();
    Thread thread = new Thread() {
      public void run() {
        try {
          loadPage(INDEX);
          progressPanel.stop();
          SwingUtilities.invokeLater(new Runnable() {
            public void run() {
              validerCode.setEnabled(true);
            }
          });
        }
        catch (Exception e) {
          progressPanel.stop();
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

    validerCode = new JButton(Lang.get("bank.sg.code.valider"));
    validerCode.setName("validerCode");
    builder.add(validerCode);
    validerCode.addActionListener(new ValiderActionListener());

    passwordField = new JTextField();
    passwordField.setEditable(false);
    builder.add("password", passwordField);

    keyboardPanel = new SgKeyboardPanel(passwordField);
    keyboardPanel.setName("imageClavier");
    builder.add(keyboardPanel);

    corriger = new JButton(Lang.get("bank.sg.corriger"));
    corriger.setName("corriger");
    builder.add(corriger);

    valider = new JButton(Lang.get("bank.sg.valider"));
    valider.setName("valider");
    builder.add(valider);

    builder.add("progressPanel", progressPanel);

    validerCode.setEnabled(false);
    corriger.setEnabled(false);
    valider.setEnabled(false);
  }

  protected Double extractAmount(String position) {
    return Amounts.extractAmount(position.replace("EUR", ""));
  }

  private class ValiderActionListener implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      try {
        HtmlElement elementById = page.getElementById("codcli");
        ((HtmlInput)elementById).setValueAttribute(code.getText());
        page = page.getElementById("button").click();
        if (hasError) {
          hasError = false;
          return;
        }

        HtmlElement zoneClavier = page.getElementById("tc_cvcs");
        HtmlInput password = (HtmlInput)zoneClavier.getElementById("tc_visu_saisie");
        HtmlImage htmlImageClavier = zoneClavier.getElementById("img_clavier");
        htmlImageClavier.fireEvent(Event.TYPE_LOAD);
        final BufferedImage imageClavier = getFirstImage(htmlImageClavier);
        keyboardPanel.setSize(imageClavier.getWidth(), imageClavier.getHeight());
        HtmlElement map = zoneClavier.getElementById("tc_tclavier");
        keyboardPanel.setImage(imageClavier, map, password);

        HtmlImage corrigerImg = zoneClavier.getElementById("tc_corriger");
        corriger.setAction(new CorrigerActionListener(corrigerImg, password));
        corriger.setEnabled(true);

        HtmlImage validerImg = zoneClavier.getElementById("tc_valider");
        valider.setAction(new ValiderPwdActionListener(validerImg));
        valider.setEnabled(true);
      }
      catch (IOException e1) {
        e1.printStackTrace();
      }
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
          startOccuped();
          page = (HtmlPage)img.click();
          client.waitForBackgroundJavaScript(10000);

          if (page.getTitleText().contains("Erreur")) {
            return;
          }
          HtmlElement content = null;
          int count = 3;
          while (!hasError && content == null && count != 0) {
            content = page.getElementById("content");
            if (content == null) {
              page = (HtmlPage)img.click();
              client.waitForBackgroundJavaScript(10000);
            }
            count--;
          }

          if (!hasError) {
            content = page.getElementById("content");
            if (content == null) {
              hasError = false;
              return;
            }
            List<HtmlTable> tables = content.getElementsByAttribute(HtmlTable.TAG_NAME, "class", "LGNTableA ListePrestation");
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
                  if (htmlElements.size() > 0){
                    HtmlDivision element = (HtmlDivision)htmlElements.get(0);
                    String title = element.getAttribute("title");
                    if (Strings.isNotEmpty(title)){
                      date = Dates.extractDateDDMMYYYY(title);
                    }
                    position = element.getTextContent();
                  }
                  else {
                    position = cell.getTextContent();
                  }
                }
              }
              createOrUpdateRealAccount(type, name, position, date, SG_ID);
            }
            page = client.getPage(URL_TELECHARGEMENT);
            doImport();
          }
        }
        catch (IOException e1) {
          e1.printStackTrace();
        }finally {
          endOccuped();
        }
      }
    }
  }

  public void loadFile() {
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
    client.closeAllWindows();
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
    try {
      final ImageReader imageReader = img.getImageReader();
      return imageReader.read(0);
    }
    catch (IOException e) {
      throw new RuntimeException("Can not load image " + img.getId());
    }
  }
}
