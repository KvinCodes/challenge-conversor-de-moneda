package principal;

import java.util.*;

public class Conversor {
    private final ExchangeRateApiClient apiClient;
    private final SimpleCache cache;
    private final Scanner scanner;

    // Lista de pares rápidos: {base, target, etiqueta}
    private final List<String[]> quickPairs = Arrays.asList(
            new String[]{"USD", "EUR", "USD -> EUR"},
            new String[]{"USD", "GBP", "USD -> GBP"},
            new String[]{"USD", "JPY", "USD -> JPY"},
            new String[]{"USD", "MXN", "USD -> MXN"},
            new String[]{"USD", "COP", "USD -> COP"},
            new String[]{"USD", "CRC", "USD -> CRC"},
            new String[]{"EUR", "GBP", "EUR -> GBP"},
            new String[]{"EUR", "JPY", "EUR -> JPY"}
    );

    // Lista de monedas recomendadas para menús y filtrado
    private final List<String> monedasInteres = Arrays.asList(
            "USD", "EUR", "GBP", "JPY", "MXN", "CRC", "COP", "BRL", "ARS", "CLP"
    );

    public Conversor() {
        String key = ExchangeRateApiClient.loadApiKeyOrNull();
        if (key == null || key.isBlank()) {
            System.err.println("⚠️  API Key no encontrada. Configure EXR_API_KEY (env) o config/config.properties");
            this.apiClient = null;
        } else {
            this.apiClient = new ExchangeRateApiClient(key);
        }
        this.cache = new SimpleCache(3600); // cache 1 hora
        this.scanner = new Scanner(System.in);
    }

    // Punto de entrada del conversor
    public void iniciar() {
        System.out.println("\n=== CONVERSOR DE MONEDAS (MENÚ AVANZADO) ===");
        boolean salir = false;

        while (!salir) {
            mostrarMenuPrincipal();
            int opcion = leerEntero("Selecciona una opción");

            switch (opcion) {
                case 0 -> {
                    System.out.println("Saliendo. ¡Hasta luego!");
                    salir = true;
                }
                case 1 -> mostrarParesRapidosMenu();
                case 2 -> procesarParPersonalizado();
                case 3 -> procesoInteractivoOrigenDestino();
                case 4 -> mostrarMonedasFiltradas();
                case 5 -> listarMonedasInteres();
                default -> System.out.println("Opción inválida. Intenta nuevamente.");
            }
        }
    }

    private void mostrarMenuPrincipal() {
        System.out.println("\n------ Menú principal ------");
        System.out.println("1) Conversión rápida (pares predefinidos)");
        System.out.println("2) Par personalizado (ingresar base y destino)");
        System.out.println("3) Conversión interactiva (elige origen y destino desde menú)");
        System.out.println("4) Ver tasas filtradas desde /latest");
        System.out.println("5) Listar monedas recomendadas");
        System.out.println("0) Salir");
        System.out.println("----------------------------");
    }

    // Menú para pares rápidos
    private void mostrarParesRapidosMenu() {
        System.out.println("\n--- Pares rápidos ---");
        for (int i = 0; i < quickPairs.size(); i++) {
            System.out.printf("%2d) %s%n", i + 1, quickPairs.get(i)[2]);
        }
        System.out.println(" 0) Volver");
        int sel = leerEntero("Elige opción (0 para volver)");
        if (sel == 0) return;
        if (sel >= 1 && sel <= quickPairs.size()) {
            String[] par = quickPairs.get(sel - 1);
            procesarConversion(par[0], par[1], par[2]);
        } else {
            System.out.println("Selección inválida.");
        }
    }

    // Proceso interactivo: elegir origen y destino desde una lista o ingresar código
    private void procesoInteractivoOrigenDestino() {
        System.out.println("\n--- Conversión interactiva ---");
        String origen = seleccionarMoneda("Seleccione moneda origen (o '0' para volver):", null);
        if (origen == null) return; // usuario eligió volver
        String destino = seleccionarMoneda("Seleccione moneda destino (no puede ser igual a origen):", origen);
        if (destino == null) return;

        String etiqueta = origen + " -> " + destino;
        procesarConversion(origen, destino, etiqueta);
    }

