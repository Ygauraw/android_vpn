package com.haiyisoft.hvpn;

import com.haiyisoft.hvpn.R;
import com.haiyisoft.hvpn.OpenvpnInstallerFile.Result;




//import com.haiyisoft.hvpn.Tab1Activity.SpinnerSelectedListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ActionBar.Tab;
import android.app.AlertDialog.Builder;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.net.Uri;
import android.net.VpnService;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.security.KeyChain;
import android.security.KeyChainAliasCallback;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView.FindListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
//import android.security.Credentials;
//import android.security.IKeyChainAliasCallback;
import android.security.KeyChain;




//import android.security.KeyStore;
//import java.security.KeyStore;
import java.security.cert.Certificate;

import android.security.KeyChainAliasCallback;

import java.security.KeyStore;


public class Tab1Activity extends Activity{
	
	
	public void parseResponse(Intent data, Context c) {
        if (VERSION.SDK_INT < VERSION_CODES.KITKAT) {
            String fileData = data.getStringExtra(FileSelect.RESULT_DATA);
            //setData(fileData, c);
        } else if (data != null) {
           


        }
    }
	private File PROFILES_ROOT;
	private File PASSWORD_ROOT;
	private File ADDR_ROOT;
	private PasswordFile passfile = null;
	
	
	private static final String TAG = Tab1Activity.class.getSimpleName();
	private static final String PROFILE_OBJ_FILE = ".pobj1";
	private static final String PASSWORD_FILE = ".proj3";
	private static final String ADDR_FILE = ".proj4";
	
	
	public interface FileSelectCallback {

        String getString(int res);

        void startActivityForResult(Intent startFC, int mTaskId);
    }
	private int mTaskId;
	private FileSelectCallback mFragment;
	
	public void setCaller(FileSelectCallback fragment, int i) {
        mTaskId = i;
        mFragment = fragment;
        //fileType = ft;
    }
	private static final int REQUEST_CONNECT = 2;
	
	private TextView error_view ;
	private Spinner spinner;
	
	private String test;
	
	private Button button_login1;
	private EditText user_name1;
	
	private ArrayAdapter<String> adapter;
	private ArrayAdapter<String> adapter1;
	
	private String pwd = "";
	private ArrayList<String> list = new ArrayList<String>();
	private ArrayList<String> name = new ArrayList<String>();
	
	private List<OpenvpnProfileFile> mVpnProfileList;
	private List<AddressFile> mAddrFileList;
	
	private OpenvpnProfileFile mConnectingProfile;
	private String mConnectingUsername;
	private String mConnectingPassword;
	private static final int FILE_PICKER_RESULT = 392;
	private static final int SELECT_PROFILE = 43;
	private OpenvpnProfileFile currentProfile = new OpenvpnProfileFile();
	
	private VpnStatus mStatus;

	private OpenvpnInstallerFile installer;
	
	private IVpnServiceFile mIVpnService;
	private ServiceConnection mConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mIVpnService = IVpnServiceFile.Stub.asInterface(service);
			try {
				mStatus = mIVpnService.checkStatus();
				Log.e(TAG, mStatus.state.toString());
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
        setContentView(R.layout.tab1);
        
        /*****************************
         *一、 files
1. Context.getFilesDir()，该方法返回/data/data/youPackageName/files的File对象。
2. Context.openFileInput()与Context.openFileOutput()，只能读取和写入files下的文件，返回的是FileInputStream和FileOutputStream对象。
3. Context.fileList()，返回files下所有的文件名，返回的是String[]对象。
4. Context.deleteFile(String)，删除files下指定名称的文件。
         * 
         *************************************/
        
        
        /*******************
         * list files目录下的所有文件名，用于删除和显示选择
         ************/
        String[] files =this.fileList();
        for(String file:files){
        	System.out.println(file+"\n");
        }
        /**
         * 删除files目录下的证书
         */
        //this.deleteFile("user2.pfx");
        File sdPath = Environment.getExternalStorageDirectory();
        //String[] sdfiles = sdPath.list();
//        for(String a : sdfiles)
//        {
//        	System.out.println(a + "\n");
//        }
        
        //System.out.println(Environment.getExternalStorageDirectory().toString());
        
        
        PROFILES_ROOT = new File(getFilesDir(), "V2");
        PASSWORD_ROOT = new File(getFilesDir(), "V3");
        ADDR_ROOT = new File(getFilesDir(), "V4");
        findViewById(R.id.tab2).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent("com.novoda.TAB");
				intent.putExtra("tab", 1);
				sendBroadcast(intent);
			}
		});
        
