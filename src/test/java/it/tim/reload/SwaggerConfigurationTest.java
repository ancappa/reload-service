package it.tim.reload;

import org.junit.Test;

import it.tim.reload.SwaggerConfiguration;

import static org.junit.Assert.*;

/**
 * Created by alongo on 02/05/18.
 */
public class SwaggerConfigurationTest {

    @Test
    public void api() throws Exception {
        SwaggerConfiguration swaggerConfiguration = new SwaggerConfiguration();
        assertNotNull(swaggerConfiguration.api());
    }

}