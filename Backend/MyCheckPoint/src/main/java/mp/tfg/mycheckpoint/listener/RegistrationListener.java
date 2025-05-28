package mp.tfg.mycheckpoint.listener;

import mp.tfg.mycheckpoint.event.OnRegistrationCompleteEvent;
import mp.tfg.mycheckpoint.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
// import org.springframework.transaction.event.TransactionalEventListener; // Alternativa

@Component
public class RegistrationListener implements ApplicationListener<OnRegistrationCompleteEvent> {

    private final EmailService emailService;

    @Autowired
    public RegistrationListener(EmailService emailService) {
        this.emailService = emailService;
    }

    @Override
    // @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT) // Alternativa si el listener es parte de la misma transacción
    public void onApplicationEvent(OnRegistrationCompleteEvent event) {
        this.confirmRegistration(event);
    }

    private void confirmRegistration(OnRegistrationCompleteEvent event) {
        emailService.sendVerificationEmail(event.getUser(), event.getToken());
    }
}
