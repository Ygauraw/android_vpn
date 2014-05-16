package com.haiyisoft.hvpn;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.haiyisoft.hvpn.OpenvpnInstaller.Result;

import android.app.DialogFragment;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.VpnService;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcelable;
import android.os.RemoteException;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.EditText;
import android.widget.Toast;

/**
 * The preference activity for configuring VPN settings.
 */
public class VpnSettings1 extends PreferenceActivity {
	// Key to the field exchanged for profile editing.
	static final String KEY_VPN_PROFILE = "vpn_profile";
	private static final String TAG = VpnSettings1.class.getSimpleName();
	private static final String PREF_ADD_VPN = "add_new_vpn2";
	private static final String PREF_VPN_LIST = "vpn_list2";
	private File PROFILES_ROOT;
	private static final String PROFILE_OBJ_FILE = ".pobj";
	private static final int REQUEST_ADD_OR_EDIT_PROFILE = 1;
	private static final int CONTEXT_MENU_DELETE_ID = ContextMenu.FIRST + 0;
	
	static final int OK_BUTTON = DialogInterface.BUTTON_POSITIVE;
	private PreferenceCategory mVpnListContainer;
	// profile name --> VpnPreference
	private Map<String, VpnPreference> mVpnPreferenceMap;
	private List<OpenvpnProfile> mVpnProfileList;
	