//        KeyStore mKeyStore = KeyStore.getInstance("BKS");
//        
//        String[] aliasArray = mKeyStore.saw(Credentials.USER_PRIVATE_KEY);
//        List<String> aliasList = ((aliasArray == null)
//                                  ? Collections.<String>emptyList()
//                                  : Arrays.asList(aliasArray));
        
        user_name1 = (EditText) findViewById(R.id.username_edit1);
        user_name1.setCursorVisible(false);
        user_name1.setFocusable(false);
        user_name1.setFocusableInTouchMode(false);
        user_name1.setText("user1");
        
        
        error_view = (TextView) findViewById(R.id.error_login);
        
        retrieveAddress();
        retrievePassword();
        //retrieveVpnListFromStorage();
//        try {
//			readFile01();
//		} catch (IOException e) {
//			// TODO: handle exception
//			
//		}
        
        try {
			writeFile01();
		} catch (IOException e) {
			// TODO: handle exception
			
		}
		
		adapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,list);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		
		spinner = (Spinner) findViewById(R.id.Spinner01);
		spinner.setAdapter(adapter);
		spinner.setOnItemSelectedListener(new SpinnerSelectedListener());
		spinner.setVisibility(View.VISIBLE);
		IntentFilter filter = new IntentFilter();
		filter.addAction("com.haiyisoft.hvpn.connectivity");
		registerReceiver(mReceiver, filter);
		
		this.getApplicationContext().bindService(new Intent(this, OpenVpnServiceFile.class), mConnection,
				Context.BIND_AUTO_CREATE);
		
