package algo.arctang;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import algo.arctang.enums.Action;
import crypto.forestfish.enums.avm.AVMChain;
import crypto.forestfish.objects.avm.connector.AVMBlockChainConnector;
import crypto.forestfish.objects.avm.model.nft.ARC19Asset;
import crypto.forestfish.objects.avm.model.nft.ARC3Asset;
import crypto.forestfish.objects.avm.model.nft.ARC69Asset;
import crypto.forestfish.objects.ipfs.connector.IPFSConnector;
import crypto.forestfish.utils.AVMUtils;
import crypto.forestfish.utils.JSONUtils;

public class MainnetARCMetadata {

	private static final Logger LOGGER = LoggerFactory.getLogger(MainnetARCMetadata.class);

	private static AVMChain chain = AVMChain.MAINNET;

	// Note #1: All tests require a properly MAINNET configuration (.avm/networks/MAINNET), see NETCONFIG cli action 
	// Note #2: Many of the MAINNET NFTs used here are still mutable which may cause these tests to fail (if they are updated)

	@BeforeClass
	public static void testBeforeClass() {
		LOGGER.info("Mainnet unit tests INIT");
	}

	@Test
	public void testARC3Metadata() {
		LOGGER.info("testARC3Metadata()");

		// java -jar ./arctang.jar --chain MAINNET --action QUERY --assetid 387411719 --metadata
		Settings settings = new Settings();
		settings.setChain(chain);
		settings.setAction(Action.QUERY);
		settings.setMetadata(true);
		settings.setAssetid(387411719L);
		settings.sanityCheck();

		// Sanity check our Algorand connection
		AVMBlockChainConnector connector = new AVMBlockChainConnector(settings.getChainInfo());
		Long lastRound = AVMUtils.getLastRound(connector);
		assertTrue("Algorand mainnet lastRound has sane value ", lastRound > 100L);
		Long lastRoundIndexer = AVMUtils.getIndexerHealthCheck(connector);
		assertTrue("Algorand mainnet lastRound lastRoundIndexer sane value ", lastRoundIndexer > 100L);

		// Check raw JSON output is obtained
		String asa_json = AVMUtils.getASARawJSONResponse(connector, settings.getAssetid());
		assertEquals("Raw JSON response check", "{\"index\":387411719,\"params\":{\"creator\":\"TEST7FVOHRUCBH25UARE34XE5PVKKDANYWAPCLPQNGSOTLNBYC64C67B74\",\"decimals\":0,\"default-frozen\":false,\"manager\":\"TEST7FVOHRUCBH25UARE34XE5PVKKDANYWAPCLPQNGSOTLNBYC64C67B74\",\"metadata-hash\":\"NWgQYgRRTYAb7p28MJDhQLe5adSeZivMUBGl/cZtdWM=\",\"name\":\"ARC3\",\"name-b64\":\"QVJDMw==\",\"reserve\":\"TEST7FVOHRUCBH25UARE34XE5PVKKDANYWAPCLPQNGSOTLNBYC64C67B74\",\"total\":1,\"unit-name\":\"NFTARC3\",\"unit-name-b64\":\"TkZUQVJDMw==\",\"url\":\"ipfs://bafkreibvnaigebcrjwabx3u5xqyjbykaw64wtve6myv4yuarux64m3lvmm#arc3\",\"url-b64\":\"aXBmczovL2JhZmtyZWlidm5haWdlYmNyandhYngzdTV4cXlqYnlrYXc2NHd0dmU2bXl2NHl1YXJ1eDY0bTNsdm1tI2FyYzM=\"}}", JSONUtils.compactPrint(asa_json));

		// Map raw JSON to ARC3 object
		ARC3Asset arcasset = AVMUtils.createARC3Asset(asa_json);
		assertTrue("Verify the assetID is parsed correctly", 387411719L == arcasset.getAssetID());
		assertEquals("Verify the asset name is parsed correctly", "ARC3", arcasset.getAssetName());
		assertEquals("Verify the unit name is parsed correctly", "NFTARC3", arcasset.getUnitName());
		assertEquals("Verify the URL is parsed correctly", "ipfs://bafkreibvnaigebcrjwabx3u5xqyjbykaw64wtve6myv4yuarux64m3lvmm#arc3", arcasset.getAssetURL());

		// Grab the metadata from IPFS
		IPFSConnector ipfs_connector = new IPFSConnector();
		String metajson = ipfs_connector.getStringContent(arcasset.getAssetURL());
		assertEquals("Metadata JSON response check", "{\"name\":\"ARC3\",\"description\":\"First ARC3 NFT?\",\"image\":\"ipfs://bafkreibsgazs6waapitr4rvwsd75z5jgcxryiqacllrexszaoha2ph6voq\",\"image_integrity\":\"sha256-MjAzL1gAeiceRraQ/9z1JhXjhEACWuJLyyBxwaef1XQ=\",\"image_mimetype\":\"image/png\",\"animation_url\":\"ipfs://bafkreibnr6etiygfl6suxntwpfkzb6bbuuirlf6jww76b4yglfxqgywiw4\",\"animation_url_integrity\":\"sha256-LY+JNGDFX6VLtnZ5VZD4IaURFZfJtb/g8wZZbwNiyLc=\",\"animation_url_mimetype\":\"image/gif\",\"properties\":{\"fun_level\":{\"name\":\"Fun level\",\"value\":1000000},\"colors\":{\"name\":\"Colors\",\"value\":[\"Black\",\"Yellow\",\"White\"]},\"text\":{\"name\":\"Text\",\"value\":\"ARC3\"}}}", JSONUtils.compactPrint(metajson));
		System.out.println(JSONUtils.prettyPrint(metajson));
	}
	

