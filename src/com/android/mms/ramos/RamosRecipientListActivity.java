/*
* Copyright (C) 2014 MediaTek Inc.
* Modification based on code covered by the mentioned copyright
* and/or permission notice(s).
*/
/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.mms.ramos;

import android.app.ActionBar;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract.Profile;
import android.provider.Telephony.Mms;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.QuickContactBadge;
import android.widget.TextView;

import com.android.mms.R;
import com.android.mms.data.Contact;
import com.android.mms.data.ContactList;
import com.android.mms.data.Conversation;

import com.android.mms.MmsConfig;
import android.util.Log;
import com.android.i18n.phonenumbers.geocoding.PhoneNumberOfflineGeocoder;
import com.android.i18n.phonenumbers.NumberParseException;
import com.android.i18n.phonenumbers.PhoneNumberUtil;
import com.android.i18n.phonenumbers.Phonenumber.PhoneNumber;
import android.location.Country;
import android.location.CountryDetector;
import java.util.Locale;
import android.widget.LinearLayout;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import com.android.mms.ramos.RamosPopupMenuWindow;
import android.telephony.SubscriptionManager;
import android.telephony.SubscriptionInfo;
import android.telecom.TelecomManager;
import com.android.internal.telephony.TelephonyIntents;
import java.util.List;
import android.telecom.PhoneAccountHandle;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.telephony.TelephonyManager;
import android.net.Uri;
import android.provider.Settings;
import android.view.WindowManager.LayoutParams;
import com.android.mms.ui.ComposeMessageActivity;
import com.android.mms.ui.ConversationList;
//import com.android.mms.ui.MessageTabSettingActivity;
//import com.android.mms.ui.SettingListActivity;

/**
 * Display a list of recipients for a group conversation. This activity expects to receive a
 * threadId in the intent's extras.
 */
public class RamosRecipientListActivity extends ListActivity implements Contact.UpdateListener {
    private final static String TAG = "RamosRecipientListActivity";

    private long mThreadId;

    /// M: fix bug ALPS00433593, For posting UI update Runnables from other threads
    private Handler mHandler = new Handler();
    private ContactList mContacts;
    private Conversation mConv;
    private RecipientListAdapter mRecipientListAdapter = null;
    private View mActionBarCustomView;
    List<PhoneAccountHandle> phoneAccountsList;
    private AlertDialog mCallSubDialog;
    private int mSubCount; //The count of current sub cards.  0/1/2
    private List<SubscriptionInfo> mSubInfoList;
	private int recipientsId;
    private Intent mAddContactIntent;   // Intent used to add a new contact
    private static final int MODE_PHONE1_ONLY = 1;
    public QuickContactBadge mBadge;
    public int mPosition;


    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        if (icicle != null) {
            // Retrieve previously saved state of this activity.
            mThreadId = icicle.getLong(ComposeMessageActivity.THREAD_ID);
        } else {
            mThreadId = getIntent().getLongExtra(ComposeMessageActivity.THREAD_ID, 0);
        }
        if (mThreadId == 0) {
            Log.w(TAG, "No thread_id specified in extras or icicle. Finishing...");
            finish();
            return;
        }

        mConv = Conversation.get(this, mThreadId, true);
        if (mConv == null) {
            Log.w(TAG, "No conversation found for threadId: " + mThreadId + ". Finishing...");
            finish();
            return;
        }
        mContacts = mConv.getRecipients();
        
        TelecomManager telecomManager = TelecomManager.from(this);
		phoneAccountsList =telecomManager.getCallCapablePhoneAccounts();

        getListView().setAdapter(new RecipientListAdapter(this, R.layout.ramos_recipient_list_item,
                mContacts));
        
        ActionBar actionBar = getActionBar();
        if (actionBar.getCustomView() == null) {
            actionBar.setCustomView(R.layout.ramos_actionbar_recipient_list);
        }
        //actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        //actionBar.setDisplayShowTitleEnabled(false);
        int cnt = mContacts.size();
        View customView = actionBar.getCustomView();
        TextView topTitle = (TextView) customView.findViewById(R.id.tv_top_title);
        TextView subTitle = (TextView) customView.findViewById(R.id.tv_top_subtitle);
        topTitle.setText(getResources().getText(R.string.contact_view));
        subTitle.setText(getResources().getQuantityString(R.plurals.recipient_count,
                cnt, cnt));
        