//		user_input = (EditText) findViewById(R.id.username_edit1);
//		user_input.setOnTouchListener(new EditText.OnTouchListener() {	
//			@Override
//			public boolean onTouch(View v, MotionEvent event) {
//				// TODO Auto-generated method stub
//				if (event.getAction()==MotionEvent.ACTION_DOWN) { 
//				    //执行操作
//					showCertDialog();
//				}
//				return false;
//			}
//		});
		
		Button select_button = (Button) findViewById(R.id.select_keystore_button);
		select_button.setOnClickListener(new Button.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				showCertDialog();
				
				
			}
		});
		
		Button button_import = (Button) findViewById(R.id.button_deletecert);
		button_import.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				//Intent startFC = new Intent(Tab1Activity.this, FileSelect.class);
		        //startFC.putExtra(FileSelect.START_DATA, mData);
		        //startFC.putExtra(FileSelect.WINDOW_TITLE, mTitle);
		        //if (fileType == Utils.FileType.PKCS12)
		          //  startFC.putExtra(FileSelect.DO_BASE64_ENCODE, true);
				//startActivity(startFC);
		        //mFragment.startActivityForResult(startFC, mTaskId);
				if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
	                startFilePicker();
	            else
				    startImportConfig();
			}
		});
		
		Button button_comuni = (Button) findViewById(R.id.button_comunicate);
		button_comuni.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(Tab1Activity.this, ComuniActivity.class);
				startActivity(intent);
			}
		});
		
		Button button_modify = (Button) findViewById(R.id.button_modifypass);
		button_modify.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(Tab1Activity.this, ModifyPasswordFile1.class);
				startActivity(intent);
			}
		});
		
		button_login1 = (Button) findViewById(R.id.button_login1);
		button_login1.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if ((button_login1.getText()).equals("断开"))
				{
					if(canDisconnect(currentProfile))
					{
						disconnect();
					}
					button_login1.setText("登录");
					return;
				}
				if(currentProfile != null && canConnect()){
					connect(currentProfile);
					error_view.setText("");
					button_login1.setText("断开");
				}else{
					error_view.setText("服务器地址为空，请添加配置文件");
				}
			}
		});
	}
	
	private void importP12(String fromPath, String toPath){
		/******************
         * 从cache目录的user1.p12读取，写入到新建的files目录下的user2.pfx(user2.pfx原本为空)
         * 
         ******************/
        try {
        	
            //String user2 = "user2.pfx";
        	//String user3 = "yuan.pfx";
        	try {
        		//String path = new File(getCacheDir(), "user1.p12").getAbsolutePath();
        		//String path = new File(Environment.getExternalStorageDirectory(),"user1.pfx").getAbsolutePath();
            	FileInputStream inputstream = new FileInputStream(fromPath);
            	
            	FileOutputStream outputstream = openFileOutput(toPath,MODE_PRIVATE);
            	byte[] bytes = new byte[1024];
            	//ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
//            	while(inputstream.read() != -1)
//            	{
//            		arrayOutputStream.write(bytes, 0, bytes.length);
//            		
//            	}
            	int len;
            	while((len = inputstream.read(bytes)) > 0 ){
            		outputstream.write(bytes, 0, len);
            	}
            	inputstream.close();
            	//arrayOutputStream.close();
            	//bb = arrayOutputStream.toByteArray();
            	
            	//outputstream.write(bb);
            	outputstream.flush();
            	outputstream.close();
			} catch (Exception e) {
				// TODO: handle exception
			}
        	
        	
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	
	@TargetApi(Build.VERSION_CODES.KITKAT)
    private void startFilePicker() {
       Intent i = Utils.getFilePickerIntent(Utils.FileType.PKCS12);
       startActivityForResult(i, FILE_PICKER_RESULT);
    }

    private void startImportConfig() {
		Intent intent = new Intent(Tab1Activity.this,FileSelect.class);
		intent.putExtra(FileSelect.NO_INLINE_SELECTION, true);
		//intent.putExtra(FileSelect.WINDOW_TITLE, R.string.import_configuration_file);
		startActivityForResult(intent, SELECT_PROFILE);
	}
	
	public static final byte[] input2byte(InputStream inStream)  
            throws IOException {  
        ByteArrayOutputStream swapStream = new ByteArrayOutputStream();  
        byte[] buff = new byte[100];  
        int rc = 0;  
        while ((rc = inStream.read(buff, 0, 100)) > 0) {  
            swapStream.write(buff, 0, rc);  
        }  
        byte[] in2b = swapStream.toByteArray();  
        return in2b;  
    }  
	
	public void readFile01() throws IOException {
        FileReader fr=new FileReader("/data/data/com.haiyisoft.hvpn/cache/123.txt");
        BufferedReader br=new BufferedReader(fr);
        String line="";
        String[] arrs=null;
        while ((line=br.readLine())!=null) {
            arrs=line.split(" ");
            System.out.println(arrs[0] + " : " + arrs[1]);
        }
        br.close();
        fr.close();
    }
	
	public void writeFile01() throws IOException {
        String[] arrs={
            "zhangsan,23,FuJian",
            "lisi,30,ShangHai",
            "wangwu,43,BeiJing",
            "laolin,21,ChongQing",
            "ximenqing,67,GuiZhou"
        };
        FileWriter fw=new FileWriter(new File("/data/data/com.haiyisoft.hvpn/cache/123.txt"),true);
        //写入中文字符时会出现乱码
        BufferedWriter  bw=new BufferedWriter(fw);
        //BufferedWriter  bw=new BufferedWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File("E:/phsftp/evdokey/evdokey_201103221556.txt")), "UTF-8")));

        for(String arr:arrs){
            bw.write(arr+"\t\n");
        }
        bw.close();
        fw.close();
    }
	
	public void showCertDialog () {
		try	{
			KeyChain.choosePrivateKeyAlias(Tab1Activity.this,
					new KeyChainAliasCallback() {

				public void alias(String alias) {
					// Credential alias selected.  Remember the alias selection for future use.
					//currentProfile.setUserCertName(alias);
					//mHandler.sendEmptyMessage(UPDATE_ALIAS);
//					EditText e = (EditText) findViewById(R.id.username_edit1);
//					e.setText(alias);
					test = alias;
					System.out.println(test);
					currentProfile.setUserCertName(test);
					runOnUiThread(new RunnableEx<String>(test) {
						@Override
						public void run(String test) {
							user_name1.setText(test);
						}
					});
				}


			},
			null, // List of acceptable key types. null for any
			null,                        // issuer, null for any
			null,      // host name of server requesting the cert, null if unavailable
			-1,                         // port of server requesting the cert, -1 if unavailable
			null);                       // alias to preselect, null if unavailable
		} catch (ActivityNotFoundException anf) {
			Builder ab = new AlertDialog.Builder(Tab1Activity.this);
			ab.setTitle(R.string.broken_image_cert_title);
			ab.setMessage(R.string.broken_image_cert);
			ab.setPositiveButton(android.R.string.ok, null);
			ab.show();
		}
	}
	
	private synchronized void connect(final OpenvpnProfileFile p) {
		connect(p, null, null);
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

	private synchronized void connect(final OpenvpnProfileFile p, String username,
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
		} else if(requestCode== SELECT_PROFILE) {
			if(resultCode == RESULT_OK){
				String fileData = data.getStringExtra(FileSelect.RESULT_DATA);
				importP12(fileData, "1");
				System.out.println(fileData);
			}
            //Uri uri = new Uri.Builder().path(fileData).scheme("file").build();

		}else {
			throw new RuntimeException("unknown request code: " + requestCode);
		}
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		installer = new OpenvpnInstallerFile();
		installer.install(this, new OpenvpnInstallerFile.Callback() {
			@Override
			public void done(Result result) {
				// TODO Auto-generated method stub
				if (result.isInstalled()) {
					
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
	
	private boolean canDisconnect(OpenvpnProfileFile p) {
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
	
	private void showDialog(DialogFragment dialog) {
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		ft.addToBackStack(null);
		dialog.show(ft, null);
	}
	
	private void retrievePassword(){
		

		File root = PASSWORD_ROOT;
		String[] dirs = root.list();
		if (dirs == null)
			return;
		for (String dir : dirs) {
			File f = new File(new File(root, dir), PASSWORD_FILE);
			if (!f.exists())
				continue;
			try {
				PasswordFile ps = deserialize1(f);
				if (ps == null)
					continue;
				if (!dir.equals("222"))
					continue;
				passfile = ps;
			} catch (IOException e) {
				Log.e(TAG, "retrievePassword()", e);
			}
		}
		
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
	
	private void retrieveVpnListFromStorage() {
		
		mVpnProfileList = Collections
				.synchronizedList(new ArrayList<OpenvpnProfileFile>());

		File root = PROFILES_ROOT;
		String[] dirs = root.list();
		if (dirs == null)
			return;
		for (String dir : dirs) {
			File f = new File(new File(root, dir), PROFILE_OBJ_FILE);
			if (!f.exists())
				continue;
			try {
				OpenvpnProfileFile p = deserialize(f);
				if (p == null)
					continue;
				if (!checkIdConsistency(dir, p))
					continue;

				mVpnProfileList.add(p);
			} catch (IOException e) {
				Log.e(TAG, "retrieveVpnListFromStorage()", e);
			}
		}
		Collections.sort(mVpnProfileList, new Comparator<OpenvpnProfileFile>() {
			@Override
			public int compare(OpenvpnProfileFile p1, OpenvpnProfileFile p2) {
				return p1.getName().compareTo(p2.getName());
			}

			@Override
			public boolean equals(Object p) {
				// not used
				return false;
			}
		});
		
		for (OpenvpnProfileFile p : mVpnProfileList) {
			list.add(p.getServerName());
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
	
	private PasswordFile deserialize1(File PasswordObjectFile)
			throws IOException {
		try {
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(
					PasswordObjectFile));
			PasswordFile ps = (PasswordFile) ois.readObject();
			ois.close();
			return ps;
		} catch (ClassNotFoundException e) {
			Log.d(TAG, "deserialize a profile", e);
			return null;
		}
	}
	
	private OpenvpnProfileFile deserialize(File profileObjectFile)
			throws IOException {
		try {
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(
					profileObjectFile));
			OpenvpnProfileFile p = (OpenvpnProfileFile) ois.readObject();
			ois.close();
			return p;
		} catch (ClassNotFoundException e) {
			Log.d(TAG, "deserialize a profile", e);
			return null;
		}
	}
	
	private boolean checkIdConsistency(String dirName, OpenvpnProfileFile p) {
		if (!dirName.equals(p.getId())) {
			Log.d(TAG, "ID inconsistent: " + dirName + " vs " + p.getId());
			return false;
		} else {
			return true;
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
	
	class SpinnerSelectedListener implements OnItemSelectedListener{
		
		public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			currentProfile.setServerName(mAddrFileList.get(arg2).getIp());
			currentProfile.setPort(mAddrFileList.get(arg2).getPort());
			TextView v1 = (TextView)arg1;
			v1.setTextColor(Color.parseColor("#AAAAAA"));
			//user_name1.setText(currentProfile.getUserCertName());
		}
		
		public void onNothingSelected(AdapterView<?> arg0) {
			
		}
	}
}
