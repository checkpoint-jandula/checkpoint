package mp.tfg.mycheckpoint.event;

import lombok.Getter;
import mp.tfg.mycheckpoint.entity.User;
import org.springframework.context.ApplicationEvent;

@Getter
public class OnRegistrationCompleteEvent extends ApplicationEvent {
    private final User user;
    private final String token;

    public OnRegistrationCompleteEvent(User user, String token) {
        super(user); // El 'source' del evento
        this.user = user;
        this.token = token;
    }
}
