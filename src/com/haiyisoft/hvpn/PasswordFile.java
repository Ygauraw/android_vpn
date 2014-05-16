package com.haiyisoft.hvpn;

import java.io.Serializable;

import android.os.Parcel;
import android.os.Parcelable;

public class PasswordFile implements Parcelable, Serializable {
private static final long serialVersionUID = 1L;


private String Username; 
private String Password;
private String mExtra;


public void setName(String username) {
Username = username;
}

public String getName() {
return Username;
}

public void setPass(String password) {
Password = password;
}

public String getPass() {
return Password;
}



public static final Parcelable.Creator<PasswordFile> CREATOR = new Parcelable.Creator<PasswordFile>() {
public PasswordFile createFromParcel(Parcel in) {
PasswordFile p = new PasswordFile();
p.readFromParcel(in);
return p;
}

public PasswordFile[] newArray(int size) {
return new PasswordFile[size];
}
};

public int describeContents() {
return 0;
}


public void setExtra(String extra) {
mExtra = extra;
}

public String getExtra() {
return mExtra;
}

protected void readFromParcel(Parcel in) {
Username = in.readString();
Password = in.readString();

}

@Override
public void writeToParcel(Parcel parcel, int flags) {
parcel.writeString(Username);
parcel.writeString(Password);
}
}