	@Test
	public void testARC19Metadata() {
		LOGGER.info("testARC19Metadata()");

		// java -jar ./arctang.jar --chain MAINNET --action QUERY --assetid 865610737 --metadata
		Settings settings = new Settings();
		settings.setChain(chain);
		settings.setAction(Action.QUERY);
		settings.setMetadata(true);
		settings.setAssetid(865610737L);
		settings.sanityCheck();

		// Sanity check our Algorand connection
		AVMBlockChainConnector connector = new AVMBlockChainConnector(settings.getChainInfo());
		Long lastRound = AVMUtils.getLastRound(connector);
		assertTrue("Algorand mainnet lastRound has sane value ", lastRound > 100L);
		Long lastRoundIndexer = AVMUtils.getIndexerHealthCheck(connector);
		assertTrue("Algorand mainnet lastRound lastRoundIndexer sane value ", lastRoundIndexer > 100L);

		// Check raw JSON output is obtained
		String asa_json = AVMUtils.getASARawJSONResponse(connector, settings.getAssetid());
		assertEquals("Raw JSON response check", "{\"index\":865610737,\"params\":{\"creator\":\"AZLC3PCLM3QLAOIXNUCHP2LRPXZ4XVS4JDXPH2QHVVPLNVC53FFYFBC63A\",\"decimals\":0,\"default-frozen\":false,\"manager\":\"AZLC3PCLM3QLAOIXNUCHP2LRPXZ4XVS4JDXPH2QHVVPLNVC53FFYFBC63A\",\"name\":\"Anon 220\",\"name-b64\":\"QW5vbiAyMjA=\",\"reserve\":\"6562RSECCFMAUO5MNFCMED4ZKWY7KUB2LTH2SIIFYNZ6BUQTYZ4BQHLQBU\",\"total\":1,\"unit-name\":\"S1ANON\",\"unit-name-b64\":\"UzFBTk9O\",\"url\":\"template-ipfs://{ipfscid:1:raw:reserve:sha2-256}\",\"url-b64\":\"dGVtcGxhdGUtaXBmczovL3tpcGZzY2lkOjE6cmF3OnJlc2VydmU6c2hhMi0yNTZ9\"}}", JSONUtils.compactPrint(asa_json));

		// Map raw JSON to ARC19 object
		ARC19Asset arcasset = AVMUtils.createARC19Asset(asa_json);
		assertTrue("Verify the assetID is parsed correctly", 865610737L == arcasset.getAssetID());
		assertEquals("Verify the asset name is parsed correctly", "Anon 220", arcasset.getAssetName());
		assertEquals("Verify the unit name is parsed correctly", "S1ANON", arcasset.getUnitName());

		// Extract IPFS cid from the ARC19 template
		String cid = AVMUtils.extractCIDFromARC19URLAndReserveAddress(arcasset.getAssetURL(), arcasset.getReserve().toString());
		assertEquals("Verify the cid is resolved properly", "bafkreihxpwumraqrlafdxldjitba7gkvwh2vaos4z6uscbodopqnee6gpa", cid);
		LOGGER.info("Resolved cid from ARC19 template to: " + cid);

		// Grab the metadata from IPFS
		IPFSConnector ipfs_connector = new IPFSConnector();
		String metajson = ipfs_connector.getStringContent("ipfs://" + cid);
		assertEquals("Metadata JSON response check", "{\"assetName\":\"Anon 220\",\"unitName\":\"S1ANON\",\"description\":\"\",\"image\":\"ipfs://bafybeidhlz7iznf5rpxwj5xfukppvkizxf4yp3cnpipjcmvbjkg7rwwwau\",\"external_url\":\"\",\"properties\":{\"background color\":\"grey\",\"background style\":\"solid\",\"mask color\":\"grey\",\"skin tone\":\"dark\"},\"royalty\":0.05,\"register\":\"Minted by KinnDAO\"}", JSONUtils.compactPrint(metajson));
		System.out.println(JSONUtils.prettyPrint(metajson));
	}
	
