package com.haiyisoft.hvpn;

import org.spongycastle.openssl.PEMReader;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceGroup;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;

/**
 * The activity class for editing a new or existing VPN profile.
 */
public class VpnEditor extends PreferenceActivity {
	private static final int MENU_SAVE = Menu.FIRST;
	private static final int MENU_CANCEL = Menu.FIRST + 1;

	static final String KEY_PROFILE = "openvpn_profile";
	private static final String TAG = VpnEditor.class.getSimpleName();

	private OpenvpnProfile mProfile;
	private boolean mAddingProfile;
	private byte[] mOriginalProfileData;

	private static final String KEY_VPN_NAME = "vpn_name";
	private static final String KEY_VPN_SERVER_NAME = "vpn_server_name";
	private static final String KEY_PORT = "set_port";
	private static final String KEY_PROTO = "set_protocol";
	private EditTextPreference mPort;
	private ListPreference mProto;
	 /***************************************** */
	
	private EditTextPreference mName;
	private EditTextPreference mServerName;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mProfile = (OpenvpnProfile) ((savedInstanceState == null) ? getIntent()
				.getParcelableExtra(VpnSettings1.KEY_VPN_PROFILE)
				: savedInstanceState.getParcelable(KEY_PROFILE));
		mAddingProfile = TextUtils.isEmpty(mProfile.getName());
		Parcel parcel = Parcel.obtain();
		mProfile.writeToParcel(parcel, 0);
		mOriginalProfileData = parcel.marshall();

		// Loads the XML preferences file
		addPreferencesFromResource(R.xml.vpn_edit);

		String formatString = mAddingProfile ? getString(R.string.vpn_edit_title_add)
				: getString(R.string.vpn_edit_title_edit);
		setTitle(String.format(formatString, "OpenVPN"));

		PreferenceGroup subpanel = getPreferenceScreen();
		mName = (EditTextPreference) subpanel.findPreference(KEY_VPN_NAME);
		mName.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference pref, Object newValue) {
				String v = ((String) newValue).trim();
				mProfile.setName(v);
				mName.setSummary(v);
				return true;
			}
		});
		String newName = mProfile.getName();
		newName = (newName == null) ? "" : newName.trim();
		mName.setSummary(newName);
		mName.setText(newName);

		mServerName = (EditTextPreference) subpanel.findPreference(KEY_VPN_SERVER_NAME);
		mServerName
				.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
					@Override
					public boolean onPreferenceChange(Preference pref,
							Object newValue) {
						String v = ((String) newValue).trim();
						mProfile.setServerName(v);
						mServerName.setSummary(v);
						return true;
					}
				});
		mServerName.setSummary(mProfile.getServerName());
		mServerName.setText(mProfile.getServerName());

		mPort = (EditTextPreference) findPreference(KEY_PORT);
		mProto = (ListPreference) findPreference(KEY_PROTO);
		mPort.setSummary(mProfile.getPort());
		mPort.setText(mProfile.getPort());
		mPort.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			public boolean onPreferenceChange(Preference pref, Object newValue) {
				String name = (String) newValue;
				name.trim();
				mProfile.setPort(name);
				mPort.setSummary(mProfile.getPort());

				return true;
			}
		});

		mProto.setSummary(mProfile.getProto());
		mProto.setValue(mProfile.getProto());
		mProto.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			public boolean onPreferenceChange(Preference pref, Object newValue) {
				String name = (String) newValue;
				name.trim();
				mProfile.setProto(name);
				mProto.setSummary(mProfile.getProto());

				return true;
			}
		});
	}	
	
	@Override
	protected synchronized void onSaveInstanceState(Bundle outState) {
		outState.putParcelable(KEY_PROFILE, mProfile);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(0, MENU_SAVE, 0, R.string.vpn_menu_done).setIcon(
				android.R.drawable.ic_menu_save);
		menu.add(
				0,
				MENU_CANCEL,
				0,
				mAddingProfile ? R.string.vpn_menu_cancel
						: R.string.vpn_menu_revert).setIcon(
				android.R.drawable.ic_menu_close_clear_cancel);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_SAVE:
			if (validateAndSetResult())
				finish();
			return true;

		case MENU_CANCEL:
			if (profileChanged()) {
				DialogFragment dialog = new DialogFragment() {
					@Override
					public Dialog onCreateDialog(Bundle savedInstanceState) {
						return new AlertDialog.Builder(VpnEditor.this)
								.setTitle(android.R.string.dialog_alert_title)
								.setIcon(android.R.drawable.ic_dialog_alert)
								.setMessage(
										mAddingProfile ? R.string.vpn_confirm_add_profile_cancellation
												: R.string.vpn_confirm_edit_profile_cancellation)
								.setPositiveButton(R.string.vpn_yes_button,
										new DialogInterface.OnClickListener() {
											public void onClick(
													DialogInterface dialog,
													int w) {
												finish();
											}
										})
								.setNegativeButton(R.string.vpn_mistake_button,
										null).create();
					}
				};
				dialog.show(getFragmentManager(), null);
			} else {
				finish();
			}
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			if (profileChanged()) {
				DialogFragment dialog = new DialogFragment() {
					@Override
					public Dialog onCreateDialog(Bundle savedInstanceState) {
						return new AlertDialog.Builder(VpnEditor.this)
								.setTitle(android.R.string.dialog_alert_title)
								.setIcon(android.R.drawable.ic_dialog_alert)
								.setMessage(
										mAddingProfile ? R.string.vpn_confirm_add_profile_cancellation
												: R.string.vpn_confirm_edit_profile_cancellation)
								.setPositiveButton(R.string.vpn_yes_button,
										new DialogInterface.OnClickListener() {
											public void onClick(
													DialogInterface dialog,
													int w) {
												finish();
											}
										})
								.setNegativeButton(R.string.vpn_mistake_button,
										null).create();
					}
				};
				dialog.show(getFragmentManager(), null);
			} else {
				finish();
			}
			return true;
		default:
			return super.onKeyDown(keyCode, event);
		}
	}

	/**
	 * Checks the validity of the inputs and set the profile as result if valid.
	 * 
	 * @return true if the result is successfully set
	 */
	private boolean validateAndSetResult() {
		String errorMsg = validate();

		if (errorMsg != null) {
			Util.showErrorMessage(this, errorMsg);
			return false;
		}

		if (profileChanged()) {
			Intent intent = new Intent(this, VpnSettings1.class);
			intent.putExtra(VpnSettings1.KEY_VPN_PROFILE, (Parcelable) mProfile);
			setResult(RESULT_OK, intent);
		}
		return true;
	}

	private boolean profileChanged() {
		Parcel newParcel = Parcel.obtain();
		mProfile.writeToParcel(newParcel, 0);
		byte[] newData = newParcel.marshall();
		if (mOriginalProfileData.length == newData.length) {
			for (int i = 0, n = mOriginalProfileData.length; i < n; i++) {
				if (mOriginalProfileData[i] != newData[i])
					return true;
			}
			return false;
		}
		return true;
	}

	public String validate() {
		if (TextUtils.isEmpty(mProfile.getName()))
			return String.format(getString(R.string.vpn_error_miss_entering),
					getString(R.string.vpn_a_name));

		if (TextUtils.isEmpty(mProfile.getServerName()))
			return String.format(getString(R.string.vpn_error_miss_entering),
					getString(R.string.vpn_a_vpn_server));
		return null;
	}
}