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

//import com.haiyisoft.hvpn.VpnSettingsFile1.VpnPreference;

//import com.haiyisoft.hvpn.Tab1Activity.SpinnerSelectedListener;
//import com.haiyisoft.hvpn.VpnSettingsFile1.VpnPreference;

//import com.haiyisoft.hvpn.VpnSettingsFile1.VpnPreference;

import android.app.Activity;
import android.location.Address;
import android.os.Bundle;
import android.preference.Preference;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemSelectedListener;

public class ComuniActivity extends Activity{
	
	private static final String[] strs = new String[] {
		    "first", "second", "third", "fourth", "fifth"
		    };
	private static final String TAG = ComuniActivity.class.getSimpleName();
	private AddressFile addrFile;
	private File ADDR_ROOT;
	private static final String ADDR_FILE = ".proj4";
	private List<AddressFile> mAddrFileList;
	private ArrayAdapter<String> adapter;
	private ArrayList<String> list = new ArrayList<String>();
	private ListView lv;
	private Button add_button;
	
	private AddressFile tempFile;
	
	private static final int CONTEXT_MENU_EDIT_ID = ContextMenu.FIRST + 0;
	private static final int CONTEXT_MENU_DELETE_ID = ContextMenu.FIRST + 1;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		ADDR_ROOT = new File(getFilesDir(), "V4");
		setContentView(R.layout.comunicate);
		final TextView m_error=(TextView) findViewById(R.id.m_error);
		
		retrieveAddress();
		adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,list);
		//adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		
		lv = (ListView) findViewById(R.id.lv);
		lv.setOnItemSelectedListener(new ListViewSelectedListener());
		lv.setVisibility(View.VISIBLE);
		lv.setAdapter(adapter);
		
		registerForContextMenu(lv);
		
		add_button = (Button) findViewById(R.id.add_button);
		add_button.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				
				if(add_button.getText().equals("修改"))
				{
					int index = getAddressIndexFromId(tempFile.getId());
					EditText text_ip= (EditText) findViewById(R.id.ip_edit);
					EditText text_port=(EditText) findViewById(R.id.port_edit);
					String ip = text_ip.getText().toString();
					String port = text_port.getText().toString();
					tempFile.setIp(ip);
					tempFile.setPort(port);
					try {
						replaceAddress(index, tempFile);
					} catch (IOException e) {
						// TODO: handle exception
					}
					
					add_button.setText("添加");
					text_ip.setText("");
					text_port.setText("");
					return;
				}
				EditText text_ip= (EditText) findViewById(R.id.ip_edit);
				EditText text_port=(EditText) findViewById(R.id.port_edit);
				String ip = text_ip.getText().toString();
				String port = text_port.getText().toString();
				if(ip.equals("")||port.equals(""))
				{
					TextView m_err11 = (TextView) findViewById(R.id.m_error11);
					m_err11.setText("服务器IP和端口号不能为空");
					return;
				}
				addrFile = new AddressFile();
				addrFile.setIp(ip);
				addrFile.setPort(port);
				try {
					addAddr(addrFile);
				} catch (IOException e) {
					// TODO: handle exception
				}
				
				text_ip.setText(" ");
				text_port.setText(" ");
			}

		});
		
		/*
		 * 重置button
		 * */	
		Button reset_button = (Button) findViewById(R.id.reset_button);
		reset_button.setOnClickListener(new Button.OnClickListener(){
			public void onClick(View v) {
				EditText text_ip= (EditText) findViewById(R.id.ip_edit);
				EditText text_port=(EditText) findViewById(R.id.port_edit);
				
				text_ip.setText("");
				text_port.setText("");
				
			}
			
			
		});
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);

		AddressFile ad = getAddress(getAddressPositionFrom((AdapterContextMenuInfo) menuInfo));
		if (ad != null) {
			menu.setHeaderTitle(ad.getIp());

			menu.add(0, CONTEXT_MENU_EDIT_ID, 0, R.string.vpn_menu_edit)
					.setEnabled(true);
			menu.add(0, CONTEXT_MENU_DELETE_ID, 0, R.string.vpn_menu_delete)
					.setEnabled(true);
		}
	}
	
	private int getAddressPositionFrom(AdapterContextMenuInfo menuInfo) {
		// excludes mVpnListContainer and the preferences above it
		return menuInfo.position;
	}
	
	private AddressFile getAddress(int position) {
		return ((position >= 0) ? mAddrFileList.get(position) : null);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		int position = getAddressPositionFrom((AdapterContextMenuInfo) item
				.getMenuInfo());
		AddressFile ad = getAddress(position);
		tempFile = ad;
		switch (item.getItemId()) {
		case CONTEXT_MENU_EDIT_ID:
			//startVpnEditor(ad);
			EditText text_ip= (EditText) findViewById(R.id.ip_edit);
			EditText text_port=(EditText) findViewById(R.id.port_edit);
			text_ip.setText(ad.getIp());
			text_port.setText(ad.getPort());
			add_button.setText("修改");
			
			return true;

		case CONTEXT_MENU_DELETE_ID:
			doDeleteProfile(ad.getId());
			return true;
		}

		return super.onContextItemSelected(item);
	}
	
	private void replaceAddress(int index, AddressFile a) throws IOException {
		
		AddressFile oldAddress = mAddrFileList.set(index, a);
		
		a.setId(oldAddress.getId());
		// TODO: remove copyFiles once the setId() code propagates.
		// Copy config files and remove the old ones if they are in different
		// directories.
		if (Util.copyFiles(getAddressDir(oldAddress), getAddressDir(a))) {
			removeAddressFromStorage(oldAddress);
		}
		saveAddressToStorage(a);
		
		list = new ArrayList<String>();
		retrieveAddress();
		adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,list);
		lv.setOnItemSelectedListener(new ListViewSelectedListener());
		lv.setVisibility(View.VISIBLE);
		lv.setAdapter(adapter);
		
		
	}
	
	private int getAddressIndexFromId(String id) {
		int index = 0;
		for (AddressFile ad : mAddrFileList) {
			if (ad.getId().equals(id)) {
				return index;
			} else {
				index++;
			}
		}
		return -1;
	}
	
	public void doDeleteProfile(String id) {
		int position = getAddressIndexFromId(id);
		if (position >= 0) {
			AddressFile ad = mAddrFileList.remove(position);
			removeAddressFromStorage(ad);
			list = new ArrayList<String>();
			retrieveAddress();
			adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,list);
			lv.setOnItemSelectedListener(new ListViewSelectedListener());
			lv.setVisibility(View.VISIBLE);
			lv.setAdapter(adapter);
		}
	}
	
	private void removeAddressFromStorage(AddressFile ad) {
		Util.deleteFile(getAddressDir(ad));
	}
	
	private void addAddr(AddressFile ad) throws IOException {
		setAddressId(ad);
		saveAddressToStorage(ad);
		mAddrFileList.add(ad);
		//addPreferenceFor(p);
		list.add(ad.getIp()+"  "+ad.getPort());
		adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,list);
		lv.setOnItemSelectedListener(new ListViewSelectedListener());
		lv.setVisibility(View.VISIBLE);
		lv.setAdapter(adapter);
		
	}
