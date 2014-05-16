package com.haiyisoft.hvpn;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

class OpenvpnInstaller {
	public interface Callback {
		public void done(Result result);
	}

	public class Result {
		private boolean installed;
		private String text;
		private String certids;
		public Result(boolean installed, String text,String ids) {
			setInstalled(installed);
			setText(text);
			if(ids!=""||ids!=null){
			setCertids(ids);}
		}

		public String getCertids() {
			return certids;
		}

		public void setCertids(String certids) {
			this.certids = certids;
		}

		public String getText() {
			return text;
		}

		public void setText(String text) {
			this.text = text;
		}

		public boolean isInstalled() {
			return installed;
		}

		public void setInstalled(boolean installed) {
			this.installed = installed;
		}
	}

	private class InstallTask extends AsyncTask<Object, String, Result> {
		private final Context c;
		private final File path;
		private final File path1;
		private final File path2;
		private final File path3;
		private final Callback cb;
		private ProgressDialog dialog;

		public InstallTask(Context context, Callback callback) {
			c = context;
			cb = callback;
			path = new File(c.getCacheDir(), "hvpn");
			path1= new File(c.getCacheDir(), "libhpkcs11.so");
			path2= new File(c.getCacheDir(), "ca.crt");
			path3= new File(c.getCacheDir(), "libsmartsddev.so");
		}

		private Result tryInstall() {
			publishProgress(c.getString(R.string.openvpn_installer_installing));
			try {
				InputStream in = c.getAssets().open("hvpn");
				InputStream in1=c.getAssets().open("libhpkcs11.so");
				InputStream in2=c.getAssets().open("ca.crt");
				InputStream in3=c.getAssets().open("libsmartsddev.so");
				
				FileOutputStream out = new FileOutputStream(path);
				FileOutputStream out1 = new FileOutputStream(path1);
				FileOutputStream out2 = new FileOutputStream(path2);
				FileOutputStream out3 = new FileOutputStream(path3);
				
				IOUtils.copy(in, out);
				IOUtils.copy(in2, out2);
				IOUtils.copy(in1, out1);
				IOUtils.copy(in3, out3);
				
				in.close();
				in1.close();
				in2.close();
				in3.close();
				
				out.close();
				out1.close();
				out2.close();
				out3.close();

				if (!path.setExecutable(true, true)) {
					throw new IOException("无法设置可执行标志");
				}

				return check(false);
			} catch (IOException e) {
				return new Result(false, e.getLocalizedMessage(),null);
			}
		}

		private Result check(boolean tryInstall) {
			
			String output1="";
			
			try {
				byte[] embeded = DigestUtils.md5(c.getAssets().open("hvpn"));
				byte[] embeded1 = DigestUtils.md5(c.getAssets().open("libhpkcs11.so"));
				byte[] embeded2 = DigestUtils.md5(c.getAssets().open("ca.crt"));
				byte[] embeded3 = DigestUtils.md5(c.getAssets().open("libsmartsddev.so"));
				
				byte[] installed;
				byte[] installed1;
				byte[] installed2;
				byte[] installed3;
				
				try {
					installed = DigestUtils.md5(new FileInputStream(path));
					installed1 = DigestUtils.md5(new FileInputStream(path1));
					installed2 = DigestUtils.md5(new FileInputStream(path2));
					installed3 = DigestUtils.md5(new FileInputStream(path3));
				} catch (FileNotFoundException e) {
					if (tryInstall)
						return tryInstall();
					else
						throw e;
				}
				if (!Arrays.equals(embeded, installed)||!Arrays.equals(embeded1, installed1)||!Arrays.equals(embeded2, installed2)||!Arrays.equals(embeded3, installed3)) {
					if (tryInstall)
						return tryInstall();
					else
						throw new RuntimeException(
								"安装程序不完整，请重新安装");
				}
				/**************************************
				 * chien add 
				 * add ca.crt
				 * add libhpkcs11.so
				 * */
				
				/**************************************/
				if (!path.setExecutable(true, true)) {
					throw new IOException("无法设置可执行标志");
				}

				Process process;
				
				try {
					ArrayList<String> config = new ArrayList<String>();
					config.add(new File(c.getCacheDir(), "hvpn").getAbsolutePath());
					config.add("--show-pkcs11-ids");
					config.add(path1.getAbsolutePath());
					process = Runtime.getRuntime().exec(config.toArray(new String[0]));
					

				} catch (IOException e) {
					if (tryInstall
							&& e.getCause().getMessage()
									.equals("No such file or directory")) {
						
						return tryInstall();
					} else {
						throw e;
					}
				}
				StringWriter writer1 = new StringWriter();
				IOUtils.copy(process.getInputStream(), writer1, "UTF-8");
				output1 = writer1.toString();
				
				try {
					process = new ProcessBuilder()
					.command(path.getAbsolutePath(), "--version")
					.redirectErrorStream(true).start();

				} catch (IOException e) {
					if (tryInstall
							&& e.getCause().getMessage()
									.equals("No such file or directory")) {
						
						return tryInstall();
					} else {
						throw e;
					}
				}


				StringWriter writer = new StringWriter();
				IOUtils.copy(process.getInputStream(), writer, "UTF-8");
				String output = writer.toString();
				
				/*chien set ids*/
				
				process.waitFor();
				return new Result(process.exitValue() == 1, output,output1);
			} catch (InterruptedException e) {
				return new Result(false, e.getLocalizedMessage(),output1);
			} catch (IOException e) {
				return new Result(false, e.getLocalizedMessage(),output1);
			}
		}

		@Override
		protected Result doInBackground(Object... params) {
			return check(true);
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			dialog = ProgressDialog.show(c,
					c.getString(R.string.openvpn_installer_title),
					c.getString(R.string.openvpn_installer_checking), true);
		}

		@Override
		protected void onPostExecute(Result result) {
			super.onPostExecute(result);
			if (dialog.isShowing())
				dialog.dismiss();
			cb.done(result);
		}

		@Override
		protected void onProgressUpdate(String... values) {
			dialog.setMessage(values[0]);
			super.onProgressUpdate(values);
		}

		@Override
		protected void onCancelled() {
			if (dialog.isShowing())
				dialog.dismiss();
			super.onCancelled();
		}
	}

	private InstallTask task;

	public void install(Context context, Callback callback) {
		task = new InstallTask(context, callback);
		task.execute(null, null);
	}

	public void cancel() {
		task.cancel(true);
		task = null;
	}
	

}
