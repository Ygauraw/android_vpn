package com.haiyisoft.hvpn;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.aisino.smartsd.SmartSDDev;
import com.aisino.smartsd.SmartSDSample;
import com.haiyisoft.hvpn.OpenvpnInstaller.Result;
import com.haiyisoft.hvpn.Tab1Activity.SpinnerSelectedListener;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.VpnService;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

public class Tab2Activity extends Activity{
	
	private ArrayList<String> list = new ArrayList<String>();
	private File PROFILES_ROOT;
	private File ADDR_ROOT;
	private static final String TAG = Tab2Activity.class.getSimpleName();
	private static final String PROFILE_OBJ_FILE = ".pobj";
	private static final String ADDR_FILE = ".proj4";
	
	private static final int REQUEST_CONNECT = 2;
	
	private Spinner spinner;
	private EditText user_name;
	private Button button_login2;
	
	private ArrayAdapter<String> adapter;
	private String username = "";
	
	private SmartSDSample SSDS;
	private App myApp;
	
	public Tab2Activity(){
		
	}
	
	private List<OpenvpnProfile> mVpnProfileList;
	private List<AddressFile> mAddrFileList;
	private OpenvpnProfile mConnectingProfile;
	private String mConnectingUsername;
	private String mConnectingPassword;
	
	private OpenvpnProfile currentProfile = new OpenvpnProfile();
	
	private VpnStatus mStatus;

	private OpenvpnInstaller installer;
	
