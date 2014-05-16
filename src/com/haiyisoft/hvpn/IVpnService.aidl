package com.haiyisoft.hvpn;
import com.haiyisoft.hvpn.LogQueue;
import com.haiyisoft.hvpn.OpenvpnProfile;
import com.haiyisoft.hvpn.VpnStatus;

/**
 * Interface to access a VPN service.
 * {@hide}
 */
interface IVpnService {
    /**
     * Sets up the VPN connection.
     * @param profile the profile object
     */
    boolean connect(in OpenvpnProfile profile, in String username, in String password);

    /**
     * Tears down the VPN connection.
     */
    void disconnect();

    /**
     * Makes the service broadcast the connectivity state.
     */
    VpnStatus checkStatus();

    /**
     * Get current/last connection log
     */
    LogQueue getLog();
}