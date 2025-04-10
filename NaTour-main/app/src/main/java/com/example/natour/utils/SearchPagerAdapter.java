package com.example.natour.utils;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.natour.R;
import com.example.natour.boundary.SearchActivity;
import com.example.natour.fragment.SearchedItineraryFragment;
import com.example.natour.fragment.SearchedUserFragment;

public class SearchPagerAdapter extends FragmentStatePagerAdapter {
    SearchActivity searchActivity;
    SearchedItineraryFragment searchedItineraryFragment;
    SearchedUserFragment searchedUserFragment;
    private static final int ITINERARY = 0;
    private static final int USER = 1;
    private Context context;

    private int numOfTabs;
    private int[] icon = {R.drawable.explore_icon, R.drawable.person_icon};

    public SearchPagerAdapter(FragmentManager fm, int numOfTabs, Context context, SearchActivity searchActivity) {
        super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        this.searchActivity = searchActivity;
        this.numOfTabs = numOfTabs;
        this.context = context;
    }

    /**
     * Method that returns the correct fragment in a determined position.
     * All the Fragments require a list that will be set.
     * @param position
     * @return Fragment
     */
    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                searchedItineraryFragment = new SearchedItineraryFragment(searchActivity.getSearchedItineraries(),searchActivity);
                setSearchedItineraryFragment(searchedItineraryFragment);
                return searchedItineraryFragment;
            case 1:
                searchedUserFragment = new SearchedUserFragment(searchActivity.getSearchedUsers(),searchActivity);
                setSearchedUserFragment(searchedUserFragment);
                return searchedUserFragment;
            default:
                return null;
        }

    }

    /**
     * Method used to set tab title.
     * @Nullable
     * @param position
     * @return
     */
    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        super.getPageTitle(position);

        switch (position){
            case 0:
                return context.getString(R.string.searchedItineraries);
            case 1:
                return context.getString(R.string.searchedUser);
            default:
                return null;
        }

    }


    @Override
    public int getCount() {
        return numOfTabs;
    }

    @Override
    public int getItemPosition(@NonNull Object object) {
        return POSITION_NONE;
    }

    /**
     * Method used to set tab icons.
     * @param position
     * @return
     */
    public int setTabIcon(int position){
        return icon[position];
    }

    public Fragment getFragment(int code){

        switch (code) {
            case ITINERARY:
                return getSearchedItineraryFragment();
            case USER:
                return getSearchedUserFragment();
            default:
                return null;
        }
    }


    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }



    public SearchedItineraryFragment getSearchedItineraryFragment() {
        return searchedItineraryFragment;
    }

    public void setSearchedItineraryFragment(SearchedItineraryFragment searchedItineraryFragment) {
        this.searchedItineraryFragment = searchedItineraryFragment;
    }


    public SearchedUserFragment getSearchedUserFragment() {
        return searchedUserFragment;
    }

    public void setSearchedUserFragment(SearchedUserFragment searchedUserFragment) {
        this.searchedUserFragment = searchedUserFragment;
    }
}
