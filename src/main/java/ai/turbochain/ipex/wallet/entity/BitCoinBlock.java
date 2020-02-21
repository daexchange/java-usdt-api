package ai.turbochain.ipex.wallet.entity;

import java.util.List;

public class BitCoinBlock {
    private List<BitCoin> blocks;

    public List<BitCoin> getBlocks() {
        return blocks;
    }

    public void setBlocks(List<BitCoin> blocks) {
        this.blocks = blocks;
    }
}
