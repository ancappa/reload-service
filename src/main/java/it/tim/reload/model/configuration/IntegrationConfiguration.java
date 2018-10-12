package it.tim.reload.model.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Created by alongo on 26/04/18.
 */
@ConfigurationProperties(prefix = "integration.soap")
@Data
@Component
public class IntegrationConfiguration {

     private String bmvbasepath;
     private String bmvvalue;

}
