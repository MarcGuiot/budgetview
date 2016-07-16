package com.budgetview.gui.license.activation;

import com.budgetview.gui.components.dialogs.PicsouDialog;
import com.budgetview.gui.config.ConfigService;
import com.budgetview.gui.components.utils.CustomFocusTraversalPolicy;
import com.budgetview.http.HttpBudgetViewConstants;
import com.budgetview.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.splits.PanelBuilder;
import org.globsframework.gui.splits.repeat.RepeatComponentFactory;
import org.globsframework.gui.splits.utils.Disposable;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.Log;
import org.globsframework.utils.Strings;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class LicenseConfirmationFeedbackPanel implements Disposable {

  private GlobRepository repository;
  private Directory directory;

  private GlobsPanelBuilder builder;
  private JPanel panel;
  private String email;
  private Action closeAction;
  private List<Question> questions = new ArrayList<Question>();

  public LicenseConfirmationFeedbackPanel(GlobRepository repository, Directory directory) {
    this.repository = repository;
    this.directory = directory;
    for (int i = 0; i < 3; i++) {
      questions.add(new Question(i));
    }
  }

  public void install(PicsouDialog dialog, String email, Action closeAction) {
    this.email = email;
    this.closeAction = closeAction;

    builder = new GlobsPanelBuilder(getClass(), "/layout/license/activation/licenseConfirmationFeedbackPanel.splits",
                                    repository, directory);

    builder.addRepeat("questions", questions, new RepeatComponentFactory<Question>() {
      public void registerComponents(PanelBuilder cellBuilder, final Question question) {
        cellBuilder.add("question", new JLabel(question.getQuestionText()));
        cellBuilder.add("answer", question.textArea);
        question.textArea.setName("answer" + question.index);
        final JScrollPane scrollPane = new JScrollPane();
        cellBuilder.add("scroll", scrollPane);
        scrollPane.addMouseListener(new MouseAdapter() {
          public void mouseClicked(MouseEvent e) {
            question.textArea.requestFocus();
          }
        });
      }
    });

    panel = builder.load();
    Dimension size = panel.getPreferredSize();
    dialog.setSize(size.width, size.height + 20);
    dialog.setPanelAndButton(panel, new OkAction());
    dialog.validate();
    GuiUtils.center(dialog);
    List<JTextArea> textAreas = new ArrayList<JTextArea>();
    for (Question question : questions) {
      textAreas.add(question.textArea);
    }
    dialog.setFocusTraversalPolicy(new CustomFocusTraversalPolicy(textAreas.toArray(new Component[textAreas.size()])));
    textAreas.get(0).requestFocus();
  }

  private void sendMessageIfNeeded() {
    if (!hasAnswers()) {
      close();
      return;
    }

    StringBuilder builder = new StringBuilder("<html>\n");
    for (Question question : questions) {
      String answer = question.getAnswer();
      if (Strings.isNotEmpty(answer)) {
        builder.append("<h3>").append(question.getQuestionText()).append("</h3>\n");
        builder.append("<p>").append(answer).append("</p>\n");
      }
    }
    builder.append("</html>");
    directory.get(ConfigService.class).sendMail(HttpBudgetViewConstants.SUPPORT_EMAIL,
                                                email,
                                                Lang.get("license.activation.feedback.email.title", email),
                                                builder.toString(),
                                                new ConfigService.Listener() {
                                                  public void sent(String mail, String title, String content) {
                                                    Log.write("Comment email sent from " + mail + " - title : " + title + "\n" + content);
                                                    close();
                                                  }

                                                  public void sendFailed(String mail, String title, String content) {
                                                    Log.write("Failed to send comment mail from " + mail + " - title : " + title + "\n" + content);
                                                    close();
                                                  }
                                                });
  }

  private void close() {
    closeAction.actionPerformed(null);
  }

  private boolean hasAnswers() {
    for (Question question : questions) {
      if (Strings.isNotEmpty(question.getAnswer())) {
        return true;
      }
    }
    return false;
  }

  private class Question {
    private int index;
    private JTextArea textArea = new JTextArea();

    private Question(int index) {
      this.index = index;
    }

    public String getQuestionText() {
      return Lang.get("license.activation.feedback.question." + index);
    }

    public String getAnswer() {
      return textArea.getText();
    }

    public void requestFocus() {
      textArea.requestFocus();
    }
  }

  private class OkAction extends AbstractAction {

    public OkAction() {
      super(Lang.get("ok"));
    }

    public void actionPerformed(ActionEvent e) {
      sendMessageIfNeeded();
    }
  }

  public void dispose() {
    if (builder == null) {
      return;
    }
    builder.dispose();
    panel = null;
  }
}
