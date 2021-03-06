package org.lightsys.emailhelper;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.PreferenceScreen;
import android.support.v7.preference.EditTextPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceFragmentCompat;

import xdroid.toaster.Toaster;

/**
 * This class is to initialize the prefrence fragments and keep the summaries up to date.
 */
public class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener,Preference.OnPreferenceChangeListener {


    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.settings);

        SharedPreferences sp = getPreferenceScreen().getSharedPreferences();
        android.support.v7.preference.PreferenceScreen ps = getPreferenceScreen();

        int count = ps.getPreferenceCount();
        for (int i = 0;i<count;i++){
            Preference p = ps.getPreference(i);
            if(!(p instanceof android.support.v7.preference.CheckBoxPreference)){
                String value = sp.getString(p.getKey(),"");
                setSummary(p,value);
            }
            if(p instanceof PreferenceCategory){
                int c = ((PreferenceCategory) p).getPreferenceCount();
                for(int j = 0;j<c;j++){
                    Preference pref = ((PreferenceCategory) p).getPreference(j);
                    if(!(pref instanceof android.support.v7.preference.CheckBoxPreference)){
                        String value = sp.getString(pref.getKey(),"");
                        setSummary(pref,value);
                        pref.setOnPreferenceChangeListener(this);
                    }
                }
            }
        }

    }
    private void setSummary(Preference preference, String value){
        if(preference instanceof ListPreference){
            ListPreference lp = (ListPreference) preference;
            int prefIndex = lp.findIndexOfValue(value);
            if(prefIndex >= 0){
                lp.setSummary(lp.getEntries()[prefIndex]);
            }
        }
        if(preference instanceof EditTextPreference){
            EditTextPreference etp = (EditTextPreference) preference;
            etp.setSummary(value);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Preference p = findPreference(key);
        if(null != p){
            if(!(p instanceof android.support.v7.preference.CheckBoxPreference)){
                String value = sharedPreferences.getString(p.getKey(),"");
                setSummary(p,value);
            }
        }
        switch(key){
            case "updateFrequency":
            case "updateTimePeriod":
                Context context = getActivity().getApplicationContext();
                Intent updateService = new Intent(context, AutoUpdater.class);
                context.stopService(updateService);
                context.startService(updateService);
                break;

        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        switch(preference.getKey()){
            case "updateFrequency":
                String stringSize = (String) newValue;
                stringSize.trim();
                try {
                    int size = Integer.valueOf(stringSize);
                    if (size > 60 || size <= 0) {
                        Toaster.toast(R.string.invalid_update_frequency);
                        return false;
                    }
                } catch (java.lang.NumberFormatException nfe) {
                    Toaster.toast(R.string.invalid_update_frequency);
                    return false;

                }
        }
        return true;//for any other preferences
    }
}