	private IVpnService mIVpnService;
	private ServiceConnection mConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mIVpnService = IVpnService.Stub.asInterface(service);
			try {
				mStatus = mIVpnService.checkStatus();
			} catch (RemoteException e) {
				Log.e(getClass().getName(), "Unable to connect service", e);
				ErrorMsgDialog dialog = new ErrorMsgDialog();
				dialog.setMessage(e.getLocalizedMessage());
				showDialog(dialog);
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			mIVpnService = null;
		}
	};

	private BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			VpnStatus s = intent.getParcelableExtra("connection_state");
			if (s != null) {
				mStatus = s;
			}
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.tab2);
        PROFILES_ROOT = new File(getFilesDir(), "V2");
        ADDR_ROOT = new File(getFilesDir(), "V4");
        retrieveAddress();
        //retrieveVpnListFromStorage();
		spinner = (Spinner) findViewById(R.id.Spinner02);
		adapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,list);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
		spinner.setOnItemSelectedListener(new SpinnerSelectedListener());
		spinner.setVisibility(View.VISIBLE);
		
		IntentFilter filter = new IntentFilter();
		filter.addAction("com.haiyisoft.hvpn.connectivity");
		registerReceiver(mReceiver, filter);
		
		this.getApplicationContext().bindService(new Intent(this, OpenVpnService.class), mConnection,
				Context.BIND_AUTO_CREATE);
		
		Button button_comuni = (Button) findViewById(R.id.button_comunicate2);
		button_comuni.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(Tab2Activity.this, ComuniActivity.class);
				startActivity(intent);
			}
		});
		
		Button button_modify = (Button) findViewById(R.id.button_modifypass2);
		button_modify.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(Tab2Activity.this, ModifyPassword.class);
				startActivity(intent);
			}
		});
		
        final TextView pass_error = (TextView) findViewById(R.id.password_error2);
        user_name = (EditText) findViewById(R.id.username_edit2);
        user_name.setCursorVisible(false);
        user_name.setFocusable(false);
        user_name.setFocusableInTouchMode(false);
        
        myApp = (App) getApplicationContext();
        SSDS = myApp.getSmartSDSample();
        
        String path = "";
	    File sdPath = Environment.getExternalStorageDirectory();
	    File parentPath = sdPath.getParentFile();
	    File[] paths = parentPath.listFiles();
	    for (int i = 0; i < paths.length; i++)
	    {
	    	if(paths[i].canWrite()){
	        	path = paths[i].getPath();
	        	if (SSDS.init(path) == SmartSDDev.OK){
	        		pass_error.setText("加载SD卡成功");
	        		break;
	        	}else{
	        		continue;
	        	}
	        }
	    	if( i == paths.length - 1 ){
	    		pass_error.setText("加载SD卡失败，请插拔SD卡后重新运行程序");
	    	}
	    }
        
        char rusername[] = new char[4096];
        char rusernamelen[] = new char[2];
        long r = SSDS.GetUserName(rusername, rusernamelen);
        if(r != 0)
        {
        	user_name.setText("获取用户名失败");
        }
        else{
        	int nlen = rusernamelen[0] + rusernamelen[1] * 256;
        	byte rr[] = new byte[4096];
        	for(int i = 0; i < nlen; i++){
        		rr[i] = (byte) rusername[i];
        	}
        	byte temp[] = new byte[4096];
        	System.arraycopy(rr, 0, temp, 0, nlen);
        	try {
				username = new String(temp, "gb2312");
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
        	user_name.setText(username);
        }
        
        button_login2 = (Button) findViewById(R.id.button_login2);
        button_login2.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				EditText password_edit = (EditText) findViewById(R.id.password_edit2);
				String pwd = password_edit.getText().toString();
				if ((button_login2.getText()).equals("断开"))
				{
					if(canDisconnect(currentProfile))
					{
						disconnect();
						password_edit.setText("");
					}
					button_login2.setText("登录");
				}else{
					long r1 = SSDS.VerifyPassWord(pwd);
					if(r1 != SmartSDDev.OK)
					{
						pass_error.setText("密码错误，请重新输入密码");
						password_edit.setText("");
					}
					else{
						//这两句太重要了了
						myApp.setLoginFlag("T");
						myApp.setPassword(pwd);
						pass_error.setText("");
						if(currentProfile != null && canConnect()){
							connect(currentProfile);
							button_login2.setText("断开");
						}else{
							pass_error.setText("服务器地址为空，请添加配置文件");
						}
					}
				}
				
			}
		});
        
        findViewById(R.id.tab1).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent("com.novoda.TAB");
				intent.putExtra("tab", 0);
				sendBroadcast(intent);
			}
		});    
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
	
	private synchronized void connect(final OpenvpnProfile p) {
		connect(p, null, null);
	}
	
	private boolean canConnect() {
		if (mStatus == null)
			return false;
		switch (mStatus.state) {
		case IDLE:
			return true;
		case PREPARING:
		case CONNECTING:
		case DISCONNECTING:
		case CANCELLED:
		case CONNECTED:
		case UNUSABLE:
		case UNKNOWN:
			return false;
		default:
			return false;
		}
	}
	
	private boolean canDisconnect(OpenvpnProfile p) {
		if (mStatus == null)
			return false;
		switch (mStatus.state) {
		case CONNECTING:
		case CONNECTED:
			return mStatus.name.equals(p.getServerName());
		case PREPARING:
		case IDLE:
		case DISCONNECTING:
		case CANCELLED:
		case UNUSABLE:
		case UNKNOWN:
			return false;
		default:
			return false;
		}
	}
	
	private synchronized void disconnect() {
		if (mIVpnService != null)
			try {
				mIVpnService.disconnect();
			} catch (RemoteException e) {
				Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_LONG);
			}
		else
			Toast.makeText(this, "Havn't bound to vpn service",
					Toast.LENGTH_LONG);
	}
	
	private synchronized void connect(final OpenvpnProfile p, String username,
			String password) {
		Intent intent = VpnService.prepare(this);
		mConnectingProfile = p;
		mConnectingUsername = username;
		mConnectingPassword = password;

		if (intent != null) {
			startActivityForResult(intent, REQUEST_CONNECT);
		} else {
			onActivityResult(REQUEST_CONNECT, RESULT_OK, null);
		}
	}
	
	@Override
	protected void onActivityResult(final int requestCode,
			final int resultCode, final Intent data) {
		if (requestCode == REQUEST_CONNECT) {
			if (mConnectingProfile == null) {
				Log.w(TAG, "profile is null");
				return;
			}
			if (resultCode == RESULT_OK) {
				if (mIVpnService != null)
					try {
						mIVpnService.connect(mConnectingProfile,
								mConnectingUsername, mConnectingPassword);
					} catch (RemoteException e) {
						Toast.makeText(this, e.getLocalizedMessage(),
								Toast.LENGTH_LONG);
					}
				else
					Toast.makeText(this, "Havn't bound to vpn service",
							Toast.LENGTH_LONG);
			}
			mConnectingProfile = null;
		}else {
			throw new RuntimeException("unknown request code: " + requestCode);
		}
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		installer = new OpenvpnInstaller();
		installer.install(this, new OpenvpnInstaller.Callback() {
			@Override
			public void done(Result result) {
				// TODO Auto-generated method stub
				if (result.isInstalled()) {
					App app=(App)getApplicationContext();
					app.initCertids(result.getCertids());
				} else {
					ErrorMsgDialog dialog = new ErrorMsgDialog();
					dialog.setMessage(result.getText());
					showDialog(dialog);
				}
			}
		});
		list = new ArrayList<String>();
		retrieveAddress();
		//retrieveVpnListFromStorage();
		adapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,list);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setOnItemSelectedListener(new SpinnerSelectedListener());
		spinner.setVisibility(View.VISIBLE);
		spinner.setAdapter(adapter);
	}
	
	@Override
	protected void onStop() {
		installer.cancel();
		installer = null;
		super.onStop();
	}
	
	@Override
	protected void onDestroy() {
		this.getApplicationContext().unbindService(mConnection);
		unregisterReceiver(mReceiver);
		super.onDestroy();
	}
	
	private void showDialog(DialogFragment dialog) {
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		ft.addToBackStack(null);
		dialog.show(ft, null);
	}
	
	private void retrieveVpnListFromStorage() {
		mVpnProfileList = Collections
				.synchronizedList(new ArrayList<OpenvpnProfile>());
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
			list.add(p.getName());
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
	
	private boolean checkIdConsistency(String dirName, OpenvpnProfile p) {
		if (!dirName.equals(p.getId())) {
			Log.d(TAG, "ID inconsistent: " + dirName + " vs " + p.getId());
			return false;
		} else {
			return true;
		}
	}
	
	class SpinnerSelectedListener implements OnItemSelectedListener{
		
		public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			//currentProfile = mVpnProfileList.get(arg2);
			currentProfile.setServerName(mAddrFileList.get(arg2).getIp());
			currentProfile.setPort(mAddrFileList.get(arg2).getPort());
			TextView v1 = (TextView)arg1;
			v1.setTextColor(Color.parseColor("#AAAAAA"));
		}
		
		public void onNothingSelected(AdapterView<?> arg0) {
			
		}
	}
}
