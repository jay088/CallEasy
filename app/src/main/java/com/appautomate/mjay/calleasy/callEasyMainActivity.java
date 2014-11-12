package com.appautomate.mjay.calleasy;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.ProgressBar;


//this class is the app's main launcher activity & handles linking the different pages/fragments of the app, enabling the actionBar tab at the top and the swipe right/left actions.
public class callEasyMainActivity extends FragmentActivity{

    TabPagerAdapter TabAdapter;
    ActionBar actionBar;
    ViewPager Tab;


    //method that links fragments to the ViewPager, enables swiping, sets up the ActionBar & the tabs.
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call);

        TabAdapter = new TabPagerAdapter(getSupportFragmentManager());
        Tab = (ViewPager) findViewById(R.id.pager);
        Tab.setOnPageChangeListener(
                new ViewPager.SimpleOnPageChangeListener() {

                    @Override
                    public void onPageSelected(int position) {
                        actionBar = getActionBar();
                        actionBar.setSelectedNavigationItem(position);
                    }
                }
        );
        Tab.setAdapter(TabAdapter);
        actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);


        ActionBar.TabListener tabListener = new ActionBar.TabListener() {
            @Override
            public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
                Tab.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

            }

            @Override
            public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

            }

        };

        actionBar.addTab(actionBar.newTab().setText("Calling Card").setTabListener(tabListener));
        actionBar.addTab(actionBar.newTab().setText("Conference Call").setTabListener(tabListener));
            }
}