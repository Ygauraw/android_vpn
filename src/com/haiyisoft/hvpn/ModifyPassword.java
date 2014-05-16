package com.haiyisoft.hvpn;


import com.aisino.smartsd.SmartSDDev;
import com.aisino.smartsd.SmartSDSample;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.app.Activity;



public class ModifyPassword extends Activity {

	private	App myApp=null;
	private SmartSDSample SSDS;
	public ModifyPassword(){
		
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		setContentView(R.layout.modifypwd);
		final TextView m_error=(TextView) findViewById(R.id.m_error);
	
//		
//		if (uRet != SmartSDDev.OK) {
//			m_error.setText("加载SD卡失败");
//		} 
//		else{
//			m_error.setText("加载SD卡成功");
//		}
		
/*
 * 确定button
 * */
		Button submit_button = (Button) findViewById(R.id.m_submit_button);
		submit_button.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				sdinit();
				EditText text_oldpwd= (EditText) findViewById(R.id.m_oldpwd_edit);
				EditText text_newpwd=(EditText) findViewById(R.id.m_newpwd_edit);
				EditText text_r_newpwd=(EditText) findViewById(R.id.m_r_newpwd_edit);
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
					
					long r =SSDS.ModifyPassWord(oldpwd,newpwd);
					if (r != SmartSDDev.OK) {
						m_error.setText("修改密码失败");
						text_oldpwd.setText("");
						text_r_newpwd.setText("");
						text_newpwd.setText("");
					
						return;
					} 
					else{
					
						myApp.setPassword(newpwd);
						m_error.setText("修改密码成功");
						text_oldpwd.setText("");
						text_r_newpwd.setText("");
						text_newpwd.setText("");
					
						return;
					
					}
				}
			}

			private void sdinit() {
				// TODO Auto-generated method stub
				
					myApp=(App)getApplicationContext();
					SSDS =myApp.getSmartSDSample();
					SSDS.close();
					String path="/mnt/extSdCard";
					int uRet = SSDS.init(path);
			}

		});
		
		/*
		 * 重置button
		 * */	
		Button reset_button = (Button) findViewById(R.id.m_reset_button);
		reset_button.setOnClickListener(new Button.OnClickListener(){
			public void onClick(View v) {
				EditText text_oldpwd= (EditText) findViewById(R.id.m_oldpwd_edit);
				EditText text_newpwd=(EditText) findViewById(R.id.m_newpwd_edit);
				EditText text_r_newpwd=(EditText) findViewById(R.id.m_r_newpwd_edit);
				text_oldpwd.setText("");
				text_r_newpwd.setText("");
				text_newpwd.setText("");
				
			}
			
			
		});
	}



}
