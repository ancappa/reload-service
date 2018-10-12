package it.tim.reload.web;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import it.tim.reload.aspects.Loggable;
import it.tim.reload.model.integration.ReloadReservationResponse;
import it.tim.reload.model.web.ReloadRequest;
import it.tim.reload.model.web.ReloadResponse;
import it.tim.reload.service.ReloadService;
import it.tim.reload.validation.ReloadControllerValidator;

/**
 * Engineering
 * PrepaidMobileTopUpMgmt
 */
// VERIFY THE SERVICE ENDPOINT
@RestController
@RequestMapping("/api")
@Api("Controller exposing reload operations")
public class ReloadController {

    private ReloadService reloadService;

    private static final DateTimeFormatter AUTH_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    
    @Autowired
    public ReloadController(ReloadService reloadService) {
        this.reloadService = reloadService;
    }
    
    @RequestMapping(method = RequestMethod.POST, value = "/reload" , produces = "application/json")
    @ApiOperation(value = "Reload operation", response = ReloadReservationResponse.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Reload success"),
            @ApiResponse(code = 400, message = "Missing or wrong mandatory parameters"),
            @ApiResponse(code = 404, message = "Wrong card number or card not found"),
            @ApiResponse(code = 401, message = "Not authorized due to max attempts reached"),
            @ApiResponse(code = 500, message = "Generic error"),
    })
    @Loggable
    public ReloadResponse reload( @RequestBody ReloadRequest request, 
    		                                @RequestHeader HttpHeaders headers,
    		                                @RequestHeader(value = "businessID", required = false) String xBusinessId,    		
											@RequestHeader(value = "messageID", required = false) String xMessageID,    		
											@RequestHeader(value = "transactionID", required = false) String xTransactionID,    		
											@RequestHeader(value = "channel", required = false) String xChannel,    		
											@RequestHeader(value = "sourceSystem", required = false) String xSourceSystem   		
								    	  )
    {
    	
        ReloadControllerValidator.validateReservationRequest(request);
        
        ReloadReservationResponse reloadResp = reloadService.reload(request.getSubSys(),request.getMsisdn(),request.getBasketValue(),headers);
        
        return new ReloadResponse(reloadResp.getStatus(), reloadResp.getCodiceEsito(), reloadResp.getDescrizioneEsito(),reloadResp.getInteractionDate());
        

    }

    public String composeHeaderDateTime(){
    	
        LocalDateTime bankAuthDate = LocalDateTime.now();
        String authDate = bankAuthDate.format(AUTH_DATE_FORMATTER);
        return authDate;
    }
}
