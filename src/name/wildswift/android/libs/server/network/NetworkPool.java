package name.wildswift.android.libs.server.network;

import java.util.Vector;

/**
 * Pool returns instance of interface {@link  Network}<br/>
 * For returned instance developer should call {@link  NetworkImpl#getInputForRequest(String, java.util.Map, int, java.util.Date, String, String)} or {@link  NetworkImpl#openConnection(String, java.util.Map, int, java.util.Date, String, String)} with {@link NetworkImpl#close()} in one synchronized block.<br/>
 * Sample: <br/>
 * <code>
 * 		Network network = NetworkImpl.getInstance();
 * 		synchronized (network) {
 * 			InputStream serverInput = network.getInputForRequest(request.getUrl(), request.getParameters(), request.getMethod());
 * 			// some work with response
 * 			network.close();
 * 		}
 * </code>
 * @author Wild Swift
 */
public class NetworkPool {
	private int lastReturned;
	private static NetworkPool instance;
	private Vector<NetworkImpl> networks;
	protected int count;

	public static NetworkPool getInstance() {
		if (instance == null) instance = new NetworkPool();
		return instance;
	}

	private NetworkPool() {
		count = Integer.parseInt(System.getProperty("com.wildswift.networkpull.count", "4"));
		networks = new Vector<NetworkImpl>();
		networks.setSize(count);
		for (int i = 0; i < networks.size(); i++) {
			networks.setElementAt(new NetworkImpl(), i);
		}
	}

	public Network getNetwork() {
		for (int i = 0; i < networks.size(); i++) {
			NetworkImpl network = networks.elementAt(i);
			if (network.isFree()) {
				lastReturned = i;
				return network;
			}
		}
		// if all is busy return next after previous 
		lastReturned = (lastReturned+1) % count;
		return networks.elementAt(lastReturned);
	}
}
