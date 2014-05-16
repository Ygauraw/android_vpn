/*
 * add by chien 2012.12.5
 * 设置全局变量用
 * **********************/

package com.haiyisoft.hvpn;
import com.aisino.smartsd.SmartSDSample;
import android.app.Application;


	public class App extends Application{
		private String loginFlag="F";
		private String password="";
		private String certids="";
		private SmartSDSample smartSDSample;
		public SmartSDSample getSmartSDSample() {
			return smartSDSample;
		}
		public App(){
			this.smartSDSample=new SmartSDSample();
			
		}
		public void initCertids(String ids) {
			
			this.certids=ids;
		
		}
		public String getCertids() {
			return certids;
		}
		public String getLoginFlag() {
			return loginFlag;
		}
		public String getPassword() {
			return password;
		}
		public void setPassword(String password) {
			this.password = password;
		}
		public void setLoginFlag(String loginFlag) {
			this.loginFlag = loginFlag;
		}
		@Override
		public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		
		}
	

		}

