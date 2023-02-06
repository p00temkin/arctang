package algo.arctang;

import java.math.BigInteger;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.algorand.algosdk.crypto.Address;

import algo.arctang.enums.Action;
import crypto.forestfish.enums.avm.AVMChain;
import crypto.forestfish.enums.avm.AVMNFTStandard;
import crypto.forestfish.objects.avm.AVMAccountASABalance;
import crypto.forestfish.objects.avm.AlgoLocalWallet;
import crypto.forestfish.objects.avm.connector.AVMBlockChainConnector;
import crypto.forestfish.objects.avm.model.nft.ARC19Asset;
import crypto.forestfish.objects.avm.model.nft.ARC3Asset;
import crypto.forestfish.objects.avm.model.nft.ARC69Asset;
import crypto.forestfish.objects.avm.model.nft.ASAVerificationStatus;
import crypto.forestfish.objects.avm.model.nft.metadata.ARC3MetaData;
import crypto.forestfish.objects.avm.model.nft.metadata.ARC69MetaData;
import crypto.forestfish.objects.ipfs.connector.IPFSConnector;
import crypto.forestfish.utils.AVMUtils;
import crypto.forestfish.utils.CryptUtils;
import crypto.forestfish.utils.FilesUtils;
import crypto.forestfish.utils.JSONUtils;
import crypto.forestfish.utils.NFTUtils;
import crypto.forestfish.utils.StringsUtils;
import crypto.forestfish.utils.SystemUtils;
import io.ipfs.multihash.Multihash;

public class Start {

	private static final Logger LOGGER = LoggerFactory.getLogger(Start.class);

