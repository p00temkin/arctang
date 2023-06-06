package algo.arctang;

import java.io.File;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import algo.arctang.enums.Action;
import crypto.forestfish.enums.avm.AVMChain;
import crypto.forestfish.enums.avm.AVMNFTStandard;
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
	private boolean probe_arcstandard = false;
	private boolean metadata = false;
	private boolean imageurl = false;
	
	private String walletname;
	private String mnemonic;
	private String to;
	
	private String from_erc_folder;
	private String to_arc3_folder;
	private String to_arc69_folder;
	
	private String metadata_cid; 
	private String mediadata_url; 
	private String metadata_filepath; 
	private AVMNFTStandard arcstandard;
	private String asset_name; 
	private String unit_name; 
	private String manager;
	private String reserve;
	private String freeze;
	private String clawback;
	private boolean force_immutable = false;
	private boolean clearmanager = false;
	private boolean clearreserve = false;
	private boolean clearfreeze = false;
	private boolean clearclawback = false;
	private String address;
	
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
		
		if (null == this.getAction()) {
			LOGGER.error("You need to define an action using --action");
			SystemUtils.halt();
		}
		
		if (false ||
				// all actions which require a network connection
				(this.getAction() == Action.QUERY) ||
				(this.getAction() == Action.MINT) ||
				(this.getAction() == Action.RECONFIG) ||
				(this.getAction() == Action.TRANSFER) ||
				(this.getAction() == Action.VERIFY) ||
				(this.getAction() == Action.NETCONFIG) ||
				(this.getAction() == Action.OPTIN) ||
				(this.getAction() == Action.DESTROY) ||
				(this.getAction() == Action.METADATAUPDATE) ||
				(this.getAction() == Action.LIST) ||
				false) {

			// require chain
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
					String json = JSONUtils.createJSONFromPOJO(chainInfo);
					FilesUtils.createFolderUnlessExists(".avm");
					FilesUtils.createFolderUnlessExists(".avm/networks");
					File f = new File(".avm/networks/" + chain.toString());
					if (!f.exists()) {
						LOGGER.info("Flushing chainInfo to .avm/networks/" + chain.toString());
						FilesUtils.writeToFileUNIXNoException(json, ".avm/networks/" + chain.toString());
					}
				}
				
				// Require defined Algorand node
				if (chainInfo.getNodes().isEmpty()) {
					LOGGER.error("You need to define a node for " + this.getChain() + " using --confignetwork since no public nodes are available");
					SystemUtils.halt();
				}
				
			} else {
				// Check if we can get the chain details from 
				LOGGER.debug("Checking for chainInfo details at .avm/networks/" + chain.toString());
				File f = new File(".avm/networks/" + chain.toString());
				if (f.exists()) {
					String json = FilesUtils.readStringFromFile(".avm/networks/" + chain.toString());
					AVMChainInfo chainInfo = JSONUtils.createPOJOFromJSON(json, AVMChainInfo.class);
					this.chainInfo = chainInfo;
				} else {
					LOGGER.error("Need chainInfo for " + this.getChain() + ", please consider using --confignetwork since no public nodes are available");
					SystemUtils.halt();
				}
			}
			
		}
		
		if ((this.getAction() == Action.QUERY) && (null == this.getAssetid())) {
			LOGGER.error("Need to provide --assetid when using QUERY action");
			SystemUtils.halt();
		}

		if ((this.getAction() == Action.WALLETCONFIG)) {
			
			if (null == this.getWalletname()) {
				LOGGER.error("Need to provide --walletname when using WALLETCONFIG action");
				SystemUtils.halt();
			}
			
			if (null == this.getMnemonic()) {
				boolean createdOrExists = AVMUtils.createNewRandomWalletWithName(this.getWalletname());
				if (!createdOrExists) LOGGER.error("Unable to create wallet with name " + this.getWalletname());
				SystemUtils.halt();
			} else {
				boolean createdOrExists = AVMUtils.createWalletWithName(this.getWalletname(), this.getMnemonic());
				if (!createdOrExists) LOGGER.error("Unable to create wallet with name " + this.getWalletname());
				SystemUtils.halt();
			}

		}
		
		if ((this.getAction() == Action.OPTIN)) {
			if (null == this.getWalletname()) {
				LOGGER.error("Need to provide --walletname when using the OPTIN action");
				SystemUtils.halt();
			}
		}
		
		if ((this.getAction() == Action.LIST)) {
			if ((null == this.getWalletname()) && (null == this.getAddress())) {
				LOGGER.error("Need to provide --walletname or --address when using the LIST action");
				SystemUtils.halt();
			}
			if ((null != this.getWalletname()) && (null != this.getAddress())) {
				LOGGER.error("Please dont provide --walletname AND --address when using the LIST action");
				SystemUtils.halt();
			}
			// check for valid Algorand address
			if (null != this.getAddress()) {
				AVMUtils.createAddressFromSTR(this.getAddress());
			}
		}
		
		if ((this.getAction() == Action.TRANSFER)) {
			if (null == this.getWalletname()) {
				LOGGER.error("Need to provide --walletname when using the TRANSFER action");
				SystemUtils.halt();
			}
			if (null == this.getTo()) {
				LOGGER.error("Need to provide --to for the target account when using the TRANSFER action");
				SystemUtils.halt();
			}
			if (!AVMUtils.isValidAlgorandAccount(this.getTo())) {
				LOGGER.error("The --to argument needs to be a valid Algorand account, you provided " + this.getTo());
				SystemUtils.halt();
			}
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

	public String getWalletname() {
		return walletname;
	}

	public void setWalletname(String walletname) {
		this.walletname = walletname;
	}

	public String getMnemonic() {
		return mnemonic;
	}

	public void setMnemonic(String mnemonic) {
		this.mnemonic = mnemonic;
	}

	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		this.to = to;
	}

	public String getFrom_erc_folder() {
		return from_erc_folder;
	}

	public void setFrom_erc_folder(String from_erc_folder) {
		this.from_erc_folder = from_erc_folder;
	}

	public String getTo_arc3_folder() {
		return to_arc3_folder;
	}

	public void setTo_arc3_folder(String to_arc3_folder) {
		this.to_arc3_folder = to_arc3_folder;
	}

	public String getTo_arc69_folder() {
		return to_arc69_folder;
	}

	public void setTo_arc69_folder(String to_arc69_folder) {
		this.to_arc69_folder = to_arc69_folder;
	}

	public AVMNFTStandard getArcstandard() {
		return arcstandard;
	}

	public void setArcstandard(AVMNFTStandard arcstandard) {
		this.arcstandard = arcstandard;
	}

	public boolean isProbe_arcstandard() {
		return probe_arcstandard;
	}

	public void setProbe_arcstandard(boolean probe_arcstandard) {
		this.probe_arcstandard = probe_arcstandard;
	}

	public String getMetadata_cid() {
		return metadata_cid;
	}

	public void setMetadata_cid(String metadata_cid) {
		this.metadata_cid = metadata_cid;
	}

	public String getAsset_name() {
		return asset_name;
	}

	public void setAsset_name(String asset_name) {
		this.asset_name = asset_name;
	}

	public String getUnit_name() {
		return unit_name;
	}

	public void setUnit_name(String unit_name) {
		this.unit_name = unit_name;
	}

	public String getManager() {
		return manager;
	}

	public void setManager(String manager) {
		this.manager = manager;
	}

	public String getReserve() {
		return reserve;
	}

	public void setReserve(String reserve) {
		this.reserve = reserve;
	}

	public String getFreeze() {
		return freeze;
	}

	public void setFreeze(String freeze) {
		this.freeze = freeze;
	}

	public String getClawback() {
		return clawback;
	}

	public void setClawback(String clawback) {
		this.clawback = clawback;
	}

	public boolean isForce_immutable() {
		return force_immutable;
	}

	public void setForce_immutable(boolean force_immutable) {
		this.force_immutable = force_immutable;
	}

	public boolean isClearmanager() {
		return clearmanager;
	}

	public void setClearmanager(boolean clearmanager) {
		this.clearmanager = clearmanager;
	}

	public boolean isClearreserve() {
		return clearreserve;
	}

	public void setClearreserve(boolean clearreserve) {
		this.clearreserve = clearreserve;
	}

	public boolean isClearfreeze() {
		return clearfreeze;
	}

	public void setClearfreeze(boolean clearfreeze) {
		this.clearfreeze = clearfreeze;
	}

	public boolean isClearclawback() {
		return clearclawback;
	}

	public void setClearclawback(boolean clearclawback) {
		this.clearclawback = clearclawback;
	}

	public String getMediadata_url() {
		return mediadata_url;
	}

	public void setMediadata_url(String mediadata_url) {
		this.mediadata_url = mediadata_url;
	}

	public String getMetadata_filepath() {
		return metadata_filepath;
	}

	public void setMetadata_filepath(String metadata_filepath) {
		this.metadata_filepath = metadata_filepath;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public boolean isImageurl() {
		return imageurl;
	}

	public void setImageurl(boolean imageurl) {
		this.imageurl = imageurl;
	}

}
