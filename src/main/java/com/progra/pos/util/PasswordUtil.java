package com.progra.pos.util;


import java.nio.charset.StandardCharsets;
import org.mindrot.jbcrypt.BCrypt;
public final class PasswordUtil {
    private PasswordUtil() {}
    public static String hash(String clave) {
        if (clave == null || clave.length() < 8 || clave.getBytes(StandardCharsets.UTF_8).length > 72)
            throw new IllegalArgumentException("La contraseña requiere al menos 8 caracteres y máximo 72 bytes UTF-8.");
        return BCrypt.hashpw(clave, BCrypt.gensalt(12));
    }
    public static boolean verificar(String clave, String hash) {
        if (clave == null || clave.getBytes(StandardCharsets.UTF_8).length > 72) return false;
        try { return BCrypt.checkpw(clave, hash); }
        catch (IllegalArgumentException e) { return false; }
    }
}
