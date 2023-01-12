## ARCTANG

Algorand NFT asset swiss army knife, using the Algorand Java SDK

![alt text](https://github.com/p00temkin/arctang/blob/master/img/arctangr3.png?raw=true)

### How it works

Command line tool to query/validate/update/mint/backup/integrate Algorand NFTs for various ARC standards on the Algorand blockchain. 

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

### Setup

TBD

### Usage

   ```
   java -jar ./arctang.jar 
   ```

Options:

   ```

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
