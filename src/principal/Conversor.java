package principal;

import java.util.Scanner;

public class Conversor {

    public void iniciar() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("\nBienvenido al Conversor de Monedas 💰");

        System.out.print("Ingresa la moneda base (por ejemplo: USD): ");
        String base = scanner.nextLine().trim().toUpperCase();

        System.out.print("Ingresa la moneda objetivo (por ejemplo: EUR): ");
        String destino = scanner.nextLine().trim().toUpperCase();

        System.out.print("Ingresa el monto a convertir: ");
        double monto = scanner.nextDouble();

        // Aquí más adelante llamaremos a la API para obtener la tasa de cambio
        System.out.println("\nProcesando conversión...");
        System.out.println("(Simulación inicial sin API)");

        double tasaFicticia = 0.92; // Valor temporal
        double resultado = monto * tasaFicticia;

        System.out.printf("%.2f %s equivalen a %.2f %s%n", monto, base, resultado, destino);
        scanner.close();
    }
}

