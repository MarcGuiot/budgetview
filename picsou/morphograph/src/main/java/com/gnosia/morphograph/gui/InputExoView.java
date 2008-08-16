package com.gnosia.morphograph.gui;

import com.gnosia.morphograph.model.Input;
import org.globsframework.gui.splits.color.BackgroundColorUpdater;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.Strings;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

public class InputExoView extends AbstractExoView {
  private List<Question> questions = new ArrayList<Question>();

  public InputExoView(Glob exercise, GlobRepository globRepository, Directory directory) {
    super(exercise, globRepository, directory, "input.ftl");
    initQuestions();
  }

  private void initQuestions() {
    int index = 0;
    for (Glob question : repository.findLinkedTo(exercise, Input.EXERCISE).sort(Input.ID)) {
      int id = index++;
      Question q = new Question(question.get(Input.TITLE),
                                question.get(Input.ANSWER),
                                "textField" + id,
                                "action" + id);
      builder.add(q.actionId, q);
      questions.add(q);
    }
  }

  public List<Question> getQuestions() {
    return questions;
  }

  public class Question extends AbstractAction {
    public final String title;
    public final String answer;
    public final String textFieldId;
    public final String actionId;
    private boolean disabled = false;

    public Question(String title, String answer, String textFieldId, String actionId) {
      this.title = title;
      this.answer = answer;
      this.textFieldId = textFieldId;
      this.actionId = actionId;
    }

    public String getActionId() {
      return actionId;
    }

    public String getAnswer() {
      return answer;
    }

    public String getTextFieldId() {
      return textFieldId;
    }

    public String getTitle() {
      return title;
    }

    public void actionPerformed(ActionEvent e) {
      if (disabled) {
        return;
      }
      JTextField textField = (JTextField)builder.getComponent(textFieldId);
      String text = textField.getText();
      if (Strings.isNullOrEmpty(text)) {
        return;
      }
      disabled = true;
      textField.setEditable(false);
      setName("");
      if (text.equalsIgnoreCase(answer)) {
        colorService.install("freetext.answer.success", new BackgroundColorUpdater(textField));
        setIcon(okIcon);
      }
      else {
        colorService.install("freetext.answer.failure", new BackgroundColorUpdater(textField));
        textField.setText(text + " (r\u00e9ponse: " + answer + ")");
        setIcon(failedIcon);
      }
    }

    private void setName(String name) {
      putValue(Action.NAME, name);
    }

    private void setIcon(Icon icon) {
      putValue(Action.SMALL_ICON, icon);
    }

    public String toString() {
      return title;
    }
  }
}
