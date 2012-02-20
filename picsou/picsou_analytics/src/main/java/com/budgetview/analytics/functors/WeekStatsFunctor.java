package com.budgetview.analytics.functors;

import com.budgetview.analytics.matchers.PotentialBuyerMatcher;
import com.budgetview.analytics.model.User;
import com.budgetview.analytics.model.UserEvaluationEntry;
import com.budgetview.analytics.model.WeekPerfStat;
import com.budgetview.analytics.utils.AnalyticsUtils;
import com.budgetview.analytics.utils.Weeks;
import org.globsframework.metamodel.fields.DateField;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.GlobFunctor;
import org.globsframework.model.utils.GlobMatcher;
import org.globsframework.model.utils.GlobMatchers;

import java.util.Date;

import static org.globsframework.model.FieldValue.value;
import static org.globsframework.model.utils.GlobMatchers.*;

public class WeekStatsFunctor implements GlobFunctor {

  public void run(Glob weekStat, GlobRepository repository) throws Exception {
    GlobList potentialBuyers =
      repository.getAll(User.TYPE, new PotentialBuyerMatcher(weekStat.get(WeekPerfStat.LAST_DAY)));
    repository.update(weekStat.getKey(), WeekPerfStat.POTENTIAL_BUYERS, potentialBuyers.size());

    processEvaluations(weekStat, repository);
  }

  private void processEvaluations(Glob weekStat, GlobRepository repository) {
    Integer weekId = weekStat.get(WeekPerfStat.ID);

    int potentialEvaluationUsers = getProbableUserEvaluations(weekId, repository).size();

    GlobList entries = getUserEvaluations(weekId, repository);
    int evaluationCount = entries.size();
    int yesCount = entries.filter(fieldEquals(UserEvaluationEntry.SATISFIED, true), repository).size();

    repository.update(weekStat.getKey(),
                      value(WeekPerfStat.EVALUATIONS_RATIO,
                            AnalyticsUtils.ratio(evaluationCount, potentialEvaluationUsers)),
                      value(WeekPerfStat.EVALUATIONS_RESULT,
                            AnalyticsUtils.ratio(yesCount, evaluationCount)));
  }

  private GlobList getProbableUserEvaluations(Integer weekId, GlobRepository repository) {
    GlobMatcher matcher = getWeekDateMatcher(weekId, User.PROBABLE_EVALUATION_DATE);
    return repository.getAll(User.TYPE, and(isNotNull(User.PROBABLE_EVALUATION_DATE), matcher));
  }

  private GlobList getUserEvaluations(Integer weekId, GlobRepository repository) {
    GlobMatcher matcher = getWeekDateMatcher(weekId, UserEvaluationEntry.DATE);
    return repository.getAll(UserEvaluationEntry.TYPE, matcher);
  }

  private GlobMatcher getWeekDateMatcher(Integer weekId, DateField field) {
    Date start = Weeks.getLastDay(Weeks.previous(weekId));
    Date end = Weeks.getFirstDay(Weeks.next(weekId));
    return and(fieldAfter(field, start), fieldBefore(field, end));
  }
}
