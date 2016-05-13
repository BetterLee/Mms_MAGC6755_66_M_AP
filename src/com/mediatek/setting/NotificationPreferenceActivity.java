/*
 * Copyright (C) 2007-2008 Esmertec AG.
 * Copyright (C) 2007-2008 The Android Open Source Project
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.mediatek.setting;

import android.app.ActionBar;
import android.content.Context;
import android.content.res.Configuration;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.preference.RingtonePreference;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;

import com.android.mms.MmsApp;
import com.android.mms.MmsConfig;
import com.android.mms.R;
import android.telephony.SubscriptionManager;

import com.android.mms.ui.MessageUtils;
import com.android.mms.util.MmsLog;
//[ramos]added by liting 20150929
import android.view.View;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.preference.SwitchPreference;
//[ramos]end liting
//[ramos] begin by liting 20160223 for dual Sim Ringtone 
import com.mediatek.audioprofile.AudioProfileManager;
import android.util.Log;
import com.android.mms.ramos.ramosDefaultRingtonePreference;
import com.android.mms.ramos.ramosDefaultRingtonePreference.OnRingtonePreferenceChange;
import android.telephony.SubscriptionInfo;
import android.os.Handler;
import android.os.Message;
import java.util.List;
import android.os.AsyncTask;
import android.database.sqlite.SQLiteException;
import android.os.Looper;
import android.provider.MediaStore;
import android.database.Cursor;
import android.content.ContentResolver;
import android.provider.OpenableColumns;
import android.preference.PreferenceCategory;
import android.telephony.TelephonyManager;
//[ramos]end liting

/**
 * With this activity, users can set preferences for MMS and SMS and
 * can access and manipulate SMS messages stored on the SIM.
 */
