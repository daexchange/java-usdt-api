package ai.turbochain.ipex.wallet.service;

import ai.turbochain.ipex.wallet.config.Constant;
import ai.turbochain.ipex.wallet.util.HttpRequest;
import ai.turbochain.ipex.wallet.util.MessageResult;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.osp.blockchain.btc.client.BtcClient;
import com.osp.blockchain.btc.client.BtcInterface;
import com.osp.blockchain.http.HttpUtil;
import org.apache.commons.lang3.StringUtils;
import org.bitcoinj.core.*;
import org.bitcoinj.crypto.TransactionSignature;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.DumpedPrivateKey;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionInput;
import org.bitcoinj.core.UTXO;
import org.bitcoinj.core.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class TransactionService {

	private static Logger logger = LoggerFactory.getLogger(TransactionService.class);

	private NetworkParameters params;

	private Boolean isMainNet = false;

	@Autowired
	private AccountService accountService;

	public TransactionService() {
		params = MainNetParams.get();
	}

	/**
	 * usdt 离线签名
	 *
	 * @param privateKey：私钥
	 * @param toAddress：接收地址
	 * @param amount:转账金额
	 * @return
	 */
	public String omniSign(String fromAddress, String toAddress, String privateKey, BigDecimal amount, Long fee,
						   Integer propertyid, List<UTXO> utxos) throws Exception {
		NetworkParameters networkParameters = isMainNet ? MainNetParams.get() : TestNet3Params.get();
		Transaction tran = new Transaction(networkParameters);
		if(utxos==null||utxos.size()==0){
			throw new Exception("utxo为空");
		}
		//这是比特币的限制最小转账金额，所以很多usdt转账会收到一笔0.00000546的btc
		Long miniBtc = 546L;
		tran.addOutput(Coin.valueOf(miniBtc),LegacyAddress.fromBase58(networkParameters, toAddress));

		//构建usdt的输出脚本 注意这里的金额是要乘10的8次方
		Long amoutTemp = amount.longValue();
		String usdtHex = "6a146f6d6e69" + String.format("%016x", propertyid) + String.format("%016x", amoutTemp);
		tran.addOutput(Coin.valueOf(0L), new Script(Utils.HEX.decode(usdtHex)));

		//如果有找零就添加找零
		String changeAddress = fromAddress;
		Long changeAmount = 0L;
		Long utxoAmount = 0L;
		List<UTXO> needUtxo = new ArrayList<>();
		for (UTXO utxo : utxos) {
			if (utxoAmount > (fee + miniBtc)) {
				break;
			} else {
				needUtxo.add(utxo);
				utxoAmount += utxo.getValue().value;
			}
		}
		changeAmount = utxoAmount - (fee + miniBtc);
		//余额判断
		if (changeAmount < 0) {
			throw new Exception("utxo余额不足");
		}
		if (changeAmount > 0) {
			tran.addOutput(Coin.valueOf(changeAmount), LegacyAddress.fromBase58(networkParameters, changeAddress));
		}

		//先添加未签名的输入，也就是utxo
		for (UTXO utxo : needUtxo) {
			tran.addInput(utxo.getHash(), utxo.getIndex(), utxo.getScript()).setSequenceNumber(TransactionInput.NO_SEQUENCE - 2);
		}

		//下面就是签名
		for (int i = 0; i < needUtxo.size(); i++) {
			ECKey ecKey = DumpedPrivateKey.fromBase58(networkParameters, privateKey).getKey();
			TransactionInput transactionInput = tran.getInput(i);
			Script scriptPubKey = ScriptBuilder.createOutputScript(LegacyAddress.fromBase58(networkParameters, fromAddress));
			Sha256Hash hash = tran.hashForSignature(i, scriptPubKey, Transaction.SigHash.ALL, false);
			ECKey.ECDSASignature ecSig = ecKey.sign(hash);
			TransactionSignature txSig = new TransactionSignature(ecSig, Transaction.SigHash.ALL, false);
			transactionInput.setScriptSig(ScriptBuilder.createInputScript(txSig, ecKey));
		}

		//这是签名之后的原始交易，直接去广播就行了
		String signedHex = Hex.toHexString(tran.bitcoinSerialize());
		//这是交易的hash
		String txHash = Hex.toHexString(Utils.reverseBytes(Sha256Hash.hash(Sha256Hash.hash(tran.bitcoinSerialize()))));
		logger.info("fee:{},utxoAmount:{},changeAmount:{}", fee, utxoAmount, changeAmount);
		return signedHex;
	}

	/**
	 * 获取矿工费用
	 * @param utxos
	 * @return
	 */
	public Long getOmniFee(List<UTXO> utxos) {
		Long miniBtc = 546L;
		Long feeRate = getFeeRate();
		Long utxoAmount = 0L;
		Long fee = 0L;
		Long utxoSize = 0L;
		for (UTXO output : utxos) {
			utxoSize++;
			if (utxoAmount > (fee + miniBtc)) {
				break;
			} else {
				utxoAmount += output.getValue().value;
				fee = (utxoSize * 148 * 34 * 3 + 10) * feeRate;
			}
		}
		return fee;
	}

	/***
	 * 获取未消费列表
	 * @param address ：地址
	 * @return
	 */
	public List<UTXO> getUnspent(String address) {
		List<UTXO> utxos = Lists.newArrayList();
		String host = this.isMainNet ? "blockchain.info" : "testnet.blockchain.info";
		String url = "https://" + host + "/zh-cn/unspent?active=" + address;
		try {
			String httpGet = HttpUtil.sendGet(url);//TODO;联网
			if (StringUtils.equals("No free outputs to spend", httpGet)) {
				return utxos;
			}
			JSONObject jsonObject = JSON.parseObject(httpGet);
			JSONArray unspentOutputs = jsonObject.getJSONArray("unspent_outputs");
			List<Map> outputs = JSONObject.parseArray(unspentOutputs.toJSONString(), Map.class);
			if (outputs == null || outputs.size() == 0) {
				System.out.println("交易异常，余额不足");
			}
			for (int i = 0; i < outputs.size(); i++) {
				Map outputsMap = outputs.get(i);
				String tx_hash = outputsMap.get("tx_hash").toString();
				String tx_hash_big_endian = outputsMap.get("tx_hash_big_endian").toString();
				String tx_index = outputsMap.get("tx_index").toString();
				String tx_output_n = outputsMap.get("tx_output_n").toString();
				String script = outputsMap.get("script").toString();
				String value = outputsMap.get("value").toString();
				String value_hex = outputsMap.get("value_hex").toString();
				String confirmations = outputsMap.get("confirmations").toString();
				UTXO utxo = new UTXO(Sha256Hash.wrap(tx_hash_big_endian), Long.valueOf(tx_output_n), Coin.valueOf(Long.valueOf(value)),
						0, false, new Script(Hex.decode(script)));
				utxos.add(utxo);
			}
			return utxos;
		} catch (Exception e) {
			logger.error("【BTC获取未消费列表】失败，", e);
			return null;
		}

	}

	/**
	 * 获取btc费率
	 *
	 * @return
	 */
	public Long getFeeRate() {
		try {
			String httpGet1 = HttpUtil.sendGet("https://bitcoinfees.earn.com/api/v1/fees/recommended");
			Map map = JSON.parseObject(httpGet1, Map.class);
			Long fastestFee = Long.valueOf(map.get("fastestFee").toString());
			return fastestFee;
		} catch (Exception e) {
			e.printStackTrace();
			return 0L;
		}
	}

	/**
	 * 转账
	 * @param fromAddress
	 * @param address
	 * @param amount
	 * @return
	 */
	public MessageResult transferFromWallet(String privateKey, String fromAddress, String address, BigDecimal amount) {
		Integer propertyid = 1;
		// 获取UTXO列表
		List< UTXO > utxos = this.getUnspent(fromAddress);
		// 获取手续费
		Long fee = this.getOmniFee(utxos);
		try {
			// 获取签名后的交易
			String signedHex = this.omniSign(fromAddress, address, privateKey,amount, fee, propertyid, utxos);
			// 广播交易
			String txHash = this.pushTx(signedHex);
			if (StringUtils.isNotBlank(txHash)) {
				// TODO 更新账户余额
				MessageResult result = new MessageResult(0, "success");
				result.setData(txHash);
				return result;
			} else {
				// 转账失败
				return MessageResult.error(500, "error:" + "转账失败");
			}
		} catch (Exception e) {
			e.printStackTrace();
			return MessageResult.error(500, "error:" + e.getMessage());
		}
	}

	private String pushTx(String signedHex) {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("jsonrpc","2.0");
		jsonObject.put("id", "viewtoken");
		jsonObject.put("method", "sendrawtransaction");
		jsonObject.put("params", signedHex);
		String resultStr = HttpRequest.sendPost(Constant.ACT_TRANSACTION_PUSHTX, jsonObject);
		if (StringUtils.isNotBlank(resultStr)) {
			JSONObject resultObj = JSONObject.parseObject(resultStr);
			if (null != resultObj && StringUtils.isNotBlank(resultObj.getString("result"))) {
				return resultObj.getString("result");
			}
		}
		return "";
	}
}
