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



import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.app.Activity;
import android.content.DialogInterface;



public class ModifyPasswordFile1 extends Activity {

	private static final String TAG = ModifyPasswordFile1.class.getSimpleName();
	private PasswordFile passfile;
	private File PASSWORD_ROOT;
	private static final String PASSWORD_FILE = ".proj3";
	public ModifyPasswordFile1(){
		
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		PASSWORD_ROOT = new File(getFilesDir(), "V3");
		setContentView(R.layout.modifypwd_file);
		final TextView m_error=(TextView) findViewById(R.id.m_error1);
		
		Button submit_button = (Button) findViewById(R.id.m_submit_button1);
		submit_button.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				
				EditText text_oldpwd= (EditText) findViewById(R.id.m_oldpwd_edit1);
				EditText text_newpwd=(EditText) findViewById(R.id.m_newpwd_edit1);
				EditText text_r_newpwd=(EditText) findViewById(R.id.m_r_newpwd_edit1);
				String oldpwd = text_oldpwd.getText().toString();
				String newpwd = text_newpwd.getText().toString();
				String r_newpwd = text_r_newpwd.getText().toString();
				
				if(oldpwd.equals("")||oldpwd==null){
					m_error.setText("密码不能为空");
					return;
				}
				if(newpwd.equals("")||newpwd==null){
					m_error.setText("密码不能为空");
					return;
				}
				if(r_newpwd.equals("")||r_newpwd==null){
					m_error.setText("密码不能为空");
					return;
				}
			
				
				
				
				if(!newpwd.equals(r_newpwd)){
					
					m_error.setText("两次输入新密码不一致");
					text_r_newpwd.setText("");
					text_newpwd.setText("");
					return;
				}
				else{
					retrievePassword();
					if(passfile == null){
						if(oldpwd.equals("111111")){
							passfile = new PasswordFile();
							passfile.setPass(newpwd);
							try {
								addPassword(passfile);	
							} catch (IOException e) {
							
							}
							m_error.setText("修改密码成功");
							text_oldpwd.setText("");
							text_r_newpwd.setText("");
							text_newpwd.setText("");
							return;
						}else{
							m_error.setText("修改密码失败");
							text_oldpwd.setText("");
							text_r_newpwd.setText("");
							text_newpwd.setText("");
							return;
						}
						
					}else{
						if(oldpwd.equals(passfile.getPass())){
							passfile.setPass(newpwd);
							try {
								addPassword(passfile);	
							} catch (IOException e) {
							
							}
							m_error.setText("修改密码成功");
							text_oldpwd.setText("");
							text_r_newpwd.setText("");
							text_newpwd.setText("");
							return;
						}else{
							m_error.setText("修改密码失败");
							text_oldpwd.setText("");
							text_r_newpwd.setText("");
							text_newpwd.setText("");
							return;
						}
					}
				}
			}

			

		});
		
		/*
		 * 重置button
		 * */	
		Button reset_button = (Button) findViewById(R.id.m_reset_button1);
		reset_button.setOnClickListener(new Button.OnClickListener(){
			public void onClick(View v) {
				EditText text_oldpwd= (EditText) findViewById(R.id.m_oldpwd_edit1);
				EditText text_newpwd=(EditText) findViewById(R.id.m_newpwd_edit1);
				EditText text_r_newpwd=(EditText) findViewById(R.id.m_r_newpwd_edit1);
				text_oldpwd.setText("");
				text_r_newpwd.setText("");
				text_newpwd.setText("");
				
			}
			
			
		});
	}

	private void addPassword(PasswordFile ps) throws IOException {
		
		savePasswordToStorage(ps);

		
	}
	
	private File getPasswordDir(PasswordFile ps) {
		return new File(PASSWORD_ROOT, "222");
	}
	
	private void savePasswordToStorage(PasswordFile ps) throws IOException {
		File f = getPasswordDir(ps);
		if (!f.exists())
			f.mkdirs();
		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(
				new File(f, PASSWORD_FILE)));
		oos.writeObject(ps);
		oos.close();
		
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

}
