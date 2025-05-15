package mp.tfg.mycheckpoint.service;

import mp.tfg.mycheckpoint.entity.User;

public interface EmailService {
    void sendVerificationEmail(User user, String token);
    // void sendPasswordResetEmail(User user, String token); // Para el futuro
    // void sendGenericEmail(String to, String subject, String body); // Para otras notificaciones
}
