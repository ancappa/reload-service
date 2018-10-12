package it.tim.reload.model.web;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Created by alongo on 13/04/18.
 */
@Getter
@AllArgsConstructor
public class ReloadResponse {

    @ApiModelProperty(notes = "Stato della risposta OK o KO")
    private String status;
    @ApiModelProperty(notes = "Esito dell'operazione")
    private String esito;
    @ApiModelProperty(notes = "Descrizione esito operazione")
    private String descrizioneEsito;
    @ApiModelProperty(notes = "Data e ora invocazione servizio formato DD-MM-YYYY HH:mm:ss.millis")
    private String dateInvocation;

}
