package ai.turbochain.ipex.wallet.config;

/**
 * 
 * <p> TODO</p>
 * @author:         shangxl
 * @Date :          2018年3月7日 下午5:37:00
 */
public class Constant {
	/**
	 * zcash币编码
	 */
	public static final String ZCASH = "zec";
	/**
	 * tv编码
	 */
	public static final String TV = "tv";
	/**
	 * usdt编码 
	 */
	public static final String USDT = "USDT";
	/**
	 * usdt的token Id 正式网络usdt=31，测试网络可以用2
	 */
	public static final String PROPERTYID_USDT = "31";

	/**
	 * 精度
	 */
	public static final String DECIMALS = "_DECIMALS";
	/**
	 * neo
	 */
	public static final String NEO = "NEO";
	/**
	 * bds 
	 */
	public static final String BDS = "BDS";
	/**
	 * bts 
	 */
	public static final String BTS = "BTS";
	/**
	 * gxs
	 */
	public static final String GXS = "GXS";
	/**
	 * eth编码
	 */
	public static final String ETHER = "ETH";
	/**
	 * etc编码
	 */
	public static final String ETC = "ETC";
	/**
	 * etz编码
	 */
	public static final String ETZ = "ETZ";

	public static final String ACT_PREFIX = "http://www.tokenview.com:8088/";
	public static final String ACT_BLOCKNO_HEIGHT = ACT_PREFIX + "block/usdt/";
	public static final String ACT_BLOCKNO_LATEST = ACT_PREFIX + "coin/latest/usdt";
	public static final String ACT_BLANCE_ADDRESS = ACT_PREFIX + "addr/b/usdt/";
	public static final String ACT_TRANSACTION_HASH = ACT_PREFIX + "search/";
	public static final String ACT_CREATE_WALLET="https://tc.ipcom.io/btcwallet/api/v2/create";
	public static final String FORMAT_PARAM = "?format=json";
	public static final String PWD_PARAM = "?password=";
	public static final String APICODE_PARAM = "&api_code=818ac2ad-fd55-426a-93e3-bc861dc2061f";
	public static final String PRIV_PARAM = "&priv=";
	public static final String LABLE_PARAM = "&lable=";
	public static final String EMAIL_PARAM = "&email=";

}
