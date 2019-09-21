package ai.turbochain.ipex.wallet.controller;

import java.math.BigDecimal;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import ai.turbochain.ipex.wallet.config.JsonrpcClient;
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

	/**
	 * 获取USDT链高度
	 * @return
	 */
	@ApiOperation(value = "获取USDT链高度", notes = "获取USDT链高度")
	@RequestMapping(value = "height", method = { RequestMethod.GET, RequestMethod.POST })
	public MessageResult getNetworkBlockHeight() {
		try {
			MessageResult result = new MessageResult(0, "success");
			result.setData(Long.valueOf(jsonrpcClient.getBlockCount()));
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			return MessageResult.error(500, "error:" + e.getMessage());
		}
	}
	
	/**
	 * 获取USDT区块高度为startBlockNumber的交易数据
	 * @return
	 */
	@ApiOperation(value = "获取USDT链区块交易数据", notes = "获取USDT链区块交易数据")
	@RequestMapping(value = "block-txns/{blockNumber}", method = { RequestMethod.GET, RequestMethod.POST })
	public MessageResult blockTxns(@PathVariable Long blockNumber) {
		try {
			MessageResult result = new MessageResult(0, "success");
			result.setData(jsonrpcClient.omniListBlockTransactions(blockNumber));
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			return MessageResult.error(500, "error:" + e.getMessage());
		}
	}
	
	/**
	 * 根据交易编号获取交易详细数据
	 * @return
	 */
	@ApiOperation(value = "根据交易编号获取交易详细数据", notes = "根据交易编号获取交易详细数据")
	@RequestMapping(value = "txninfo/{txid}", method = { RequestMethod.GET, RequestMethod.POST })
	public MessageResult txninfo(@PathVariable String txid) {
		try {
			MessageResult result = new MessageResult(0, "success");
			Map<String, Object> map = jsonrpcClient.omniGetTransactions(txid);
			result.setData(map);
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			return MessageResult.error(500, "error:" + e.getMessage());
		}
	}
	

	/**
	 * 获取账户地址
	 * @param account
	 * @return
	 */
	@ApiOperation(value = "获取账户地址", notes = "获取账户地址")
	@RequestMapping(value = "address/{account}", method = { RequestMethod.GET, RequestMethod.POST })
	public MessageResult getNewAddress(@PathVariable String account) {
		logger.info("create new address :" + account);
		try {
			String address = jsonrpcClient.getNewAddress(account);
			MessageResult result = new MessageResult(0, "success");
			result.setData(address);
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			return MessageResult.error(500, "error:" + e.getMessage());
		}
	}

	/**
	 * 转账 
	 * @param fromAddress
	 * @param address
	 * @param amount
	 * @param fee
	 * @return
	 */
	@ApiOperation(value = "转账", notes = "转账")
	@RequestMapping(value = "transfer-from-address", method = { RequestMethod.GET, RequestMethod.POST })
	public MessageResult transferFromAddress(String fromAddress, String address, BigDecimal amount, BigDecimal fee) {
		logger.info("transfer:fromeAddress={},address={},amount={},fee={}", fromAddress, address, amount, fee);
		try {
			BigDecimal transferedAmt = BigDecimal.ZERO;
			if (fromAddress.equalsIgnoreCase(address))
				return MessageResult.error(500, "转入转出地址不能一样");
			BigDecimal availAmt = jsonrpcClient.omniGetBalance(fromAddress);
			if (availAmt.compareTo(amount) > 0) {
				availAmt = amount;
			}
			String txid = jsonrpcClient.omniSend(fromAddress, address, amount);
			if (txid != null) {
				logger.info("fromAddress" + fromAddress + ",txid:" + txid);
				transferedAmt = transferedAmt.add(availAmt);
			}
			MessageResult result = new MessageResult(0, "success");
			result.setData(transferedAmt);
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			return MessageResult.error(500, "error:" + e.getMessage());
		}
	}

	/**
	 * 获取账户地址余额
	 * @param address
	 * @return
	 */
	@ApiOperation(value = "根据USDT地址获取余额", notes = "根据USDT地址获取余额")
	@RequestMapping(value = "balance/{address}", method = { RequestMethod.GET, RequestMethod.POST })
	public MessageResult balance(@PathVariable String address) {
		try {
			BigDecimal balance = jsonrpcClient.omniGetBalance(address);
			MessageResult result = new MessageResult(0, "success");
			result.setData(balance);
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			return MessageResult.error(500, "error:" + e.getMessage());
		}
	}

}
