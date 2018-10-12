package it.tim.reload.service;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpHeaders;

import it.tim.reload.integration.client.OPSCClient;
import it.tim.reload.model.configuration.Constants;
import it.tim.reload.model.exception.GenericException;
import it.tim.reload.model.integration.ReloadReservationResponse;
import it.tim.reload.service.ReloadService;

@RunWith(MockitoJUnitRunner.class)
public class ReloadReservationServiceTest {


    @Mock
    private OPSCClient bmvClient;


    private ReloadService service;

    @Before
    public void init(){
        service = new ReloadService(bmvClient);
    }

    @After
    public void cleanup(){
        Mockito.reset(bmvClient);
    }

    @Test(expected = GenericException.class)
    public void pinCodeReservationIncompleteResponse() {

    	ReloadReservationResponse reserve = service.reload(
                "3400000001",
                "10000000000",
                Constants.Subsystems.MYTIMAPP.toString(),
                new HttpHeaders()
        );

        Assert.assertNotNull(reserve);

    }
}