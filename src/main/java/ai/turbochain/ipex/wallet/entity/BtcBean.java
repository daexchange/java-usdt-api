package ai.turbochain.ipex.wallet.entity;

import java.util.List;

import lombok.Builder;

@Builder
public class BtcBean {
	private String btcAddress;
	private String btcPrivKey;
	private String btcPubKey;
	private String mnemonic;
	private List<String> mnemonicList;
	private String file;

	public String getBtcAddress() {
		return btcAddress;
	}

	public void setBtcAddress(String btcAddress) {
		this.btcAddress = btcAddress;
	}

	public String getBtcPrivKey() {
		return btcPrivKey;
	}

	public void setBtcPrivKey(String btcPrivKey) {
		this.btcPrivKey = btcPrivKey;
	}

	public String getBtcPubKey() {
		return btcPubKey;
	}

	public void setBtcPubKey(String btcPubKey) {
		this.btcPubKey = btcPubKey;
	}

	public String getMnemonic() {
		return mnemonic;
	}

	public void setMnemonic(String mnemonic) {
		this.mnemonic = mnemonic;
	}

	public List<String> getMnemonicList() {
		return mnemonicList;
	}

	public void setMnemonicList(List<String> mnemonicList) {
		this.mnemonicList = mnemonicList;
	}

	public String getFile() {
		return file;
	}

	public void setFile(String file) {
		this.file = file;
	}
}
