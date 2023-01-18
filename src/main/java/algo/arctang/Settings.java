package algo.arctang;

import java.io.File;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import algo.arctang.enums.Action;
import crypto.forestfish.enums.avm.AVMChain;
import crypto.forestfish.objects.avm.AlgoIndexerNode;
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
	private AVMChainInfo chainInfo;
	
	private String nodeurl;
	private Integer nodeport;
	private String nodeauthtoken;
	private String nodeauthtoken_key;
	
	private String idxurl;
	private Integer idxport;
	private String idxauthtoken;
	private String idxauthtoken_key;

	// Action  
	private Action action;
	private Long assetid;
	
	// Options
	private boolean safemode = true;
	private boolean raw = false;
	private boolean parsed = false;
	private boolean arctype = false;
	private boolean metadata = false;
	
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

	public boolean isMetadata() {
		return metadata;
	}

	public void setMetadata(boolean metadata) {
		this.metadata = metadata;
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
				(null != this.getNodeurl()) &&
				(null != this.getNodeport()) &&
				(null != this.getNodeauthtoken()) &&
				(null != this.getNodeauthtoken_key()) &&
				true) {
			
			// Create chaininfo instance
			AVMChainInfo chainInfo = AVMUtils.getAVMChainInfo(this.getChain());
			
			// Update the relay nodes
			ArrayList<AlgoRelayNode> nodes = new ArrayList<AlgoRelayNode>();
			nodes.add(new AlgoRelayNode(this.getNodeurl(), this.getNodeport(), this.getNodeauthtoken(), this.getNodeauthtoken_key()));
			chainInfo.setNodes(nodes);
			
			if (true && 
					(null != this.getIdxurl()) &&
					(null != this.getIdxport()) &&
					(null != this.getIdxauthtoken()) &&
					(null != this.getIdxauthtoken_key()) &&
					true) {
				// Update the indexer nodes
				ArrayList<AlgoIndexerNode> idxnodes = new ArrayList<AlgoIndexerNode>();
				idxnodes.add(new AlgoIndexerNode(this.getIdxurl(), this.getIdxport(), this.getIdxauthtoken(), this.getIdxauthtoken_key()));
				chainInfo.setIdxnodes(idxnodes);
			}
			
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

	public String getNodeurl() {
		return nodeurl;
	}

	public void setNodeurl(String nodeurl) {
		this.nodeurl = nodeurl;
	}

	public Integer getNodeport() {
		return nodeport;
	}

	public void setNodeport(Integer nodeport) {
		this.nodeport = nodeport;
	}

	public String getNodeauthtoken() {
		return nodeauthtoken;
	}

	public void setNodeauthtoken(String nodeauthtoken) {
		this.nodeauthtoken = nodeauthtoken;
	}

	public String getNodeauthtoken_key() {
		return nodeauthtoken_key;
	}

	public void setNodeauthtoken_key(String nodeauthtoken_key) {
		this.nodeauthtoken_key = nodeauthtoken_key;
	}

	public String getIdxurl() {
		return idxurl;
	}

	public void setIdxurl(String idxurl) {
		this.idxurl = idxurl;
	}

	public Integer getIdxport() {
		return idxport;
	}

	public void setIdxport(Integer idxport) {
		this.idxport = idxport;
	}

	public String getIdxauthtoken() {
		return idxauthtoken;
	}

	public void setIdxauthtoken(String idxauthtoken) {
		this.idxauthtoken = idxauthtoken;
	}

	public String getIdxauthtoken_key() {
		return idxauthtoken_key;
	}

	public void setIdxauthtoken_key(String idxauthtoken_key) {
		this.idxauthtoken_key = idxauthtoken_key;
	}

}
