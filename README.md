## ARCTANG

Swiss army knife to query/validate/transfer/update/mint/backup NFTs for various ARC standards on the Algorand blockchain.

![alt text](https://github.com/p00temkin/arctang/blob/master/img/arctang_r7.png?raw=true)

### Quick introduction for EVM users
On Ethereum an NFT is represented by a smart contract where the contract keeps track of its owners, burns etc. The smart contract represents the 'NFT collection' and all activity is handled by this contract. 
Ownership basically represents being part of a club and ERC721 is the pure OG NFT contract standard. ERC1155 is a more recent cousin which supports semi-fungible tokens and adds additional features such as 
batch transfers and transaction security.

On Algorand an NFT is instead represented as an individual ASA (Algorand Standard Asset). An ASA is not a smart contract but a separate first-class citizen on the Algorand blockchain. To be considered an NFT 
the ASA needs to comply with one of the current Algorand NFT-related ARCs. An ARC is the equivalent of an ERC on Ethereum. 

### Main ARC types

- **ARC3**
  - <https://github.com/algorandfoundation/ARCs/blob/main/ARCs/arc-0003.md>
  - NFT metadata focused standard.
  - The url field points to the NFT metadata. The metadata supports a schema which can have associated integrity and mimetype fields. 
  - Suitable for immutable NFTs with large metadata files (>1KB size of JSON) and multiple off-chain data references. [1]

- **ARC69**
  - <https://github.com/algorandfoundation/ARCs/blob/main/ARCs/arc-0069.md>
  - NFT mediafile focused standard. 
  - The url field points to the NFT digital asset file. The ASA metadata is stored on-chain and represented by the note field of the latest valid assetconfig transaction. Since the note field is limited to 1KB 
the metadata JSON is also restricted to this size. This design means fetching the metadata is complex and requires access to an archive node, but also allows metadata to be updated with a single transaction 
and simple access to the mediafile url.
  - Suitable for mutable NFTs where the mediafile is locked, easily accessed, but the compact metadata associated with it changes over time. [1]
 
- **ARC19**
  - <https://github.com/algorandfoundation/ARCs/blob/main/ARCs/arc-0019.md>
  - NFT metadata focused standard. 
  - Enforces off-chain IPFS metadata by using the url field as a template populated by the reserve address field which holds the CID. Easy to update since the reserve address value can be replaced with a single 
transaction, which in turn changes the metadata. 
  - Suitable for mutable NFTs intended to transition into immutable NFTs, with complete metadata (+mediafile) changes. [1]

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
- Why? An excuse to extend forestfish capabilities beyond Ethereum and participate in a the Greenhouse Hackathon ;)

### User cases

First configure how you connect to the Algorand blockchain.

   ```
	java -jar ./arctang.jar --chain MAINNET --action NETCONFIG --nodeurl https://mainnet-algorand.api.purestake.io/ps2 --nodeport 443 --authtoken_key "X-API-Key" --authtoken <api-key>
   ```

This stores the details in .avm/networks/MAINNET and you no longer need to specify these parameters for every action, only the --chain option. 

### Query the on-chain ASA JSON (raw format)

- ARC3 asset:
   ```
	java -jar ./arctang.jar --chain MAINNET --action QUERY --assetid 925168558

   {
	  "index" : 925168558,
	  "params" : {
		"clawback" : "X6MNR4AVJQEMJRHAPZ6F4O4SVDIYN67ZRMD2O3ULPY4QFMANQNZOEYHODE",
		"creator" : "X6MNR4AVJQEMJRHAPZ6F4O4SVDIYN67ZRMD2O3ULPY4QFMANQNZOEYHODE",
		"decimals" : 0,
		"default-frozen" : true,
		"freeze" : "EMWPXNULSR3US737FFOSEJJB4B3R5BJQRYCVYPJSP7IUBRXUN3LF4MG2NA",
		"manager" : "EMWPXNULSR3US737FFOSEJJB4B3R5BJQRYCVYPJSP7IUBRXUN3LF4MG2NA",
		"metadata-hash" : "WiPFje9tLPWl1SBB49MB3GBdr9QR1cIwoVg+6CyrhbI=",
		"name" : "D02-31 #29863",
		"name-b64" : "RDAyLTMxICMyOTg2Mw==",
		"reserve" : "X6MNR4AVJQEMJRHAPZ6F4O4SVDIYN67ZRMD2O3ULPY4QFMANQNZOEYHODE",
		"total" : 1,
		"unit-name" : "D02-31",
		"unit-name-b64" : "RDAyLTMx",
		"url" : "ipfs://Qme9e7yjXTn5iL2gqnVY2H1UydE45B2SEauH6HDJoqS34a#arc3",
		"url-b64" : "aXBmczovL1FtZTllN3lqWFRuNWlMMmdxblZZMkgxVXlkRTQ1QjJTRWF1SDZIREpvcVMzNGEjYXJjMw=="
	  }
	}
   ```

- ARC19 asset:
   ```
   java -jar ./arctang.jar --chain MAINNET --action QUERY --assetid 865610737

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
  
- ARC69 asset:
   ```
   java -jar ./arctang.jar --chain MAINNET --action QUERY --assetid 490139078

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
   <TBD>
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
- <https://github.com/ethereum/EIPs/blob/master/EIPS/eip-1155.md>
- <https://docs.opensea.io/docs/metadata-standards>

Misc:
- <https://nftfactory.org/blog/algorand-nft-assembly-line>
- <https://stackoverflow.com/questions/74052032/algorand-arc-19-and-arc-69-what-exaclty-is-the-difference>
- <https://www.techdreams.org/crypto-currency/algorand-arc3-and-arc69-standard-nfts-overview/12382-20220118>
