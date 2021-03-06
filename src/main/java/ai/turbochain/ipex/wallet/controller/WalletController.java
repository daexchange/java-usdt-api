package ai.turbochain.ipex.wallet.controller;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ai.turbochain.ipex.wallet.config.Constant;
import ai.turbochain.ipex.wallet.entity.Account;
import ai.turbochain.ipex.wallet.entity.Deposit;
import ai.turbochain.ipex.wallet.service.TransactionService;
import ai.turbochain.ipex.wallet.util.HttpRequest;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.osp.blockchain.btc.client.OspBlockExplorer;
import com.osp.blockchain.btc.model.BlockchainAddressResponse;
import com.osp.blockchain.http.HttpUtil;
import com.spark.blockchain.rpcclient.Bitcoin;
import com.spark.blockchain.rpcclient.BitcoinException;
import info.blockchain.api.blockexplorer.entity.Input;
import info.blockchain.api.blockexplorer.entity.Output;
import info.blockchain.api.blockexplorer.entity.Transaction;
import org.apache.commons.lang.StringUtils;
import org.bitcoinj.core.UTXO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;

import ai.turbochain.ipex.wallet.config.JsonrpcClient;
import ai.turbochain.ipex.wallet.entity.BtcBean;
import ai.turbochain.ipex.wallet.service.AccountService;
import ai.turbochain.ipex.wallet.util.BTCAccountGenerator;
import ai.turbochain.ipex.wallet.util.MessageResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(description = "usdt java api接口")
@RestController
@RequestMapping("/rpc")
public class WalletController {
	private Logger logger = LoggerFactory.getLogger(WalletController.class);

	@Autowired
	private JsonrpcClient jsonrpcClient;
	@Autowired
	private BTCAccountGenerator btcAccountGenerator;
	@Autowired
	private AccountService accountService;
	@Autowired
	private TransactionService transactionService;

	@PostMapping("/test")
	public MessageResult test(String address) {
		try {
//			List< UTXO > utxos = transactionService.getUnspent(fromAddress);
//			Long fee = transactionService.getOmniFee(utxos);
//			String signedHex = transactionService.omniSign(fromAddress, address, "", amount, fee, propertyid, utxos);
			return MessageResult.success();
		} catch (Exception e) {
			e.printStackTrace();
			return MessageResult.error(500, "error:" + e.getMessage());
		}
	}


	/**
	 * 获取USDT链高度
	 * 
	 * @return
	 */
	@ApiOperation(value = "获取USDT链高度", notes = "获取USDT链高度")
	@RequestMapping(value = "height", method = { RequestMethod.GET, RequestMethod.POST })
	public MessageResult getNetworkBlockHeight() {
		try {
			MessageResult result = new MessageResult(0, "success");
			String resultStr = HttpRequest.sendGetData(Constant.ACT_BLOCKNO_LATEST, "");
			if (StringUtils.isNotBlank(resultStr)) {
				JSONObject resultObj = JSONObject.parseObject(resultStr);
				if (null != resultObj) {
					result.setData(resultObj.getLong("data"));
				}
			}
//			result.setData(Long.valueOf(jsonrpcClient.getBlockCount()));
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			return MessageResult.error(500, "error:" + e.getMessage());
		}
	}

	/**
	 * 获取USDT区块高度为startBlockNumber的交易数据
	 * 
	 * @return
	 */
	@ApiOperation(value = "获取USDT链区块交易数据", notes = "获取USDT链区块交易数据")
	@RequestMapping(value = "block-txns/{blockNumber}", method = { RequestMethod.GET, RequestMethod.POST })
	public MessageResult blockTxns(@PathVariable Long blockNumber) {
		try {
			MessageResult result = new MessageResult(0, "success");
			String resultStr = HttpRequest.sendGetData(Constant.ACT_BLOCKNO_HEIGHT + blockNumber, "");
			if (StringUtils.isNotBlank(resultStr)) {
				JSONObject resultObj = JSONObject.parseObject(resultStr);
				if (null != resultObj) {
					JSONArray resultData = resultObj.getJSONArray("data");
					if (null != resultData && null != resultData.getJSONObject(0)) {
						JSONObject blockData = resultData.getJSONObject(0);
						JSONArray txArray = blockData.getJSONArray("txs");
						result.setData(txArray);
					}
				}
			}
//			result.setData(jsonrpcClient.omniListBlockTransactions(blockNumber));
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			return MessageResult.error(500, "error:" + e.getMessage());
		}
	}

