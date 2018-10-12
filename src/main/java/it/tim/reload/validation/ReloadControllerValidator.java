package it.tim.reload.validation;

import java.util.function.Predicate;

import it.tim.reload.model.exception.BadRequestException;
import it.tim.reload.model.web.ReloadRequest;

/**
 * Engineering
 */
public class ReloadControllerValidator {

    ReloadControllerValidator() {}

    public static void validateReservationRequest(ReloadRequest request) {

        boolean valid = validateStrings(CommonValidators.validPhoneNumber, request.getMsisdn())
                && request.getMsisdn()!=null
                && request.getSubSys()!=null;
        
        if(!valid)
            throw new BadRequestException("Missing/Wrong parameters in ReloadRequest");

    }

    //UTIL

    private static boolean validateStrings(Predicate<String> predicate, String... strings){
        for(String s : strings){
            if(!predicate.test(s))
                return false;
        }
        return true;
    }

}
