package it.tim.reload.model.web;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Engineering
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReloadRequest {

	@ApiModelProperty(notes = "Channel from which the request comes", required = true)
    private String subSys;
    @ApiModelProperty(notes = "User to reload", required = true)
    private String msisdn;
    @ApiModelProperty(notes = "Reload value", required = true)
    private String basketValue;
    
}