	/**
	 * 根据交易编号获取交易详细数据
	 * 
	 * @return
	 */
	@ApiOperation(value = "根据交易编号获取交易详细数据", notes = "根据交易编号获取交易详细数据")
	@RequestMapping(value = "txninfo/{txid}", method = { RequestMethod.GET, RequestMethod.POST })
	public MessageResult txninfo(@PathVariable String txid) {
		try {
			MessageResult result = new MessageResult(0, "success");
			String resultStr = HttpRequest.sendGetData(Constant.ACT_TRANSACTION_HASH + txid, "");
			if (StringUtils.isNotBlank(resultStr)) {
				JSONObject resultObj = JSONObject.parseObject(resultStr);
				if (null != resultObj) {
					JSONObject resultData = resultObj.getJSONObject("data");
					result.setData(resultData);
				}
			}
//			Map<String, Object> map = jsonrpcClient.omniGetTransactions(txid);
//			result.setData(map);
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			return MessageResult.error(500, "error:" + e.getMessage());
		}
	}
	
	/**
	 * 创建钱包
	 * 
	 * @param account
	 * @return
	 */
	@ApiOperation(value = "获取账户地址", notes = "获取账户地址")
	@GetMapping("address/{account}")
	public MessageResult createWallet(@PathVariable String account,
			@RequestParam(required = false, defaultValue = "") String password) {
		logger.info("create new wallet:account={},password={}", account, password);
		try {
			BtcBean btcBean = btcAccountGenerator.createBtcAccount();
			accountService.saveBTCOne(account, btcBean.getFile(), btcBean.getBtcAddress(), "");
			MessageResult result = new MessageResult(0, "success");
			result.setData(btcBean.getBtcAddress());
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			return MessageResult.error(500, "查询失败， error: " + e.getMessage());
		}
	}

	/**
	 * 转账
	 * 
	 * @param fromAddress
	 * @param address
	 * @param amount
	 * @return
	 */
	@ApiOperation(value = "转账", notes = "转账")
	@RequestMapping(value = "transfer-from-address", method = { RequestMethod.GET, RequestMethod.POST })
	public MessageResult transferFromAddress(String username, String fromAddress, String address, BigDecimal amount) {
		logger.info("transfer:fromeAddress={},address={},amount={}", fromAddress, address, amount);
		try {
			if (fromAddress.equalsIgnoreCase(address)) {
				return MessageResult.error(500, "转入转出地址不能一样");
			}
			Account account = accountService.findByName(username);
			if (account == null) {
				return MessageResult.error(500, "请传入正确的用户名" + username);
			}
//			BigDecimal availAmt = jsonrpcClient.omniGetBalance(fromAddress);
//			 查询余额
			BigDecimal availAmt = new BigDecimal(0);
			String resultStr = HttpRequest.sendGetData(Constant.ACT_BLANCE_ADDRESS + fromAddress, "");
			if (StringUtils.isNotBlank(resultStr)) {
				JSONObject resultObj = JSONObject.parseObject(resultStr);
				if (null != resultObj &&  null != resultObj.getBigDecimal("data")) {
					availAmt = resultObj.getBigDecimal("data");
				} else {
					return MessageResult.error(500, "地址余额为空");
				}
			}
			if (availAmt.compareTo(amount) < 0) {
				return MessageResult.error(500, "余额不足不能转账");
			}
//			String txid = jsonrpcClient.omniSend(fromAddress, address, amount);
			//发送交易
			MessageResult result = transactionService.transferFromWallet(account ,fromAddress, address, amount);
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			return MessageResult.error(500, "error:" + e.getMessage());
		}
	}

	/**
	 * 获取账户地址余额
	 * 
	 * @param address
	 * @return
	 */
	@ApiOperation(value = "根据USDT地址获取余额", notes = "根据USDT地址获取余额")
	@RequestMapping(value = "balance/{address}", method = { RequestMethod.GET, RequestMethod.POST })
	public MessageResult balance(@PathVariable String address) {
		try {
			MessageResult result = new MessageResult(0, "success");
			String resultStr = HttpRequest.sendGetData(Constant.ACT_BLANCE_ADDRESS + address, "");
			if (StringUtils.isNotBlank(resultStr)) {
				JSONObject resultObj = JSONObject.parseObject(resultStr);
				if (null != resultObj) {
					BigDecimal balance = resultObj.getBigDecimal("data");
					result.setData(balance);
				}

			}
//			BigDecimal balance = jsonrpcClient.omniGetBalance(address);
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			return MessageResult.error(500, "error:" + e.getMessage());
		}
	}

}
