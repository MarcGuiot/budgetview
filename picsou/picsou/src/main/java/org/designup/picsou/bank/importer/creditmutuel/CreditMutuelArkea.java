package org.designup.picsou.bank.importer.creditmutuel;

import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.TextPage;
import com.gargoylesoftware.htmlunit.WebResponse;
import com.gargoylesoftware.htmlunit.html.*;
import org.designup.picsou.bank.BankSynchroService;
import org.designup.picsou.bank.importer.WebBankPage;
import org.designup.picsou.gui.description.PicsouDescriptionService;
import org.designup.picsou.gui.startup.OpenRequestManager;
import org.designup.picsou.gui.utils.ApplicationColors;
import org.designup.picsou.model.RealAccount;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.splits.SplitsBuilder;
import org.globsframework.gui.splits.TextLocator;
import org.globsframework.gui.splits.ui.UIService;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.GlobList;
import org.globsframework.model.format.DescriptionService;
import org.globsframework.model.repository.DefaultGlobIdGenerator;
import org.globsframework.model.repository.DefaultGlobRepository;
import org.globsframework.utils.Log;
import org.globsframework.utils.directory.DefaultDirectory;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class CreditMutuelArkea extends WebBankPage implements PageAccessor {
  private JTextField codeField;
  private JButton validerCode;
  private String INDEX = "https://www.cmso.com/creditmutuel/cmso/index.jsp?fede=cmso";
  private JPasswordField passwordTextField;
  private HtmlTable accountsTable;
  public static final int ID = 15;

  public CreditMutuelArkea(Directory directory, GlobRepository repository, Integer bankId) {
    super(directory, repository, bankId);
  }

  public HtmlPage getPage() {
    return page;
  }

  public void setPage(HtmlPage page) {
    this.page = page;
  }

  public static class Init implements BankSynchroService.BankSynchro {

    public GlobList show(Directory directory, GlobRepository repository) {
      CreditMutuelArkea creditMutuelArkea = CreditMutuelArkea.init(directory, repository);
      creditMutuelArkea.init();
      return creditMutuelArkea.show();
    }
  }

  private static CreditMutuelArkea init(Directory directory, GlobRepository repository) {
    return new CreditMutuelArkea(directory, repository, ID);
  }


  public JPanel getPanel() {
    SplitsBuilder builder = SplitsBuilder.init(directory);
    builder.setSource(getClass(), "/layout/bank/connection/userAndPasswordPanel.splits");

    codeField = new JTextField();
    codeField.setName("code");
    builder.add(codeField);

    validerCode = new JButton("valider");
    validerCode.setName("validerCode");
    builder.add(validerCode);
    validerCode.addActionListener(new ValiderActionListener());

    passwordTextField = new JPasswordField();
    passwordTextField.setName("password");
    builder.add(passwordTextField);

    Thread thread = new Thread() {
      public void run() {
        try {
          loadPage(INDEX);
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

  public void loadFile() {
    PageChecker checker = new PageChecker(this);
    FormChecker formChecker = checker.getForm("choixCompte");
    for (Glob glob : this.accounts) {
      int count = accountsTable.getRowCount();
      for (int i = 1; i < count; i++) {
        if (accountsTable.getCellAt(i, 1).getTextContent().contains(glob.get(RealAccount.NAME))) {
          try {
            List<HtmlElement> elementList = accountsTable.getCellAt(i, 0).getHtmlElementsByTagName(HtmlInput.TAG_NAME);
            if (elementList.size() == 1) {
              elementList.get(0).click();
            }
          }
          catch (IOException e) {
            throw new RuntimeException(e);
          }
        }
      }
    }
    formChecker.getAnchorWithImg("valider.gif").click();
    FormChecker patametersChecker = checker.getForm("parametresForm");
    patametersChecker.getInputWithValue("2").select();
    checker.getAnchorWithImg("telecharger.gif").click();
    DomNodeList<HtmlElement> tables = page.getElementsByTagName(HtmlTable.TAG_NAME);
    HtmlTable table = (HtmlTable)tables.get(0);
    int count = table.getRowCount();
    for (int i = 1; i < count; i++) {
      HtmlTableCell at = table.getCellAt(i, 0);
      List<HtmlElement> htmlElements = at.getHtmlElementsByTagName(HtmlAnchor.TAG_NAME);
      if (!htmlElements.isEmpty()) {
        for (Glob glob : accounts) {
          HtmlElement link = htmlElements.get(0);
          if (link.getTextContent().contains(glob.get(RealAccount.NAME))) {
            File file = downloadFile(glob, link);
            repository.update(glob.getKey(), RealAccount.FILE_NAME, file.getAbsolutePath());
            break;
          }
        }
      }
    }
  }

  protected File downloadFile(Glob realAccount, HtmlElement anchor) {
    try {
      Page page1 = anchor.click();
      TextPage page = (TextPage)page1;
      WebResponse response = page.getWebResponse();
      InputStream contentAsStream = response.getContentAsStream();
      return createQifLocalFile(realAccount, contentAsStream, response.getContentCharset());
    }
    catch (IOException e) {
      Log.write("In anchor click", e);
      return null;
    }
  }

  private class ValiderActionListener implements ActionListener {

    public void actionPerformed(ActionEvent e) {
      HtmlForm form = page.getFormByName("formIdentification");
      HtmlInput personne = form.getInputByName("noPersonne");
      HtmlInput password = form.getInputByName("motDePasse");
      personne.setValueAttribute(codeField.getText());
      password.setValueAttribute(new String(passwordTextField.getPassword()));
      HtmlElement element = getAnchor(form);
      try {
        page = element.click();
        client.waitForBackgroundJavaScript(10000);
        HtmlElement elementById = getElementById("quotidien");
        getAnchor(elementById).click();
        PageChecker pageChecker = new PageChecker(CreditMutuelArkea.this);
        pageChecker.findAnchorContain("telechargement").click();
        HtmlElement comptes = pageChecker.getElementByName("choixCompte");
        accountsTable = (HtmlTable)comptes.getElementsByTagName(HtmlTable.TAG_NAME).get(1);
        int count = accountsTable.getRowCount();
        for (int i = 1; i < count; i++) {
          HtmlTableCell name = accountsTable.getCellAt(i, 1);
          HtmlTableCell position = accountsTable.getCellAt(i, 2);
          createOrUpdateRealAccount(name.getTextContent(), "", position.getTextContent(), ID);
        }
        doImport();
      }
      catch (IOException e1) {
        throw new RuntimeException(page.asXml(), e1);
      }
    }

    private HtmlElement getAnchor(HtmlElement form) {
      DomNodeList<HtmlElement> htmlElements = form.getElementsByTagName(HtmlAnchor.TAG_NAME);
      if (htmlElements.isEmpty()) {
        throw new RuntimeException("missing a link");
      }
      return htmlElements.get(0);
    }
  }

  public static void main(String[] args) throws IOException {
    DefaultDirectory defaultDirectory = new DefaultDirectory();
    defaultDirectory.add(SelectionService.class, new SelectionService());
    defaultDirectory.add(DescriptionService.class, new PicsouDescriptionService());
    defaultDirectory.add(TextLocator.class, Lang.TEXT_LOCATOR);
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
    CreditMutuelArkea creditMutuelArkea = new CreditMutuelArkea(defaultDirectory,
                                                                new DefaultGlobRepository(new DefaultGlobIdGenerator()), -1);
    creditMutuelArkea.init();
    creditMutuelArkea.show();
  }

  public static HtmlAnchorChecker getAnchorWithImage(String str, HtmlElement htmlElement, PageAccessor pageAccessor1) {
    DomNodeList<HtmlElement> htmlElements = htmlElement.getElementsByTagName(HtmlAnchor.TAG_NAME);
    for (HtmlElement element : htmlElements) {
      Iterable<HtmlElement> childElements = element.getChildElements();
      for (HtmlElement childElement : childElements) {
        if (childElement instanceof HtmlImage) {
          if (((HtmlImage)childElement).getSrcAttribute().contains(str)) {
            return new HtmlAnchorChecker(pageAccessor1, ((HtmlAnchor)element));
          }
        }
      }
    }
    throw new RuntimeException("no anchor with img " + str + " " + pageAccessor1.getPage().asXml());
  }

  private static class HtmlChecker {
    protected PageAccessor pageAccessor;
    protected HtmlElement currentElement;

    private HtmlChecker(PageAccessor pageAccessor, HtmlElement currentElement) {
      this.pageAccessor = pageAccessor;
      this.currentElement = currentElement;
    }

    public HtmlAnchorChecker getAnchorWithImg(String str) {
      return getAnchorWithImage(str, currentElement, pageAccessor);
    }
  }

  static class PageChecker {
    private PageAccessor page;

    public PageChecker(PageAccessor page) {
      this.page = page;
    }

    public HtmlAnchorChecker findAnchorContain(String str) {
      List<HtmlAnchor> anchors = page.getPage().getAnchors();
      for (HtmlAnchor anchor : anchors) {
        if (anchor.getHrefAttribute().contains(str)) {
          return new HtmlAnchorChecker(page, anchor);
        }
      }
      throw new RuntimeException("Can not find anchor with " + str + " in " + page.getPage().asXml());
    }

    public HtmlElement getElementByName(String s) {
      return page.getPage().getElementByName(s);
    }

    public FormChecker getForm(String name) {
      return new FormChecker(page, page.getPage().getFormByName(name));
    }

    public HtmlAnchorChecker getAnchorWithImg(String str) {
      return getAnchorWithImage(str, page.getPage().getDocumentElement(), page);
    }
  }

  private static class FormChecker extends HtmlChecker {
    private HtmlForm form;

    public FormChecker(PageAccessor pageAccessor, HtmlForm form) {
      super(pageAccessor, form);
      this.form = form;
    }

    public InputChecker getInputWithValue(String value) {
      HtmlInput input = form.getInputByValue(value);
      return new InputChecker(input);
    }
  }

  private static class HtmlAnchorChecker {
    private PageAccessor pageAccessor;
    private HtmlAnchor anchor;

    public HtmlAnchorChecker(PageAccessor pageAccessor, HtmlAnchor anchor) {
      this.pageAccessor = pageAccessor;
      this.anchor = anchor;
    }

    public void click() {
      try {
        this.pageAccessor.setPage((HtmlPage)anchor.click());
      }
      catch (IOException e) {
        throw new RuntimeException("click fail " + pageAccessor.getPage().asXml(), e);
      }
    }
  }

  private static class InputChecker {
    private HtmlInput input;

    public InputChecker(HtmlInput input) {
      this.input = input;
    }

    public void select() {
      input.setChecked(true);
    }
  }
}