    // Muestra el menú de selección de moneda y permite elegir de monedasInteres o ingresar manual
    private String seleccionarMoneda(String prompt, String exclude) {
        while (true) {
            System.out.println("\n" + prompt);
            for (int i = 0; i < monedasInteres.size(); i++) {
                String code = monedasInteres.get(i);
                if (exclude != null && exclude.equalsIgnoreCase(code)) continue;
                System.out.printf("%2d) %s%n", i + 1, code);
            }
            System.out.println(" 0) Ingresar otro código manualmente");
            System.out.println("-1) Volver");

            int sel = leerEntero("Elige opción (número) ");
            if (sel == -1) return null; // volver
            if (sel == 0) {
                System.out.print("Ingresa el código de la moneda (ej: USD): ");
                String manual = scanner.nextLine().trim().toUpperCase();
                if (manual.isBlank() || (exclude != null && manual.equalsIgnoreCase(exclude))) {
                    System.out.println("Código inválido o igual al excluido. Intenta nuevamente.");
                    continue;
                }
                return manual;
            } else {
                // mapear índice a moneda (considerar que excluidos cambian índices visuales)
                int idx = 1;
                for (String code : monedasInteres) {
                    if (exclude != null && exclude.equalsIgnoreCase(code)) continue;
                    if (idx == sel) return code;
                    idx++;
                }
                System.out.println("Selección no válida. Intenta de nuevo.");
            }
        }
    }

    // Procesa un par personalizado simple (sin submenús)
    private void procesarParPersonalizado() {
        System.out.print("Ingresa moneda base (ej: USD): ");
        String base = scanner.nextLine().trim().toUpperCase();
        if (base.isBlank()) {
            System.out.println("Código inválido.");
            return;
        }
        System.out.print("Ingresa moneda destino (ej: EUR): ");
        String destino = scanner.nextLine().trim().toUpperCase();
        if (destino.isBlank()) {
            System.out.println("Código inválido.");
            return;
        }
        procesarConversion(base, destino, base + " -> " + destino);
    }

    // Método principal que muestra sub-opciones y ejecuta la conversión
    private void procesarConversion(String base, String destino, String etiqueta) {
        System.out.println("\nHas elegido: " + etiqueta);
        System.out.println("Opciones:");
        System.out.println(" 1) Convertir " + base + " -> " + destino);
        System.out.println(" 2) Convertir inverso (" + destino + " -> " + base + ")");
        System.out.println(" 3) Volver");
        int opt = leerEntero("Elige 1, 2 ó 3");

        if (opt == 3) return;

        double monto;
        try {
            monto = leerDouble("Ingresa el monto a convertir (ej: 100.50)");
        } catch (IllegalArgumentException e) {
            System.err.println("Monto inválido. Abortando esta operación.");
            return;
        }

        try {
            if (opt == 1) {
                Double rate = obtenerTasaConCache(base, destino);
                if (rate == null) {
                    System.err.println("No se pudo obtener la tasa para " + base + " -> " + destino);
                    return;
                }
                double resultado = convertir(monto, rate);
                System.out.printf("%n%s %s = %s %s (tasa=%.6f)%n",
                        formatear(monto), base, formatear(resultado), destino, rate);
            } else if (opt == 2) {
                // intentar tasa directa destino->base
                Double rateDirect = obtenerTasaConCache(destino, base);
                if (rateDirect != null) {
                    double resultado = convertir(monto, rateDirect);
                    System.out.printf("%n%s %s = %s %s (tasa directa %s->%s=%.6f)%n",
                            formatear(monto), destino, formatear(resultado), base, destino, base, rateDirect);
                } else {
                    Double rate = obtenerTasaConCache(base, destino);
                    if (rate == null || rate == 0.0) {
                        System.err.println("No se pudo obtener la tasa para calcular la inversa.");
                        return;
                    }
                    double resultado = convertirInverso(monto, rate);
                    double inverse = 1.0 / rate;
                    System.out.printf("%n%s %s = %s %s (tasa inversa=%.6f)%n",
                            formatear(monto), destino, formatear(resultado), base, inverse);
                }
            } else {
                System.out.println("Opción no válida en este submenú.");
            }
        } catch (Exception ex) {
            System.err.println("Error durante la conversión: " + ex.getMessage());
        }
    }

