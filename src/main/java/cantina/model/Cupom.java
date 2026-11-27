package cantina.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class Cupom {

    private final String codigo;
    private final int percentual;

    private Cupom(Builder builder) {
        this.codigo = builder.codigo.trim().toUpperCase();
        this.percentual = builder.percentual;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getCodigo() {
        return codigo;
    }

    public int getPercentual() {
        return percentual;
    }

    public BigDecimal desconto(BigDecimal valor) {
        return valor.multiply(BigDecimal.valueOf(percentual))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    @Override
    public String toString() {
        return codigo + " (-" + percentual + "%)";
    }

    public static class Builder {

        private String codigo;
        private int percentual;

        private Builder() {
        }

        public Builder codigo(String codigo) {
            this.codigo = codigo;
            return this;
        }

        public Builder percentual(int percentual) {
            this.percentual = percentual;
            return this;
        }

        public Cupom build() {
            if (codigo == null || codigo.isBlank()) {
                throw new IllegalStateException("O cupom precisa de um código.");
            }
            if (percentual < 1 || percentual > 100) {
                throw new IllegalStateException("O percentual do cupom deve ser entre 1 e 100.");
            }
            return new Cupom(this);
        }
    }
}
