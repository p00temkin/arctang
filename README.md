## ARCTANG

Swiss army knife to query/validate/transfer/convert/mint/update NFTs for various ARC standards on the Algorand blockchain. Using the official Java SDK via [forestFISH](https://github.com/p00temkin/forestfish) (part of this project) for Algorand support. 

| ![alt text](https://github.com/p00temkin/arctang/blob/master/img/arctang_r7.png?raw=true) |
| :--: |
* Proud winner of [Greenhouse Hackathon 03](https://www.reddit.com/r/AlgorandOfficial/comments/11aw8k9/algorand_greenhouse_hack_03_winners)

### Quick introduction for EVM users
On Ethereum an NFT is represented by a smart contract where the contract keeps track of its owners, burns etc. The smart contract represents the 'NFT collection' and all activity is handled by this contract. Ownership basically represents being part of a club and ERC721 is the pure OG NFT contract standard. ERC1155 is a more recent cousin which supports semi-fungible tokens and adds additional features such as 
batch transfers and transaction security.

On Algorand an NFT is instead represented as an individual ASA (Algorand Standard Asset). An ASA is not a smart contract but a separate first-class citizen on the Algorand blockchain. To be considered an NFT the ASA needs to comply with one of the current Algorand NFT-related ARCs. An ARC is the equivalent of an ERC on Ethereum. 

### Algorand ASA NFT ARCs

- **ARC3**
  - <https://github.com/algorandfoundation/ARCs/blob/main/ARCs/arc-0003.md>
  - NFT metadata focused standard.
  - The url field points to the NFT metadata. The metadata supports a schema which can have associated integrity and mimetype fields. 
  - Suitable for immutable NFTs with large metadata files (>1KB size of JSON) and multiple off-chain data references.

- **ARC19**
  - <https://github.com/algorandfoundation/ARCs/blob/main/ARCs/arc-0019.md>
  - NFT metadata focused standard.
  - Enforces off-chain IPFS metadata by using the url field as a template populated by the reserve address field which holds the IPFS CID. Easy to update since the reserve address value can be replaced with a single transaction, which in turn changes the metadata. The reserve address is only irrelevant (and thus can be used in this way) for pure NFTs (1 of 1).
  - Suitable for mutable NFTs intended to transition into immutable NFTs, with complete metadata (+mediafile) changes. 

- **ARC69**
  - <https://github.com/algorandfoundation/ARCs/blob/main/ARCs/arc-0069.md>
  - NFT mediafile focused standard.
  - The url field points to the NFT digital asset file and is immutable. The ASA metadata is stored on-chain and represented by the note field of the latest valid assetconfig transaction. Since the note field is limited to 1KB the metadata JSON is also restricted to this size. This design means fetching the metadata is complex and requires access to an archive node, but also allows metadata to be updated with a single transaction and simple access to the mediafile url.
  - Suitable for mutable NFTs where the mediafile is locked, easily accessed, but the compact metadata associated with it changes over time.

In common for all of these standards is that the four addresses of an ASA (manager, reserve, freeze and clawback) can be updated by the manager address unless it is set to "". 

There are also odd combos of the standards, example given below:

- **ARC69 + ARC19**
  - ARC69 with ARC19 url field encoding.
  - The url field points to the NFT digital asset file, which is made mutable using the ARC19 templating standard. The ASA metadata is stored on-chain and represented by the note field of the latest valid assetconfig transaction. Since the note field is limited to 1KB the metadata JSON is also restricted to this size. This design means fetching the metadata is complex and requires access to an archive node, but also allows metadata to be updated with a single transaction. The mediafile URL is no longer easily fetched (IPFS located) but is mutable. This combo results in a ARC69 SHOULD requirement failure but is still valid.
  - Suitable for mutable NFTs where the mediafile and the compact metadata associated with it changes over time.
  - Example NFT collection which uses this combo: R4V3N (https://algoxnft.com/asset/805179829)

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

### Algorand Smart Contract NFT ARCs

- **ARC72**
  - <https://github.com/algorandfoundation/ARCs/blob/main/ARCs/arc-0072.md> 
  - Suitable for NFTs which need full flexibility. 

### Why arctang?
- Name? Suggested by chatGPT as a project name including the word 'arc'
- Logo? Generated by Midjourney using 'arc' as one of the keywords
- Why? An excuse to extend [forestFISH](https://github.com/p00temkin/forestfish) capabilities beyond Ethereum and participate in a the Greenhouse Hackathon ;)

### User cases

First configure how you connect to the Algorand blockchain (node+indexer*). Defaults are available from the forestfish but use the NETCONFIG action to define custom values. Mainnet example below for node and indexer*:

   ```
	java -jar ./arctang.jar 
	--chain MAINNET 
	--action NETCONFIG 
	--nodeurl https://<your-custom-node>
	--nodeport 443 
	--nodeauthtoken_key "X-API-Key" 
	--nodeauthtoken <api-key>
	--idxurl <your-custom-indexer>
	--idxport 443
	--idxauthtoken_key "X-API-Key" 
	--idxauthtoken <api-key>
   ```

This stores the details in .avm/networks/[MAINNET|BETANET|TESTNET|VOI_TESTNET] and you no longer need to specify these parameters for every action, only the --chain option.

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

Note that this raw command works against any ASA type and highlights the differences between ARC3, ARC19 and ARC69. If you just want to identify the ARC type of an asset then you can use --probe_arcstandard as shown below:
   
   ```
	java -jar ./arctang.jar --chain MAINNET --action QUERY --assetid 387411719 --probe_arcstandard
	ASA identified as: ARC3
		
	java -jar ./arctang.jar --chain MAINNET --action QUERY --assetid 865610737 --probe_arcstandard
	ASA identified as: ARC19
	
	java -jar ./arctang.jar --chain MAINNET --action QUERY --assetid 490139078 --probe_arcstandard
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

For ARC3 ASAs we can verify the integrity of the NFT by checking that the metadata JSON is intact and that the integrity checksum fields of the metadata are inteact (ie the actual linked mediafiles). This can be achived with with VERIFY action which provides an ARC compliance summary along with overview of potential issues found:

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

### Query for ASA image url

A common usercase is to quickly display the image data of an NFT, which is supported using the --imageurl QUERY option:

- **ARC3 asset:** (fetches image url from metadata 'image' key)
   ```
   java -jar ./arctang.jar --chain MAINNET --action QUERY --assetid 387411719 --imageurl
   
   .. Updating list of active IPFS gateway URLs ..
   .. Nr of active IPFS gateways: 15
   .. Attempting to fetch ipfs://bafkreibvnaigebcrjwabx3u5xqyjbykaw64wtve6myv4yuarux64m3lvmm
   ipfs://bafkreibsgazs6waapitr4rvwsd75z5jgcxryiqacllrexszaoha2ph6voq
   ```
   
- **ARC19 asset**: (fetches image url from metadata 'image' key)
   ```
   java -jar ./arctang.jar --chain MAINNET --action QUERY --assetid 865610737 --imageurl
   
   .. Resolved cid from ARC19 template to: bafkreihxpwumraqrlafdxldjitba7gkvwh2vaos4z6uscbodopqnee6gpa
   .. Updating list of active IPFS gateway URLs ..
   .. Nr of active IPFS gateways: 14
   .. Attempting to fetch ipfs://bafkreihxpwumraqrlafdxldjitba7gkvwh2vaos4z6uscbodopqnee6gpa
   ipfs://bafybeidhlz7iznf5rpxwj5xfukppvkizxf4yp3cnpipjcmvbjkg7rwwwau
   ```
   
- **ARC69 asset**: (fetches image url from the on-chain 'url' key)
   ```
   java -jar ./arctang.jar --chain MAINNET --action QUERY --assetid 490139078 --imageurl
   https://gateway.pinata.cloud/ipfs/QmVxZFeLHtbrdtFabb46ToSvegpKyva1jzTkR61a8uM7qT
   ```

- **ARC69 asset with ARC19 style url**: (fetches image url from the on-chain 'url' key, resolves using reserve address)
   ```
   java -jar ./arctang.jar --chain MAINNET --action QUERY --assetid 805169021 --imageurl
   ipfs://QmXzA1ktmaVKiEj3FWdstH7G8L5h1TXxoDSv7a3AWX3pyx
   ```
   
The last example showcases a ARC69 ASA which uses ARC19 to dynamically update the image url using the reserve address.  

### Creating a wallet

In order to transfer, mint or reconfigure ASAs we need an Algorand account to work from. We can create a named wallet using the WALLETCONFIG action

   ```
	java -jar ./arctang.jar 
	--action WALLETCONFIG 
	--walletname bob
	--mnemonic "xxx xxx xxx .."
	
	.. Generated wallet from mnemonic with name bob with address ..
   ```

This creates a walletfile in your local .avm/wallets folder (with the walletname 'bob' in this case) which can be used for future actions which requires an on-chain action. If you dont specify the mnemonic argument a random wallet will be generated for you which you then need to fund somehow (transfer from other account or use a faucet). 

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

Transferring ARC ASA assets once the target address has opted in can be done using the action command as shown below:

   ```
	java -jar ./arctang.jar --chain TESTNET --action TRANSFER --walletname bob --assetid <assetid> --to <target_account_address>
	
	.. OPTIN status for account <target_account_address> for assetid <assetid>: true
	.. Wallet bob ASA ID <assetid> owned: 1
	.. Sending ASA to <target_account_address> from wallet bob
	.. ASA transfer completed with txhash: V3B4..
   ```
   
### Convert ERC-721/1155 Metadata to ARC

The NFT Metadata used in the ARC standards are heavily inspired by the Ethereum counterparts, and the arctang tool supports converting/upgrading to the ARC standards using the CONVERT action. The changes includes adding 'integrity' checksums (for ARC3) and moving from 'attributes' to 'properties' as defined in the ARC standards. 

To convert a collection on Ethereum to Algorand, download all metadata JSON files into a folder and specify the target folder using one of the ARC type specific target folder options. An example is shown below for the Ethereum [Bored Ape Yacht Club](https://boredapeyachtclub.com/) collection where the metadata files are placed in a local folder "boredapes_erc721". Although the Bored Ape metadata is very limited, the appropriate ARC3 image integrity field is added to the Algorand version (along with attributes -> properties conversion) as shown below: 

   ```
	java -jar ./arctang.jar --action CONVERT --from_erc_folder ./boredapes_erc721 --to_arc3_folder ./boredapes_arc3
	
	.. Updating list of active IPFS gateway URLs ..
    .. Nr of active IPFS gateways: 13
	------------------------ ERC721 metadata ------------------------
	{
	  "image": "ipfs://QmRRPWG96cmgTn2qSzjwr2qvfNEuhunv6FNeMFGa9bx6mQ",
	  "attributes": [
		{
		  "trait_type": "Earring",
		  "value": "Silver Hoop"
		},
		{
		  "trait_type": "Background",
		  "value": "Orange"
		},
		{
		  "trait_type": "Fur",
		  "value": "Robot"
		},
		{
		  "trait_type": "Clothes",
		  "value": "Striped Tee"
		},
		{
		  "trait_type": "Mouth",
		  "value": "Discomfort"
		},
		{
		  "trait_type": "Eyes",
		  "value": "X Eyes"
		}
	  ]
	}
	-----------------------------------------------------------------

    .. Attempting to fetch ipfs://QmRRPWG96cmgTn2qSzjwr2qvfNEuhunv6FNeMFGa9bx6mQ using https://c4rex.co/ipfs/
	
	--------------- ERC721 -> ARC3, metadata results ---------------
	{
	  "image": "ipfs://QmRRPWG96cmgTn2qSzjwr2qvfNEuhunv6FNeMFGa9bx6mQ",
	  "image_data_integrity": "sha256-5Fofb0zgg/BxhAkGa53OHHADxyxRToVSzzNRWsEDvOo=",
	  "properties": {
		"Fur": "Robot",
		"Eyes": "X Eyes",
		"Background": "Orange",
		"Mouth": "Discomfort",
		"Clothes": "Striped Tee",
		"Earring": "Silver Hoop"
	  }
	}
	----------------------------------------------------------------
    .. convert status: true
	
   ```

If we try with a more rich ERC-721 such as [Luchadores](https://luchadores.io) we can see how onchain image data is kept as well, here with ARC69 metadata output:

| ![alt text](https://github.com/p00temkin/arctang/blob/master/img/lucha_4044.png?raw=true) |
| :--: |

   ```
   java -jar ./arctang.jar --action CONVERT --from_erc_folder ./luchadores_erc721 --to_arc69_folder ./luchadores_arc69
   	
   .. Updating list of active IPFS gateway URLs ..
   .. Nr of active IPFS gateways: 14
	------------------------ ERC721 metadata ------------------------
	{
	  "name": "Luchador #4044",
	  "description": "Luchadores are randomly generated using Chainlink VRF and have 100% on-chain art and metadata - Only 10000 will ever exist!",
	  "image_data": "<svg id='luchador4044' ...</svg>",
	  "external_url": "https://luchadores.io/luchador/4044",
	  "attributes": [
		{
		  "trait_type": "Mask",
		  "value": "Dash"
		},
		{
		  "trait_type": "Boots",
		  "value": "High"
		},
		{
		  "trait_type": "Attributes",
		  "value": 2
		}
	  ]
	}
	-----------------------------------------------------------------
	--------------- ERC721 -> ARC69, metadata results ---------------
	{
	  "attributes": [
		{
		  "trait_type": "Mask",
		  "value": "Dash"
		},
		{
		  "trait_type": "Boots",
		  "value": "High"
		},
		{
		  "trait_type": "Attributes",
		  "value": 2
		}
	  ],
	  "description": "Luchadores are randomly generated using Chainlink VRF and have 100% on-chain art and metadata - Only 10000 will ever exist!",
	  "external_url": "https://luchadores.io/luchador/4044",
	  "image_data": "<svg id='luchador4044' ...</svg>",
	  "name": "Luchador #4044",
	  "properties": {
		"Boots": "High",
		"Mask": "Dash",
		"Attributes": 2
	  },
	  "standard": "arc69"
	}
	----------------------------------------------------------------
   .. convert status: true

   ```
   
Note that ARC19 does not have any specific requirements for the JSON metadata content (only how to find it) so there is no separate --to_arc19_folder option. 
   
### Mint an ASA Asset

In common for most NFTs on Algorand is the use of assetName and unitName, where unitName is restricted to 8 characters. When migrating Ethereum NFTs such as [Bored Ape Yacht Club](https://boredapeyachtclub.com/) can be difficult since it is missing a name in the metadata JSON. The arctang tool attempts to derive the assetname from the published IPFS metadata file, and then in turn generate a unit name from the assetname. If this doesnt work you can always override by using the --asset_name and --unit_name feature flags. Below is an example of minting from the boredapes_arc3 folder produced earlier, where the folder has been published to IPFS: 

   ```
   java -jar ./arctang.jar --walletname bob --chain TESTNET --action MINT --arcstandard ARC3 --metadata_cid QmXULvyXqRhhhMMtTHBSKpw3QzNRjLMMrn5wkpGpAehov4/0.json --asset_name "Bored Ape Yacht Club #0000"
   
   .. Updating list of active IPFS gateway URLs ..
   .. Nr of active IPFS gateways: 15
   .. Attempting to fetch ipfs://QmXULvyXqRhhhMMtTHBSKpw3QzNRjLMMrn5wkpGpAehov4/0.json
   .. Identified standard ARC3 matches the specified
   .. Using wallet with address P... for minting
   .. Using specified --asset_name value
   .. Generated the unitName BAYC0000 from the assetName
   .. result: txhash=ORQIZMM.... assetid=157.. confirmed=true

   ```

### Reconfigure an ASA Asset

This one turned out to be quite tricky. If your wallet is listed as "Manager address" you are able to update the four mutable addresses of the ASA: 

- Manager address
- Reserve address
- Freeze address
- Clawback address

Every reconfiguration made to the ASA asset needs to re-apply the previous values, otherwise they will be cleared and thus become immutable. If an ASA ARC was minted with all 4 addresses set, you need two transactions to purge all addresses from the ASA: First clear all addresses except the Manager address, then clear the Manager address but attempt to set any of the other 3 addresses at the same time. Clearing all of the 4 addresses at the same time does not leave the ASA in an immutable state, it instead destroys the ASA. Note that for ARC19 NFTs the Reserve address needs to be kept intact (used to resolve the IPFS CID of metadata file).

Arctang adds protection when performing any of these steps which can be seen by running the sequence below and checking the ASA state after each execution:

   ```
	java -jar ./arctang.jar --walletname bob --chain TESTNET --action RECONFIG --assetid <assetid> --clawback <new address>
	java -jar ./arctang.jar --walletname bob --chain TESTNET --action RECONFIG --assetid <assetid> --freeze <new address>
	java -jar ./arctang.jar --walletname bob --chain TESTNET --action RECONFIG --assetid <assetid> --reserve <new address>
	java -jar ./arctang.jar --walletname bob --chain TESTNET --action RECONFIG --assetid <assetid> --clearreserve
	java -jar ./arctang.jar --walletname bob --chain TESTNET --action RECONFIG --assetid <assetid> --clearfreeze
	java -jar ./arctang.jar --walletname bob --chain TESTNET --action RECONFIG --assetid <assetid> --clearclawback
	java -jar ./arctang.jar --walletname bob --chain TESTNET --action RECONFIG --assetid <assetid> --clearmanager
   ```
Lets create a 10/10 rated ARC3 on the Algorand network based on the [Luchadores](https://luchadores.io) ERC-721 we used as an example earlier.

1. First generate a new wallet named 'arctang':

   ```
	java -jar ./arctang.jar --action WALLETCONFIG --walletname arctang
	
	..  Generated wallet from mnemonic with name arctang with address D62ZQTP5UAY4AUDSUOT3U7KLHFMTESDTL6D4ZJ26LBMG4PJ2VWWC3YLTRQ
   ```

2. Fund the account using an Algorand faucet, such as https://testnet.algoexplorer.io/dispenser

3. We download the metadata file from [OpenSea](https://opensea.io/assets/ethereum/0x8b4616926705fb61e9c4eeac07cd946a5d4b0760/4044) into a folder named 'luchadores_erc721'. 

4. We then convert the metadata to ARC3 standard:

   ```
	java -jar ./arctang.jar --action CONVERT --from_erc_folder ./luchadores_erc721 --to_arc3_folder ./luchadores_arc3
	
	.. Updating list of active IPFS gateway URLs ..
	.. Nr of active IPFS gateways: 14
	..
	.. convertion successful: true

   ```

5. We then upload the 'luchadores_arc3' folder to IPFS and note the CID (Qmd95Cm5QAoDmtXRD7dD2PZox8EU19wfLXPVasm3fzNTYe), here using [Pinata](https://pinata.cloud):

| ![alt text](https://github.com/p00temkin/arctang/blob/master/img/lucha_pinata.png?raw=true) |
| :--: |

6. We then mint the ARC3 NFT using the IPFS CID as argument: 

   ```
	java -jar ./arctang.jar --walletname arctang --chain TESTNET --action MINT --arcstandard ARC3 --metadata_cid Qmd95Cm5QAoDmtXRD7dD2PZox8EU19wfLXPVasm3fzNTYe/4044.json
	
	.. Updating list of active IPFS gateway URLs ..
	.. Nr of active IPFS gateways: 15
	.. Attempting to fetch ipfs://Qmd95Cm5QAoDmtXRD7dD2PZox8EU19wfLXPVasm3fzNTYe/4044.json
	.. Identified standard ARC3 matches the specified
	.. Using wallet with address D62ZQTP5UAY4AUDSUOT3U7KLHFMTESDTL6D4ZJ26LBMG4PJ2VWWC3YLTRQ for minting
	.. Generated the unitName L_4044 from the assetName
	.. result: txhash=G7JC4VITOYBN7I3ISVJXMEFCHLQJTFJBPJZS6JDLKMR3YI5CPJIA assetid=157597369 confirmed=true
   ```

7. Now lets verify/rate our newly minted ARC3 NFT

   ```
	java -jar ./arctang.jar --chain TESTNET --action VERIFY --assetid 157597369
	
	.. Standard determined to be: ARC3
	.. Updating list of active IPFS gateway URLs ..
	.. Nr of active IPFS gateways: 16
	.. Getting metadata from assetURL ipfs://Qmd95Cm5QAoDmtXRD7dD2PZox8EU19wfLXPVasm3fzNTYe/4044.json#arc3
	.. Attempting to fetch ipfs://Qmd95Cm5QAoDmtXRD7dD2PZox8EU19wfLXPVasm3fzNTYe/4044.json
	.. Attempting to fetch https://luchadores.io/luchador/4044 using https://c4rex.co/ipfs/
	
	Verified     : true
	Score [0-10] : 6
	-----------------------------------
	Warnings:
	 [#1] Manager address is set, NFT is mutable (not yours)
	 [#2] Clawback address is set, ASA can be withdrawn (not yours)
	 [#3] Freeze address is set, ASA can be frozen (restricted)
	Verified parameters:
	 [+] asset URL endswith #arc3 and name is not fixed to arc3 or contains @arc3
	 [+] asset URL uses IPFS
	 [+] Calculated metadata hash matches the ASA specified hash (KAxdha9sHefU894BRE8g7obw5X1xqGAQesNnNNXNQA0=)
	 [+] Metadata name (Luchador #4044) related the ASA specified unit name (L_4044)
	 [+] Calculated external_url_integrity hash matches the metadata specified hash (W+ChYOUVZx/mJ8M3nIKqK8kOO59tqcGoXyEMf6NqdwE=)

   ```

The NFT looks ok but is left at a score of 6/10 since all the 4 mutable addresses are set. 

8. Make the NFT immutable 

   ```
	java -jar ./arctang.jar --walletname arctang --chain TESTNET --action RECONFIG --assetid 157597369 --force_immutable
	
	.. Using wallet with address D62ZQTP5UAY4AUDSUOT3U7KLHFMTESDTL6D4ZJ26LBMG4PJ2VWWC3YLTRQ for reconfig
	.. Instructed to force the ASA into an immutable state
	.. IMMUTABLE ASA action result: true
   ```

9. Verify that 10/10 rating ..

   ```
	java -jar ./arctang.jar --chain TESTNET --action VERIFY --assetid 157597369
	
	.. Standard determined to be: ARC3
	.. Updating list of active IPFS gateway URLs ..
	.. Nr of active IPFS gateways: 14
	.. Getting metadata from assetURL ipfs://Qmd95Cm5QAoDmtXRD7dD2PZox8EU19wfLXPVasm3fzNTYe/4044.json#arc3
	.. Attempting to fetch ipfs://Qmd95Cm5QAoDmtXRD7dD2PZox8EU19wfLXPVasm3fzNTYe/4044.json
	.. Attempting to fetch https://luchadores.io/luchador/4044 using https://c4rex.co/ipfs/
	
	Verified     : true
	Score [0-10] : 10
	-----------------------------------
	Verified parameters:
	 [+] ASA is immutable since manager address is blank or set to ""
	 [+] ASA cannot currently be withdrawn since clawback address is blank or set to ""
	 [+] ASA cannot currently be frozen since freeze address is blacnk or set to ""
	 [+] asset URL endswith #arc3 and name is not fixed to arc3 or contains @arc3
	 [+] asset URL uses IPFS
	 [+] Calculated metadata hash matches the ASA specified hash (KAxdha9sHefU894BRE8g7obw5X1xqGAQesNnNNXNQA0=)
	 [+] Metadata name (Luchador #4044) related the ASA specified unit name (L_4044)
	 [+] Calculated external_url_integrity hash matches the metadata specified hash (W+ChYOUVZx/mJ8M3nIKqK8kOO59tqcGoXyEMf6NqdwE=)
   ```
   
The asset can then be found on https://testnet.algoexplorer.io/asset/157597369

Note that for ARC69 the process is similar but you need to use --to_arc69_folder, --metadata_filepath and --mediadata_url instead while minting (and the mediadata_url should point to the actual mediafile, not the metadata JSON). Example below for re-minting a [MAYG](https://mayg.io/) NFT on Algorand:

| ![alt text](https://github.com/p00temkin/arctang/blob/master/img/mayg_2596.png?raw=true) |
| :--: |

   ```
	java -jar ./arctang.jar --action WALLETCONFIG --walletname may
	.. Generated wallet from mnemonic with name may with address 4SUROY77T4U5ZBNF2IOBWUZBOWPPFGZRWO26NZZXNIUMWAUETDUQ3GKZ5Q
	(use faucet)

	java -jar ./arctang.jar --action CONVERT --from_erc_folder ./mayg_erc721/ --to_arc69_folder ./mayg_arc69
	.. convert status: true

	java -jar ./arctang.jar --walletname may --chain TESTNET --action MINT --arcstandard ARC69 --mediadata_url https://mayg.mypinata.cloud/ipfs/QmQFSAQc99frsfAncQihDRSm4N1U1RGcX1F59Uin6AZqXW/2596.png --metadata_filepath ./mayg_arc69/2596.json
	.. Identified standard ARC69 matches the specified
	.. Using metadata name as assetName: MAYG #2596 Sygne Princess
	.. Generated the unitName M_2596SP from the assetName
	.. result: txhash=DNBFLJ2FXPMJOAI6PLTYRQS3DY7NTAXCHTOQWPTAFBPEI3QHHKMQ assetid=157615784 confirmed=true

	java -jar ./arctang.jar --walletname may --chain TESTNET --action RECONFIG --assetid 157615784 --force_immutable
	.. Using wallet with address 4SUROY77T4U5ZBNF2IOBWUZBOWPPFGZRWO26NZZXNIUMWAUETDUQ3GKZ5Q for reconfig
	.. Instructed to force the ASA into an immutable state
	.. IMMUTABLE ASA action result: true

	java -jar ./arctang.jar --chain TESTNET --action VERIFY --assetid 157615784
	.. Standard determined to be: ARC69
	.. Updating list of active IPFS gateway URLs ..

	Verified     : true
	Score [0-10] : 9
	-----------------------------------
	Warnings:
	 [#1] ARC69 ASA media URL uses https:// instead of IPFS
	Verified parameters:
	 [+] ASA is immutable since manager address is blank or set to ""
	 [+] ASA cannot currently be withdrawn since clawback address is blank or set to ""
	 [+] ASA cannot currently be frozen since freeze address is blank or set to ""
	 [+] ARC69 ASA media URL specifies media type using # fragment identifier
	 [+] Metadata name (MAYG #2596 Sygne Princess) related the ASA specified unit name (M_2596SP)
   ```

As noted we end up with an ARC69 with a near perfect score. We could have manually fixed the score by replacing the https IPFS Pinata URL with a pure ipfs:// URL in the mint action above.  

### Updating the Metadata JSON

In the previous example we could leave the Manager address intact and create a "metadata timeline". To do this we mint the asset just like before but now ship new versions of the metadata with the METADATAUPDATE action: 

   ```
   java -jar ./arctang.jar --walletname may --chain TESTNET --action MINT --arcstandard ARC69 --mediadata_url https://mayg.mypinata.cloud/ipfs/QmQFSAQc99frsfAncQihDRSm4N1U1RGcX1F59Uin6AZqXW/2596.png --metadata_filepath ./mayg_arc69/2596.json
   .. result: txhash=UB27LTOWOWOTI6DYJ7AC6G7PGL53KVDKLCFOU3OKYZF35MWY3QFA assetid=157743663 confirmed=true
   
   java -jar ./arctang.jar --walletname may --chain TESTNET --action METADATAUPDATE --assetid 157743663 --metadata_filepath ./mayg_arc69/2596_2.json
   .. Using wallet with address 4SUROY77T4U5ZBNF2IOBWUZBOWPPFGZRWO26NZZXNIUMWAUETDUQ3GKZ5Q for metadataupdate
   .. METADATAUPDATE txhash: QJONSTTITTIF2AMXHL6PF2VZHTPXAGWGWZULKCMIKNJV5TX37HYA
   
   java -jar ./arctang.jar --walletname may --chain TESTNET --action METADATAUPDATE --assetid 157743663 --metadata_filepath ./mayg_arc69/2596_3.json
   .. Using wallet with address 4SUROY77T4U5ZBNF2IOBWUZBOWPPFGZRWO26NZZXNIUMWAUETDUQ3GKZ5Q for metadataupdate
   .. METADATAUPDATE txhash: FITL3BQ6C37NKDHANYHLSRTISRZM6CX6BA7KQTH27F6XMUSMHYIQ
   
   java -jar ./arctang.jar --walletname may --chain TESTNET --action METADATAUPDATE --assetid 157743663 --metadata_filepath ./mayg_arc69/2596_4.json
   .. Using wallet with address 4SUROY77T4U5ZBNF2IOBWUZBOWPPFGZRWO26NZZXNIUMWAUETDUQ3GKZ5Q for metadataupdate
   .. METADATAUPDATE txhash: PRXGSUS5BAIHHS3ZVQFE3EDSHQM3GOVTN5EWX4Z4HIXTTUDD5ERA
   ```

Only difference in the above metadata versions is the added "sequence" property which changes with every update. A gamification usercase for this would be to boost various scores/traits in the metadata file of the NFT until the game season ends. When the season ends, all active NFTs are ranked and made immutable (plenty of game setups/variants to play around with). 

### Destroying an ASA Asset

As long as the manager address is still intact in the ASA, the entire NFT collection can be destroyed by sending a reconfigure transaction to the assetid with none of the 4 mutable addresses set. With arctang you can do this with the DESTROY action as shown below: 

   ```
   java -jar ./arctang.jar --chain TESTNET --walletname bob --action DESTROY --assetid <assetid>
   
   .. Using wallet with address PO.. to destroy asset
   .. Completed destroy action with txhash: F4UVVIDU4W..
   ```

### What ARCS are in your wallet?

To get a console print of ARC assets you can use the LIST action, either with the --walletname (for your arcs) or --address option (for any arcs)

   ```
   java -jar ./arctang.jar --chain TESTNET --action LIST --walletname bob
   java -jar ./arctang.jar --chain MAINNET --action LIST --address S3S5AHMEVU5YXIE56DS..
   
   .. <table of ARC ownership>
   ARC ASAs owned by S3S5AHMEVU5YXIE5...
   amount=1/8000                  unit-name=ALCH0046       standard=ARC69     url=https://gatew...
   ```
   
### Track dynamic Metadata JSON changes

Some projects combine the ARC69 and ARC19 standards to create mutable NFTs where the mediafile and the compact metadata associated with it changes over time. 

An example project which does this is [Project Raven](https://projectraven.world/), where wearable/gear updates are represented by metadata updates to the ASA. If we consider [ASA ID 805168778](https://www.nftexplorer.app/asset/805168778):

| ![alt text](https://github.com/p00temkin/arctang/blob/master/img/r4v3n.png?raw=true) |
| :--: |

To get the full metadata JSON history we can use the --metadata_trail query option to arctang:

   ```
	java -jar ./arctang.jar --chain MAINNET --action QUERY --assetid 805168778 --metadata_trail
	.. 
	======== txid=FDMLQJ5EI5UBU6CL7TGM2TYLX3X6SQD6SGXQVSJR7M6V4FI3SAAQ block=22214178 UTCtime=2022-07-14 17:34:12 ========
	{
	  "standard": "arc69",
	  "properties": {
	    "Background": "none",
	    "Back": "none",
	    "Mouth": "none",
	    "Eyes": "none",
	    "Facewear": "none",
	    "Basewear": "none",
	    "Outerwear": "none",
	    "Hair": "none"
	  }
	}
	======== txid=5F3HCB27HL5F6ICQ3IH3MWF3BGSLWCW6OXNNXCUPSQAHPZB4X63A block=22252947 UTCtime=2022-07-16 16:07:29 ========
	{
	  "standard": "arc69",
	  "properties": {
	    "Background": "RVT01#11 Dusty Pink Portal",
	    "Back": "RVT02#04 Compound Bow",
	    "Mouth": "RVT03#05 Tongue Out",
	    "Eyes": "RVT04#02 Blue",
	    "Facewear": "none",
	    "Basewear": "none",
	    "Outerwear": "none",
	    "Hair": "none"
	  }
	}
	..
   ```

This allows us to track all the changes to the metadata made by the manager account R4V3NI5QQSJNSYJZH63PJUN3O2HOVUX5N4OUO754H4STN472DES2MS5JSY using metadata reconfig updates using the ARC69 (+ARC19) standard. 

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
   mv target/arctang-0.0.1-SNAPSHOT-jar-with-dependencies.jar ./arctang.jar
   ```

### Usage

   ```
   java -jar ./arctang.jar 
   ```

Options:

   ```
   --chain				The Algorand chain: MAINNET, BETANET or TESTNET
   --action			Action to perform: QUERY, VERIFY, TRANSFER, MINT, WALLETCONFIG, NETCONFIG, OPTIN, CONVERT, DESTROY, RECONFIG, METADATAUPDATE
   --nodeurl			The Algorand custom network node URL
   --nodeport			The Algorand custom network node port
   --nodeauthtoken			The Algorand custom network node authtoken
   --nodeauthtoken_key		The Algorand custom network node authtoken keyname (defaults to X-Algo-API-Token)
   --idxurl			The Algorand custom network indexer URL
   --idxport			The Algorand custom network indexer port
   --idxauthtoken			The Algorand custom network indexer authtoken
   --idxauthtoken_key		The Algorand custom network indexer authtoken keyname (defaults to X-Algo-API-Token)
   --assetid			The ASA assetID
   --parsed			Parsed output format
   --raw				Raw output format
   --probe_arcstandard		Estimates ARC standard of assetid
   --debug				Debug mode
   --metadata			Grab the JSON metadata of ARC NFT with specified assetid
   --metadata_trail		Grab the JSON metadata update history for the specified ARC NFT
   --imageurl			Grab the image URL of the of ARC NFT with specified assetid
   --walletname			Wallet name to use for specified action
   --mnemonic			Mnemonic to use for creating an Algorand account. Use with --walletname
   --to				Target account address for asset TRANSFER action
   --from_erc_folder		Folder path to source ERC721 JSON metadata files to be converted to ARC
   --to_arc3_folder		Folder path to target ARC3 JSON metadata files
   --to_arc69_folder		Folder path to target ARC69 JSON metadata files
   --metadata_cid			IPFS CID of the metadata JSON file to be minted
   --mediadata_url			URL of the media data file to be minted
   --metadata_filepath		Filepath to metadata to be minted (ARC69)
   --arcstandard			ARC standard to use for minting: ARC3, ARC19 or ARC69
   --asset_name			Name of asset to be minted (can be excluded if metadata has name properties)
   --unit_name			Unit name of asset to be minted (can be exluded if metadata has name properties)
   --manager			The new manager address to be set with RECONFIG action
   --reserve			The new reserve address to be set with RECONFIG action
   --freeze			The new freeze address to be set with RECONFIG action
   --clawback			The new clawback address to be set with RECONFIG action
   --clearmanager			The new manager address to be set with RECONFIG action
   --clearreserve			The new reserve address to be set with RECONFIG action
   --clearfreeze			The new freeze address to be set with RECONFIG action
   --clearclawback			The new clawback address to be set with RECONFIG action
   --force_immutable		Force the specified ASA to be fully immutable
   --address			Can be used with LIST action to specify wallet you do not own
   ```

### Next steps
- Code cleanup and unit tests
- Add more granular feature flags and control
- Handle IPFS uploads during ARC minting
- Add support for ARC3 'extra_metadata'
- Extend ARC rating system with two different scores (mutable/immutable)
- Include nr of pins on IPFS in ARC asset rating
- Battletest across collections
- Support CONVERT action by directly communicating with ERC721 contracts to extract entire collections. 

### Random thoughts
 
- Since the ARC standards are new and allow for mutability it seems most creators enable this functionality. This is very different from Ethereum and overall tells the user 'this NFT isnt really yours'. If the NFT Manager wallet is compromised the entire collection can be destroyed with a single acfg command. Would likely be beneficial for the ecosystem to endorse immutability for top collections. Perhaps introduce a gamification aspect which transforms the NFT to an immutable state after some achievement. This would embrace the differences to Ethereum but still respect the ethos of immutable blockchain assets. 
- On Algorand the NFT ARC standards seems to be lacking the concept of 'collection' or 'club', ie the 'smart contract clubhouse' when compared to Ethereum. Would be interesting to explore if this would benefit the Algorand NFT ecosystem. 
- Another approach which might be possible to pursue is more complex dynamic NFTs, ie going the route of [RMRK](https://www.rmrk.app/) with EIP-6059 amd EIP-6220, or Aavegotchi with EIP-998. Not sure what can be supported on Algorand but the initial versions of RMRK seems to use techniques similar to ARC69. 

### Future projects

- Add support Algorand ARC NFT support to [forestFISHD](https://github.com/p00temkin/forestfishd) (token-gated content access)
- ARC NFT rating site/dapp?

### Additional useful options/resources

Official ARCs:
- <https://github.com/algorandfoundation/ARCs/blob/main/ARCs/arc-0003.md>
- <https://github.com/algorandfoundation/ARCs/blob/main/ARCs/arc-0018.md>
- <https://github.com/algorandfoundation/ARCs/blob/main/ARCs/arc-0019.md>
- <https://github.com/algorandfoundation/ARCs/blob/main/ARCs/arc-0020.md>
- <https://github.com/algorandfoundation/ARCs/blob/main/ARCs/arc-0069.md>

Official ARCs: (recent)
- <https://github.com/algorandfoundation/ARCs/blob/main/ARCs/arc-0072.md>
- <https://github.com/algorandfoundation/ARCs/blob/main/ARCs/arc-0200.md>

Algorand documentation:
- <https://developer.algorand.org/docs/get-details/asa/>
- <https://developer.algorand.org/docs/get-details/transactions/transactions/#asset-parameters>
- <https://developer.algorand.org/docs/rest-apis/indexer/#assetparams>

Algorand NFT galleries:
- <https://www.nftexplorer.app>
- <https://www.randgallery.com/algo-collection>
- <https://exa.market>
- <https://algoxnft.com/>
- <https://algogems.io>
- <https://zestbloom.com>
- <https://dartroom.xyz>

Ethereum EIPs directly referenced by ARC standards:
- <https://github.com/ethereum/EIPs/blob/master/EIPS/eip-721.md>
- <https://github.com/ethereum/EIPs/blob/master/EIPS/eip-1155.md>
- <https://docs.opensea.io/docs/metadata-standards>

Related Ethereum EIPs:
- <https://eips.ethereum.org/EIPS/eip-998>
- <https://eips.ethereum.org/EIPS/eip-6059>
- <https://eips.ethereum.org/EIPS/eip-6220>
- <https://eips.ethereum.org/EIPS/eip-777>

Misc:
- https://forum.algorand.org/t/distinctions-between-using-asa-and-application-for-tokens-nfts-on-algorand/10466
- <https://hex2algo.vercel.app>
- <https://nftfactory.org/blog/algorand-nft-assembly-line>
- <https://stackoverflow.com/questions/74052032/algorand-arc-19-and-arc-69-what-exaclty-is-the-difference>
- <https://www.techdreams.org/crypto-currency/algorand-arc3-and-arc69-standard-nfts-overview/12382-20220118>
- <https://gitcoin.co/hackathon/greenhouse3>
- <https://www.rmrk.app/>
- <https://www.aavegotchi.com/>

### BTW, is arctang really needed?

At the moment probably not. It was built with the intention to learn and be useful to others. The Algorand Java SDK pulls all the heavy lifting in this project and its rare to see a mature Java blockchain library. Development of this tool and improved Algorand support in [forestFISH](https://github.com/p00temkin/forestfish) will continue after this Hackathon.

### Support/Donate

To support this project directly:

   ```
   Ethereum/EVM: forestfish.x / 0x207d907768Df538F32f0F642a281416657692743
   Algorand: forestfish.x / 3LW6KZ5WZ22KAK4KV2G73H4HL2XBD3PD3Z5ZOSKFWGRWZDB5DTDCXE6NYU
   ```

Or please consider donating to EFF:
[Electronic Frontier Foundation](https://supporters.eff.org/donate)
