package principal;

import java.text.DecimalFormat;

public class Utilidades {

    /* Método para redondear o formatear montos */
    public static String formatear(double valor) {
        DecimalFormat df = new DecimalFormat("#,##0.00");
        return df.format(valor);
    }
}
