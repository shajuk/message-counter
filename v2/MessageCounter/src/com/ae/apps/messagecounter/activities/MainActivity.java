/*
 * Copyright 2013 Midhun Harikumar
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ae.apps.messagecounter.activities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;

import com.ae.apps.common.managers.ContactManager;
import com.ae.apps.common.managers.SMSManager;
import com.ae.apps.common.utils.DialogUtils;
import com.ae.apps.messagecounter.R;
import com.ae.apps.messagecounter.adapters.SectionsPagerAdapter;
import com.ae.apps.messagecounter.data.MessageDataConsumer;
import com.ae.apps.messagecounter.data.MessageDataReader;
import com.ae.apps.messagecounter.utils.MessageCounterUtils;
import com.ae.apps.messagecounter.vo.ContactMessageVo;

/**
 * Main Activity for the project
 * 
 * @author Midhun
 * 
 */
public class MainActivity extends FragmentActivity implements MessageDataReader {

	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide fragments for each of the sections. We use a
	 * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which will keep every loaded fragment in memory.
	 * If this becomes too memory intensive, it may be best to switch to a
	 * {@link android.support.v4.app.FragmentStatePagerAdapter}.
	 */
	SectionsPagerAdapter				mSectionsPagerAdapter;

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	ViewPager							mViewPager;

	private boolean						isDataReady;
	private Handler						handler;
	private ProgressDialog				loadingDialog;
	private List<ContactMessageVo>		contactMessageList;
	private Map<String, Integer>		messageCountsCache	= new HashMap<String, Integer>();
	private List<MessageDataConsumer>	consumers			= new ArrayList<MessageDataConsumer>();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// getActionBar().setBackgroundDrawable(new ColorDrawable(Color.RED));

		final SMSManager smsManager = new SMSManager(getBaseContext());
		final ContactManager contactManager = new ContactManager(getContentResolver());

		// Cache the message counts
		messageCountsCache.put(SMSManager.SMS_URI_ALL, smsManager.getMessagesCount(SMSManager.SMS_URI_ALL));
		messageCountsCache.put(SMSManager.SMS_URI_SENT, smsManager.getMessagesCount(SMSManager.SMS_URI_SENT));
		messageCountsCache.put(SMSManager.SMS_URI_INBOX, smsManager.getMessagesCount(SMSManager.SMS_URI_INBOX));
		messageCountsCache.put(SMSManager.SMS_URI_DRAFTS, smsManager.getMessagesCount(SMSManager.SMS_URI_DRAFTS));

		// The mViewPager object should be null when running on tablets
		mViewPager = (ViewPager) findViewById(R.id.pager);
		// This adapter that will return a fragment for each of the three primary sections
		mSectionsPagerAdapter = new SectionsPagerAdapter(getBaseContext(), getSupportFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager.setAdapter(mSectionsPagerAdapter);
		mViewPager.setCurrentItem(1);
		
		// Start to show a progress dialog
		loadingDialog = ProgressDialog.show(this, getResources().getString(R.string.title_loading), getResources()
				.getString(R.string.message_loading));

		// Create the handler in the main thread
		handler = new Handler();

		// the data is not yet ready
		isDataReady = false;

		// Do the read and parse operations in a new thread
		new Thread(new Runnable() {

			@Override
			public void run() {
				final List<ContactMessageVo> data;
				boolean isMockedRun = false;
				if (isMockedRun) {
					// We are doing a mock run
					data = MessageCounterUtils.getMockContactMessageList();
				} else {
					// Get the mapping of address and message count
					Map<String, Integer> messageSendersMap = smsManager.getUniqueSenders();
					// Convert to mapping of contact and message count
					messageSendersMap = MessageCounterUtils.convertAddressToContact(contactManager, messageSendersMap);
					// Sorting the map based on message count
					Map<String, Integer> sortedValuesMap = MessageCounterUtils.sortThisMap(messageSendersMap);
					// Convert this data to a list of ContactMessageVos
					data = MessageCounterUtils
							.getContactMessageList(contactManager, sortedValuesMap, messageSendersMap);
				}
				isDataReady = true;
				contactMessageList = data;
				// Dismiss the loading dialog
				loadingDialog.dismiss();

				handler.post(new Runnable() {

					@Override
					public void run() {
						// Inform the consumers that the data is ready
						for (MessageDataConsumer consumer : consumers) {
							consumer.onDataReady(data);
						}
					}
				});
			}
		}).start();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		/*
		 * case R.id.menu_about: startActivity(new Intent(this, AboutActivity.class)); return true;
		 */
		case R.id.menu_license:
			// Show the license dialog
			DialogUtils.showWithMessageAndOkButton(this, R.string.menu_license, R.string.str_license_text,
					android.R.string.ok);
			return true;
		case R.id.menu_share_app:
			// Throw an intent so the user can share this app
			Intent shareIntent = new Intent();
			shareIntent.setAction(Intent.ACTION_SEND);
			shareIntent.setType("text/plain");
			shareIntent.putExtra(Intent.EXTRA_TEXT, getResources().getString(R.string.play_store_url));
			startActivity(shareIntent);
			return true;
		case R.id.menu_settings:
			// Display the preference screen
			startActivity(new Intent(this, SettingsActivity.class));
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void registerForData(MessageDataConsumer consumer) {
		if (consumers == null) {
			consumers = new ArrayList<MessageDataConsumer>();
		}
		consumers.add(consumer);
		// if data is ready, invoke the data ready method
		if (isDataReady) {
			consumer.onDataReady(contactMessageList);
		}
	}

	@Override
	public int getMessageCount(String type) {
		if (messageCountsCache != null) {
			if (messageCountsCache.containsKey(type))
				return messageCountsCache.get(type);
		}
		return 0;
	}

}