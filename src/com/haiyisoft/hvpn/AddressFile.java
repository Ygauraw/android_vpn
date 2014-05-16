package com.haiyisoft.hvpn;

import java.io.Serializable;

import android.os.Parcel;
import android.os.Parcelable;

public class AddressFile implements Parcelable, Serializable {
private static final long serialVersionUID = 1L;


private String Ip; 
private String Port;
private String mExtra;
private String mId;


public void setIp(String ip) {
Ip = ip;
}

public String getIp() {
return Ip;
}

public void setPort(String port) {
Port = port;
}

public String getPort() {
return Port;
}

public void setId(String id) {
mId = id;
}

public String getId() {
return mId;
}

public static final Parcelable.Creator<AddressFile> CREATOR = new Parcelable.Creator<AddressFile>() {
public AddressFile createFromParcel(Parcel in) {
AddressFile p = new AddressFile();
p.readFromParcel(in);
return p;
}

public AddressFile[] newArray(int size) {
return new AddressFile[size];
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
Ip = in.readString();
Port = in.readString();
}

@Override
public void writeToParcel(Parcel parcel, int flags) {
parcel.writeString(Ip);
parcel.writeString(Port);
}
}