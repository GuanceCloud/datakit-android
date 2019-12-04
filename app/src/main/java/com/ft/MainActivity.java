package com.ft;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;


public class MainActivity extends AppCompatActivity implements PlusOneFragment.OnFragmentInteractionListener, BlankFragment.OnFragmentInteractionListener {
    private FrameLayout fragmentFL;
    FragmentManager fragmentManager;
    Fragment fragment1;
    Fragment fragment2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fragmentFL = findViewById(R.id.fragment);
        fragmentManager = getSupportFragmentManager();
        fragment1 = PlusOneFragment.newInstance("12","23");
        fragment2 = BlankFragment.newInstance("12","23");
        gotoFFragment(fragmentFL);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }


    public void clickText(View view) {

    }

    public void gotoFFragment(View view) {
        if(!fragment1.isAdded()){
            fragmentManager.beginTransaction().add(R.id.fragment,fragment1,"fragment1").commitAllowingStateLoss();
        }else{
            fragmentManager.beginTransaction().show(fragment1).commitAllowingStateLoss();
        }
        if(fragment2.isAdded()) {
            fragmentManager.beginTransaction().hide(fragment2).commitAllowingStateLoss();
        }
    }

    public void gotoSFragment(View view) {
        if(!fragment2.isAdded()){
            fragmentManager.beginTransaction().add(R.id.fragment,fragment2,"fragment2").commitAllowingStateLoss();
        }else{
            fragmentManager.beginTransaction().show(fragment2).commitAllowingStateLoss();
        }
        if(fragment1.isAdded()) {
            fragmentManager.beginTransaction().hide(fragment1).commitAllowingStateLoss();
        }
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
