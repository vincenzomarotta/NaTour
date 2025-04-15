package com.example.natour.utils;

import android.content.Context;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.example.natour.R;
import com.example.natour.boundary.HomeActivity;
import com.example.natour.fragment.FavoritesFragment;
import com.example.natour.fragment.LastSeenFragment;
import com.example.natour.fragment.MessageFragment;
import com.example.natour.fragment.MyItineraryFragment;
import com.example.natour.fragment.VisitFragment;


public class PagerAdapter extends FragmentPagerAdapter {

    HomeActivity homeActivity;
    private Context context;

    LastSeenFragment lastSeenFragment;
    VisitFragment visitFragment;
    FavoritesFragment favoritesFragment;
    MyItineraryFragment myItineraryFragment;
    MessageFragment messageFragment;

    private int numOfTabs;
    private int[] icon = {R.drawable.home_icon, R.drawable.favorite_icon,
            R.drawable.explore_icon, R.drawable.room_icon, R.drawable.message_icon};

    public PagerAdapter(FragmentManager fm, int numOfTabs, Context context, HomeActivity homeActivity) {
        super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        this.numOfTabs = numOfTabs;
        this.context = context;
        this.homeActivity = homeActivity;
    }

    /**
     * Returns the correct fragment in a determined position.
     * All the Fragments require a list that will be set.
     * @param position of the fragment in the list.
     * @return Fragment
     */
    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                lastSeenFragment = new LastSeenFragment(homeActivity.updateLastSeen(), homeActivity);
                setLastSearchedFragment(lastSeenFragment);
                return lastSeenFragment;
            case 1:
                favoritesFragment = new FavoritesFragment(homeActivity.getFavorites(), homeActivity);
                setFavoritesFragment(favoritesFragment);
                return favoritesFragment;
            case 2:
                visitFragment = new VisitFragment(homeActivity.getVisit(), homeActivity);
                setVisitFragment(visitFragment);
                return visitFragment;
            case 3:
                myItineraryFragment = new MyItineraryFragment(homeActivity.getMyItineraries(), homeActivity);
                setMyItineraryFragment(myItineraryFragment);
                return myItineraryFragment;
            case 4:
                messageFragment = new MessageFragment(homeActivity.getChats(), homeActivity);
                setMessageFragment(messageFragment);
                return messageFragment;
            default:
                return null;
        }

    }

    /**
     * Set tab titles.
     * @Nullable
     * @param position of the fragment in the list.
     * @return
     */
    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        super.getPageTitle(position);

        switch (position){
            case 0:
                return context.getString(R.string.home_tab);
            case 1:
                return context.getString(R.string.favourites_tab);
            case 2:
                return context.getString(R.string.visit_tab);
            case 3:
                return context.getString(R.string.my_itineraries_tab);
            case 4:
                return context.getString(R.string.messages_tab);
            default:
                return null;
        }

    }


    @Override
    public int getCount() {
        return numOfTabs;
    }

    /**
     * Sets tab icons.
     * @param position of the icons in its array.
     * @return
     */
    public int setTabIcon(int position){
        return icon[position];
    }

    public LastSeenFragment getLastSearchedFragment() {
        return lastSeenFragment;
    }

    public VisitFragment getVisitFragment() {
        return visitFragment;
    }

    public FavoritesFragment getFavoritesFragment() {
        return favoritesFragment;
    }

    public MyItineraryFragment getMyItineraryFragment() {
        return myItineraryFragment;
    }

    public MessageFragment getMessageFragment() {
        return messageFragment;
    }

    public void setLastSearchedFragment(LastSeenFragment lastSeenFragment) {
        this.lastSeenFragment = lastSeenFragment;
    }

    public void setVisitFragment(VisitFragment visitFragment) {
        this.visitFragment = visitFragment;
    }

    public void setFavoritesFragment(FavoritesFragment favoritesFragment) {
        this.favoritesFragment = favoritesFragment;
    }

    public void setMyItineraryFragment(MyItineraryFragment myItineraryFragment) {
        this.myItineraryFragment = myItineraryFragment;
    }

    public void setMessageFragment(MessageFragment messageFragment) {
        this.messageFragment = messageFragment;
    }
}
