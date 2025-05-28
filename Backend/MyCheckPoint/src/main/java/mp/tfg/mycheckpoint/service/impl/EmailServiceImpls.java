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
// import org.springframework.mail.javamail.MimeMessageHelper; // Para emails HTML
// import jakarta.mail.MessagingException; // Si usas MimeMessageHelper
// import jakarta.mail.internet.MimeMessage; // Si usas MimeMessageHelper
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpls implements EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailServiceImpls.class);

    private final JavaMailSender mailSender;

    @Value("${app.api.base-url:http://localhost:8080}")
    private String apiBaseUrl;

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

            // IMPORTANTE: Sin frontend, el enlace apuntará a una página/endpoint donde el usuario
            // pueda introducir el token y su nueva contraseña.
            // O, si quieres simplificar al máximo, el enlace podría llevar el token
            // y el frontend luego haría una petición POST con ese token y la nueva contraseña.
            // Por ahora, el enlace solo informa. Un frontend construiría la URL de reseteo.
            // Si tuvieras un frontend en http://localhost:3000/reset-password?token=TOKEN_AQUI
            // String resetUrl = "http://localhost:3000/reset-password?token=" + token;

            // Dado que no hay frontend, el email podría instruir al usuario a usar
            // una herramienta como Postman con el token, o simplemente informar que se ha generado un token.
            // Para una implementación real, el enlace llevaría a una página del frontend.
            // Aquí simulamos un enlace que el usuario podría usar en un futuro frontend.
            String hypotheticalFrontendResetUrl = apiBaseUrl.replace(":8080", ":3000") + "/reset-password?token=" + token; // Asumiendo frontend en puerto 3000

            String emailBody = String.format(
                    "Hola %s,\n\n" +
                            "Hemos recibido una solicitud para restablecer la contraseña de tu cuenta en MyCheckPoint.\n" +
                            "Si no solicitaste esto, puedes ignorar este correo de forma segura.\n\n" +
                            "Para restablecer tu contraseña, necesitarás el siguiente token y utilizarlo en la funcionalidad de reseteo de contraseña:\n" +
                            "Token: %s\n\n" +
                            "O, si tuvieras una interfaz frontend, podrías hacer clic aquí (este enlace es hipotético por ahora):\n" +
                            "%s\n\n" +
                            "Este token expirará en %d minutos.\n\n" +
                            "Saludos,\nEl equipo de MyCheckPoint",
                    user.getNombreUsuario(),
                    token, // Mostrar el token directamente en el correo ya que no hay UI
                    hypotheticalFrontendResetUrl,
                    PasswordResetToken.EXPIRATION_MINUTES // Asumiendo que EXPIRATION_MINUTES es public static final
            );

            message.setText(emailBody);
            mailSender.send(message);
            logger.info("Correo de restablecimiento de contraseña enviado a {} desde {}", user.getEmail(), fromEmail);
        } catch (MailException e) {
            logger.error("Error al enviar correo de restablecimiento de contraseña a {} desde {}: {}", user.getEmail(), fromEmail, e.getMessage(), e);
        }

    }
        // Si quisieras enviar emails con formato HTML en el futuro:
    /*
    public void sendHtmlVerificationEmail(User user, String token) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8"); // true para multipart si adjuntas archivos

            String confirmationUrl = apiBaseUrl + "/api/v1/auth/confirm-account?token=" + token;
            String htmlMsg = "<h3>Hola " + user.getNombreUsuario() + ",</h3>" +
                             "<p>Gracias por registrarte en MyCheckPoint. Por favor, haz clic en el siguiente enlace para confirmar tu correo:</p>" +
                             "<p><a href=\"" + confirmationUrl + "\">Confirmar mi Correo</a></p>" +
                             "<p>O copia y pega esta URL en tu navegador: " + confirmationUrl + "</p>" +
                             "<p>Saludos,<br/>El equipo de MyCheckPoint</p>";

            helper.setText(htmlMsg, true); // true indica que es HTML
            helper.setTo(user.getEmail());
            helper.setSubject("MyCheckPoint - Confirma tu dirección de correo electrónico");
            helper.setFrom(fromEmail);

            mailSender.send(mimeMessage);
            logger.info("Correo de verificación HTML enviado a {} desde {}", user.getEmail(), fromEmail);
        } catch (MessagingException | MailException e) {
            logger.error("Error al enviar correo de verificación HTML a {} desde {}: {}", user.getEmail(), fromEmail, e.getMessage(), e);
        }
    }
    */

}
