package org.designup.picsou.bank.importer.sg;

import com.gargoylesoftware.htmlunit.*;
import com.gargoylesoftware.htmlunit.attachment.AttachmentHandler;
import com.gargoylesoftware.htmlunit.html.*;
import com.gargoylesoftware.htmlunit.javascript.host.Event;
import org.designup.picsou.bank.BankSynchroService;
import org.designup.picsou.bank.importer.BankPage;
import org.designup.picsou.gui.components.dialogs.MessageDialog;
import org.designup.picsou.gui.description.PicsouDescriptionService;
import org.designup.picsou.gui.startup.OpenRequestManager;
import org.designup.picsou.gui.utils.ApplicationColors;
import org.designup.picsou.model.RealAccount;
import org.designup.picsou.model.util.Amounts;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.splits.SplitsBuilder;
import org.globsframework.gui.splits.ui.UIService;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.format.DescriptionService;
import org.globsframework.model.impl.DefaultGlobIdGenerator;
import org.globsframework.model.impl.DefaultGlobRepository;
import org.globsframework.utils.Log;
import org.globsframework.utils.Strings;
import org.globsframework.utils.directory.DefaultDirectory;
import org.globsframework.utils.directory.Directory;

import javax.imageio.ImageReader;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class SG extends BankPage {
  private static final String INDEX = "https://particuliers.secure.societegenerale.fr/index.html";
  private static final String URL_TELECHARGEMENT = "https://particuliers.secure.societegenerale.fr/restitution/tel_telechargement.html";
  //  private static final String INDEX = "file:index.html";
  //  private static final String URL_TELECHARGEMENT = "file:tel_telechargement.html";
  public static final Integer SG_ID = 4;
  private JButton corriger;
  private ClavierPanel clavierPanel;
  private JButton valider;
  private JButton validerCode;
  private WebClient client;
  private HtmlPage page;
  private JTextField code;
  private SG.ErrorAlertHandler errorAlertHandler;
  private boolean hasError = false;
  private JTextField passwordTextField;

  public static void main(String[] args) throws IOException {
    DefaultDirectory defaultDirectory = new DefaultDirectory();
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
    SG sg = new SG(defaultDirectory, new DefaultGlobRepository(new DefaultGlobIdGenerator()));
    sg.init();
    sg.show();
  }

  public static class Init implements BankSynchroService.BankSynchro {

    public void show(Directory directory, GlobRepository repository) {
      SG sg = SG.init(directory, repository);
      sg.show();
    }
  }

  public SG(final Directory directory, GlobRepository repository) {
    super(directory, repository, SG_ID);
  }

  public static SG init(final Directory directory, GlobRepository repository) {
    SG sg = new SG(directory, repository);
    sg.init();
    return sg;
  }

  public JPanel getPanel() {
    SplitsBuilder builder = SplitsBuilder.init(directory);
    builder.setSource(getClass(), "/layout/connection/sgPanel.splits");

    initCardCode(builder);

    Thread thread = new Thread() {
      public void run() {
        try {
          loadPage();
          SwingUtilities.invokeLater(new Runnable() {
            public void run() {
              validerCode.setEnabled(true);
            }
          });
        }
        catch (IOException e) {
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
    passwordTextField = new JTextField();
    passwordTextField.setEditable(false);
    clavierPanel = new ClavierPanel(passwordTextField);
    clavierPanel.setName("imageClavier");
    builder.add(clavierPanel);
    corriger = new JButton(Lang.get("bank.sg.corriger"));
    corriger.setName("corriger");
    builder.add(corriger);
    valider = new JButton(Lang.get("bank.sg.valider"));
    valider.setName("valider");
    builder.add(valider);
    builder.add("password", passwordTextField);
    validerCode.setEnabled(false);
    corriger.setEnabled(false);
    valider.setEnabled(false);
  }

  private void loadPage() throws IOException {
    client = new WebClient();
    client.setCssEnabled(false);
    client.setJavaScriptEnabled(true);
    client.setCache(new Cache());
    client.setAjaxController(new NicelyResynchronizingAjaxController());
    page = (HtmlPage)client.getPage(INDEX);
    errorAlertHandler = new ErrorAlertHandler();
    client.setAlertHandler(errorAlertHandler);
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
        clavierPanel.setSize(imageClavier.getWidth(), imageClavier.getHeight());
        HtmlElement map = zoneClavier.getElementById("tc_tclavier");
        clavierPanel.setImage(imageClavier, map, password);
//        Dimension size = clavierPanel.getParent().getSize();
//        clavierPanel.setLocation((int)((size.getWidth() - imageClavier.getWidth()) * Math.random()),
//                                 (int)((size.getHeight() - imageClavier.getHeight()) * Math.random()));

        HtmlImage corrigerImg = zoneClavier.getElementById("tc_corriger");
        corriger.setAction(new CorrigerActionListener(corrigerImg, password));
        corriger.setEnabled(true);

        HtmlImage validerImg = zoneClavier.getElementById("tc_valider");
        valider.setAction(new ValiderPwdActionListener(validerImg));
        valider.setEnabled(true);
        dialog.validate();
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
          passwordTextField.setText(password.getValueAttribute());
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
            List<HtmlTable> tables = content.getElementsByAttribute(HtmlTable.TAG_NAME, "class", "LGNTableA");
            if (tables.size() != 1) {
              throw new RuntimeException("Find " + tables.size() + " table(s) in " + page.asXml());
            }
            HtmlTable table = tables.get(0);

            for (HtmlTableRow row : table.getRows()) {

              String type = null;
              String name = null;
              String position = null;
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
                  position = cell.getTextContent();
                }
              }
              createOrUpdateRealAccount(type, name, position, SG_ID);
            }
            showAccounts();
            page = client.getPage(URL_TELECHARGEMENT);
          }
        }
        catch (IOException e1) {
          e1.printStackTrace();
        }
      }
    }
  }

  public List<File> loadFile() {
    HtmlSelect compte = getElement("compte");
    List<HtmlOption> accountList = compte.getOptions();
    List<File> downloadedFiles = new ArrayList<File>();
    for (HtmlOption option : accountList) {
      Glob realAccount = find(option, accountsInPage);
      if (realAccount != null) {
        if (realAccount.get(RealAccount.IMPORTED)) {
          page = (HtmlPage)compte.setSelectedAttribute(option, true);
          File file = downloadFor(realAccount);
          if (file != null) {
            downloadedFiles.add(file);
          }
        }
      }
    }
    client.closeAllWindows();
    return downloadedFiles;
  }

  private Glob find(HtmlOption option, GlobList accounts) {
    for (Glob account : accounts) {
      String s = account.get(RealAccount.NAME);
      if (option.getTextContent().contains(s)) {
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

  private <T extends HtmlElement> T getElement(final String id) {
    T select = (T)page.getElementById(id);
    if (select == null) {
      throw new RuntimeException("Can not find tag '" + id + "' in :\n" + page.asXml());
    }
    return select;
  }

  private <T> T getFirst(List<T> htmlElements, final String attribute) {
    if (htmlElements.size() == 0) {
      throw new RuntimeException("no " + attribute + " in :\n" + page.asXml());
    }
    return htmlElements.get(0);
  }


  private class DownloadAttachmentHandler implements AttachmentHandler {
    private Page page;

    public void handleAttachment(Page page) {
      synchronized (this) {
        this.page = page;
        notifyAll();
      }
    }
  }

  private File downloadFor(Glob realAccount) {
    HtmlElement div = getElement("logicielFull");
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
    HtmlElement periodes = getElement("Periode");
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
          String s = ((HtmlInput)toDate).getValueAttribute();
          if (Strings.isNullOrEmpty(s)) {
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

    DownloadAttachmentHandler downloadAttachmentHandler = new DownloadAttachmentHandler();
    client.setAttachmentHandler(downloadAttachmentHandler);
    try {
      anchor.click();
    }
    catch (IOException e) {
      Log.write("In anchor click", e);
      return null;
    }
    synchronized (downloadAttachmentHandler) {
      if (downloadAttachmentHandler.page == null) {
        try {
          downloadAttachmentHandler.wait(3000);
        }
        catch (InterruptedException e1) {
        }
      }
    }
    if (downloadAttachmentHandler.page != null) {
      InputStream contentAsStream = downloadAttachmentHandler.page.getWebResponse().getContentAsStream();
      return createQifLocalFile(realAccount, contentAsStream);
    }
    else {
      Log.write("No download");
    }
    return null;
  }

  private HtmlAnchor findLink(List<HtmlAnchor> anchors, String ref) {
    for (HtmlAnchor anchor : anchors) {
      if (anchor.getHrefAttribute().contains(ref)) {
        return anchor;
      }
    }
    throw new RuntimeException("Can not find ref '" + ref + "' in :\n" + page.asXml());
  }

  private class ErrorAlertHandler implements AlertHandler {
    public void handleAlert(Page page, String s) {
      hasError = true;
      MessageDialog.show("bank.error", dialog, directory, "bank.error.msg", s);
    }
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
