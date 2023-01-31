## ARCTANG

Swiss army knife to query/validate/transfer/mint/update NFTs for various ARC standards on the Algorand blockchain. Using the official Java SDK via [ForestFISH](https://github.com/p00temkin/forestfish) Algorand support (part of this project). 

![alt text](https://github.com/p00temkin/arctang/blob/master/img/arctang_r7.png?raw=true)

### Quick introduction for EVM users
On Ethereum an NFT is represented by a smart contract where the contract keeps track of its owners, burns etc. The smart contract represents the 'NFT collection' and all activity is handled by this contract. 
Ownership basically represents being part of a club and ERC721 is the pure OG NFT contract standard. ERC1155 is a more recent cousin which supports semi-fungible tokens and adds additional features such as 
batch transfers and transaction security.

On Algorand an NFT is instead represented as an individual ASA (Algorand Standard Asset). An ASA is not a smart contract but a separate first-class citizen on the Algorand blockchain. To be considered an NFT 
the ASA needs to comply with one of the current Algorand NFT-related ARCs. An ARC is the equivalent of an ERC on Ethereum. 

### Algorand NFT ARCs

- **ARC3**
  - <https://github.com/algorandfoundation/ARCs/blob/main/ARCs/arc-0003.md>
  - NFT metadata focused standard.
  - The url field points to the NFT metadata. The metadata supports a schema which can have associated integrity and mimetype fields. 
  - Suitable for immutable NFTs with large metadata files (>1KB size of JSON) and multiple off-chain data references.

- **ARC19**
  - <https://github.com/algorandfoundation/ARCs/blob/main/ARCs/arc-0019.md>
  - NFT metadata focused standard. 
  - Enforces off-chain IPFS metadata by using the url field as a template populated by the reserve address field which holds the CID. Easy to update since the reserve address value can be replaced with a single transaction, which in turn changes the metadata. The reserve address is only irrelevant (and thus can be used in this way) for pure NFTs (1 of 1).
  - Suitable for mutable NFTs intended to transition into immutable NFTs, with complete metadata (+mediafile) changes. 

- **ARC69**
  - <https://github.com/algorandfoundation/ARCs/blob/main/ARCs/arc-0069.md>
  - NFT mediafile focused standard. 
  - The url field points to the NFT digital asset file. The ASA metadata is stored on-chain and represented by the note field of the latest valid assetconfig transaction. Since the note field is limited to 1KB the metadata JSON is also restricted to this size. This design means fetching the metadata is complex and requires access to an archive node, but also allows metadata to be updated with a single transaction 
and simple access to the mediafile url.
  - Suitable for mutable NFTs where the mediafile is locked, easily accessed, but the compact metadata associated with it changes over time.
 
In common for all of these standards is that the four addresses of an ASA (manager, reservice, freeze and clawback) can be updated by the manager address unless it is set to "". 

### Supporting ARCs:

- **ARC20**
  - <https://github.com/algorandfoundation/ARCs/blob/main/ARCs/arc-0020.md>
  - Standard which defines how to wrap/control an ARC ASA with a smart contract
  - Able to wrap ARC3, ARC69, ARC19 NFTs
  - Populates the 'arc-20' key in the ARC 'properties', indicating the link to the smart contract wrapper

- **ARC18**
  - <https://github.com/algorandfoundation/ARCs/blob/main/ARCs/arc-0018.md>
  - Standard to enforce royalty payments
  - Implementation of ARC20
  - Populates the 'arc-18' key in the ARC 'properties'

### Why arctang?
- Name? Suggested by chatGPT as a project name including the word 'arc'
- Logo? Generated by Midjourney using 'arc' as one of the keywords
- Why? An excuse to extend [ForestFISH](https://github.com/p00temkin/forestfish) capabilities beyond Ethereum and participate in a the Greenhouse Hackathon ;)

### User cases

First configure how you connect to the Algorand blockchain, which is done by specifying an Algorand node and indexer*:

   ```
	java -jar ./arctang.jar 
	--chain MAINNET 
	--action NETCONFIG 
	--nodeurl https://mainnet-algorand.api.purestake.io/ps2 
	--nodeport 443 
	--nodeauthtoken_key "X-API-Key" 
	--nodeauthtoken <api-key>
	--idxurl https://mainnet-algorand.api.purestake.io/idx2
	--idxport 443
	--idxauthtoken_key "X-API-Key" 
	--idxauthtoken <api-key>
   ```

   ```
	java -jar ./arctang.jar 
	--chain TESTNET 
	--action NETCONFIG 
	--nodeurl https://testnet-algorand.api.purestake.io/ps2 
	--nodeport 443 
	--nodeauthtoken_key "X-API-Key" 
	--nodeauthtoken <api-key>
	--idxurl https://testnet-algorand.api.purestake.io/idx2
	--idxport 443
	--idxauthtoken_key "X-API-Key" 
	--idxauthtoken <api-key>
   ```

This stores the details in .avm/networks/[MAINNET|TESTNET] and you no longer need to specify these parameters for every action, only the --chain option. You can get a free apikey over at [Purestake](https://www.purestake.com/).

Note for EVM users: The indexer is similar to an archive node with various indexes, ie subset of [The Graph](https://thegraph.com/) functionality but using REST calls. 

### Query the on-chain ASA JSON (raw format)

- **ARC3 asset**:
   ```
	java -jar ./arctang.jar --chain MAINNET --action QUERY --assetid 387411719 --raw

	{
	  "index" : 387411719,
	  "params" : {
		"creator" : "TEST7FVOHRUCBH25UARE34XE5PVKKDANYWAPCLPQNGSOTLNBYC64C67B74",
		"decimals" : 0,
		"default-frozen" : false,
		"manager" : "TEST7FVOHRUCBH25UARE34XE5PVKKDANYWAPCLPQNGSOTLNBYC64C67B74",
		"metadata-hash" : "NWgQYgRRTYAb7p28MJDhQLe5adSeZivMUBGl/cZtdWM=",
		"name" : "ARC3",
		"name-b64" : "QVJDMw==",
		"reserve" : "TEST7FVOHRUCBH25UARE34XE5PVKKDANYWAPCLPQNGSOTLNBYC64C67B74",
		"total" : 1,
		"unit-name" : "NFTARC3",
		"unit-name-b64" : "TkZUQVJDMw==",
		"url" : "ipfs://bafkreibvnaigebcrjwabx3u5xqyjbykaw64wtve6myv4yuarux64m3lvmm#arc3",
		"url-b64" : "aXBmczovL2JhZmtyZWlidm5haWdlYmNyandhYngzdTV4cXlqYnlrYXc2NHd0dmU2bXl2NHl1YXJ1eDY0bTNsdm1tI2FyYzM="
	  }
	}
   ```

- **ARC19 asset**:
   ```
   java -jar ./arctang.jar --chain MAINNET --action QUERY --assetid 865610737 --raw

   {
	  "index" : 865610737,
	  "params" : {
		"creator" : "AZLC3PCLM3QLAOIXNUCHP2LRPXZ4XVS4JDXPH2QHVVPLNVC53FFYFBC63A",
		"decimals" : 0,
		"default-frozen" : false,
		"manager" : "AZLC3PCLM3QLAOIXNUCHP2LRPXZ4XVS4JDXPH2QHVVPLNVC53FFYFBC63A",
		"name" : "Anon 220",
		"name-b64" : "QW5vbiAyMjA=",
		"reserve" : "6562RSECCFMAUO5MNFCMED4ZKWY7KUB2LTH2SIIFYNZ6BUQTYZ4BQHLQBU",
		"total" : 1,
		"unit-name" : "S1ANON",
		"unit-name-b64" : "UzFBTk9O",
		"url" : "template-ipfs://{ipfscid:1:raw:reserve:sha2-256}",
		"url-b64" : "dGVtcGxhdGUtaXBmczovL3tpcGZzY2lkOjE6cmF3OnJlc2VydmU6c2hhMi0yNTZ9"
	  }
	}
   ```
  
- **ARC69 asset**:
   ```
   java -jar ./arctang.jar --chain MAINNET --action QUERY --assetid 490139078 --raw

   {
	  "index" : 490139078,
	  "params" : {
		"creator" : "OJGTHEJ2O5NXN7FVXDZZEEJTUEQHHCIYIE5MWY6BEFVVLZ2KANJODBOKGA",
		"decimals" : 0,
		"default-frozen" : false,
		"manager" : "OJGTHEJ2O5NXN7FVXDZZEEJTUEQHHCIYIE5MWY6BEFVVLZ2KANJODBOKGA",
		"name" : "Zip",
		"name-b64" : "Wmlw",
		"reserve" : "OJGTHEJ2O5NXN7FVXDZZEEJTUEQHHCIYIE5MWY6BEFVVLZ2KANJODBOKGA",
		"total" : 8000,
		"unit-name" : "ALCH0046",
		"unit-name-b64" : "QUxDSDAwNDY=",
		"url" : "https://gateway.pinata.cloud/ipfs/QmVxZFeLHtbrdtFabb46ToSvegpKyva1jzTkR61a8uM7qT",
		"url-b64" : "aHR0cHM6Ly9nYXRld2F5LnBpbmF0YS5jbG91ZC9pcGZzL1FtVnhaRmVMSHRicmR0RmFiYjQ2VG9TdmVncEt5dmExanpUa1I2MWE4dU03cVQ="
	  }
	}

   ```

### Query for ASA type

Note that this raw command works against any ASA type and highlights the differences between ARC3, ARC19 and ARC69. If you just want to identify the ARC type of an asset then you can use --arctype as shown below:
   
   ```
	java -jar ./arctang.jar --chain MAINNET --action QUERY --assetid 387411719 --arctype
	ASA identified as: ARC3
		
	java -jar ./arctang.jar --chain MAINNET --action QUERY --assetid 865610737 --arctype
	ASA identified as: ARC19
	
	java -jar ./arctang.jar --chain MAINNET --action QUERY --assetid 490139078 --arctype
	ASA identified as: ARC69
   ```

### Query for ASA metadata

Note that the NFT metadata is fetched differently for each of these ARC standards. Arctang handles this for you when using the --metadata option:

- **ARC3 asset:** (fetches metadata using IPFS/HTTPS specified 'url')
   ```
   java -jar ./arctang.jar --chain MAINNET --action QUERY --assetid 387411719 --metadata
   
   .. Updating list of active IPFS gateway URLs ..
   .. Nr of active IPFS gateways: 18
   .. Attempting to fetch ipfs://Qme9e7yjXTn5iL2gqnVY2H1UydE45B2SEauH6HDJoqS34a#arc3
	{
	  "name": "ARC3",
	  "description": "First ARC3 NFT?",
	  "image": "ipfs://bafkreibsgazs6waapitr4rvwsd75z5jgcxryiqacllrexszaoha2ph6voq",
	  "image_integrity": "sha256-MjAzL1gAeiceRraQ/9z1JhXjhEACWuJLyyBxwaef1XQ=",
	  "image_mimetype": "image/png",
	  "animation_url": "ipfs://bafkreibnr6etiygfl6suxntwpfkzb6bbuuirlf6jww76b4yglfxqgywiw4",
	  "animation_url_integrity": "sha256-LY+JNGDFX6VLtnZ5VZD4IaURFZfJtb/g8wZZbwNiyLc=",
	  "animation_url_mimetype": "image/gif",
	  "properties": {
		"fun_level": {
		  "name": "Fun level",
		  "value": 1000000
		},
		"colors": {
		  "name": "Colors",
		  "value": [
			"Black",
			"Yellow",
			"White"
		  ]
		},
		"text": {
		  "name": "Text",
		  "value": "ARC3"
		}
	  }
	}
   ```
   
- **ARC19 asset**: (fetches metadata from IPFS using CID generated from reserve address)
   ```
   java -jar ./arctang.jar --chain MAINNET --action QUERY --assetid 865610737 --metadata
   
   .. Resolved cid from ARC19 template to: bafkreihxpwumraqrlafdxldjitba7gkvwh2vaos4z6uscbodopqnee6gpa
   .. Updating list of active IPFS gateway URLs ..
   .. Nr of active IPFS gateways: 18
   .. Attempting to fetch ipfs://bafkreihxpwumraqrlafdxldjitba7gkvwh2vaos4z6uscbodopqnee6gpa
	{
	  "assetName": "Anon 220",
	  "unitName": "S1ANON",
	  "description": "",
	  "image": "ipfs://bafybeidhlz7iznf5rpxwj5xfukppvkizxf4yp3cnpipjcmvbjkg7rwwwau",
	  "external_url": "",
	  "properties": {
		"background color": "grey",
		"background style": "solid",
		"mask color": "grey",
		"skin tone": "dark"
	  },
	  "royalty": 0.05,
	  "register": "Minted by KinnDAO"
	}
   ```
   
- **ARC69 asset**: (fetches metadata from the note of the latest assetconfig tx, using the indexer node)
   ```
   java -jar ./arctang.jar --chain MAINNET --action QUERY --assetid 490139078 --metadata
   
   .. Using indexer to fetch latest tx note ..
	{
	  "standard": "arc69",
	  "description": "Zip",
	  "external_url": "Alchemon.net",
	  "mime_type": "image/png",
	  "properties": {
		"Number": "0046",
		"Rarity": "Common",
		"Type": "Electric",
		"Strength": "70",
		"Health": "58",
		"Speed": "67",
		"Defense": "55"
	  }
	}
   ```

### Verify ASA content and metadata

For ARC3 ASAs we can verify the integrity of the NFT by checking that the metadata JSON is intact and that the integrity checksum fields of the metadata are inteact (ie the actual linked mediafiles) . This can be achived with with VERIFY action which provides an ARC compliance summary along with overview of potential issues found:

- **ARC3 asset:**
   ```
   java -jar ./arctang.jar --chain MAINNET --action VERIFY --assetid 387411719
   
   .. Updating list of active IPFS gateway URLs ..
   .. Nr of active IPFS gateways: 18
   .. Attempting to fetch ipfs://bafkreibvnaigebcrjwabx3u5xqyjbykaw64wtve6myv4yuarux64m3lvmm#arc3
   .. Attempting to fetch ipfs://bafkreibsgazs6waapitr4rvwsd75z5jgcxryiqacllrexszaoha2ph6voq
   .. Attempting to fetch ipfs://bafkreibnr6etiygfl6suxntwpfkzb6bbuuirlf6jww76b4yglfxqgywiw4
	Verified     : true
	Score [0-10] : 8
	-----------------------------------
	Warnings:
	 [#1] Manager address is still set, NFT is mutable
	Verified parameters:
	 [+] asset URL endswith #arc3 and name is not fixed to arc3 or contains @arc3
	 [+] asset URL uses IPFS
	 [+] Calculated metadata hash matches the ASA specified hash (NWgQYgRRTYAb7p28MJDhQLe5adSeZivMUBGl/cZtdWM=)
	 [+] Metadata name (ARC3) related the ASA specified unit name (NFTARC3)
	 [+] Calculated image_integrity hash matches the metadata specified hash (MjAzL1gAeiceRraQ/9z1JhXjhEACWuJLyyBxwaef1XQ=)
	 [+] Calculated animation_url_integrity hash matches the metadata specified hash (LY+JNGDFX6VLtnZ5VZD4IaURFZfJtb/g8wZZbwNiyLc=)
   ```
The concept of string similarity in the ARC standard is handled by thresholds using LCS (Longest Common Subsequence). The ARC3 standard is the most expressive in terms of integrity checksums, but the same also works for ARC19 and ARC69:

- **ARC19 asset**: 
   ```
   java -jar ./arctang.jar --chain MAINNET --action VERIFY --assetid 865610737
   
   .. Updating list of active IPFS gateway URLs ..
   .. Nr of active IPFS gateways: 17
   .. Attempting to fetch ipfs://bafkreihxpwumraqrlafdxldjitba7gkvwh2vaos4z6uscbodopqnee6gpa
	Verified     : true
	Score [0-10] : 6
	-----------------------------------
	Warnings:
	 [#1] Manager address is still set, NFT is mutable
	 [#2] No unit name value specified in the ASA
	Verified parameters:
	 [+] ARC19 ASA url template is 'ipfscid'
	 [+] ARC19 ASA url specifies IPFS CID version '0' or '1'
	 [+] ARC19 ASA url specifies valid multicodec
	 [+] ARC19 ASA url fieldname is 'reserve'
	 [+] ARC19 ASA url hashtype is 'sha2-256'
   ```

- **ARC69 asset**:
   ```
   java -jar ./arctang.jar --chain MAINNET --action VERIFY --assetid 490139078
   
   .. Updating list of active IPFS gateway URLs ..
   .. Nr of active IPFS gateways: 18
    Verified     : true
	Score [0-10] : 4
	-----------------------------------
	Warnings:
	 [#1] Manager address is still set, NFT is mutable
	 [#2] ARC69 ASA media URL uses https:// instead of IPFS
	 [#3] ARC69 ASA media URL does not specify media type using # fragment identifier
	 [#4] No unit name value specified in the ASA
   ```

### Creating a wallet

In order to transfer, mint or reconfigure ASAs we need an Algorand account to work from. We can create a named wallet using the WALLETCONFIG action

   ```
	java -jar ./arctang.jar 
	--action WALLETCONFIG 
	--walletname bob
	--mnemonic "xxx xxx xxx .."
	
	.. Generated wallet from mnemonic with name bob with address ..
   ```

This creates a walletfile in your local .avm/wallets folder (with the walletname 'bob' in this case) which can be used for future actions which requires an on-chain action. 

### Opt-in to an ASA Asset

On Ethereum the most common token standards such as ERC-20 and ERC-721 can be sent to your account without restrictions. On Algorand an account must first opt-in to an asset before it can be received and there is a 0.1 ALGO account deposit requirement for every asset you hold in your Algorand account (regardless of the amount of that asset). This prevents your account from being polluted with dust tokens and acts as housekeeping for the blockchain (but also prevents random airdrops which are quite common on Ethereum). In a sense Algorand is more similar to ERC-777 as it allows more control over your tokens.

In order to opt-in to an ARC NFT you can use the OPTIN action: 

   ```
	java -jar ./arctang.jar --chain TESTNET --action OPTIN --walletname bob --assetid 66753108
	
	.. You are about to opt-in to an ARC asset of standard ARC19
    .. Using wallet with address P...
    .. optin status for account P... for assetid 66753108: false
    .. ASA optin tx request for account P... and assetid 66753108
    .. We just opted in to ARC ASA with assetID 66753108, txhash_optin: WQG3WG...
   ```

Your account is now able to receive this ARC19 asset and 0.1 ALGO in your account is locked until you opt-out of this asset.

### Transfer an ASA Asset

### Mint an ASA Asset

### Reconfigure an ASA Asset

### Prerequisites

[Java 17+, Maven 3.x]

   ```
 java -version # jvm 17+ required
 mvn -version # maven 3.x required
 git clone https://github.com/p00temkin/forestfish
 mvn clean package install
   ```

### Building the application

   ```
   mvn clean package
   mv target/arctang-0.0.1-SNAPSHOT-jar-with-dependencies.jar arctang.jar
   ```

### Usage

   ```
   java -jar ./arctang.jar 
   ```

Options:

   ```
   --chain
   --action
   --nodeurl
   --nodeport
   --nodeauthtoken
   --nodeauthtoken_key
   --idxurl
   --idxport
   --idxauthtoken
   --idxauthtoken_key
   --assetid
   --parsed
   --raw
   --arctype
   --debug
   --metadata
   --walletname
   --mnemonic
   ```

### Additional useful options/resources

Official ARCs:
- <https://github.com/algorandfoundation/ARCs/blob/main/ARCs/arc-0003.md>
- <https://github.com/algorandfoundation/ARCs/blob/main/ARCs/arc-0018.md>
- <https://github.com/algorandfoundation/ARCs/blob/main/ARCs/arc-0019.md>
- <https://github.com/algorandfoundation/ARCs/blob/main/ARCs/arc-0020.md>
- <https://github.com/algorandfoundation/ARCs/blob/main/ARCs/arc-0069.md>

Algorand documentation:
- <https://developer.algorand.org/docs/get-details/asa/>
- <https://developer.algorand.org/docs/get-details/transactions/transactions/#asset-parameters>
- <https://developer.algorand.org/docs/rest-apis/indexer/#assetparams>

Algorand NFT galleries:
- <https://algogems.io>
- <https://zestbloom.com>
- <https://dartroom.xyz>
- <https://www.nftexplorer.app>
- <https://www.randgallery.com/algo-collection>

Ethereum influenced EIPs:
- <https://github.com/ethereum/EIPs/blob/master/EIPS/eip-721.md>
- <https://github.com/ethereum/EIPs/blob/master/EIPS/eip-1155.md>
- <https://github.com/ethereum/EIPs/blob/master/EIPS/eip-777.md>
- <https://docs.opensea.io/docs/metadata-standards>

Misc:
- <https://hex2algo.vercel.app>
- <https://nftfactory.org/blog/algorand-nft-assembly-line>
- <https://stackoverflow.com/questions/74052032/algorand-arc-19-and-arc-69-what-exaclty-is-the-difference>
- <https://www.techdreams.org/crypto-currency/algorand-arc3-and-arc69-standard-nfts-overview/12382-20220118>
