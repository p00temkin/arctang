package algo.arctang;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import crypto.forestfish.enums.avm.AVMChain;
import crypto.forestfish.enums.avm.AVMNFTStandard;
import crypto.forestfish.objects.avm.AlgoRelayNode;
import crypto.forestfish.objects.avm.connector.AVMBlockChainConnector;
import crypto.forestfish.objects.avm.model.nft.ARC19Asset;
import crypto.forestfish.objects.avm.model.nft.ARC3Asset;
import crypto.forestfish.objects.avm.model.nft.ARC69Asset;
import crypto.forestfish.utils.AVMUtils;

public class Start {

	private static final Logger LOGGER = LoggerFactory.getLogger(Start.class);

	public static void main(String[] args) {
		LOGGER.info("init()");
		
		AVMBlockChainConnector connector = new AVMBlockChainConnector(AVMChain.MAINNET,
				new AlgoRelayNode("https://mainnet-algorand.api.purestake.io/ps2", 443, "<api-key>", "X-API-Key"));
		
		Long lastRound = AVMUtils.getLastRound(connector);
		LOGGER.info("lastRound: " + lastRound);
		
		/**
		 * ARC3 sample
		 * - https://algoexplorer.io/asset/925168558
		 */
		AVMNFTStandard standard_sample1 = AVMUtils.identifyARCStandard(connector, 925168558L);
		LOGGER.info("ARC3 asset identified as: " + standard_sample1);
		if (standard_sample1 == AVMNFTStandard.ARC3) {
			ARC3Asset arc3asset_sample1 = AVMUtils.getARC3Info(connector, 925168558L);
			LOGGER.info("ARC3 unitName: " + arc3asset_sample1.getUnitName());
		}
		
		/**
		 * ARC19 sample
		 * - https://algoexplorer.io/asset/865610737
		 */
		AVMNFTStandard standard_sample2 = AVMUtils.identifyARCStandard(connector, 865610737L);
		LOGGER.info("ARC19 asset identified as: " + standard_sample2);
		if (standard_sample2 == AVMNFTStandard.ARC19) {
			ARC19Asset arc19asset_sample1 = AVMUtils.getARC19Info(connector, 865610737L);
			LOGGER.info("ARC19 unitName: " + arc19asset_sample1.getUnitName());
		}
		
		/**
		 * ARC19 sample (DESTROYED)
		 * - https://algoexplorer.io/asset/865610736
		 */
		
		/**
		 * ARC69 sample
		 * - https://algoexplorer.io/asset/490139078
		 */
		AVMNFTStandard standard_sample3 = AVMUtils.identifyARCStandard(connector, 490139078L);
		LOGGER.info("ARC69 asset identified as: " + standard_sample3);
		if (standard_sample3 == AVMNFTStandard.ARC69) {
			ARC69Asset arc69asset_sample1 = AVMUtils.getARC69Info(connector, 490139078L);
			LOGGER.info("ARC69 unitName: " + arc69asset_sample1.getUnitName());
		}
		
	}

}
