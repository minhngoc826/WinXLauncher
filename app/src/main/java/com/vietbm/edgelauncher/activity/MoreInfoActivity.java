package com.vietbm.edgelauncher.activity;

import android.os.Bundle;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.widget.Toolbar;

import com.vietbm.edgelauncher.R;
import com.vietbm.edgelauncher.fragment.MoreInfoFragment;

public class MoreInfoActivity extends ThemeActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_more);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setBackgroundColor(_appSettings.getPrimaryColor());

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        MoreInfoFragment moreInfoFragment = MoreInfoFragment.newInstance();
        transaction.replace(R.id.fragment_holder, moreInfoFragment, MoreInfoFragment.TAG).commit();
    }
}
