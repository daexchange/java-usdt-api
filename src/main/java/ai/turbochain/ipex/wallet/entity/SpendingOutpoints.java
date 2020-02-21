package ai.turbochain.ipex.wallet.entity;

public class SpendingOutpoints {
    private Long tx_index;
    private Integer n;

    public Long getTx_index() {
        return tx_index;
    }

    public void setTx_index(Long tx_index) {
        this.tx_index = tx_index;
    }

    public Integer getN() {
        return n;
    }

    public void setN(Integer n) {
        this.n = n;
    }
}