//[ramos] begin by liting 20160223 for dual Sim Ringtone 
/*
public class NotificationPreferenceActivity extends PreferenceActivity
        implements Preference.OnPreferenceChangeListener {
*/
public class NotificationPreferenceActivity extends PreferenceActivity
        implements Preference.OnPreferenceChangeListener, OnRingtonePreferenceChange {
//[ramos]end liting
    private static final String TAG = "NotificationPreferenceActivity";

    private static final boolean DEBUG = false;

    // Symbolic names for the keys used for preference lookup
    public static final String NOTIFICATION_MUTE = "pref_key_mute";

    public static final String NOTIFICATION_VIBRATE = "pref_key_vibrate";

    public static final String POPUP_NOTIFICATION = "pref_key_popup_notification";

    public static final String NOTIFICATION_ENABLED = "pref_key_enable_notifications";

    public static final String NOTIFICATION_RINGTONE = "pref_key_ringtone";

    public static final String AUTO_RETRIEVAL = "pref_key_mms_auto_retrieval";

    public static final String MUTE_START = "mute_start";

    public static final String DEFAULT_RINGTONE = "content://settings/system/notification_sound";

    // System ring tone path start with "content://media/internal/audio/media/".
    // If the ring tone file is added by user, like put music under storage/Notifications folder,
    // then the ring tone URI start with this.
    private static final String EXTERNAL_RINGTONE_PATH = "content://media/external/audio/media/";

    // Menu entries
    private static final int MENU_RESTORE_DEFAULTS = 1;

	//[ramos] modified by liting 20150929 CheckBoxPreference -> SwitchPreference
    private SwitchPreference mEnableNotificationsPref;

    private SwitchPreference mVibratePref;

    private SwitchPreference mPopupNotificationPref;
	//[ramos] end liting
    private ListPreference mNotificaitonMute;

    private int mCurrentSimCount = 0;

    private RingtonePreference mRingtonePref;

    //[ramos] begin by liting 20160223 for dual Sim Ringtone 
    private ramosDefaultRingtonePreference mSIM1InSMSRingtone;

    private ramosDefaultRingtonePreference mSIM2InSMSRingtone;

    public static final String NOTIFICATION_RINGTONE_SIM1 = "pref_key_ringtone_sim1";

    public static final String NOTIFICATION_RINGTONE_SIM2 = "pref_key_ringtone_sim2";

    private AudioProfileManager mProfileManager;

    private static final String mKey = "mtk_audioprofile_general";

    private PreferenceCategory root;

    private Context mContext;

    private final H mHandler = new H();
    //[ramos] end liting
    
    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        /// KK migration, for default MMS function. @{
        boolean isSmsEnabled = MmsConfig.isSmsEnabled(this);
        MmsLog.d(TAG, "onResume sms enable? " + isSmsEnabled);
        if (!isSmsEnabled) {
            finish();
            return;
        }
        /// @}
        // Since the enabled notifications pref can be changed outside of this activity,
        // we have to reload it whenever we resume.
        setEnabledNotificationsPref();
        setListPrefSummary();
        // for ALPS01836799, refresh ring tone summary.
        setRingtoneSummary(getMmsRingtone(this));
        //[ramos] begin by liting 20160223 for dual Sim Ringtone 
        if (MmsApp.isDualRingtone) {
            updateRingtonePref();
        }
		//[ramos] end liting
    }

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        MmsLog.d(TAG, "onCreate");
        ActionBar actionBar = getActionBar();
        actionBar.setTitle(getResources().getString(R.string.actionbar_notification_setting));
        actionBar.setDisplayHomeAsUpEnabled(true);
		//[ramos]modified by liting 20150918
		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
		actionBar.setCustomView(R.layout.ramos_actionbar);
        TextView actionbartitle=(TextView)findViewById(R.id.ramos_actionbar_title);
        actionbartitle.setText(R.string.actionbar_notification_setting);
        
    	TextView returntextview=(TextView)findViewById(R.id.preference_return_textview);
    	returntextview.setText(R.string.set);
    	returntextview.setVisibility(View.VISIBLE);
    	LinearLayout linear=(LinearLayout)findViewById(R.id.preference_actionbar_return);
    	linear.setVisibility(View.VISIBLE);
		linear.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    finish();
                }
            });
		//[ramos]end liting
        setMessagePreferences();
    }

    private void setListPrefSummary() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        long muteStart = sp.getLong(MUTE_START, 0);
        int muteOrigin = Integer.parseInt(sp.getString(NOTIFICATION_MUTE, "0"));
        if (muteStart > 0 && muteOrigin > 0) {
            MmsLog.d(TAG, "thread mute timeout, reset to default.");
            int currentTime = (int) (System.currentTimeMillis() / 1000);
            if ((muteOrigin * 3600 + muteStart / 1000) <= currentTime) {
                SharedPreferences.Editor editor =
                                PreferenceManager.getDefaultSharedPreferences(
                                        getApplicationContext()).edit();
                editor.putLong(NotificationPreferenceActivity.MUTE_START, 0);
                editor.putString(NOTIFICATION_MUTE, "0");
                editor.commit();
                // Fix ALPS01088380, should call setValueIndex when preference changed.
                mNotificaitonMute.setValueIndex(0);
            }
        }
        // For notificationMute;
        String notificationMute = sp.getString(NOTIFICATION_MUTE, "0");
        mNotificaitonMute.setSummary(MessageUtils.getVisualTextName(this,
                notificationMute, R.array.pref_mute_choices,
                R.array.pref_mute_values));
    }

    private void setMessagePreferences() {
        mCurrentSimCount = SubscriptionManager.from(this).getActiveSubscriptionInfoCount();
		//[ramos] modified by liting 20150929 
        //addPreferencesFromResource(R.xml.notificationpreferences);
        addPreferencesFromResource(R.xml.ramos_notificationpreferences);
		//[ramos] end liting
        mNotificaitonMute = (ListPreference) findPreference(NOTIFICATION_MUTE);
        mNotificaitonMute.setOnPreferenceChangeListener(this);
		//[ramos] modified by liting 20150929 CheckBoxPreference -> SwitchPreference
        mEnableNotificationsPref = (SwitchPreference) findPreference(NOTIFICATION_ENABLED);
        mVibratePref = (SwitchPreference) findPreference(NOTIFICATION_VIBRATE);
        mPopupNotificationPref = (SwitchPreference) findPreference(POPUP_NOTIFICATION);
		//[ramos] end liting
        mRingtonePref = (RingtonePreference) findPreference(NOTIFICATION_RINGTONE);
		//[ramos] added by liting 20151028 for BUG0009460
		mRingtonePref.setShowDefault(false);
		//[ramos] end liting
        mRingtonePref.setOnPreferenceChangeListener(this);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String soundValue = sharedPreferences.getString(NOTIFICATION_RINGTONE, null);
        setRingtoneSummary(soundValue);
        //[ramos] begin by liting 20160223 for dual Sim Ringtone 
        mContext = this;
		mSIM1InSMSRingtone = (ramosDefaultRingtonePreference) findPreference(NOTIFICATION_RINGTONE_SIM1);
        mSIM2InSMSRingtone = (ramosDefaultRingtonePreference) findPreference(NOTIFICATION_RINGTONE_SIM2);

        root =(PreferenceCategory) getPreferenceScreen().findPreference("pref_key_notification");
        if (MmsApp.isDualRingtone) {
            root.removePreference(mRingtonePref);
            updateRingtonePref();
        } else {
			root.removePreference(mSIM1InSMSRingtone);
			root.removePreference(mSIM2InSMSRingtone);
		}
		//[ramos] end liting
    }

    
    //[ramos] begin by liting 20160223 for dual Sim Ringtone 
    private void updateRingtonePref() {

        SubscriptionInfo sir0 = null;
        SubscriptionInfo sir1 = null;
        final TelephonyManager tm =
            (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
        final int numSlots = tm.getSimCount();

        for (int i = 0; i < numSlots; ++i) {
            if(null == sir0){
                sir0 = findRecordBySlotId(mContext, i);
            }else{
                sir1 = findRecordBySlotId(mContext, i);
                break;
            }
        }
        int sub_sim1 = sir0==null?-1:sir0.getSubscriptionId();
        int sub_sim2 = sir1==null?-1:sir1.getSubscriptionId();
        
        //int SlotId_SIM1 = sub_sim1!=-1 ? SubscriptionManager.getSlotId(sub_sim1):-1;
        //int SlotId_SIM2 = sub_sim2!=-1 ? SubscriptionManager.getSlotId(sub_sim2):-1;
        
        Log.d("yu_shoud", "initRingtone: sub_sim1="+sub_sim1+"; sub_sim2="+sub_sim2);
        //Log.d("yu_shoud", "initRingtone: SlotId_SIM1="+SlotId_SIM1+"; SlotId_SIM2="+SlotId_SIM2);
        
        
        if((sir0 != null && sir1 != null) || (sir0 == null && sir1 == null)){
            if(sub_sim1 == -1){
                sub_sim1 = 1;
            }
            if(sub_sim2 == -1){
                sub_sim2 = 2;
            }

    		mSIM1InSMSRingtone
    				.setStreamType(ramosDefaultRingtonePreference.SMS_TYPE);
    		mSIM1InSMSRingtone.setProfile(mKey);
    		mSIM1InSMSRingtone
    				.setRingtoneType(AudioProfileManager.TYPE_RECEIVED_SMS);
    		mSIM1InSMSRingtone.setSimId(sub_sim1);
    		mSIM1InSMSRingtone.setOnRingtonePreferenceChange(this);
    		if (mSIM2InSMSRingtone != null) {
        		mSIM2InSMSRingtone
        				.setStreamType(ramosDefaultRingtonePreference.SMS_TYPE);
        		mSIM2InSMSRingtone.setProfile(mKey);
        		mSIM2InSMSRingtone
        				.setRingtoneType(AudioProfileManager.TYPE_RECEIVED_SMS);
        		mSIM2InSMSRingtone.setSimId(sub_sim2);
        		mSIM2InSMSRingtone.setOnRingtonePreferenceChange(this);
            }
        }else{
            int sub_id = sub_sim1==-1 ? sub_sim2:sub_sim1;
            Log.d("yu_shoud", "initRingtone: sub_id="+sub_id);
            if (mSIM2InSMSRingtone != null) {
                root.removePreference(mSIM2InSMSRingtone);
    			mSIM2InSMSRingtone = null;
            }
			mSIM1InSMSRingtone.setTitle(R.string.pref_title_notification_ringtone_ramos);
    		mSIM1InSMSRingtone
    				.setStreamType(ramosDefaultRingtonePreference.SMS_TYPE);
    		mSIM1InSMSRingtone.setProfile(mKey);
    		mSIM1InSMSRingtone
    				.setRingtoneType(AudioProfileManager.TYPE_RECEIVED_SMS);
    		mSIM1InSMSRingtone.setSimId(sub_id);
    		mSIM1InSMSRingtone.setOnRingtonePreferenceChange(this);

        }
        
        lookupRingtoneNames();
    }
    
    public static SubscriptionInfo findRecordBySlotId(Context context, final int slotId) {
        final List<SubscriptionInfo> subInfoList =
                SubscriptionManager.from(context).getActiveSubscriptionInfoList();
        if (subInfoList != null) {
            final int subInfoLength = subInfoList.size();

            for (int i = 0; i < subInfoLength; ++i) {
                final SubscriptionInfo sir = subInfoList.get(i);
                if (sir.getSimSlotIndex() == slotId) {
                    //Right now we take the first subscription on a SIM.
                    return sir;
                }
            }
        }

        return null;
    }

	@Override
	public void onRingtoneChange(Uri ringtoneUri, int RingtoneType, long simId){
		final CharSequence summary = ringtoneUri!=null ? 
		updateRingtoneName(mContext, RingtoneType, ringtoneUri, simId):
		mContext.getString(R.string.silent_ringtone);
	
	    //Log.d("yu_dafdsfd", "onRingtoneChange RingtoneType="+RingtoneType);
	    //Log.d("yu_dafdsfd", "onRingtoneChange RingtoneType="+RingtoneType);
		Log.d("litingnew", "onRingtoneChange summary="+summary);
		if (summary != null) {
			if(RingtoneType == RingtoneManager.TYPE_RECEIVED_SMS){
                Log.d("litingnew","mSIM1InSMSRingtone :  "+mSIM1InSMSRingtone);
                Log.d("litingnew","simId :  "+simId);
                Log.d("litingnew","mSIM1InSMSRingtone.getSimId() :  "+mSIM1InSMSRingtone.getSimId());
				if(mSIM1InSMSRingtone != null && simId == mSIM1InSMSRingtone.getSimId()){
				    mHandler.obtainMessage(H.UPDATE_PHONE_SMSTONE_SIM1, summary).sendToTarget();
				}else if(mSIM2InSMSRingtone != null && simId == mSIM2InSMSRingtone.getSimId()){
					mHandler.obtainMessage(H.UPDATE_PHONE_SMSTONE_SIM2, summary).sendToTarget();
				}
			}
		}else{
			lookupRingtoneNames();
		}
	}

	public void lookupRingtoneNames() {
        Log.d("litingnew","lookupRingtoneNames  ");
		 AsyncTask.execute(mLookupRingtoneNames);
	}
    
	private final Runnable mLookupRingtoneNames = new Runnable() {
		 @Override
		 public void run() {
             Log.d("litingnew","mSIM1InSMSRingtone :  "+mSIM1InSMSRingtone);
			 if (mSIM1InSMSRingtone != null) {
				 final CharSequence summary = updateRingtoneName(
						 mContext, RingtoneManager.TYPE_RECEIVED_SMS, null, mSIM1InSMSRingtone.getSimId());
                 Log.d("litingnew","mLookupRingtoneNames--summary:  "+summary);
				 if (summary != null) {
					 mHandler.obtainMessage(H.UPDATE_PHONE_SMSTONE_SIM1, summary).sendToTarget();
				 }
			 }
			 if (mSIM2InSMSRingtone != null) {
				 final CharSequence summary = updateRingtoneName(
						 mContext, RingtoneManager.TYPE_RECEIVED_SMS, null, mSIM2InSMSRingtone.getSimId());
				 if (summary != null) {
					 mHandler.obtainMessage(H.UPDATE_PHONE_SMSTONE_SIM2, summary).sendToTarget();
				 }
			 }
		 }
	 };

    private static CharSequence updateRingtoneName(Context context, int type, Uri ringtoneUri, long simId) {
        if (context == null) {
            Log.e(TAG, "Unable to update ringtone name, no context provided");
            return null;
        }
    
        if(ringtoneUri == null){
            AudioProfileManager prleManager = (AudioProfileManager) context.getSystemService(Context.AUDIO_PROFILE_SERVICE);
            ringtoneUri = prleManager.getRingtoneUri(mKey, type, simId);
            //ringtoneUri = RingtoneManager.getActualDefaultRingtoneUri(context, type);
            Log.d(TAG, "updateRingtoneName ringtoneUri="+ringtoneUri+"; type="+type);
        }
        
        CharSequence summary = context.getString(R.string.silent_ringtone /*ringtone_unknown*/);
        // Is it a silent ringtone?
        if (ringtoneUri == null) {
            //summary = context.getString(com.android.internal.R.string.ringtone_silent);
        } else {
            Cursor cursor = null;
            try {
                if (MediaStore.AUTHORITY.equals(ringtoneUri.getAuthority())) {
                    // Fetch the ringtone title from the media provider
                    cursor = context.getContentResolver().query(ringtoneUri,
                            new String[] { MediaStore.Audio.Media.TITLE }, null, null, null);
                } else if (ContentResolver.SCHEME_CONTENT.equals(ringtoneUri.getScheme())) {
                    cursor = context.getContentResolver().query(ringtoneUri,
                            new String[] { OpenableColumns.DISPLAY_NAME }, null, null, null);
                }
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        summary = cursor.getString(0);
                    }else{
                        summary = context.getString(com.android.internal.R.string.ringtone_unknown);
                    }
                    
                }else{
                    summary = context.getString(com.android.internal.R.string.ringtone_unknown);
                }
            } catch (SQLiteException sqle) {
                summary = context.getString(com.android.internal.R.string.ringtone_unknown);
                // Unknown title for the ringtone
            } catch (IllegalArgumentException iae) {
                // Some other error retrieving the column from the provider
                summary = context.getString(com.android.internal.R.string.ringtone_unknown);
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }   
    
        return summary;
    }

    private final class H extends Handler {
         private static final int UPDATE_PHONE_RINGTONE_SIM1 = 1;
         private static final int UPDATE_PHONE_RINGTONE_SIM2 = 2;
         private static final int UPDATE_PHONE_SMSTONE_SIM1 = 3;
         private static final int UPDATE_PHONE_SMSTONE_SIM2 = 4;
    
         private static final int UPDATE_NOTIFICATION_RINGTONE = 5;
         private static final int STOP_SAMPLE = 6;
         private static final int UPDATE_RINGER_ICON = 7;
         private static final int RINGTONE_CHANGE = 8;
         private static final int UPDATE_EFFECTS_SUPPRESSOR = 9;
    
         private H() {
             super(Looper.getMainLooper());
         }
    
         @Override
         public void handleMessage(Message msg) {
             switch (msg.what) {
                 case UPDATE_PHONE_SMSTONE_SIM1: 
                    
                    Log.d("litingnew","handleMessage--UPDATE_PHONE_SMSTONE_SIM1:  "+msg.obj);
                     if(mSIM1InSMSRingtone != null){
                         mSIM1InSMSRingtone.setSummry((CharSequence) msg.obj+"");
                     }  
                     break;
                 case UPDATE_PHONE_SMSTONE_SIM2: 
                     if(mSIM2InSMSRingtone != null){
                         mSIM2InSMSRingtone.setSummry((CharSequence) msg.obj+"");
                     }   
                     break;
             }
         }
     }
    //[ramos] end liting

    private void setRingtoneSummary(String soundValue) {
        MmsLog.d(TAG, "setRingtoneSummary soundValue " + soundValue);
        /// for ALPS01836799, set the ring tone as DEFAULT_RINGTONE if the ring tone not exist. @{
        if (!TextUtils.isEmpty(soundValue) && soundValue.startsWith(EXTERNAL_RINGTONE_PATH)) {
            boolean isRingtoneExist = RingtoneManager.isRingtoneExist(this, Uri.parse(soundValue));
            MmsLog.d(TAG, "Ring tone is exist: " + isRingtoneExist);
            if (!isRingtoneExist) {
                restoreDefaultRingtone();
                soundValue = DEFAULT_RINGTONE;
            }
        }
        /// @}
        Uri soundUri = TextUtils.isEmpty(soundValue) ? null : Uri.parse(soundValue);
        Ringtone tone = soundUri != null ? RingtoneManager.getRingtone(this, soundUri) : null;
        mRingtonePref.setSummary(tone != null ? tone.getTitle(this)
                : getResources().getString(R.string.silent_ringtone));
    }

    private void setEnabledNotificationsPref() {
        // The "enable notifications" setting is really stored in our own prefs. Read the
        // current value and set the checkbox to match.
        mEnableNotificationsPref.setChecked(getNotificationEnabled(this));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case MENU_RESTORE_DEFAULTS:
            restoreDefaultPreferences();
            return true;
        case android.R.id.home:
            // The user clicked on the Messaging icon in the action bar. Take them back from
            // wherever they came from
            finish();
            return true;
        default:
            break;
        }
        return false;
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.clear();
		//[ramos]added by liting 20150923
        //menu.add(0, MENU_RESTORE_DEFAULTS, 0, R.string.restore_default);
		//[ramos]end liting
        return true;
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mEnableNotificationsPref) {
            // Update the actual "enable notifications" value that is stored in secure settings.
            enableNotifications(mEnableNotificationsPref.isChecked(), this);
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    private void restoreDefaultPreferences() {
        SharedPreferences.Editor editor =
                        PreferenceManager.getDefaultSharedPreferences(
                                NotificationPreferenceActivity.this).edit();
        editor.putBoolean(NOTIFICATION_ENABLED, true);
        editor.putString(NOTIFICATION_MUTE, "0");
        editor.putString(NOTIFICATION_RINGTONE, DEFAULT_RINGTONE);
        editor.putBoolean(NOTIFICATION_VIBRATE, true);
		//[ramos] modified by liting 20151113 for modified the default value of notification true->false
        //editor.putBoolean(POPUP_NOTIFICATION, true);
        editor.putBoolean(POPUP_NOTIFICATION, false);
		//[ramos] end liting
        editor.apply();
        setPreferenceScreen(null);
        setMessagePreferences();
        setListPrefSummary();
    }

    public static boolean getNotificationEnabled(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean notificationsEnabled
                = prefs.getBoolean(NotificationPreferenceActivity.NOTIFICATION_ENABLED, true);
        return notificationsEnabled;
    }

    public static void enableNotifications(boolean enabled, Context context) {
        // Store the value of notifications in SharedPreferences
        SharedPreferences.Editor editor
                = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putBoolean(NotificationPreferenceActivity.NOTIFICATION_ENABLED, enabled);
        editor.apply();
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        final String key = preference.getKey();
        String notificationMute = (String) newValue;
        if (NOTIFICATION_MUTE.equals(key)) {
            CharSequence mMute = MessageUtils.getVisualTextName(
                    this, notificationMute, R.array.pref_mute_choices,
                    R.array.pref_mute_values);
            mNotificaitonMute.setSummary(mMute);
            MmsLog.d(TAG, "preference change: " + mMute.toString());
            if (notificationMute.equals("0")) {
                SharedPreferences sp = PreferenceManager
                        .getDefaultSharedPreferences(NotificationPreferenceActivity.this);
                SharedPreferences.Editor editor = sp.edit();
                editor.putLong(MUTE_START, 0);
                editor.commit();
            } else {
                Long muteTime = System.currentTimeMillis();
                SharedPreferences sp = PreferenceManager
                        .getDefaultSharedPreferences(NotificationPreferenceActivity.this);
                SharedPreferences.Editor editor = sp.edit();
                editor.putLong(MUTE_START, muteTime);
                editor.commit();
            }
        } else if (NOTIFICATION_RINGTONE.equals(key)) {
            setRingtoneSummary((String) newValue);
        }
        return true;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        MmsLog.d(TAG, "onConfigurationChanged: newConfig = " + newConfig + ",this = " + this);
        super.onConfigurationChanged(newConfig);
        this.getListView().clearScrapViewsIfNeeded();
    }

    public static boolean isNotificationEnable() {
        SharedPreferences prefs
                = PreferenceManager.getDefaultSharedPreferences(MmsApp.getApplication());
        boolean enable = prefs.getBoolean(NotificationPreferenceActivity.NOTIFICATION_ENABLED,
                false);
        return enable;
    }

    public static boolean isPopupNotificationEnable() {
        if (!isNotificationEnable()) {
            return false;
        }
        SharedPreferences prefs
                = PreferenceManager.getDefaultSharedPreferences(MmsApp.getApplication());
		//[ramos] modified by liting 20151113 for modified the default value of notification true->false
        //boolean enable = prefs.getBoolean(NotificationPreferenceActivity.POPUP_NOTIFICATION, true);
        boolean enable = prefs.getBoolean(NotificationPreferenceActivity.POPUP_NOTIFICATION, false);
		//[ramos] end liting
        return enable;
    }

    public static String getMmsRingtone(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(context);
        return sharedPreferences.getString(NOTIFICATION_RINGTONE, null);
    }

    /**
     * Use to set DEFAULT_RINGTONE as ring tone.
     */
    private void restoreDefaultRingtone() {
        // Restore the value of ring tone in SharedPreferences
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this)
                .edit();
        editor.putString(NotificationPreferenceActivity.NOTIFICATION_RINGTONE, DEFAULT_RINGTONE);
        editor.apply();
    }
}
