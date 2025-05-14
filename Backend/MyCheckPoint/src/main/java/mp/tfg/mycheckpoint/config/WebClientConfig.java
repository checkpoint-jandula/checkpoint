package mp.tfg.mycheckpoint.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${igdb.api.baseurl}")
    private String igdbBaseUrl;

    @Value("${igdb.api.client-id}")
    private String igdbClientId;

    @Value("${igdb.api.authorization}")
    private String igdbAuthorizationHeader;

    @Bean // Define este WebClient como un bean de Spring
    public WebClient igdbWebClient(WebClient.Builder builder) {
        return builder
                .baseUrl(igdbBaseUrl)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("Client-ID", igdbClientId)
                .defaultHeader(HttpHeaders.AUTHORIZATION, igdbAuthorizationHeader)
                .build();
    }
}
