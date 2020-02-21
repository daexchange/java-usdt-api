package ai.turbochain.ipex.wallet.entity;

import info.blockchain.api.blockexplorer.entity.Input;
import info.blockchain.api.blockexplorer.entity.Output;

public class BtcInput extends Input {

    public BtcInput(String address) {

        super(new BtcOutput(address), 0, "");
    }
    public BtcInput(Output previousOutput, long sequence, String scriptSignature) {
        super(previousOutput, sequence, scriptSignature);
    }
}
