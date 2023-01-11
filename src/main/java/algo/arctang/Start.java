package algo.arctang;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import crypto.forestfish.enums.avm.AVMChain;
import crypto.forestfish.enums.avm.AVMNFTStandard;
import crypto.forestfish.objects.avm.AlgoRelayNode;
import crypto.forestfish.objects.avm.connector.AVMBlockChainConnector;
import crypto.forestfish.utils.AVMUtils;

public class Start {

	private static final Logger LOGGER = LoggerFactory.getLogger(Start.class);

	public static void main(String[] args) {
		LOGGER.info("init()");
		
		AVMBlockChainConnector connector = new AVMBlockChainConnector(AVMChain.MAINNET,
				new AlgoRelayNode("https://mainnet-algorand.api.purestake.io/ps2", 443, "xxx", "X-API-Key"));
		
		Long lastRound = AVMUtils.getLastRound(connector);
		LOGGER.info("lastRound: " + lastRound);
		
		/**
		 * ARC3 sample
		 * - https://algoexplorer.io/asset/925168558
		 */
		AVMNFTStandard standard_sample1 = AVMUtils.identifyARC3Standard(connector, 925168558L);
		LOGGER.info("ARC3 asset identified as: " + standard_sample1);
		
		/**
		 * ARC19 sample
		 * - https://algoexplorer.io/asset/865610737
		 */
		AVMNFTStandard standard_sample2 = AVMUtils.identifyARC3Standard(connector, 865610737L);
		LOGGER.info("ARC19 asset identified as: " + standard_sample2);
		
		/**
		 * ARC19 sample (DESTROYED)
		 * - https://algoexplorer.io/asset/865610736
		 */
		
		/**
		 * ARC69 sample
		 * - https://algoexplorer.io/asset/490139078
		 */
		AVMNFTStandard standard_sample3 = AVMUtils.identifyARC3Standard(connector, 490139078L);
		LOGGER.info("ARC69 asset identified as: " + standard_sample3);
		
	}

}
