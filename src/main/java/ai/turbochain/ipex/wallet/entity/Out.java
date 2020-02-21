package ai.turbochain.ipex.wallet.entity;

import java.util.List;

public class Out {
    private Boolean spent;
    private List<SpendingOutpoints> spending_outpoints;
    private Long tx_index;
    private Integer type;
    private String addr;
    private Long value;
    private Integer n;
    private String script;

    public Boolean getSpent() {
        return spent;
    }

    public void setSpent(Boolean spent) {
        this.spent = spent;
    }

    public List<SpendingOutpoints> getSpending_outpoints() {
        return spending_outpoints;
    }

    public void setSpending_outpoints(List<SpendingOutpoints> spending_outpoints) {
        this.spending_outpoints = spending_outpoints;
    }

    public Long getTx_index() {
        return tx_index;
    }

    public void setTx_index(Long tx_index) {
        this.tx_index = tx_index;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getAddr() {
        return addr;
    }

    public void setAddr(String addr) {
        this.addr = addr;
    }

    public Long getValue() {
        return value;
    }

    public void setValue(Long value) {
        this.value = value;
    }

    public Integer getN() {
        return n;
    }

    public void setN(Integer n) {
        this.n = n;
    }

    public String getScript() {
        return script;
    }

    public void setScript(String script) {
        this.script = script;
    }
}
