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

import algo.arctang.enums.Action;
import crypto.forestfish.enums.avm.AVMChain;
import crypto.forestfish.enums.avm.AVMNFTStandard;
import crypto.forestfish.objects.avm.AlgoRelayNode;
import crypto.forestfish.objects.avm.connector.AVMBlockChainConnector;
import crypto.forestfish.objects.avm.model.nft.ARC19Asset;
import crypto.forestfish.objects.avm.model.nft.ARC3Asset;
import crypto.forestfish.objects.avm.model.nft.ARC69Asset;
import crypto.forestfish.utils.AVMUtils;
import crypto.forestfish.utils.SystemUtils;

public class Start {

	private static final Logger LOGGER = LoggerFactory.getLogger(Start.class);

	public static void main(String[] args) {
		LOGGER.info("arctang init()");

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
		LOGGER.info("lastRound: " + lastRound);

		/**
		 * QUERY action
		 */
		if ((settings.getAction() == Action.QUERY) && (null != settings.getAssetid())) {
			String json = AVMUtils.getASARawJSONResponse(connector, settings.getAssetid());
			System.out.println(json);
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

		// url
		Option urlOption = new Option(null, "nodeurl", true, "The Algorand custom network node URL");
		options.addOption(urlOption);

		// port
		Option portOption = new Option(null, "nodeport", true, "The Algorand custom network node port");
		options.addOption(portOption);

		// authtoken
		Option authtokenOption = new Option(null, "authtoken", true, "The Algorand custom network node authtoken");
		options.addOption(authtokenOption);

		// authtoken_key
		Option authtokenkeyOption = new Option(null, "authtoken_key", true, "The Algorand custom network node authtoken keyname (defaults to X-Algo-API-Token)");
		options.addOption(authtokenkeyOption);

		// override safemode
		Option overridesafemodeOption = new Option(null, "override_safemode", false, "Safe mode override (defaults to false)");
		options.addOption(overridesafemodeOption);

		// confignetwork
		Option confignetworkOption = new Option(null, "confignetwork", false, "Configure node to use for Algorand network connectivity, requires --url, --port, --authtoken, --authtoken_key");
		options.addOption(confignetworkOption);
		
		// assetID
		Option assetidOption = new Option(null, "assetid", true, "The ASA assetID");
		options.addOption(assetidOption);

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

			if (cmd.hasOption("nodeurl")) settings.setUrl(cmd.getOptionValue("nodeurl"));
			if (cmd.hasOption("nodeport")) settings.setPort(Integer.parseInt(cmd.getOptionValue("nodeport")));
			if (cmd.hasOption("authtoken")) settings.setAuthtoken(cmd.getOptionValue("authtoken"));
			if (cmd.hasOption("authtoken_key")) settings.setAuthtoken_key(cmd.getOptionValue("authtoken_key"));
			if (cmd.hasOption("assetid")) {
				try {
					settings.setAssetid(Long.parseLong(cmd.getOptionValue("assetid")));
				} catch (Exception e) {
					LOGGER.error("Unable to parse the assetid parameter");
					SystemUtils.halt();
				}
			}
			if (cmd.hasOption("override_safemode")) settings.setSafemode(false);

			settings.sanityCheck();
			settings.print();

		} catch (ParseException e) {
			LOGGER.error("ParseException: " + e.getMessage());
			formatter.printHelp(" ", options);
			SystemUtils.halt();
		}

		return settings;
	}


}
