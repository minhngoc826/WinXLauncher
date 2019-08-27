package com.vietbm.edgelauncher.customview;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.RelativeSizeSpan;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowInsets;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mikepenz.fastadapter.IItemAdapter;
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter;
import com.vietbm.edgelauncher.R;
import com.vietbm.edgelauncher.manager.Setup;
import com.vietbm.edgelauncher.model.App;
import com.vietbm.edgelauncher.model.Item;
import com.vietbm.edgelauncher.util.DragAction;
import com.vietbm.edgelauncher.util.DragHandler;
import com.vietbm.edgelauncher.util.Tool;
import com.vietbm.edgelauncher.viewutil.IconLabelItem;


import java.util.ArrayList;
import java.util.List;

public class SearchAppView extends FrameLayout {

    public AppCompatImageView _switchButton;
    public AppCompatEditText _searchInput;
    public RecyclerView _searchRecycler;
    private FastItemAdapter<IconLabelItem> _adapter = new FastItemAdapter<>();
    private CallBack _callback;
    private int bottomInset;
    private int textSize = 13;

    public SearchAppView(@NonNull Context context) {
        super(context);
        init();
    }

    public SearchAppView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SearchAppView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void setCallback(CallBack callback) {
        _callback = callback;
    }


    private void init() {
        int dp1 = Tool.dp2px(1);
        int iconMarginOutside = dp1 * 16;
        int iconSize = dp1 * 24;
        Typeface createFromAsset = Typeface.createFromAsset(getContext().getAssets(), "fonts/SFProTextMedium.otf");

        _switchButton = new AppCompatImageView(getContext());
        _switchButton.setImageResource(R.drawable.ic_search_light_24dp);
        _switchButton.setPadding(0, 0, 0, 0);

        LayoutParams switchButtonParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        switchButtonParams.setMargins(iconMarginOutside / 2, 0, 0, 0);
        switchButtonParams.gravity = Gravity.START | Gravity.CENTER_VERTICAL;

        if (isInEditMode()) return;

        CardView _searchCardContainer = new CardView(getContext());
        _searchCardContainer.setCardBackgroundColor(Color.TRANSPARENT);
        _searchCardContainer.setRadius(0);
        _searchCardContainer.setCardElevation(0);
        _searchCardContainer.setContentPadding(dp1 * 4, 0, dp1 * 4, 0);

        _searchInput = new AppCompatEditText(getContext());
        _searchInput.setBackground(null);
        _searchInput.setHint(R.string.search_hint);
        _searchInput.setTypeface(createFromAsset);
        _searchInput.setHintTextColor(Color.WHITE);
        _searchInput.setTextColor(Color.WHITE);
        _searchInput.setSingleLine();
        _searchInput.setTextSize(textSize);
        _searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                _adapter.filter(s);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        _searchInput.setOnKeyListener((v, keyCode, event) -> {
            if ((event != null) && (event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                _callback.onInternetSearch(_searchInput.getText().toString());
                _searchInput.getText().clear();
                return true;
            }
            return false;
        });

        LayoutParams inputCardParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        inputCardParams.setMargins(0, 0, 0, 0);

        LayoutParams inputParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        inputParams.setMargins(iconMarginOutside + iconSize, 0, 0, 0);

        _searchCardContainer.addView(_switchButton, switchButtonParams);
        _searchCardContainer.addView(_searchInput, inputParams);

        initRecyclerView();

        Setup.appLoader().addUpdateListener(apps -> {
            _adapter.clear();
            if (Setup.appSettings().getSearchBarShouldShowHiddenApps()) {
                apps = Setup.appLoader().getAllApps(getContext(), true);
            }
            List<IconLabelItem> items = new ArrayList<>();
            for (int i = 0; i < apps.size(); i++) {
                final App app = apps.get(i);
                items.add(new IconLabelItem(app.getIcon(), app.getLabel())
                        .withIconSize(getContext(), 32)
                        .withTextColor(Color.WHITE)
                        .withIsAppLauncher(true)
                        .withIconPadding(getContext(), 5)
                        .withTextSize(textSize)
                        .withTypeFace(createFromAsset)
                        .withOnClickAnimate(false)
                        .withTextGravity(Gravity.CENTER_VERTICAL)
                        .withIconGravity(Gravity.START)
                        .withOnClickListener(v -> Tool.startApp(v.getContext(), app, null))
                        .withOnLongClickListener(DragHandler.getLongClick(Item.newAppItem(app), DragAction.Action.SEARCH, null)));
            }
            _adapter.set(items);
            _searchRecycler.setAdapter(_adapter);
            return false;
        });
        _adapter.getItemFilter().withFilterPredicate((IItemAdapter.Predicate<IconLabelItem>) (item, constraint) -> {
            if ((constraint != null ? constraint.length() : 0) == 0) {
                return true;
            }

            String s = constraint.toString().toLowerCase();
            return item._label.toLowerCase().contains(s);
        });

        final LayoutParams recyclerParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        addView(_searchRecycler, recyclerParams);
        addView(_searchCardContainer, inputCardParams);

        _searchInput.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                _searchInput.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                int marginTop = Tool.dp2px(40);
                recyclerParams.setMargins(0, marginTop, 0, 0);
                _searchRecycler.setLayoutParams(recyclerParams);
                _searchRecycler.setPadding(0, 0, 0, (int) (bottomInset * 1.5));
            }
        });
    }

    protected void initRecyclerView() {
        _searchRecycler = new RecyclerView(getContext());
        _searchRecycler.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));
        _searchRecycler.setItemAnimator(null);
        _searchRecycler.setClipToPadding(false);
        _searchRecycler.setHasFixedSize(true);
        _searchRecycler.setOverScrollMode(OVER_SCROLL_NEVER);
    }

    @Override
    public WindowInsets onApplyWindowInsets(WindowInsets insets) {
        bottomInset = insets.getSystemWindowInsetBottom();
        return insets;
    }

    public interface CallBack {
        void onInternetSearch(String string);

        void onExpand();

        void onCollapse();
    }
}
