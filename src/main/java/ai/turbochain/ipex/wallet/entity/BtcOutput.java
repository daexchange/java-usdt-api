package ai.turbochain.ipex.wallet.entity;

import info.blockchain.api.blockexplorer.entity.Output;

public class BtcOutput extends Output {


    public BtcOutput(int n, long value, String address, long txIndex, String script, boolean spent) {
        super(n, value, address, txIndex, script, spent);
    }

    public BtcOutput(Long value, String address) {
        super(0, value, address, 0, "", false);
    }
    public BtcOutput(String address) {
        super(0, 0, address, 0, "", false);
    }
}
