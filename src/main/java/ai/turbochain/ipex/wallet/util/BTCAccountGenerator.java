package ai.turbochain.ipex.wallet.util;

import java.io.File;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.LegacyAddress;
import org.bitcoinj.crypto.ChildNumber;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.HDKeyDerivation;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.wallet.DeterministicSeed;
import org.bitcoinj.wallet.Wallet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.web3j.utils.Numeric;

import com.google.common.base.Joiner;

import ai.turbochain.ipex.wallet.entity.BtcBean;

/**
 * 生成虚拟货币地址
 * 
 * @author
 * @date 2018年11月16日 下午2:44:17
 */
@Component
public class BTCAccountGenerator {

	@Value("${btc.keyStoreDirPath}")
	private String keyStoreDirPath;

	private static Logger logger = LoggerFactory.getLogger(BTCAccountGenerator.class);

	/**
	 * 生成随机的DeterministicSeed 可以同时得到助记词（16进制seed值）
	 */
	public static DeterministicSeed createRandomDeterministicSeed(String password) {
		DeterministicSeed deterministicSeed = new DeterministicSeed(new SecureRandom(), 128, password);
		return deterministicSeed;
	}

	/**
	 * 助记词数据变成String
	 *
	 * @param mnemonicList
	 * @return
	 */
	public static String mnemonic2String(List<String> mnemonicList) {
		return Joiner.on(" ").join(mnemonicList);
	}

	/**
	 * 使用助记词种子生成钱包
	 *
	 * @param ds
	 * @return
	 */
	public static ECKey generateECKey(DeterministicSeed ds) {
		DeterministicKey deterministicKey = HDKeyDerivation.createMasterPrivateKey(ds.getSeedBytes());
		String[] pathArray = "m/0'/0/0".split("/");
		for (int i = 1; i < pathArray.length; i++) {
			ChildNumber childNumber;
			if (pathArray[i].endsWith("'")) {
				int number = Integer.parseInt(pathArray[i].substring(0, pathArray[i].length() - 1));
				childNumber = new ChildNumber(number, true);
			} else {
				int number = Integer.parseInt(pathArray[i]);
				childNumber = new ChildNumber(number, false);
			}
			deterministicKey = HDKeyDerivation.deriveChildKey(deterministicKey, childNumber);
		}
		BigInteger privKeyBTC = deterministicKey.getPrivKey();
		ECKey ecKey = ECKey.fromPrivate(privKeyBTC);
		return ecKey;
	}

	/**
	 * 从助记词中获取 DeterministicSeed
	 *
	 * @param mnemonicList 助记词
	 * @param passphrase   密码 密码建议使用方法空字符，都这样使用
	 * @return
	 */
	public static DeterministicSeed getDeterministicSeed(List<String> mnemonicList, String passphrase) {
		if (passphrase == null) {
			passphrase = "";
		}
		long creationTimeSeconds = System.currentTimeMillis() / 1000;
		DeterministicSeed deterministicSeed = new DeterministicSeed(mnemonicList, null, passphrase,
				creationTimeSeconds);
		System.out.println("16进制seed值为：" + deterministicSeed.toHexString());
		return deterministicSeed;
	}

	/**
	 * 助记词String转换为 list
	 *
	 * @param mnemonics
	 * @return
	 */
	public static List<String> mnemonicString2List(String mnemonics) {
		return Arrays.asList(mnemonics.split(" "));
	}

	public BtcBean createBtcAccount() throws Exception {
		// 生成助记词
		DeterministicSeed deterministicSeed = BTCAccountGenerator.createRandomDeterministicSeed("");
		List<String> mnemonicList = deterministicSeed.getMnemonicCode();
		String mnemonic = BTCAccountGenerator.mnemonic2String(mnemonicList);
		// 生成eckey
		ECKey ecKey = BTCAccountGenerator.generateECKey(deterministicSeed);
		// 解析公钥
		String publickey = Numeric.toHexStringNoPrefixZeroPadded(new BigInteger(ecKey.getPubKey()), 66);
		// 解析私钥
		String privateKey = ecKey.getPrivateKeyEncoded(MainNetParams.get()).toString();
		// 解析地址
		// 正式网：MainNetParams.get()，测试网：TestNet3Params.get()
		Address myAddress = LegacyAddress.fromKey(MainNetParams.get(), ecKey);
		String btcAddress = myAddress.toString();
		// 保存文件
		Wallet wallet = Wallet.fromSeed(MainNetParams.get(), deterministicSeed);
		// 文件名
		SimpleDateFormat dateFormat = new SimpleDateFormat("'BTC--'yyyy-MM-dd'T'HH-mm-ss.SSS'--'");
		String fileName = dateFormat.format(new Date()) + btcAddress + ".wallet";
		File keyStoreFile = new File(keyStoreDirPath, fileName);
		wallet.saveToFile(keyStoreFile);

		logger.info("eckey 生成的btcAddress:" + btcAddress + " ，wallet 生成的btcAddress:" + wallet.currentReceiveAddress());

		BtcBean btcBean = BtcBean.builder().btcAddress(btcAddress).btcPrivKey(privateKey).btcPubKey(publickey)
				.mnemonic(mnemonic).mnemonicList(mnemonicList).file(fileName).build();
		return btcBean;
	}
	

}