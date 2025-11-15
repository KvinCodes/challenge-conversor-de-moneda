package principal;

public class Moneda {
    private String codigo;
    private double valor;

    public Moneda(String codigo, double valor) {
        this.codigo = codigo;
        this.valor = valor;
    }

    public String getCodigo() {
        return codigo;
    }

    public double getValor() {
        return valor;
    }
}