	public static void main(String[] args) {
		LOGGER.debug("arctang init()");

		Settings settings = null;
		settings = parseCliArgs(args);

		// Early exit if NETCONFIG/WALLETCONFIG actions
		if ((settings.getAction() == Action.NETCONFIG) || (settings.getAction() == Action.WALLETCONFIG)){
			LOGGER.info("Exiting ..");
			SystemUtils.halt();
		}

		/**
		 *  Connect to Algorand blockchain
		 */
		AVMBlockChainConnector connector = null;
		if (settings.getAction() != Action.CONVERT) {
			connector = new AVMBlockChainConnector(settings.getChainInfo());
			Long lastRound = AVMUtils.getLastRound(connector);
			LOGGER.debug("lastRound: " + lastRound);
			Long lastRoundIndexer = AVMUtils.getIndexerHealthCheck(connector);
			LOGGER.debug("lastRoundIndexer: " + lastRoundIndexer);
		}

		/**
		 * QUERY action
		 */

		// raw output
		if ((settings.getAction() == Action.QUERY) && (null != settings.getAssetid()) && settings.isRaw()) {
			String json = AVMUtils.getASARawJSONResponse(connector, settings.getAssetid());
			System.out.println(json);
		}

		// parsed output
		if ((settings.getAction() == Action.QUERY) && (null != settings.getAssetid()) && settings.isParsed()) {
			String json = AVMUtils.getASARawJSONResponse(connector, settings.getAssetid());

			AVMNFTStandard standard = AVMUtils.identifyARCStandardFromASAJSON(json);
			if (standard == AVMNFTStandard.ARC3) {
				ARC3Asset arcasset = AVMUtils.getARC3Info(connector, settings.getAssetid());
				System.out.println(arcasset.toString());
			}
			if (standard == AVMNFTStandard.ARC19) {
				ARC19Asset arcasset = AVMUtils.getARC19Info(connector, settings.getAssetid());
				System.out.println(arcasset.toString());
			}
			if (standard == AVMNFTStandard.ARC69) {
				ARC69Asset arcasset = AVMUtils.getARC69Info(connector, settings.getAssetid());
				System.out.println(arcasset.toString());
			}
		}

		// arctype output
		if ((settings.getAction() == Action.QUERY) && (null != settings.getAssetid()) && settings.isProbe_arcstandard()) {
			AVMNFTStandard standard = AVMUtils.identifyARCStandard(connector, settings.getAssetid());
			System.out.println("ASA identified as: " + standard);
		}

		// metadata
		if ((settings.getAction() == Action.QUERY) && (null != settings.getAssetid()) && settings.isMetadata()) {
			String json = AVMUtils.getASARawJSONResponse(connector, settings.getAssetid());

			AVMNFTStandard standard = AVMUtils.identifyARCStandardFromASAJSON(json);
			if (standard == AVMNFTStandard.ARC3) {
				ARC3Asset arcasset = AVMUtils.createARC3Asset(json);

				System.out.println("assetURL: " + arcasset.getAssetURL());
				IPFSConnector ipfs_connector = new IPFSConnector();

				// Grab the metadata
				String metajson = ipfs_connector.getStringContent(arcasset.getAssetURL());
				System.out.println(JSONUtils.prettyPrint(metajson));
			}
			if (standard == AVMNFTStandard.ARC19) {
				ARC19Asset arcasset = AVMUtils.createARC19Asset(json);
				String cid = AVMUtils.extractCIDFromARC19URLAndReserveAddress(arcasset.getAssetURL(), arcasset.getReserve().toString());

				if (!"".equals(cid)) {
					LOGGER.info("Resolved cid from ARC19 template to: " + cid);
					IPFSConnector ipfs_connector = new IPFSConnector();

					// Grab the metadata
					String metajson = ipfs_connector.getStringContent("ipfs://" + cid);
					System.out.println(JSONUtils.prettyPrint(metajson));
				}
			}
			if (standard == AVMNFTStandard.ARC69) {
				ARC69Asset arcasset = AVMUtils.createARC69Asset(json);

				LOGGER.info("Using indexer to fetch latest tx note ..");
				String latesttxnote = AVMUtils.getASALatestConfigTransactionNote(connector, arcasset.getAssetID());
				System.out.println(JSONUtils.prettyPrint(latesttxnote));
			}
		}

		// verify
		if ((settings.getAction() == Action.VERIFY) && (null != settings.getAssetid())) {
			String asa_json = AVMUtils.getASARawJSONResponse(connector, settings.getAssetid());

			AVMNFTStandard standard = AVMUtils.identifyARCStandardFromASAJSON(asa_json);
			LOGGER.info("Standard determined to be: " + standard);
			if (standard == AVMNFTStandard.ARC3) {
				ARC3Asset arc3asset = AVMUtils.createARC3Asset(asa_json);
				IPFSConnector ipfs_connector = new IPFSConnector();

				// Grab the metadata
				LOGGER.info("Getting metadata from assetURL " + arc3asset.getAssetURL());
				String metajson = ipfs_connector.getStringContent(arc3asset.getAssetURL());
				ARC3MetaData arc3metadata = JSONUtils.createARC3MetaData(metajson);

				ASAVerificationStatus vstatus = AVMUtils.verifyARC3Asset(ipfs_connector, arc3asset, arc3metadata, metajson);
				System.out.println(vstatus.toString());
			}
			if (standard == AVMNFTStandard.ARC19) {
				ARC19Asset arc19asset = AVMUtils.createARC19Asset(asa_json);
				String cid = AVMUtils.extractCIDFromARC19URLAndReserveAddress(arc19asset.getAssetURL(), arc19asset.getReserve().toString());

				if (!"".equals(cid)) {
					LOGGER.info("Resolved cid from ARC19 template to: " + cid);
					IPFSConnector ipfs_connector = new IPFSConnector();

					// Grab the metadata
					String metajson = ipfs_connector.getStringContent("ipfs://" + cid);
					ARC69MetaData arcmetadata = JSONUtils.createARCMetaData(metajson);

					ASAVerificationStatus vstatus = AVMUtils.verifyARC19Asset(ipfs_connector, arc19asset, arcmetadata, metajson);
					System.out.println(vstatus.toString());
				}
			}
			if (standard == AVMNFTStandard.ARC69) {
				ARC69Asset arcasset = AVMUtils.createARC69Asset(asa_json);

				// Grab the metadata
				String metajson = AVMUtils.getASALatestConfigTransactionNote(connector, arcasset.getAssetID());
				ARC69MetaData arcmetadata = JSONUtils.createARCMetaData(metajson);

				IPFSConnector ipfs_connector = new IPFSConnector();

				ASAVerificationStatus vstatus = AVMUtils.verifyARC69Asset(ipfs_connector, arcasset, arcmetadata, metajson);
				System.out.println(vstatus.toString());
			}
		}

		// optin
		if ((settings.getAction() == Action.OPTIN) && (null != settings.getAssetid()) && (null != settings.getWalletname())) {

			// First we make sure the assetid represents an ARC
			String json = AVMUtils.getASARawJSONResponse(connector, settings.getAssetid());
			AVMNFTStandard standard = AVMUtils.identifyARCStandardFromASAJSON(json);
			if (false ||
					(standard == AVMNFTStandard.ARC3) ||
					(standard == AVMNFTStandard.ARC19) ||
					(standard == AVMNFTStandard.ARC69) ||
					false) {
				LOGGER.info("You are about to opt-in to an ARC asset of standard " + standard);
			} else {
				LOGGER.error("The assetid does not seem to represent an ARC");
				SystemUtils.halt();
			}

			// Make sure the wallet exists
			AlgoLocalWallet wallet = AVMUtils.getWalletWithName(settings.getWalletname());
			if (null == wallet) {
				LOGGER.error("Unable to find wallet with name " + settings.getWalletname());
				SystemUtils.halt();
			}
			LOGGER.info("Using wallet with address " + wallet.getAddress());

			// Check if we already have an opt-in for this asset
			boolean optin = AVMUtils.isAccountOptinForASA(connector, wallet.fetchAccount().getAddress(), settings.getAssetid());
			LOGGER.info("optin status for account " + wallet.fetchAccount().getAddress() + " for assetid " + settings.getAssetid() + ": " + optin);

			// Perform the opt-in to the ASA
			if (!optin) {
				LOGGER.info("ASA optin tx request for account " + wallet.fetchAccount().getAddress() + " and assetid " + settings.getAssetid());
				String txhash_optin = AVMUtils.sendTXOptInToAsset(connector, wallet, settings.getAssetid(), true);
				LOGGER.info("We just opted in to ARC ASA with assetID " + settings.getAssetid() + ", txhash_optin: " + txhash_optin);
			}
		}

		// transfer
		if ((settings.getAction() == Action.TRANSFER) && (null != settings.getAssetid()) && (null != settings.getWalletname()) && (null != settings.getTo())) {

			// Make sure the wallet exists
			AlgoLocalWallet wallet = AVMUtils.getWalletWithName(settings.getWalletname());
			if (null == wallet) {
				LOGGER.error("Unable to find wallet with name " + settings.getWalletname());
				SystemUtils.halt();
			}

			// Make sure the target address resolves
			Address to_addr = null;
			try {
				to_addr = new Address(settings.getTo());
			} catch (Exception e) {
				LOGGER.info("Unable to properly parse --to argument as an Algorand address");
				SystemUtils.halt();
			}

			// Check if the target account has an opt-in for this asset
			boolean optin = AVMUtils.isAccountOptinForASA(connector, to_addr, settings.getAssetid());
			LOGGER.info("OPTIN status for account " + settings.getTo() + " for assetid " + settings.getAssetid() + ": " + optin);

			// Make sure we have 1 of the asset
			BigInteger asa_balance = AVMUtils.getAccountBalanceForASA(connector, wallet.fetchAccount().getAddress(), settings.getAssetid());
			LOGGER.info("Wallet " + settings.getWalletname() + " ASAs ID " + settings.getAssetid() + " owns " + asa_balance + " of ASA with ID " + settings.getAssetid());

			if (asa_balance.compareTo(BigInteger.ONE) >= 1) {
				LOGGER.info("Sending ASA to " + settings.getTo() + " from wallet " + settings.getWalletname());
				String txhash = AVMUtils.sendTXTransferASA(connector, wallet, to_addr, settings.getAssetid(), BigInteger.ONE, true);
				LOGGER.info("ASA transfer completed with txhash: " + txhash);
			}

		}

		// arc3 convert
		if ((settings.getAction() == Action.CONVERT) && (null != settings.getFrom_erc_folder()) && (null != settings.getTo_arc3_folder())) {
			IPFSConnector ipfs_connector = new IPFSConnector();
			boolean convert_success = NFTUtils.convertERC721MetadataFolderToARC(ipfs_connector, settings.getFrom_erc_folder(), settings.getTo_arc3_folder(), AVMNFTStandard.ARC3, false);
			LOGGER.info("convert status: " + convert_success);
		}

		// arc69 convert
		if ((settings.getAction() == Action.CONVERT) && (null != settings.getFrom_erc_folder()) && (null != settings.getTo_arc69_folder())) {
			IPFSConnector ipfs_connector = new IPFSConnector();
			boolean convert_success = NFTUtils.convertERC721MetadataFolderToARC(ipfs_connector, settings.getFrom_erc_folder(), settings.getTo_arc69_folder(), AVMNFTStandard.ARC69, false);
			LOGGER.info("convert status: " + convert_success);
		}

		// arc3 mint
		if ((settings.getAction() == Action.MINT) && (null != settings.getMetadata_cid()) && (null != settings.getArcstandard()) && (null != settings.getWalletname())) {

			// cleanup the cid path
			if (settings.getMetadata_cid().startsWith("ipfs://")) {
				String metadata_cid = settings.getMetadata_cid().replace("ipfs://", "");
				settings.setMetadata_cid(metadata_cid);
			}

			// Make sure metadata exists and aligns with specified ARC standard
			IPFSConnector ipfs_connector = new IPFSConnector();	
			String metadata_json = ipfs_connector.getStringContent("ipfs://" + settings.getMetadata_cid());
			System.out.println(JSONUtils.prettyPrint(metadata_json));
			AVMNFTStandard identified_standard = AVMUtils.identifyARCStandardFromMetadata(metadata_json);
			if (false ||
					(identified_standard == settings.getArcstandard()) ||
					((identified_standard == AVMNFTStandard.ARC3) && (settings.getArcstandard() == AVMNFTStandard.ARC19)) ||
					false) {
				// ARC3 metadata is ok for ARC19
				if (identified_standard == settings.getArcstandard()) {
					LOGGER.info("Identified standard " + identified_standard + " matches the specified");
				} else {
					LOGGER.info("Identified standard " + identified_standard + " is compatible with " + settings.getArcstandard());
				}
			} else {
				LOGGER.error("ARC standard mismatch, ARC standard identified as " + identified_standard + " but specified as " + settings.getArcstandard());
				SystemUtils.halt();
			}

			// Make sure the wallet exists
			AlgoLocalWallet wallet = AVMUtils.getWalletWithName(settings.getWalletname());
			if (null == wallet) {
				LOGGER.error("Unable to find wallet with name " + settings.getWalletname());
				SystemUtils.halt();
			}
			LOGGER.info("Using wallet with address " + wallet.getAddress() + " for minting");

			String unitName = "";
			String assetName = "";

			if (settings.getArcstandard() == AVMNFTStandard.ARC3) {
				ARC3MetaData arc3_metadata = JSONUtils.createARC3MetaData(metadata_json);
				if (null == arc3_metadata.getName()) {
					if (null == settings.getAsset_name()) {
						LOGGER.warn("We have the metadata JSON but no name is defined and --asset_name was not specified");
					} else {
						LOGGER.info("Using specified --asset_name value");
						assetName = settings.getAsset_name();
					}
				} else {
					LOGGER.info("Using specified --asset_name value");
					assetName = settings.getAsset_name();
				}
				if ("".equals(assetName)) {
					LOGGER.error("Unable to determine suitable assetName for ARC asset");
					SystemUtils.halt();
				}

				// unitName sanity check
				if (null != settings.getUnit_name()) {
					unitName = settings.getUnit_name();
				} else {
					unitName = NFTUtils.createUnitNameFromName(assetName);
					LOGGER.info("Generated the unitName " + unitName + " from the assetName");
				}
				if ("".equals(unitName)) {
					LOGGER.error("Unable to determine suitable unitName for ARC asset");
					SystemUtils.halt();
				}
				if (unitName.length() > 8) {
					LOGGER.error("UnitName for ARC asset needs to be less than 8 characters");
					SystemUtils.halt();
				}

				// Create the arc3params
				ARC3Asset arc3params = new ARC3Asset();
				arc3params.setUnitName(unitName);
				arc3params.setAssetName(assetName);
				arc3params.setAssetURL("ipfs://" + settings.getMetadata_cid() + "#arc3");
				arc3params.setTotalNrUnits(BigInteger.ONE);
				arc3params.setDecimals(0);
				arc3params.setDefaultFrozen(false);

				byte[] sha256 = CryptUtils.calculateSHA256(metadata_json);
				arc3params.setAssetMetadataHash(sha256);

				arc3params.setManager(wallet.fetchAccount().getAddress());
				arc3params.setReserve(wallet.fetchAccount().getAddress());
				arc3params.setFreeze(wallet.fetchAccount().getAddress());
				arc3params.setClawback(wallet.fetchAccount().getAddress());

				// perform the mint
				String txhash = AVMUtils.createARC3ASA(connector, wallet, arc3params);
				LOGGER.info("txhash: " + txhash);

			}
		}
	}

