package ai.turbochain.ipex.wallet.component;

import ai.turbochain.ipex.wallet.config.Constant;
import ai.turbochain.ipex.wallet.config.JsonrpcClient;
import ai.turbochain.ipex.wallet.entity.Coin;
import ai.turbochain.ipex.wallet.entity.Deposit;
import ai.turbochain.ipex.wallet.entity.WatcherLog;
import ai.turbochain.ipex.wallet.event.DepositEvent;
import ai.turbochain.ipex.wallet.service.AccountService;
import ai.turbochain.ipex.wallet.service.WatcherLogService;
import ai.turbochain.ipex.wallet.util.HttpRequest;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.spark.blockchain.rpcclient.Bitcoin;
import com.spark.blockchain.rpcclient.BitcoinRPCClient;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
public class UsdtWatcher extends Watcher {
	private Logger logger = LoggerFactory.getLogger(UsdtWatcher.class);
	@Autowired
	private AccountService accountService;
	@Autowired
	private WatcherLogService watcherLogService;
	@Autowired
	private DepositEvent depositEvent;
	@Autowired
	private Coin coin;
	// 比特币单位转换聪
	private BigDecimal bitcoin = new BigDecimal("100000000");
	// 默认同步间隔3分钟
	private Long checkInterval = 180000L;
	private boolean stop = false;
	// 区块确认数
	private int confirmation = 1;
	private Long currentBlockHeight = 0L;
	private int step = 1;



	@Override
	public List<Deposit> replayBlock(Long startBlockNumber, Long endBlockNumber) {
		List<Deposit> deposits = new ArrayList<Deposit>();
		try {
			for (Long blockHeight = startBlockNumber; blockHeight <= endBlockNumber; blockHeight++) {
				String blockHeightData = HttpRequest
						.sendGetData(Constant.ACT_BLOCKNO_HEIGHT + blockHeight, "");
				JSONObject jsonObject = JSONObject.parseObject(blockHeightData);
				JSONArray blocksArray = jsonObject.getJSONArray("data");
				String blockHash = blocksArray.getJSONObject(0).getString("blockhash");
				Long height = blocksArray.getJSONObject(0).getLong("block_no");
				JSONArray txList = blocksArray.getJSONObject(0).getJSONArray("txs");
				for (int i = 0; i < txList.size(); i++) {
					JSONObject txObj = txList.getJSONObject(i);
					String txHash = txObj.getString("txid");
					JSONArray outArray = txObj.getJSONArray("outputs");
					for (int j = 0; j < outArray.size(); j++) {
						JSONObject out = outArray.getJSONObject(j);
						String address = out.getString("address");
						if (StringUtils.isNotBlank(address) && accountService.isAddressExist(address)) {
							BigDecimal amount = out.getBigDecimal("value").divide(bitcoin).setScale(8,
									BigDecimal.ROUND_DOWN);
							Deposit deposit = new Deposit();
							deposit.setTxid(txHash);
							deposit.setBlockHeight(height);
							deposit.setBlockHash(blockHash);
							deposit.setAddress(address);
							deposit.setAmount(amount);
							deposit.setTime(txObj.getDate("time"));
							deposits.add(deposit);
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return deposits;
	}

	@Override
	public Long getNetworkBlockHeight() {
		try {
			// 获取最新块的块高
			String result = HttpRequest.sendGetData(Constant.ACT_BLOCKNO_LATEST, "");
			JSONObject resultObj = JSONObject.parseObject(result);
			return resultObj.getLong("data");
		} catch (Exception e) {
			e.printStackTrace();
			return 0L;
		}
	}

	@Override
	public void run() {
		stop = false;
		long nextCheck = 0;
		while (!(Thread.interrupted() || stop)) {
			if (nextCheck <= System.currentTimeMillis()) {
				try {
					nextCheck = System.currentTimeMillis() + checkInterval;
					logger.info("check...");
					check();
				} catch (Exception ex) {
					logger.info(ex.getMessage());
				}
			} else {
				try {
					Thread.sleep(Math.max(nextCheck - System.currentTimeMillis(), 100));
				} catch (InterruptedException ex) {
					logger.info(ex.getMessage());
				}
			}
		}
	}

	public void check() {
		try {
			Long networkBlockNumber = getNetworkBlockHeight() - confirmation + 1;
			Thread.sleep(90000);
			if (currentBlockHeight < networkBlockNumber) {
				long startBlockNumber = currentBlockHeight + 1;
				currentBlockHeight = (networkBlockNumber - currentBlockHeight > step) ? currentBlockHeight + step
						: networkBlockNumber;
				logger.info("replay block from {} to {}", startBlockNumber, currentBlockHeight);
				List<Deposit> deposits = replayBlock(startBlockNumber, currentBlockHeight);
				deposits.forEach(deposit -> {
					depositEvent.onConfirmed(deposit);
				});
				// 记录日志
				watcherLogService.update(coin.getName(), currentBlockHeight);
			} else {
				logger.info("already latest height {},nothing to do!", currentBlockHeight);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
