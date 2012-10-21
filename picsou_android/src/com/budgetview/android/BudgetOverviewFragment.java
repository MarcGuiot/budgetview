package com.budgetview.android;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.budgetview.android.components.GaugeView;
import com.budgetview.shared.model.AccountEntity;
import com.budgetview.shared.model.BudgetAreaEntity;
import com.budgetview.shared.model.BudgetAreaValues;
import com.budgetview.shared.utils.AccountEntityMatchers;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.GlobFieldComparator;
import org.globsframework.model.utils.GlobMatcher;

import java.util.ArrayList;
import java.util.List;

import static org.globsframework.model.utils.GlobMatchers.fieldEquals;

public class BudgetOverviewFragment extends Fragment {

  public static String BUDGET_OVERVIEW_MONTH = "com.budgetview.budgetOverviewFragment";
  private int monthId;

  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

    View view = inflater.inflate(R.layout.budget_overview, container, false);

    this.monthId = getArguments().getInt(BUDGET_OVERVIEW_MONTH);

    TextView text = (TextView)view.findViewById(R.id.overviewLabel);
    if (text != null) {
      text.setText(Text.monthToString(monthId, getResources()));
    }

    ListView list = (ListView)view.findViewById(R.id.budgetAreaList);
    final BudgetListAdapter adapter = new BudgetListAdapter(inflater);
    list.setAdapter(adapter);

    return view;
  }

  private class BudgetListAdapter extends BaseAdapter {

    private LayoutInflater inflater;
    private List<Block> blocks = new ArrayList<Block>();

    private BudgetListAdapter(LayoutInflater inflater) {
      this.inflater = inflater;

      GlobRepository repository = ((App)getActivity().getApplication()).getRepository();

      addBudgetAreaBlocks(repository);
      addAccountBlocks(repository, AccountEntityMatchers.main(), R.string.main_accounts_section);
      addAccountBlocks(repository, AccountEntityMatchers.savings(), R.string.savings_accounts_section);
    }

    private void addBudgetAreaBlocks(GlobRepository repository) {

      blocks.add(new AccountSectionBlock(R.string.budget_section));

      GlobList budgetAreaValuesList =
        repository
          .getAll(BudgetAreaValues.TYPE, fieldEquals(BudgetAreaValues.MONTH, monthId))
          .sort(new GlobFieldComparator(BudgetAreaValues.BUDGET_AREA));
      for (Glob budgetAreaValues : budgetAreaValuesList) {
        blocks.add(new BudgetAreaBlock(budgetAreaValues));
      }
    }

    private void addAccountBlocks(GlobRepository repository, GlobMatcher matcher, int sectionTitleId) {
      GlobList accountList =
        repository
          .getAll(AccountEntity.TYPE, matcher).sort(AccountEntity.SEQUENCE_NUMBER);
      if (accountList.isEmpty()) {
        return;
      }

      blocks.add(new AccountSectionBlock(sectionTitleId));
      for (Glob accountEntity : accountList) {
        blocks.add(new AccountBlock(accountEntity));
      }
    }

    public int getCount() {
      return blocks.size();
    }

    public Object getItem(int i) {
      return blocks.get(i);
    }

    public long getItemId(int i) {
      return getItem(i).hashCode();
    }

    public View getView(int i, View previousView, ViewGroup parent) {
      return blocks.get(i).getView(inflater, previousView, parent);
    }
  }

  protected class BudgetAreaBlock extends Block {
    private Glob budgetAreaValues;

    public BudgetAreaBlock(Glob budgetAreaValues) {
      super(R.layout.budget_area_block);
      this.budgetAreaValues = budgetAreaValues;
    }

    protected boolean isProperViewType(View view) {
      return view.findViewById(R.id.budgetAreaLabel) != null;
    }

    protected void populateView(View view) {

      App app = (App)getActivity().getApplication();
      Glob entity = app.getRepository().findLinkTarget(budgetAreaValues, BudgetAreaValues.BUDGET_AREA);

      view.setOnClickListener(new View.OnClickListener() {
        public void onClick(View view) {
          Intent intent = new Intent(getActivity(), SeriesListActivity.class);
          intent.putExtra(SeriesListActivity.MONTH_PARAMETER, budgetAreaValues.get(BudgetAreaValues.MONTH));
          intent.putExtra(SeriesListActivity.BUDGET_AREA_PARAMETER, budgetAreaValues.get(BudgetAreaValues.BUDGET_AREA));
          startActivity(intent);
        }
      });

      setText(view, R.id.budgetAreaLabel, entity.get(BudgetAreaEntity.LABEL));
      setText(view, R.id.budgetAreaActual, budgetAreaValues.get(BudgetAreaValues.ACTUAL));
      setText(view, R.id.budgetAreaPlanned, budgetAreaValues.get(BudgetAreaValues.INITIALLY_PLANNED));

      GaugeView gaugeView = (GaugeView)view.findViewById(R.id.budgetAreaGauge);
      gaugeView.getModel()
        .setValues(budgetAreaValues.get(BudgetAreaValues.ACTUAL),
                   budgetAreaValues.get(BudgetAreaValues.INITIALLY_PLANNED),
                   budgetAreaValues.get(BudgetAreaValues.OVERRUN),
                   budgetAreaValues.get(BudgetAreaValues.REMAINDER),
                   "", false);
    }
  }

  protected class AccountBlock extends Block {
    private Glob accountEntity;

    public AccountBlock(Glob accountEntity) {
      super(R.layout.account_block);
      this.accountEntity = accountEntity;
    }

    protected boolean isProperViewType(View view) {
      return view.findViewById(R.id.accountLabel) != null;
    }

    protected void populateView(View view) {
      setText(view, R.id.accountLabel, accountEntity.get(AccountEntity.LABEL));
      Double position = accountEntity.get(AccountEntity.POSITION);
      setText(view, R.id.accountPosition, position);
      setText(view, R.id.accountPositionDate, Text.toOnDayMonthString(accountEntity.get(AccountEntity.POSITION_DAY),
                                                                      accountEntity.get(AccountEntity.POSITION_MONTH),
                                                                      getResources()));

      Views.setColorAmount(view, R.id.accountPosition, position);

      view.setOnClickListener(new View.OnClickListener() {
        public void onClick(View view) {
          Intent intent = new Intent(getActivity(), TransactionListActivity.class);
          intent.putExtra(TransactionListActivity.MONTH_PARAMETER, monthId);
          intent.putExtra(TransactionListActivity.ACCOUNT_PARAMETER, accountEntity.get(AccountEntity.ID));
          startActivity(intent);
        }
      });
    }
  }

  protected class AccountSectionBlock extends Block {
    private int titleId;

    public AccountSectionBlock(int titleId) {
      super(R.layout.account_section_block);
      this.titleId = titleId;
    }

    protected boolean isProperViewType(View view) {
      return view.findViewById(R.id.accountSectionLabel) != null;
    }

    protected void populateView(View view) {
      setText(view, R.id.accountSectionLabel, getResources().getText(titleId));
    }
  }
}
