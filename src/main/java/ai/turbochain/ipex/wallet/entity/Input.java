package ai.turbochain.ipex.wallet.entity;

import jnr.ffi.annotations.In;

public class Input {
    private Long sequence;
    private String witness;
    private Out prev_out;
    private String script;

    public Long getSequence() {
        return sequence;
    }

    public void setSequence(Long sequence) {
        this.sequence = sequence;
    }

    public String getWitness() {
        return witness;
    }

    public void setWitness(String witness) {
        this.witness = witness;
    }

    public Out getPrev_out() {
        return prev_out;
    }

    public void setPrev_out(Out prev_out) {
        this.prev_out = prev_out;
    }

    public String getScript() {
        return script;
    }

    public void setScript(String script) {
        this.script = script;
    }
}