    /**
     * Obtiene la tasa consultando primero la cache y si no busca en la API.
     */
    private Double obtenerTasaConCache(String base, String destino) {
        // 1) intentar cache
        Double rate = cache.get(base, destino);
        if (rate != null) {
            System.out.println("(Usando cache para " + base + "->" + destino + ")");
            return rate;
        }

        // 2) intentar API si existe
        if (apiClient == null) {
            System.err.println("API client no configurado.");
            return null;
        }

        try {
            Optional<Double> opt = apiClient.fetchPairRate(base, destino);
            if (opt.isPresent()) {
                rate = opt.get();
                cache.put(base, destino, rate);
                return rate;
            } else {
                return null;
            }
        } catch (Exception e) {
            System.err.println("Error al obtener tasa desde API: " + e.getMessage());
            return null;
        }
    }

    // ====================== Métodos utilitarios de conversión y formato ======================

    private double convertir(double cantidad, double tasa) {
        return cantidad * tasa;
    }

    private double convertirInverso(double cantidad, double tasa) {
        if (tasa == 0.0) throw new IllegalArgumentException("Tasa no puede ser cero para cálculo inverso.");
        return cantidad * (1.0 / tasa);
    }

    private String formatear(double valor) {
        java.text.DecimalFormat df = (java.text.DecimalFormat) java.text.NumberFormat.getNumberInstance(Locale.US);
        df.applyPattern("#,##0.00");
        return df.format(valor);
    }

    // ====================== Mostrar tasas filtradas (paso 8) ======================
    private void mostrarMonedasFiltradas() {
        if (apiClient == null) {
            System.out.println("API Key no configurada.");
            return;
        }

        System.out.print("Ingresa moneda base (ej: USD): ");
        String base = scanner.nextLine().trim().toUpperCase();
        if (base.isBlank()) {
            System.out.println("Código inválido.");
            return;
        }

        Optional<Map<String, Double>> optRates = apiClient.fetchLatestRates(base);

        if (optRates.isEmpty()) {
            System.out.println("No se pudieron obtener las tasas.");
            return;
        }

        Map<String, Double> rates = optRates.get();

        System.out.println("\n=== Monedas filtradas para base " + base + " ===");
        for (String moneda : monedasInteres) {
            if (rates.containsKey(moneda)) {
                System.out.printf("%s: %.6f%n", moneda, rates.get(moneda));
            } else {
                System.out.printf("%s: (no disponible)%n", moneda);
            }
        }
        System.out.println("============================================\n");
    }

    // Muestra la lista de monedas recomendadas para ayudar al usuario
    private void listarMonedasInteres() {
        System.out.println("\nMonedas recomendadas:");
        for (String m : monedasInteres) {
            System.out.println("- " + m);
        }
    }

    // ====================== Lectura segura de entradas ======================
    private int leerEntero(String prompt) {
        System.out.print(prompt + ": ");
        try {
            String line = scanner.nextLine().trim();
            return Integer.parseInt(line);
        } catch (Exception e) {
            return -1;
        }
    }

    private double leerDouble(String prompt) {
        System.out.print(prompt + ": ");
        try {
            String line = scanner.nextLine().trim();
            return Double.parseDouble(line);
        } catch (Exception e) {
            throw new IllegalArgumentException("Número inválido");
        }
    }
}
