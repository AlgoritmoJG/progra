package com.progra.pos.util;


import java.math.BigDecimal;
import java.math.RoundingMode;
public final class Validacion {
    private Validacion() {}
    public static String texto(String valor, String campo, int maximo) {
        if (valor == null || valor.isBlank() || valor.strip().length() > maximo)
            throw new IllegalArgumentException(campo + " es obligatorio; máximo " + maximo + " caracteres.");
        return valor.strip();
    }
    public static int positivo(int valor, String campo) {
        if (valor <= 0) throw new IllegalArgumentException(campo + " debe ser un entero positivo.");
        return valor;
    }
    public static int noNegativo(int valor, String campo) {
        if (valor < 0) throw new IllegalArgumentException(campo + " no puede ser negativo.");
        return valor;
    }
    public static BigDecimal dinero(BigDecimal valor) {
        if (valor == null || valor.signum() <= 0 || valor.compareTo(new BigDecimal("9999999999.99")) > 0)
            throw new IllegalArgumentException("El precio debe ser mayor a cero y caber en DECIMAL(12,2).");
        try { return valor.setScale(2, RoundingMode.UNNECESSARY); }
        catch (ArithmeticException e) { throw new IllegalArgumentException("Use como máximo dos decimales."); }
    }
}
