package com.progra.pos.util;


public class AccesoDatosException extends RuntimeException {
    public AccesoDatosException(Throwable causa) {
        super("No se pudo completar la operación en MySQL. Revise conexión, duplicados y referencias.", causa);
    }
}
