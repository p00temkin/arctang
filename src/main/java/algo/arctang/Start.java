package algo.arctang;

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
import crypto.forestfish.objects.avm.connector.AVMBlockChainConnector;
import crypto.forestfish.objects.avm.model.nft.ARC19Asset;
import crypto.forestfish.objects.avm.model.nft.ARC3Asset;
import crypto.forestfish.objects.avm.model.nft.ARC69Asset;
import crypto.forestfish.objects.avm.model.nft.ASAVerificationStatus;
import crypto.forestfish.objects.avm.model.nft.metadata.ARC3MetaData;
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

		// Early exit if NETCONFIG
		if (settings.getAction() == Action.NETCONFIG) {
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
				ARC3Asset arcasset = AVMUtils.createARC3Asset(json);
				IPFSConnector ipfs_connector = new IPFSConnector();

				// Grab the metadata
				String metajson = ipfs_connector.getStringContent(arcasset.getAssetURL());
				ARC3MetaData arc3metadata = JSONUtils.createARC3MetaData(metajson);
				
				ASAVerificationStatus vstatus = AVMUtils.verifyARC3Asset(ipfs_connector, arcasset, arc3metadata, metajson);
				System.out.println(vstatus.toString());
			}
		}
		
	}

	private static Settings parseCliArgs(String[] args) {

		Settings settings = new Settings();
		Options options = new Options();

		// chain
		Option chainOption = new Option(null, "chain", true, "Chain, MAINNET, BETANET or TESTNET");
		chainOption.setRequired(true);
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
				if (cmd.getOptionValue("action").equalsIgnoreCase("EVM_BACKUP")) settings.setAction(Action.EVM_BACKUP);
				if (cmd.getOptionValue("action").equalsIgnoreCase("NETCONFIG")) settings.setAction(Action.NETCONFIG);
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
