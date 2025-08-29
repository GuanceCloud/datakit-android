package com.ft.fragment;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 * Fragment Manager Helper Class
 * Used to manage complex Fragment operations, including nested Fragments, Fragment stacks, etc.
 */
public class FragmentManagerHelper {
    
    private static final String TAG = "FragmentManagerHelper";
    
    private final FragmentManager fragmentManager;
    private final int containerId;
    private final Map<String, Stack<Fragment>> fragmentStacks;
    private String currentTab;
    
    public FragmentManagerHelper(FragmentManager fragmentManager, int containerId) {
        this.fragmentManager = fragmentManager;
        this.containerId = containerId;
        this.fragmentStacks = new HashMap<>();
        this.currentTab = "default";
    }
    
    /**
     * Switch to specified tab
     */
    public void switchTab(String tabName, Fragment fragment) {
        if (currentTab.equals(tabName)) {
            return;
        }
        
        // Save current tab's Fragment stack
        saveCurrentTabState();
        
        // Switch to new tab
        currentTab = tabName;
        
        // Restore new tab's Fragment stack, create if not exists
        if (!fragmentStacks.containsKey(tabName)) {
            fragmentStacks.put(tabName, new Stack<>());
        }
        
        // Show new tab's Fragment
        if (fragmentStacks.get(tabName).isEmpty()) {
            addFragment(fragment, false);
        } else {
            showFragment(fragmentStacks.get(tabName).peek());
        }
    }
    
    /**
     * Add Fragment
     */
    public void addFragment(Fragment fragment, boolean addToBackStack) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        
        // Set transition animations
        transaction.setCustomAnimations(
            android.R.anim.fade_in,
            android.R.anim.fade_out,
            android.R.anim.fade_in,
            android.R.anim.fade_out
        );
        
        transaction.replace(containerId, fragment);
        
        if (addToBackStack) {
            transaction.addToBackStack(fragment.getClass().getSimpleName());
            // Add to current tab's stack
            if (!fragmentStacks.containsKey(currentTab)) {
                fragmentStacks.put(currentTab, new Stack<>());
            }
            fragmentStacks.get(currentTab).push(fragment);
        }
        
        transaction.commit();
        
        Log.d(TAG, "Added fragment: " + fragment.getClass().getSimpleName() + " to tab: " + currentTab);
    }
    
    /**
     * Show Fragment
     */
    public void showFragment(Fragment fragment) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        
        // Hide all Fragments
        for (Fragment f : fragmentManager.getFragments()) {
            if (f != null) {
                transaction.hide(f);
            }
        }
        
        // Show specified Fragment
        transaction.show(fragment);
        transaction.commit();
        
        Log.d(TAG, "Showed fragment: " + fragment.getClass().getSimpleName());
    }
    
    /**
     * Remove Fragment
     */
    public void removeFragment(Fragment fragment) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.remove(fragment);
        transaction.commit();
        
        // Remove from stack
        if (fragmentStacks.containsKey(currentTab)) {
            fragmentStacks.get(currentTab).remove(fragment);
        }
        
        Log.d(TAG, "Removed fragment: " + fragment.getClass().getSimpleName());
    }
    
    /**
     * Go back to previous Fragment
     */
    public boolean goBack() {
        if (fragmentManager.getBackStackEntryCount() > 0) {
            fragmentManager.popBackStack();
            
            // Remove from current tab's stack
            if (fragmentStacks.containsKey(currentTab) && !fragmentStacks.get(currentTab).isEmpty()) {
                fragmentStacks.get(currentTab).pop();
            }
            
            Log.d(TAG, "Went back in tab: " + currentTab);
            return true;
        }
        return false;
    }
    
    /**
     * Save current tab state
     */
    private void saveCurrentTabState() {
        if (currentTab != null && fragmentStacks.containsKey(currentTab)) {
            Fragment currentFragment = fragmentManager.findFragmentById(containerId);
            if (currentFragment != null) {
                fragmentStacks.get(currentTab).push(currentFragment);
            }
        }
    }
    
    /**
     * Get current Fragment
     */
    public Fragment getCurrentFragment() {
        return fragmentManager.findFragmentById(containerId);
    }
    
    /**
     * Get current tab's Fragment count
     */
    public int getCurrentTabFragmentCount() {
        if (fragmentStacks.containsKey(currentTab)) {
            return fragmentStacks.get(currentTab).size();
        }
        return 0;
    }
    
    /**
     * Clear specified tab's Fragment stack
     */
    public void clearTabStack(String tabName) {
        if (fragmentStacks.containsKey(tabName)) {
            fragmentStacks.get(tabName).clear();
            Log.d(TAG, "Cleared fragment stack for tab: " + tabName);
        }
    }
    
    /**
     * Clear all tabs' Fragment stacks
     */
    public void clearAllStacks() {
        fragmentStacks.clear();
        Log.d(TAG, "Cleared all fragment stacks");
    }
}