	private OpenvpnProfile mConnectingProfile;
	private String mConnectingUsername;
	private String mConnectingPassword;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		PROFILES_ROOT = new File(getFilesDir(), "V2");
		addPreferencesFromResource(R.xml.vpn_settings1);
		// restore VpnProfile list and construct VpnPreference map
		mVpnListContainer = (PreferenceCategory) findPreference(PREF_VPN_LIST);
		((PreferenceScreen) findPreference(PREF_ADD_VPN))
				.setOnPreferenceClickListener(new OnPreferenceClickListener() {
					public boolean onPreferenceClick(Preference preference) {
						startVpnEditor(new OpenvpnProfile());
						return true;
					}
				});
		// for long-press gesture on a profile preference
		registerForContextMenu(getListView());
		retrieveVpnListFromStorage();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putParcelable("profile", mConnectingProfile);
		outState.putString("username", mConnectingUsername);
		outState.putString("password", mConnectingPassword);
	}

	@Override
	protected void onRestoreInstanceState(Bundle state) {
		super.onRestoreInstanceState(state);
		mConnectingProfile = state.getParcelable("profile");
		mConnectingUsername = state.getString("username");
		mConnectingPassword = state.getString("password");
	}
	
	@Override
	protected void onDestroy() {
		unregisterForContextMenu(getListView());
		super.onDestroy();
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		OpenvpnProfile p = getProfile(getProfilePositionFrom((AdapterContextMenuInfo) menuInfo));
		if (p != null) {
			menu.setHeaderTitle(p.getName());
			menu.add(0, CONTEXT_MENU_DELETE_ID, 0, R.string.vpn_menu_delete)
			.setEnabled(true);
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		int position = getProfilePositionFrom((AdapterContextMenuInfo) item
				.getMenuInfo());
		OpenvpnProfile p = getProfile(position);
		switch (item.getItemId()) {
		case CONTEXT_MENU_DELETE_ID:
			deleteProfile(p);
			return true;
		}
		return super.onContextItemSelected(item);
	}

	@Override
	protected void onActivityResult(final int requestCode,
			final int resultCode, final Intent data) {
		if (requestCode == REQUEST_ADD_OR_EDIT_PROFILE) {
			if (resultCode == RESULT_CANCELED || data == null) {
				Log.d(TAG, "no result returned by editor");
				return;
			}
			OpenvpnProfile p = data.getParcelableExtra(KEY_VPN_PROFILE);
			if (p == null) {
				Log.e(TAG, "null object returned by editor");
				return;
			}

			int index = getProfileIndexFromId(p.getId());
			if (checkDuplicateName(p, index)) {
				final OpenvpnProfile profile = p;
				Util.showErrorMessage(this, String.format(
						getString(R.string.vpn_error_duplicate_name),
						p.getName()), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int w) {
						startVpnEditor(profile);
					}
				});
				return;
			}

			try {
				if (index < 0) {
					addProfile(p);
					Util.showShortToastMessage(this, String.format(
							getString(R.string.vpn_profile_added), p.getName()));
				} else {
					replaceProfile(index, p);
					Util.showShortToastMessage(this, String.format(
							getString(R.string.vpn_profile_replaced),
							p.getName()));
				}
			} catch (IOException e) {
				final OpenvpnProfile profile = p;
				Util.showErrorMessage(this, e + ": " + e.getLocalizedMessage(),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int w) {
								startVpnEditor(profile);
							}
						});
			}
		}else {
			throw new RuntimeException("unknown request code: " + requestCode);
		}
	}

	private int getProfileIndexFromId(String id) {
		int index = 0;
		for (OpenvpnProfile p : mVpnProfileList) {
			if (p.getId().equals(id)) {
				return index;
			} else {
				index++;
			}
		}
		return -1;
	}

	// Replaces the profile at index in mVpnProfileList with p.
	// Returns true if p's name is a duplicate.
	private boolean checkDuplicateName(OpenvpnProfile p, int index) {
		VpnPreference pref = mVpnPreferenceMap.get(p.getName());
		if ((pref != null) && (index >= 0) && (index < mVpnProfileList.size())) {
			// not a duplicate if p is to replace the profile at index
			if (pref.mProfile == mVpnProfileList.get(index))
				pref = null;
		}
		return (pref != null);
	}

	private int getProfilePositionFrom(AdapterContextMenuInfo menuInfo) {
		// excludes mVpnListContainer and the preferences above it
		return menuInfo.position - mVpnListContainer.getOrder() - 1;
	}

	// position: position in mVpnProfileList
	private OpenvpnProfile getProfile(int position) {
		return ((position >= 0) ? mVpnProfileList.get(position) : null);
	}

	// position: position in mVpnProfileList
	private void deleteProfile(final OpenvpnProfile p) {
		DeleteConformDialog1 dialog = new DeleteConformDialog1();
		dialog.setId(p.getId());
		showDialog(dialog);
	}

	// Randomly generates an ID for the profile.
	// The ID is unique and only set once when the profile is created.
	private void setProfileId(OpenvpnProfile profile) {
		String id;

		while (true) {
			id = String
					.valueOf(Math.abs(Double.doubleToLongBits(Math.random())));
			if (id.length() >= 8)
				break;
		}
		for (OpenvpnProfile p : mVpnProfileList) {
			if (p.getId().equals(id)) {
				setProfileId(profile);
				return;
			}
		}
		profile.setId(id);
	}

	private void addProfile(OpenvpnProfile p) throws IOException {
		setProfileId(p);
		saveProfileToStorage(p);
		mVpnProfileList.add(p);
		addPreferenceFor(p);
	}

	// Adds a preference in mVpnListContainer
	private VpnPreference addPreferenceFor(OpenvpnProfile p) {
		VpnPreference pref = new VpnPreference(this, p);
		mVpnPreferenceMap.put(p.getName(), pref);
		mVpnListContainer.addPreference(pref);

		pref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference pref) {
				startVpnEditor(((VpnPreference) pref).mProfile);
				return true;
			}
		});
		pref.setEnabled(true);
		return pref;
	}

	// index: index to mVpnProfileList
	private void replaceProfile(int index, OpenvpnProfile p) throws IOException {
		Map<String, VpnPreference> map = mVpnPreferenceMap;
		OpenvpnProfile oldProfile = mVpnProfileList.set(index, p);
		VpnPreference pref = map.remove(oldProfile.getName());
		if (pref.mProfile != oldProfile) {
			throw new RuntimeException("inconsistent state!");
		}
		p.setId(oldProfile.getId());
		// TODO: remove copyFiles once the setId() code propagates.
		// Copy config files and remove the old ones if they are in different
		// directories.
		if (Util.copyFiles(getProfileDir(oldProfile), getProfileDir(p))) {
			removeProfileFromStorage(oldProfile);
		}
		saveProfileToStorage(p);
		pref.setProfile(p);
		map.put(p.getName(), pref);
	}

	private void startVpnEditor(final OpenvpnProfile profile) {
		Intent intent = new Intent(this, VpnEditor.class);
		intent.putExtra(KEY_VPN_PROFILE, (Parcelable) profile);
		startActivityForResult(intent, REQUEST_ADD_OR_EDIT_PROFILE);
	}

	private File getProfileDir(OpenvpnProfile p) {
		return new File(PROFILES_ROOT, p.getId());
	}

	private void saveProfileToStorage(OpenvpnProfile p) throws IOException {
		File f = getProfileDir(p);
		if (!f.exists())
			f.mkdirs();
		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(
				new File(f, PROFILE_OBJ_FILE)));
		oos.writeObject(p);
		oos.close();
	}

	private void removeProfileFromStorage(OpenvpnProfile p) {
		Util.deleteFile(getProfileDir(p));
	}

	private void retrieveVpnListFromStorage() {
		mVpnPreferenceMap = new LinkedHashMap<String, VpnPreference>();
		mVpnProfileList = Collections
				.synchronizedList(new ArrayList<OpenvpnProfile>());
		mVpnListContainer.removeAll();

		File root = PROFILES_ROOT;
		String[] dirs = root.list();
		if (dirs == null)
			return;
		for (String dir : dirs) {
			File f = new File(new File(root, dir), PROFILE_OBJ_FILE);
			if (!f.exists())
				continue;
			try {
				OpenvpnProfile p = deserialize(f);
				if (p == null)
					continue;
				if (!checkIdConsistency(dir, p))
					continue;

				mVpnProfileList.add(p);
			} catch (IOException e) {
				Log.e(TAG, "retrieveVpnListFromStorage()", e);
			}
		}
		Collections.sort(mVpnProfileList, new Comparator<OpenvpnProfile>() {
			@Override
			public int compare(OpenvpnProfile p1, OpenvpnProfile p2) {
				return p1.getName().compareTo(p2.getName());
			}

			@Override
			public boolean equals(Object p) {
				// not used
				return false;
			}
		});
		for (OpenvpnProfile p : mVpnProfileList) {
			addPreferenceFor(p);
		}
	}

	// A sanity check. Returns true if the profile directory name and profile ID
	// are consistent.
	private boolean checkIdConsistency(String dirName, OpenvpnProfile p) {
		if (!dirName.equals(p.getId())) {
			Log.d(TAG, "ID inconsistent: " + dirName + " vs " + p.getId());
			return false;
		} else {
			return true;
		}
	}

	private OpenvpnProfile deserialize(File profileObjectFile)
			throws IOException {
		try {
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(
					profileObjectFile));
			OpenvpnProfile p = (OpenvpnProfile) ois.readObject();
			ois.close();
			return p;
		} catch (ClassNotFoundException e) {
			Log.d(TAG, "deserialize a profile", e);
			return null;
		}
	}

	private static class VpnPreference extends Preference {
		OpenvpnProfile mProfile;

		VpnPreference(Context c, OpenvpnProfile p) {
			super(c);
			setProfile(p);
		}

		void setProfile(OpenvpnProfile p) {
			mProfile = p;
			setTitle(p.getName());
		}
	}

	private void showDialog(DialogFragment dialog) {
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		ft.addToBackStack(null);
		dialog.show(ft, null);
	}
	
	public void doDeleteProfile(String id) {
		int position = getProfileIndexFromId(id);
		if (position >= 0) {
			OpenvpnProfile p = mVpnProfileList.remove(position);
			VpnPreference pref = mVpnPreferenceMap.remove(p.getName());
			mVpnListContainer.removePreference(pref);
			removeProfileFromStorage(p);
		}
	}
}