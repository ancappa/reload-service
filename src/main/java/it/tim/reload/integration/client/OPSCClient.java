package it.tim.reload.integration.client;

import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import it.tim.reload.integration.FeignConfiguration;

@FeignClient(
        name="reload",
        url = "${integration.soap.opscbasepath}"
        , configuration = FeignConfiguration.class
)
public interface OPSCClient {
    @PostMapping(value = "/${integration.soap.opscvalue}",  produces = "application/xml", consumes = "application/xml")
    String callOBJ(@RequestBody String request );
}
