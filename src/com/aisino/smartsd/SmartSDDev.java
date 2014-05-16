package com.aisino.smartsd;

public class SmartSDDev {

	static {
		System.loadLibrary("smartsddev_java");
	}

	public static final int OK = 0;
	public static final int ERR_PARA = 1;
	public static final int ERR_WR_CMD_DAT = 2;
	public static final int ERR_RD_CMD_DAT = 3;
	public static final int ERR_GET_HANDLE = 4;
	public static final int ERR_CLOSE_HANDLE = 5;
	public static final int ERR_TIMEOUT = 6;
	public static final int ERR_WR_DATA = 7;
	public static final int ERR_RD_DATA = 8;
	public static final int ERR_FAIL = 9;

	public static final int STATE_ERROR = 0;
	public static final int STATE_BOOT = 1;
	public static final int STATE_APP = 2;

	public native int EnSmartCardTrans();

	public native int DisSmartCardTrans();

	public native int IsSmartSD(String Driver);

	public native int MicroSD_RunApp();

	public native int MicroSD_GetCardInfo(char InfoBuf[]);

	public native int MicroSD_SendAPDUCommand(char CaseType, int APDU_len,
			char APDU_buffer[]);

	public native int MicroSD_GetAPDUCommand(int APDU_len[], char APDU_buffer[]);

	public native int MicroSD_SendData(int DATA_Len, char pDatBuf[]);

	public native int MicroSD_GetData(int DATA_Len, char pDatBuf[]);

	public native int Controller_Rst();

	public native int SendBootCmd(char PCmdBuf[], int CmdLen, char pResp[],
			char RespLen);
	
	public native String GetDllVer();
}