		TextView returntextview=(TextView)findViewById(R.id.preference_return_textview);
    	returntextview.setVisibility(View.VISIBLE);
    	returntextview.setText(R.string.conversation_list_title);
		LinearLayout linear=(LinearLayout)findViewById(R.id.preference_actionbar_return);
    	linear.setVisibility(View.VISIBLE);
		linear.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    finish();
                }
            });
        mActionBarCustomView = actionBar.getCustomView();
        
        mGetSubInfoRunnable.run();
        /// M: fix bug ALPS00433593, refresh View when update Contact
        Contact.addListener(this);
        mFirstEnterActivity = true;
    }
    
    Runnable mGetSubInfoRunnable = new Runnable() {
        public void run() {
            getSubInfoList();
        }
    };

	private void updateRecipientsPopupWindow(int recipientsId) {
		
        int currentSimMode = Settings.System.getInt(RamosRecipientListActivity.this.getContentResolver(),
                Settings.System.MSIM_MODE_SETTING, -1);
        boolean SimMode1 = false;

		RamosPopupMenuWindow mRecipientsPopupMenu = new RamosPopupMenuWindow(this, getWindow().getDecorView());

		mRecipientsPopupMenu.setTitle(getString(R.string.menu_option));
		mRecipientsPopupMenu.init();
		mRecipientsPopupMenu.clear();
        mRecipientsPopupMenu.addItem(R.id.menu_create_contact, getString(R.string.menu_add_to_contacts), true);
        if (mSubCount == 0) {
            mRecipientsPopupMenu.addItem(R.id.menu_call_recipient, getString(R.string.menu_call), false);
        }else if (mSubCount == 1) {
            SimMode1 = (currentSimMode & (MODE_PHONE1_ONLY << mSubInfoList.get(0).getSimSlotIndex())) != 0;
            if ( !SimMode1) {
                mRecipientsPopupMenu.addItem(R.id.menu_call_recipient, getString(R.string.menu_call), false);
            } else {
                mRecipientsPopupMenu.addItem(R.id.menu_call_recipient, getString(R.string.menu_call), true);
            }
        } else {
            SimMode1 = (currentSimMode & (MODE_PHONE1_ONLY << mSubInfoList.get(0).getSimSlotIndex())) != 0;
            boolean SimMode2 = (currentSimMode & (MODE_PHONE1_ONLY << mSubInfoList.get(1).getSimSlotIndex())) != 0;
            if( !SimMode1 && !SimMode2) {
                mRecipientsPopupMenu.addItem(R.id.menu_call_recipient, getString(R.string.menu_call), false);
            } else {
                mRecipientsPopupMenu.addItem(R.id.menu_call_recipient, getString(R.string.menu_call), true);
            }
        }
		mRecipientsPopupMenu.show();
		mRecipientsPopupMenu.setOnPopupItemClickListener(new RamosPopupMenuWindow.OnPopupItemClickListener() { 	
			@Override
			public boolean onPopupItemClick(int itemId) {
				View v = findViewById(itemId);
				if(null != v)
					mRecipientsOnClickListener.onClick(v);
				return true;
			}
		});
	}

	View.OnClickListener mRecipientsOnClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View item) {
			Contact c = mContacts.get(recipientsId);
			c.reload(true);
			switch (item.getId()) {
				case R.id.menu_call_recipient:
                    dialRecipientRamosMenu(false);					
					break;
				case R.id.menu_create_contact:
					mAddContactIntent = ConversationList.createAddContactIntent(c.getNumber());
					startActivityForResult(mAddContactIntent, ComposeMessageActivity.REQUEST_CODE_ADD_CONTACT);
					break;
			}
		}
	};	
    
    private BroadcastReceiver mSubReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (action != null && action.equals(TelephonyIntents.ACTION_SUBINFO_RECORD_UPDATED)) {
                    getSubInfoList();
            }
        }
    };

    private void getSubInfoList() {
        mSubInfoList = SubscriptionManager.from(this).getActiveSubscriptionInfoList();
        mSubCount = (mSubInfoList != null && !mSubInfoList.isEmpty()) ? mSubInfoList.size() : 0;
        Log.d(TAG, "RamosRecipientListActivity.getSubInfoList(): mSubCount = " + mSubCount);
    }
    private boolean isRecipientCallable() {
        return (mContacts.size() == 1 && !mContacts.containsEmail());
    }
    
    private void dialRecipientRamosMenu(Boolean isVideoCall) {
        TelephonyManager telephony = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        if (telephony != null && telephony.isVoiceCapable()) {
            if (mSubCount <= 1) {
                //if (isRecipientCallable()) {
                    String number = mContacts.get(mPosition).getNumber();
                    Intent dialIntent ;
                    if (isVideoCall) {
                        dialIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + number));
                        dialIntent.putExtra("com.android.phone.extra.video", true);
                    } else {
                        dialIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + number));
                    }
                    startActivity(dialIntent);
                //}
            } else {
                int currentSimMode = Settings.System.getInt(RamosRecipientListActivity.this.getContentResolver(),
                        Settings.System.MSIM_MODE_SETTING, -1);
                boolean SimMode1 = (currentSimMode & (MODE_PHONE1_ONLY << mSubInfoList.get(0).getSimSlotIndex())) != 0;
                boolean SimMode2 = (currentSimMode & (MODE_PHONE1_ONLY << mSubInfoList.get(1).getSimSlotIndex())) != 0;
                if( SimMode1 && SimMode2) {
                    showCallSubSelectedDialog();                   
                } else if (SimMode1) {
                    dialRecipientSIM1(false);
                } else if (SimMode2) {
                    dialRecipientSIM2(false);
                }
            }
        }

    }

    private void showCallSubSelectedDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        mCallSubDialog = builder.create();
        mCallSubDialog.show();
        LayoutParams layoutParams;
        layoutParams = mCallSubDialog.getWindow().getAttributes();
        layoutParams.width = getResources().getDimensionPixelSize(R.dimen.ramos_dialog_width);
        mCallSubDialog.getWindow().setAttributes(layoutParams); 
        mCallSubDialog.setContentView(R.layout.ramos_dial_dialog);
        mCallSubDialog.setCancelable(true);
        mCallSubDialog.setCanceledOnTouchOutside(true);
        LinearLayout mCard1 = (LinearLayout) mCallSubDialog.findViewById(R.id.ramos_card_one);
        mCard1.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    dialRecipientSIM1(false);
                    mCallSubDialog.dismiss();
                }
            });
        LinearLayout mCard2 = (LinearLayout) mCallSubDialog.findViewById(R.id.ramos_card_two);
        mCard2.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    dialRecipientSIM2(false);
                    mCallSubDialog.dismiss();
                }
            });
        TextView mTextCard1 = (TextView) mCallSubDialog.findViewById(R.id.btn_sim1_call);
        TextView mTextCard2 = (TextView) mCallSubDialog.findViewById(R.id.btn_sim2_call);
        mTextCard1.setText(mSubInfoList.get(0).getDisplayName().toString());
        mTextCard2.setText(mSubInfoList.get(1).getDisplayName().toString());
    }


	private void dialRecipientSIM1(Boolean isVideoCall) {
        //if (isRecipientCallable()) {
            String number = mContacts.get(mPosition).getNumber();
            Intent dialIntent ;
            if (isVideoCall) {
                dialIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + number));
                dialIntent.putExtra("com.android.phone.extra.video", true);
            } else {
				getTelecomManager().setUserSelectedOutgoingPhoneAccount(phoneAccountsList.get(0));
                dialIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + number));
            }
            startActivity(dialIntent);
			
        //}
	}
	
	private void dialRecipientSIM2(Boolean isVideoCall) {
        //if (isRecipientCallable()) {
            String number = mContacts.get(mPosition).getNumber();
            Intent dialIntent ;
            if (isVideoCall) {
                dialIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + number));
                dialIntent.putExtra("com.android.phone.extra.video", true);
            } else {
				getTelecomManager().setUserSelectedOutgoingPhoneAccount(phoneAccountsList.get(1));
                dialIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + number));
            }
            startActivity(dialIntent);
			
        //}
	}
    
    private TelecomManager getTelecomManager() {
        return (TelecomManager) this.getSystemService(Context.TELECOM_SERVICE);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putLong(ComposeMessageActivity.THREAD_ID, mThreadId);
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.action_settings:
/*
                Intent settingIntent = null;
                if (MmsConfig.isSupportTabSetting()) {
                    settingIntent = new Intent(this, MessageTabSettingActivity.class);
                } else {
                    settingIntent = new Intent(this, SettingListActivity.class);
                }
                startActivityIfNeeded(settingIntent, -1);
*/
                break;
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.recipient_list_menu, menu);
        menu.clear();
        return true;
    }

    /// M: fix bug ALPS00433593, refresh View when update Contact @{
    public void onUpdate(Contact updated) {
        Log.v(TAG, "Contact onUpdate");
        mHandler.post(new Runnable() {
            public void run() {
                getListView().invalidateViews();
            }
        });
    }

    private boolean mFirstEnterActivity;

    @Override
    protected void onResume() {
        super.onResume();
        if (!mFirstEnterActivity) {
            Log.v(TAG, "onResume: Contact reload thread start");
            new Thread(new Runnable() {
                public void run() {
                    mContacts = mConv.getRecipients();
                    for (int i = 0; i < mContacts.size(); i++) {
                        mContacts.get(i).reload(false);
                    }
                }
            }).start();
        }
        mFirstEnterActivity = false;
    }

    @Override
    protected void onStart() {
        // TODO Auto-generated method stub
        super.onStart();
        if (!mFirstEnterActivity) {
            Log.v(TAG, "onStart: Contact reload thread start");
            new Thread(new Runnable() {
                public void run() {
                    mContacts = mConv.getRecipients();
                    for (int i = 0; i < mContacts.size(); i++) {
                        mContacts.get(i).reload(false);
                    }
                }
            }).start();
        }
        mFirstEnterActivity = false;
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        Contact.removeListener(this);
        super.onDestroy();
    }
    /// @}

    private class RecipientListAdapter extends ArrayAdapter<Contact> {
        private final int mResourceId;
        private final LayoutInflater mInflater;
        private final Drawable mDefaultContactImage;
        public Context mContext;

        public RecipientListAdapter(Context context, int resource,
                ContactList recipients) {
            super(context, resource, recipients);
            mContext = context;

            mResourceId = resource;
            mInflater = LayoutInflater.from(context);
            mDefaultContactImage =
                    context.getResources().getDrawable(R.drawable.ic_contact_picture);
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            final View listItemView =  mInflater.inflate(mResourceId, null);
            final LinearLayout mListItem =(LinearLayout)listItemView.findViewById(R.id.ramos_recipient_list_item);

            final TextView nameView = (TextView)listItemView.findViewById(R.id.name);
            final TextView numberView = (TextView)listItemView.findViewById(R.id.number);

            final Contact contact = getItem(position);
            final String name = contact.getName();
            final String number = contact.getNumber();
            if (!name.equals(number)) {
                nameView.setText(name);
                numberView.setText(number);
            } else {
                nameView.setText(number);
//                numberView.setText(null);
//                if (MmsConfig.isNumberLocationEnable()) {
                numberView.setText(getNumberLocation(mContext, number));
//                }
            }

            final QuickContactBadge badge = (QuickContactBadge)listItemView.findViewById(R.id.avatar);
            //mBadge = badge;
            mListItem.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        mPosition = position;
                        if (Mms.isEmailAddress(number)) {
                            //badge.assignContactFromEmail(number, true);
                            badge.onClick(mActionBarCustomView);
                        } else {
                            if (contact.existsInDatabase()) {
                                badge.onClick(mActionBarCustomView);
                            } else {
                                recipientsId = position;
                                updateRecipientsPopupWindow(recipientsId);
                            }
                        }
                    }
                });
            if (Mms.isEmailAddress(number)) {
                badge.assignContactFromEmail(number, true);
            } else {
                if (contact.existsInDatabase()) {
                    badge.assignContactUri(contact.getUri());
                } else {
                    badge.assignContactFromPhone(contact.getNumber(), true);
                }
            }

            final Drawable avatarDrawable = contact.getAvatar(getContext(), mDefaultContactImage, -1);
            badge.setImageDrawable(avatarDrawable);
            badge.setVisibility(View.GONE);

            return listItemView;
        }
        
        public String getNumberLocation(Context context, String number) {
            
            PhoneNumberUtil util = PhoneNumberUtil.getInstance();
            PhoneNumberOfflineGeocoder geocoder = PhoneNumberOfflineGeocoder.getInstance();
        
            Locale locale = context.getResources().getConfiguration().locale;
            String countryIso = getCurrentCountryIso(context, locale);
            PhoneNumber pn = null;
            try {
                Log.d(TAG, "parsing '" + number
                                + "' for countryIso '" + countryIso + "'...");
                pn = util.parse(number, countryIso);
                Log.d(TAG, "- parsed number: " + pn);
            } catch (NumberParseException e) {
                Log.d(TAG, "getGeoDescription: NumberParseException for incoming number '" + number + "'");
            }
        
            if (pn != null) {
                String description = geocoder.getDescriptionForNumber(pn, locale);
                Log.d(TAG, "- got description: '" + description + "'");
                return description;
            } else {
                return null;
            }
        }
        
        private String getCurrentCountryIso(Context context, Locale locale) {
            String countryIso = null;
            CountryDetector detector = (CountryDetector) context.getSystemService(
                    Context.COUNTRY_DETECTOR);
            if (detector != null) {
                Country country = detector.detectCountry();
                if (country != null) {
                    countryIso = country.getCountryIso();
                } else {
                    Log.d(TAG, "CountryDetector.detectCountry() returned null.");
                }
            }
            if (countryIso == null) {
                countryIso = locale.getCountry();
                Log.d(TAG, "No CountryDetector; falling back to countryIso based on locale: "
                        + countryIso);
            }
            return countryIso;
        }
        
    }
    
}
