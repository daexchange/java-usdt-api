package ai.turbochain.ipex.wallet.controller;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ai.turbochain.ipex.wallet.config.Constant;
import ai.turbochain.ipex.wallet.entity.Deposit;
import ai.turbochain.ipex.wallet.util.HttpRequest;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.spark.blockchain.rpcclient.Bitcoin;
import com.spark.blockchain.rpcclient.BitcoinException;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
	// 比特币单位转换聪
	private BigDecimal bitcoin = new BigDecimal("100000000");
	@PostMapping("/test")
	public MessageResult test() {
		try {
			MessageResult result = new MessageResult(0, "success");
			return result;
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
			JSONObject resultObj = JSONObject.parseObject(resultStr);
			result.setData(resultObj.getLong("data"));
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
			JSONObject resultObj = JSONObject.parseObject(resultStr);
			result.setData(resultObj.getLong("data"));
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
			JSONObject resultObj = JSONObject.parseObject(resultStr);
			result.setData(resultObj.getLong("data"));
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
	public MessageResult transferFromAddress(String fromAddress, String address, BigDecimal amount) {
		logger.info("transfer:fromeAddress={},address={},amount={}", fromAddress, address, amount);
		try {
			if (fromAddress.equalsIgnoreCase(address))
				return MessageResult.error(500, "转入转出地址不能一样");
			BigDecimal availAmt = jsonrpcClient.omniGetBalance(fromAddress);
			if (availAmt.compareTo(amount) < 0) {
				return MessageResult.error(500, "余额不足不能转账");
			}
			String txid = jsonrpcClient.omniSend(fromAddress, address, amount);
			MessageResult result = new MessageResult(0, "success");
			result.setData(txid);
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
			String resultStr = HttpRequest.sendGetData(Constant.ACT_BLANCE_ADDRESS + address, "");
			JSONObject resultObj = JSONObject.parseObject(resultStr);
			BigDecimal balance = resultObj.getBigDecimal("data");
//			BigDecimal balance = jsonrpcClient.omniGetBalance(address);
			MessageResult result = new MessageResult(0, "success");
			result.setData(balance);
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			return MessageResult.error(500, "error:" + e.getMessage());
		}
	}

}