//	
//	private VpnPreference addPreferenceFor(OpenvpnProfileFile p) {
//		VpnPreference pref = new VpnPreference(this, p);
//		mVpnPreferenceMap.put(p.getName(), pref);
//		mVpnListContainer.addPreference(pref);
//		pref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
//			public boolean onPreferenceClick(Preference pref) {
//				startVpnEditor(((VpnPreference) pref).mProfile);
//				return true;
//			}
//		});
//		pref.setEnabled(true);
//		return pref;
//	}
	
	private File getAddressDir(AddressFile ad) {
		return new File(ADDR_ROOT, ad.getId());
	}
	
	private void setAddressId(AddressFile ad) {
		String id;

		while (true) {
			id = String
					.valueOf(Math.abs(Double.doubleToLongBits(Math.random())));
			if (id.length() >= 8)
				break;
		}
		for (AddressFile a : mAddrFileList) {
			if (a.getId().equals(id)) {
				setAddressId(ad);
				return;
			}
		}
		ad.setId(id);
	}
	
	private void saveAddressToStorage(AddressFile ad) throws IOException {
		File f = getAddressDir(ad);
		if (!f.exists())
			f.mkdirs();
		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(
				new File(f, ADDR_FILE)));
		oos.writeObject(ad);
		oos.close();
		
	}
	
	private void retrieveAddress(){
		mAddrFileList = Collections
				.synchronizedList(new ArrayList<AddressFile>());
		File root = ADDR_ROOT;
		String[] dirs = root.list();
		if (dirs == null)
			return;
		for (String dir : dirs) {
			File f = new File(new File(root, dir), ADDR_FILE);
			if (!f.exists())
				continue;
			try {
				AddressFile ad = deserialize2(f);
				if (ad == null)
					continue;
				if (!checkIdConsistency(dir, ad))
					continue;
				mAddrFileList.add(ad);
			} catch (IOException e) {
				Log.e(TAG, "retrieveAddress()", e);
			}
		}
		
		for (AddressFile ad : mAddrFileList) {
			//addPreferenceFor(ad);
			list.add(ad.getIp() + "  " +ad.getPort());
		}
		
	}
	
	private boolean checkIdConsistency(String dirName, AddressFile ad) {
		if (!dirName.equals(ad.getId())) {
			Log.d(TAG, "ID inconsistent: " + dirName + " vs " + ad.getId());
			return false;
		} else {
			return true;
		}
	}
	
	private AddressFile deserialize2(File AddressObjectFile)
			throws IOException {
		try {
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(
					AddressObjectFile));
			AddressFile ad = (AddressFile) ois.readObject();
			ois.close();
			return ad;
		} catch (ClassNotFoundException e) {
			Log.d(TAG, "deserialize a AddrProfile", e);
			return null;
		}
		
	
	}
	
	class ListViewSelectedListener implements OnItemSelectedListener{
		
		public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			//currentProfile = mVpnProfileList.get(arg2);
			//user_name1.setText(currentProfile.getUserCertName());
		}
		
		public void onNothingSelected(AdapterView<?> arg0) {
			
		}
	}
}
