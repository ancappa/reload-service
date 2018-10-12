package it.tim.reload.model.integration;

import lombok.Data;

@Data
public class ReloadReservationRequest {

    private String subSys;
    private String msisdn;
    private String basketValue;
    private String userType;
    private String interactionDate;
	
	
}
