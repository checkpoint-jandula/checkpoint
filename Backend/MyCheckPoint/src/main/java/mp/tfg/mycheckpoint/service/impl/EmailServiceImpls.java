package mp.tfg.mycheckpoint.service.impl;

import mp.tfg.mycheckpoint.entity.PasswordResetToken;
import mp.tfg.mycheckpoint.entity.User;
import mp.tfg.mycheckpoint.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage; // Correcto para texto plano
import org.springframework.mail.javamail.JavaMailSender;

import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpls implements EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailServiceImpls.class);

    private final JavaMailSender mailSender;

    @Value("${app.api.base-url:http://localhost:8080}")
    private String apiBaseUrl;

    @Value("${app.frontend.base-url:http://localhost:5173}") // Nueva propiedad para la URL base del frontend
    private String frontendBaseUrl;

    // Spring inyectará el username configurado en spring.mail.username aquí.
    // Este será el remitente ("De:") de tus correos.
    @Value("${spring.mail.username}")
    private String fromEmail;


    @Autowired
    public EmailServiceImpls(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void sendVerificationEmail(User user, String token) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail); // Establecer el remitente
            message.setTo(user.getEmail());
            message.setSubject("MyCheckPoint - Confirma tu dirección de correo electrónico");

            String confirmationUrl = apiBaseUrl + "/api/v1/auth/confirm-account?token=" + token;
            String emailBody = String.format( // Usar String.format para mejor legibilidad
                    "Hola %s,\n\n" +
                            "Gracias por registrarte en MyCheckPoint. Por favor, haz clic en el siguiente enlace " +
                            "o cópialo en tu navegador para confirmar tu dirección de correo electrónico:\n\n" +
                            "%s\n\n" +
                            "Si no te registraste, por favor ignora este mensaje.\n\n" +
                            "Saludos,\nEl equipo de MyCheckPoint",
                    user.getNombreUsuario(),
                    confirmationUrl
            );

            message.setText(emailBody);
            mailSender.send(message);
            logger.info("Correo de verificación enviado a {} desde {}", user.getEmail(), fromEmail);
        } catch (MailException e) {
            logger.error("Error al enviar correo de verificación a {} desde {}: {}", user.getEmail(), fromEmail, e.getMessage(), e);
            // Considera una estrategia de reintento o notificación si el envío es crítico
        }
    }

    public void sendPasswordResetEmail(User user, String token) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(user.getEmail());
            message.setSubject("MyCheckPoint - Restablecimiento de Contraseña");

            // Construye la URL que apuntará a tu página de frontend para resetear la contraseña
            // Esta página en el frontend leerá el token de la URL.
            String frontendResetUrl = frontendBaseUrl + "resetear-password?token=" + token;

            String emailBody = String.format(
                    "Hola %s,\n\n" +
                            "Hemos recibido una solicitud para restablecer la contraseña de tu cuenta en MyCheckPoint.\n" +
                            "Si no solicitaste esto, puedes ignorar este correo de forma segura.\n\n" +
                            "Para restablecer tu contraseña, por favor haz clic en el siguiente enlace o cópialo en tu navegador:\n" +
                            "%s\n\n" +
                            "Si no puedes hacer clic en el enlace, también puedes ir a la sección de restablecer contraseña de nuestra web e introducir manualmente el siguiente token:\n" +
                            "Token: %s\n\n" +
                            "Este token expirará en %d minutos.\n\n" +
                            "Saludos,\nEl equipo de MyCheckPoint",
                    user.getNombreUsuario(),
                    frontendResetUrl, // El enlace directo a tu frontend
                    token,            // El token por si necesitan copiarlo
                    PasswordResetToken.EXPIRATION_MINUTES
            );

            message.setText(emailBody);
            mailSender.send(message);
            logger.info("Correo de restablecimiento de contraseña enviado a {} desde {}", user.getEmail(), fromEmail);
        } catch (MailException e) {
            logger.error("Error al enviar correo de restablecimiento de contraseña a {} desde {}: {}", user.getEmail(), fromEmail, e.getMessage(), e);
        }
    }

}
