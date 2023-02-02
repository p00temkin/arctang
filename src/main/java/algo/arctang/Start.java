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
import crypto.forestfish.objects.avm.model.nft.metadata.ARCMetaData;
import crypto.forestfish.objects.ipfs.connector.IPFSConnector;
import crypto.forestfish.utils.AVMUtils;
import crypto.forestfish.utils.CryptUtils;
import crypto.forestfish.utils.JSONUtils;
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
		AVMBlockChainConnector connector = new AVMBlockChainConnector(settings.getChainInfo());
		Long lastRound = AVMUtils.getLastRound(connector);
		LOGGER.debug("lastRound: " + lastRound);
		Long lastRoundIndexer = AVMUtils.getIndexerHealthCheck(connector);
		LOGGER.debug("lastRoundIndexer: " + lastRoundIndexer);

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

			AVMNFTStandard standard = AVMUtils.identifyARCStandard(json);
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
		if ((settings.getAction() == Action.QUERY) && (null != settings.getAssetid()) && settings.isArctype()) {
			AVMNFTStandard standard = AVMUtils.identifyARCStandard(connector, settings.getAssetid());
			System.out.println("ASA identified as: " + standard);
		}

		// metadata
		if ((settings.getAction() == Action.QUERY) && (null != settings.getAssetid()) && settings.isMetadata()) {
			String json = AVMUtils.getASARawJSONResponse(connector, settings.getAssetid());

			AVMNFTStandard standard = AVMUtils.identifyARCStandard(json);
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
			String json = AVMUtils.getASARawJSONResponse(connector, settings.getAssetid());

			AVMNFTStandard standard = AVMUtils.identifyARCStandard(json);
			if (standard == AVMNFTStandard.ARC3) {
				ARC3Asset arc3asset = AVMUtils.createARC3Asset(json);
				IPFSConnector ipfs_connector = new IPFSConnector();

				// Grab the metadata
				String metajson = ipfs_connector.getStringContent(arc3asset.getAssetURL());
				ARC3MetaData arc3metadata = JSONUtils.createARC3MetaData(metajson);

				ASAVerificationStatus vstatus = AVMUtils.verifyARC3Asset(ipfs_connector, arc3asset, arc3metadata, metajson);
				System.out.println(vstatus.toString());
			}
			if (standard == AVMNFTStandard.ARC19) {
				ARC19Asset arc19asset = AVMUtils.createARC19Asset(json);
				String cid = AVMUtils.extractCIDFromARC19URLAndReserveAddress(arc19asset.getAssetURL(), arc19asset.getReserve().toString());

				if (!"".equals(cid)) {
					LOGGER.info("Resolved cid from ARC19 template to: " + cid);
					IPFSConnector ipfs_connector = new IPFSConnector();

					// Grab the metadata
					String metajson = ipfs_connector.getStringContent("ipfs://" + cid);
					ARCMetaData arcmetadata = JSONUtils.createARCMetaData(metajson);

					ASAVerificationStatus vstatus = AVMUtils.verifyARC19Asset(ipfs_connector, arc19asset, arcmetadata, metajson);
					System.out.println(vstatus.toString());
				}
			}
			if (standard == AVMNFTStandard.ARC69) {
				ARC69Asset arcasset = AVMUtils.createARC69Asset(json);

				// Grab the metadata
				String metajson = AVMUtils.getASALatestConfigTransactionNote(connector, arcasset.getAssetID());
				ARCMetaData arcmetadata = JSONUtils.createARCMetaData(metajson);

				IPFSConnector ipfs_connector = new IPFSConnector();

				ASAVerificationStatus vstatus = AVMUtils.verifyARC69Asset(ipfs_connector, arcasset, arcmetadata, metajson);
				System.out.println(vstatus.toString());
			}
		}

		// optin
		if ((settings.getAction() == Action.OPTIN) && (null != settings.getAssetid()) && (null != settings.getWalletname())) {
			
			// First we make sure the assetid represents an ARC
			String json = AVMUtils.getASARawJSONResponse(connector, settings.getAssetid());
			AVMNFTStandard standard = AVMUtils.identifyARCStandard(json);
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

	}

	private static Settings parseCliArgs(String[] args) {

		Settings settings = new Settings();
		Options options = new Options();

		// chain
		Option chainOption = new Option(null, "chain", true, "Chain, MAINNET, BETANET or TESTNET");
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

		// arctype
		Option arctypeOption = new Option(null, "arctype", false, "ARC type of assetid");
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
			if (cmd.hasOption("arctype")) settings.setArctype(true);
			if (cmd.hasOption("debug")) settings.setDebug(true);
			if (cmd.hasOption("metadata")) settings.setMetadata(true);

			if (cmd.hasOption("walletname")) settings.setWalletname(cmd.getOptionValue("walletname"));
			if (cmd.hasOption("mnemonic")) settings.setMnemonic(cmd.getOptionValue("mnemonic"));
			if (cmd.hasOption("to")) settings.setTo(cmd.getOptionValue("to"));

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
