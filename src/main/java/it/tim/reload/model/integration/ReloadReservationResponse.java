package it.tim.reload.model.integration;

import lombok.Data;

@Data
public class ReloadReservationResponse {

	private String operationType;
    private String responseType;
    
    private String subsys;
    private String codiceEsito;
    private String descrizioneEsito;
    private String msisdn;
    
    private String status;
    private String interactionDate;
    
    
}