	private static Settings parseCliArgs(String[] args) {

		Settings settings = new Settings();
		Options options = new Options();

		// chain
		Option chainOption = new Option(null, "chain", true, "The Algorand chain: MAINNET, BETANET or TESTNET");
		options.addOption(chainOption);

		// action
		Option actionOption = new Option(null, "action", true, "Action to perform");
		actionOption.setRequired(true);
		options.addOption(actionOption);

		// nodeurl
		Option nodeurlOption = new Option(null, "nodeurl", true, "The Algorand custom network node URL");
		options.addOption(nodeurlOption);

		// nodeport
		Option nodeportOption = new Option(null, "nodeport", true, "The Algorand custom network node port");
		options.addOption(nodeportOption);

		// nodeauthtoken
		Option nodeauthtokenOption = new Option(null, "nodeauthtoken", true, "The Algorand custom network node authtoken");
		options.addOption(nodeauthtokenOption);

		// authtoken_key
		Option nodeauthtokenkeyOption = new Option(null, "nodeauthtoken_key", true, "The Algorand custom network node authtoken keyname (defaults to X-Algo-API-Token)");
		options.addOption(nodeauthtokenkeyOption);

		// idxurl
		Option idxurlOption = new Option(null, "idxurl", true, "The Algorand custom network indexer URL");
		options.addOption(idxurlOption);

		// idxport
		Option idxportOption = new Option(null, "idxport", true, "The Algorand custom network indexer port");
		options.addOption(idxportOption);

		// idxauthtoken
		Option idxauthtokenOption = new Option(null, "idxauthtoken", true, "The Algorand custom network indexer authtoken");
		options.addOption(idxauthtokenOption);

		// authtoken_key
		Option idxauthtokenkeyOption = new Option(null, "idxauthtoken_key", true, "The Algorand custom network indexer authtoken keyname (defaults to X-Algo-API-Token)");
		options.addOption(idxauthtokenkeyOption);

		// override safemode
		Option overridesafemodeOption = new Option(null, "override_safemode", false, "Safe mode override (defaults to false)");
		options.addOption(overridesafemodeOption);

		// confignetwork
		Option confignetworkOption = new Option(null, "confignetwork", false, "Configure node to use for Algorand network connectivity, requires --nodeurl, --nodeport, --nodeauthtoken, --nodeauthtoken_key");
		options.addOption(confignetworkOption);

		// parsed
		Option debugOption = new Option(null, "debug", false, "Debug mode");
		options.addOption(debugOption);

		// assetID
		Option assetidOption = new Option(null, "assetid", true, "The ASA assetID");
		options.addOption(assetidOption);

		// raw
		Option rawOption = new Option(null, "raw", false, "Raw output format");
		options.addOption(rawOption);

		// parsed
		Option parsedOption = new Option(null, "parsed", false, "Parsed output format");
		options.addOption(parsedOption);

		// probe_arcstandard
		Option arctypeOption = new Option(null, "probe_arcstandard", false, "Estimates ARC standard of assetid");
		options.addOption(arctypeOption);

		// metadata
		Option metadataOption = new Option(null, "metadata", false, "Grab the JSON metadata of ARC NFT with specified assetid");
		options.addOption(metadataOption);

		// mnemonic
		Option mnemonicOption = new Option(null, "mnemonic", true, "Mnemonic to use for creating an Algorand account. Use with --walletname");
		options.addOption(mnemonicOption);

		// walletname
		Option walletnameOption = new Option(null, "walletname", true, "Wallet name to use for specified action");
		options.addOption(walletnameOption);

		// to
		Option toOption = new Option(null, "to", true, "Target account address for asset TRANSFER action");
		options.addOption(toOption);

		// from_erc_folder
		Option from_erc_folderOption = new Option(null, "from_erc_folder", true, "Folder path to source ERC721 JSON metadata files to be converted to ARC");
		options.addOption(from_erc_folderOption);

		// to_arc3_folder
		Option to_arc3_folderOption = new Option(null, "to_arc3_folder", true, "Folder path to target ARC3 JSON metadata files");
		options.addOption(to_arc3_folderOption);

		// to_arc69_folder
		Option to_arc69_folderOption = new Option(null, "to_arc69_folder", true, "Folder path to target ARC69 JSON metadata files");
		options.addOption(to_arc69_folderOption);

		// metadata_cid
		Option metadatacidOption = new Option(null, "metadata_cid", true, "IPFS CID of the metadata JSON file to be minted");
		options.addOption(metadatacidOption);

		// asset_name
		Option asset_nameOption = new Option(null, "asset_name", true, "Name of asset to be minted (can be excluded if metadata has name properties)");
		options.addOption(asset_nameOption);

		// unit_name
		Option unit_nameOption = new Option(null, "unit_name", true, "Unit name of asset to be minted (can be exluded if metadata has name properties)");
		options.addOption(unit_nameOption);

		// arcstandard
		Option arcstandardOption = new Option(null, "arcstandard", true, "ARC standard to use for minting: ARC3, ARC19 or ARC69");
		options.addOption(arcstandardOption);

		HelpFormatter formatter = new HelpFormatter();
		CommandLineParser parser = new DefaultParser();
		CommandLine cmd;
		try {
			cmd = parser.parse(options, args);

			if (cmd.hasOption("chain")) {
				if (cmd.getOptionValue("chain").equalsIgnoreCase("MAINNET")) settings.setChain(AVMChain.MAINNET);
				if (cmd.getOptionValue("chain").equalsIgnoreCase("BETANET")) settings.setChain(AVMChain.BETANET);
				if (cmd.getOptionValue("chain").equalsIgnoreCase("TESTNET")) settings.setChain(AVMChain.TESTNET);
			}

			if (cmd.hasOption("action")) {
				if (cmd.getOptionValue("action").equalsIgnoreCase("QUERY")) settings.setAction(Action.QUERY);
				if (cmd.getOptionValue("action").equalsIgnoreCase("VERIFY")) settings.setAction(Action.VERIFY);
				if (cmd.getOptionValue("action").equalsIgnoreCase("TRANSFER")) settings.setAction(Action.TRANSFER);
				if (cmd.getOptionValue("action").equalsIgnoreCase("MINT")) settings.setAction(Action.MINT);
				if (cmd.getOptionValue("action").equalsIgnoreCase("RECONFIG")) settings.setAction(Action.RECONFIG);
				if (cmd.getOptionValue("action").equalsIgnoreCase("WALLETCONFIG")) settings.setAction(Action.WALLETCONFIG);
				if (cmd.getOptionValue("action").equalsIgnoreCase("NETCONFIG")) settings.setAction(Action.NETCONFIG);
				if (cmd.getOptionValue("action").equalsIgnoreCase("OPTIN")) settings.setAction(Action.OPTIN);
				if (cmd.getOptionValue("action").equalsIgnoreCase("CONVERT")) settings.setAction(Action.CONVERT);
			}

			if (cmd.hasOption("arcstandard")) {
				if (cmd.getOptionValue("arcstandard").equalsIgnoreCase("ARC3")) settings.setArcstandard(AVMNFTStandard.ARC3);
				if (cmd.getOptionValue("arcstandard").equalsIgnoreCase("ARC19")) settings.setArcstandard(AVMNFTStandard.ARC19);
				if (cmd.getOptionValue("arcstandard").equalsIgnoreCase("ARC69")) settings.setArcstandard(AVMNFTStandard.ARC69);
			}

			if (cmd.hasOption("nodeurl")) settings.setNodeurl(cmd.getOptionValue("nodeurl"));
			if (cmd.hasOption("nodeport")) settings.setNodeport(Integer.parseInt(cmd.getOptionValue("nodeport")));
			if (cmd.hasOption("nodeauthtoken")) settings.setNodeauthtoken(cmd.getOptionValue("nodeauthtoken"));
			if (cmd.hasOption("nodeauthtoken_key")) settings.setNodeauthtoken_key(cmd.getOptionValue("nodeauthtoken_key"));

			if (cmd.hasOption("idxurl")) settings.setIdxurl(cmd.getOptionValue("idxurl"));
			if (cmd.hasOption("idxport")) settings.setIdxport(Integer.parseInt(cmd.getOptionValue("idxport")));
			if (cmd.hasOption("idxauthtoken")) settings.setIdxauthtoken(cmd.getOptionValue("idxauthtoken"));
			if (cmd.hasOption("idxauthtoken_key")) settings.setIdxauthtoken_key(cmd.getOptionValue("idxauthtoken_key"));

			if (cmd.hasOption("assetid")) {
				try {
					settings.setAssetid(Long.parseLong(cmd.getOptionValue("assetid")));
				} catch (Exception e) {
					LOGGER.error("Unable to parse the assetid parameter");
					SystemUtils.halt();
				}
			}
			if (cmd.hasOption("override_safemode")) settings.setSafemode(false);

			if (cmd.hasOption("parsed")) settings.setParsed(true);
			if (cmd.hasOption("raw")) settings.setRaw(true);
			if (cmd.hasOption("probe_arcstandard")) settings.setProbe_arcstandard(true);
			if (cmd.hasOption("debug")) settings.setDebug(true);
			if (cmd.hasOption("metadata")) settings.setMetadata(true);

			if (cmd.hasOption("walletname")) settings.setWalletname(cmd.getOptionValue("walletname"));
			if (cmd.hasOption("mnemonic")) settings.setMnemonic(cmd.getOptionValue("mnemonic"));
			if (cmd.hasOption("to")) settings.setTo(cmd.getOptionValue("to"));

			if (cmd.hasOption("from_erc_folder")) settings.setFrom_erc_folder(cmd.getOptionValue("from_erc_folder"));
			if (cmd.hasOption("to_arc3_folder")) settings.setTo_arc3_folder(cmd.getOptionValue("to_arc3_folder"));
			if (cmd.hasOption("to_arc69_folder")) settings.setTo_arc69_folder(cmd.getOptionValue("to_arc69_folder"));

			if (cmd.hasOption("metadata_cid")) settings.setMetadata_cid(cmd.getOptionValue("metadata_cid"));
			if (cmd.hasOption("asset_name")) settings.setAsset_name(cmd.getOptionValue("asset_name"));
			if (cmd.hasOption("unit_name")) settings.setUnit_name(cmd.getOptionValue("unit_name"));

			settings.sanityCheck();
			if (settings.isDebug()) settings.print();

		} catch (ParseException e) {
			LOGGER.error("ParseException: " + e.getMessage());
			formatter.printHelp(" ", options);
			SystemUtils.halt();
		}

		return settings;
	}

}
