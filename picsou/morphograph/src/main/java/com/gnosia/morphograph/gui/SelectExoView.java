package com.gnosia.morphograph.gui;

import com.gnosia.morphograph.model.Select;
import org.crossbowlabs.globs.metamodel.fields.StringField;
import org.crossbowlabs.globs.model.Glob;
import org.crossbowlabs.globs.model.GlobRepository;
import org.crossbowlabs.globs.utils.Strings;
import org.crossbowlabs.globs.utils.directory.Directory;
import org.crossbowlabs.globs.utils.exceptions.InvalidConfiguration;
import org.crossbowlabs.globs.utils.exceptions.InvalidParameter;
import org.crossbowlabs.splits.SplitsBuilder;
import org.crossbowlabs.splits.color.ForegroundColorUpdater;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

public class SelectExoView extends AbstractExoView {
  private List<Question> questions = new ArrayList<Question>();

  private static int buttonCount = 0;

  public SelectExoView(Glob exercise, GlobRepository globRepository, Directory directory) {
    super(exercise, globRepository, directory, "select.ftl");
    initPanel();
  }

  private void initPanel() {
    for (Glob select : globRepository.findLinkedTo(exercise, Select.EXERCISE).sort(Select.ID)) {
      Question question = new Question(select);
      questions.add(question);
      question.register(builder);
    }
    createLayout();
  }

  public List<Question> getQuestions() {
    return questions;
  }

  public class Question {
    private List<Answer> answers = new ArrayList<Answer>();
    private String title;
    private boolean answered = false;

    public Question(Glob select) {
      title = select.get(Select.TITLE);

      String answerIndex = select.get(Select.ANSWERS);
      List<Integer> answers = getAnswers(answerIndex);
      add(Select.ALT1, 1, select, answers);
      add(Select.ALT2, 2, select, answers);
      add(Select.ALT3, 3, select, answers);
      add(Select.ALT4, 4, select, answers);
      add(Select.ALT5, 5, select, answers);
    }

    private List<Integer> getAnswers(String value) {
      String[] strings = value.trim().split(",");
      List<Integer> result = new ArrayList<Integer>();
      for (int i = 0; i < strings.length; i++) {
        try {
          result.add(Integer.valueOf(strings[i]));
        }
        catch (Exception e) {
          throw new InvalidParameter("Invalid format for answer '" + value + "'");
        }
      }
      return result;
    }

    private void add(StringField field, int position, Glob select, List<Integer> answers) {
      String alt = select.get(field);
      if (!Strings.isNullOrEmpty(alt)) {
        this.answers.add(new Answer(this, alt, answers.contains(position)));
      }
      else if (answers.contains(position)) {
        throw new InvalidConfiguration("Answer " + position + " is not defined for Select with title " + title);
      }
    }

    public String getTitle() {
      return title != null ? title : "";
    }

    public List<Answer> getAnswers() {
      return answers;
    }

    public void register(SplitsBuilder builder) {
      for (Answer answer : answers) {
        answer.register(builder);
      }
    }

    public void select(Answer selectedAnswer) {
      if (answered) {
        return;
      }
      selectedAnswer.select();
      if (selectedAnswer.isCorrectAnswer) {
        for (Answer answer : answers) {
          if (answer.isWaiting()) {
            return;
          }
        }
      }
      for (Answer answer : answers) {
        answer.complete();
      }
      answered = true;
    }

    public String toString() {
      return title;
    }
  }

  public class Answer extends AbstractAction {

    protected JButton button = new JButton(this);
    private boolean isCorrectAnswer;
    private boolean completed;
    private String buttonName;
    private Question question;

    public Answer(Question question, String text, boolean isCorrect) {
      super(text);
      this.question = question;
      this.buttonName = "button" + buttonCount++;
      this.isCorrectAnswer = isCorrect;
    }

    public String getButtonId() {
      return buttonName;
    }

    public void register(SplitsBuilder builder) {
      builder.add(getButtonId(), button);
    }

    public void actionPerformed(ActionEvent e) {
      question.select(this);
    }

    public void select() {
      if (isCorrectAnswer) {
        colorService.install("select.answer.success", new ForegroundColorUpdater(button));
        button.setIcon(okIcon);
      }
      else {
        colorService.install("select.answer.failure", new ForegroundColorUpdater(button));
        button.setIcon(failedIcon);
      }
      completed = true;
    }

    public void complete() {
      if (completed) {
        return;
      }
      if (isCorrectAnswer) {
        colorService.install("select.answer.success", new ForegroundColorUpdater(button));
      }
      else {
        button.setEnabled(false);
      }
      completed = true;
    }

    public boolean isCompleted() {
      return completed;
    }

    public boolean isWaiting() {
      return isCorrectAnswer && !completed;
    }
  }
}