	@Test
	public void testARC69Metadata() {
		LOGGER.info("testARC69Metadata()");

		// java -jar ./arctang.jar --chain MAINNET --action QUERY --assetid 490139078 --metadata
		Settings settings = new Settings();
		settings.setChain(chain);
		settings.setAction(Action.QUERY);
		settings.setMetadata(true);
		settings.setAssetid(490139078L);
		settings.sanityCheck();

		// Sanity check our Algorand connection
		AVMBlockChainConnector connector = new AVMBlockChainConnector(settings.getChainInfo());
		Long lastRound = AVMUtils.getLastRound(connector);
		assertTrue("Algorand mainnet lastRound has sane value ", lastRound > 100L);
		Long lastRoundIndexer = AVMUtils.getIndexerHealthCheck(connector);
		assertTrue("Algorand mainnet lastRound lastRoundIndexer sane value ", lastRoundIndexer > 100L);

		// Check raw JSON output is obtained
		String asa_json = AVMUtils.getASARawJSONResponse(connector, settings.getAssetid());
		assertEquals("Raw JSON response check", "{\"index\":490139078,\"params\":{\"creator\":\"OJGTHEJ2O5NXN7FVXDZZEEJTUEQHHCIYIE5MWY6BEFVVLZ2KANJODBOKGA\",\"decimals\":0,\"default-frozen\":false,\"manager\":\"OJGTHEJ2O5NXN7FVXDZZEEJTUEQHHCIYIE5MWY6BEFVVLZ2KANJODBOKGA\",\"name\":\"Zip\",\"name-b64\":\"Wmlw\",\"reserve\":\"OJGTHEJ2O5NXN7FVXDZZEEJTUEQHHCIYIE5MWY6BEFVVLZ2KANJODBOKGA\",\"total\":8000,\"unit-name\":\"ALCH0046\",\"unit-name-b64\":\"QUxDSDAwNDY=\",\"url\":\"https://gateway.pinata.cloud/ipfs/QmVxZFeLHtbrdtFabb46ToSvegpKyva1jzTkR61a8uM7qT\",\"url-b64\":\"aHR0cHM6Ly9nYXRld2F5LnBpbmF0YS5jbG91ZC9pcGZzL1FtVnhaRmVMSHRicmR0RmFiYjQ2VG9TdmVncEt5dmExanpUa1I2MWE4dU03cVQ=\"}}", JSONUtils.compactPrint(asa_json));

		// Map raw JSON to ARC69 object
		ARC69Asset arcasset = AVMUtils.createARC69Asset(asa_json);
		assertTrue("Verify the assetID is parsed correctly", 490139078L == arcasset.getAssetID());
		assertEquals("Verify the asset name is parsed correctly", "Zip", arcasset.getAssetName());
		assertEquals("Verify the unit name is parsed correctly", "ALCH0046", arcasset.getUnitName());

		// Grab metadata from latest tx
		LOGGER.info("Using indexer to fetch latest tx note ..");
		String latesttxnote = AVMUtils.getASALatestConfigTransactionNote(connector, arcasset.getAssetID());
		assertEquals("Verify metadata JSON content is correct", "{\"standard\":\"arc69\",\"description\":\"Zip\",\"external_url\":\"Alchemon.net\",\"mime_type\":\"image/png\",\"properties\":{\"Number\":\"0046\",\"Rarity\":\"Common\",\"Type\":\"Electric\",\"Strength\":\"70\",\"Health\":\"58\",\"Speed\":\"67\",\"Defense\":\"55\"}}", JSONUtils.compactPrint(latesttxnote));
		System.out.println(JSONUtils.prettyPrint(latesttxnote));
	}

}
