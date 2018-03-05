package com.th1b0.budget.features.budgets;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import com.th1b0.budget.R;
import com.th1b0.budget.app.MainActivity;
import com.th1b0.budget.app.Toolbar;
import com.th1b0.budget.features.budgetform.BudgetFormActivity;
import com.th1b0.budget.model.Budget;
import com.th1b0.budget.util.ConfirmDeletionDialog;
import com.th1b0.budget.util.DataManager;
import com.th1b0.budget.util.FragmentRecycler;
import com.th1b0.budget.util.SimpleItemAdapter;
import java.util.ArrayList;

/**
 * Created by 7h1b0.
 */

public final class BudgetFragment
    extends FragmentRecycler<BudgetPresenter, SimpleItemAdapter<Budget>>
    implements BudgetView, SimpleItemAdapter.OnSimpleItemClick<Budget> {

  public static BudgetFragment newInstance() {
    return new BudgetFragment();
  }

  @Override public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    mPresenter = new BudgetPresenterImpl(DataManager.getInstance(getActivity()));
    mAdapter = new SimpleItemAdapter<>(this);
  }

  @Override public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    initializeRecycler();
    initializeFAB();
    initializeToolbar();

    mPresenter.attach(this);
    mPresenter.loadBudgets();
  }

  @Override public void onActivityResult(int requestCode, int resultCode, Intent data) {
    switch (requestCode) {
      case CONFIRM_DELETE:
        if (resultCode == Activity.RESULT_OK && data.hasExtra(ConfirmDeletionDialog.PARCELABLE)) {
          Budget budget = data.getParcelableExtra(ConfirmDeletionDialog.PARCELABLE);
          mPresenter.deleteBudget(budget);
        }
        break;
    }
  }

  private void initializeRecycler() {
    LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
    mView.recycler.setLayoutManager(layoutManager);
    mView.recycler.setAdapter(mAdapter);
  }

  private void initializeFAB() {
    mView.fab.setOnClickListener(
        v -> startActivity(BudgetFormActivity.newInstance(getActivity())));
  }

  private void initializeToolbar() {
    Toolbar toolbar = (Toolbar) getActivity();
    toolbar.setToolbarTitle(getString(R.string.budgets));
  }

  @Override public void onBudgetsLoaded(@NonNull ArrayList<Budget> budgets) {
    mAdapter.addAll(budgets);
    if (budgets.isEmpty()) {
      mView.included.text.setText(getString(R.string.no_budget));
      mView.included.noItem.setVisibility(View.VISIBLE);
    } else {
      mView.included.noItem.setVisibility(View.GONE);
    }
  }

  @Override public void onError(@Nullable String error) {
    super.onError(error);
  }

  @Override public void onSimpleItemClick(@NonNull Budget budget) {
    View view = View.inflate(getActivity(), R.layout.bottomsheet_edit, null);
    BottomSheetDialog dialog = new BottomSheetDialog(getActivity());
    dialog.setContentView(view);
    dialog.show();

    view.findViewById(R.id.edit).setOnClickListener(v -> {
      startActivity(BudgetFormActivity.newInstance(getActivity(), budget));
      dialog.dismiss();
    });

    view.findViewById(R.id.delete).setOnClickListener(v -> {
      String title = getString(R.string.confirm_deletion_title, budget.getTitle());
      String msg = getString(R.string.confirm_budget_deletion);
      ConfirmDeletionDialog.newInstance(title, msg, budget, this, CONFIRM_DELETE)
          .show(getFragmentManager(), null);
      dialog.dismiss();
    });
  }
}
