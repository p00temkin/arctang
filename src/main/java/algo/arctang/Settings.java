package algo.arctang;

import java.io.File;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import algo.arctang.enums.Action;
import crypto.forestfish.enums.avm.AVMChain;
import crypto.forestfish.objects.avm.AlgoRelayNode;
import crypto.forestfish.objects.avm.model.chain.AVMChainInfo;
import crypto.forestfish.utils.AVMUtils;
import crypto.forestfish.utils.FilesUtils;
import crypto.forestfish.utils.JSONUtils;
import crypto.forestfish.utils.SystemUtils;

public class Settings {

	private static final Logger LOGGER = LoggerFactory.getLogger(Settings.class);

	// Algorand connectivity
	private AVMChain chain;
	private String url;
	private Integer port;
	private String authtoken;
	private String authtoken_key;
	private AVMChainInfo chainInfo;

	// Action  
	private Action action;
	private Long assetid;
	
	private boolean safemode = true;
	private boolean raw = false;
	private boolean parsed = false;
	private boolean arctype = false;
	
	private boolean debug = false;
	
	public Settings() {
		super();
	}

	public AVMChain getChain() {
		return chain;
	}

	public void setChain(AVMChain chain) {
		this.chain = chain;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getAuthtoken() {
		return authtoken;
	}

	public void setAuthtoken(String authtoken) {
		this.authtoken = authtoken;
	}

	public Integer getPort() {
		return port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}

	public String getAuthtoken_key() {
		return authtoken_key;
	}

	public void setAuthtoken_key(String authtoken_key) {
		this.authtoken_key = authtoken_key;
	}

	public Action getAction() {
		return action;
	}

	public void setAction(Action action) {
		this.action = action;
	}

	public boolean isSafemode() {
		return safemode;
	}

	public void setSafemode(boolean safemode) {
		this.safemode = safemode;
	}

	public AVMChainInfo getChainInfo() {
		return chainInfo;
	}

	public void setChainInfo(AVMChainInfo chainInfo) {
		this.chainInfo = chainInfo;
	}

	public Long getAssetid() {
		return assetid;
	}

	public void setAssetid(Long assetid) {
		this.assetid = assetid;
	}

	public boolean isRaw() {
		return raw;
	}

	public void setRaw(boolean raw) {
		this.raw = raw;
	}

	public boolean isParsed() {
		return parsed;
	}

	public void setParsed(boolean parsed) {
		this.parsed = parsed;
	}

	public boolean isArctype() {
		return arctype;
	}

	public void setArctype(boolean arctype) {
		this.arctype = arctype;
	}

	public boolean isDebug() {
		return debug;
	}

	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	public void sanityCheck() {

		// chain
		if (null == this.getChain()) {
			LOGGER.error("You need to specify a chain, MAINNET, BETANET or TESTNET");
			SystemUtils.halt();
		}

		// Check for local network configuration unless provided as cli params
		if (true && 
				(null != this.getChain()) &&
				(null != this.getUrl()) &&
				(null != this.getPort()) &&
				(null != this.getAuthtoken()) &&
				(null != this.getAuthtoken_key()) &&
				true) {
			
			// Create chaininfo instance
			AVMChainInfo chainInfo = AVMUtils.getAVMChainInfo(this.getChain());
			ArrayList<AlgoRelayNode> nodes = new ArrayList<AlgoRelayNode>();
			nodes.add(new AlgoRelayNode(this.getUrl(), this.getPort(), this.getAuthtoken(), this.getAuthtoken_key()));
			chainInfo.setNodes(nodes);
			this.chainInfo = chainInfo;
			
			// Save to local disk if present
			if (this.getAction() == Action.NETCONFIG) {
				String json = JSONUtils.createJSONFromAVMChainInfo(chainInfo);
				FilesUtils.createFolderUnlessExists(".avm");
				FilesUtils.createFolderUnlessExists(".avm/networks");
				File f = new File(".avm/networks/" + chain.toString());
				if (!f.exists()) {
					LOGGER.info("Flushing chainInfo to .avm/networks/" + chain.toString());
					FilesUtils.writeToFileUNIXNoException(json, ".avm/networks/" + chain.toString());
				}
			}
			
		} else {
			// Check if we can get the chain details from 
			LOGGER.debug("Checking for chainInfo details at .avm/networks/" + chain.toString());
			File f = new File(".avm/networks/" + chain.toString());
			if (f.exists()) {
				String json = FilesUtils.readStringFromFile(".avm/networks/" + chain.toString());
				AVMChainInfo chainInfo = JSONUtils.createAVMChainInfo(json);
				this.chainInfo = chainInfo;
			} else {
				LOGGER.error("Need chainInfo for " + this.getChain() + ", please consider using --confignetwork since no public nodes are available");
				SystemUtils.halt();
			}
		}
		
		if ((this.getAction() == Action.QUERY) && (null == this.getAssetid())) {
			LOGGER.error("Need to provide --assetid when using QUERY action");
			SystemUtils.halt();
		}

		if (chainInfo.getNodes().isEmpty()) {
			LOGGER.error("You need to define a node for " + this.getChain() + " using --confignetwork since no public nodes are available");
			SystemUtils.halt();
		}

		return;
	}

	public void print() {
		System.out.println("Action Settings:");
		System.out.println(" - chain: " + this.getChain());
		System.out.println(" - action: " + this.getAction());
		System.out.println("Network Settings:");
		System.out.println(" - nodeurl: " + this.getChainInfo().getNodes().get(0).getUrl());
		System.out.println(" - nodeport: " + this.getChainInfo().getNodes().get(0).getPort());
		System.out.println(" - authtoken: " + this.getChainInfo().getNodes().get(0).getAuthtoken());
		System.out.println(" - authtoken_key: " + this.getChainInfo().getNodes().get(0).getAuthtoken_key());
	}

}
