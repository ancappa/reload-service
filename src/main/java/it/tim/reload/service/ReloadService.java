package it.tim.reload.service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Base64.Encoder;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import it.tim.reload.integration.client.OPSCClient;
import it.tim.reload.model.exception.GenericException;
import it.tim.reload.model.integration.ReloadReservationRequest;
import it.tim.reload.model.integration.ReloadReservationResponse;
import lombok.extern.slf4j.Slf4j;


@Service
@Slf4j
public class ReloadService {

	private static final DateTimeFormatter AUTH_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
	private static final String USER_TYPE = "prepagato";
	private static final String OPSC_OK_CODE ="1";
	private OPSCClient opscClient;

	@Autowired
	public ReloadService( OPSCClient opscClient) {
		this.opscClient = opscClient;
	}

	public ReloadReservationResponse reload(String subSys, String toMsisdn, String basketValue, HttpHeaders headers){

		// Chiamata al servizio PrepaidMobileTopUpMgmt 
		LocalDateTime interactionDate = LocalDateTime.now();

		ReloadReservationRequest reloadReservReq = new ReloadReservationRequest();
		reloadReservReq.setSubSys(subSys);
		reloadReservReq.setMsisdn(toMsisdn);
		reloadReservReq.setBasketValue(basketValue);
		reloadReservReq.setInteractionDate(interactionDate.format(AUTH_DATE_FORMATTER));
		reloadReservReq.setUserType(USER_TYPE);

		ReloadReservationResponse reloadResp = prepaidMobileTopUpMgmt(reloadReservReq,headers);
		
		
		
		
		return reloadResp;
	}



	private ReloadReservationResponse prepaidMobileTopUpMgmt(ReloadReservationRequest reloadRequest, HttpHeaders headers){

		ReloadReservationResponse resp = new ReloadReservationResponse();
		resp.setStatus("KO");

		try {

			String opscReq = getOBJRequest(reloadRequest, headers);
			log.info("=================  OPSC REQ: " + opscReq);

			String opscResponse = opscClient.callOBJ(opscReq);

			log.info("================= OPSC REPONSE: " + opscResponse);

			// /reloadResponse/ProcessData/returnCode
			String codiceEsito = getTagValue(opscResponse, "ns:returnCode" , "-1");
			log.info("Return code from OPSC = " + codiceEsito);

			// /reloadResponse/ProcessData/returnDescription
			String descrizioneEsito = getTagValue(opscResponse, "ns:returnDescription" , "-1");
			log.info("Return Description from OPSC = " + descrizioneEsito);

			if (codiceEsito!=null) {
				resp.setCodiceEsito(codiceEsito);
				resp.setDescrizioneEsito(descrizioneEsito);
				resp.setStatus("OK");
			}
			
			// Date format
			StringBuilder interactionDate = new StringBuilder(reloadRequest.getInteractionDate().substring(8, 10)).append("-")
					.append(reloadRequest.getInteractionDate().substring(5, 7)).append("-")
					.append(reloadRequest.getInteractionDate().substring(0, 4)).append(" ")
					.append((reloadRequest.getInteractionDate().substring(11))).append(".000");
			resp.setInteractionDate(interactionDate.toString());
			
			log.info("Return resp = " + resp);
			return resp;
			
		}
		catch(Exception ex) {
			log.error("OPSC EXC " + ex);
			throw new GenericException("Incomplete response reveived by service 'PrepaidMobileTopUpMgmt'");
		}

	}


	private String getOBJRequest(ReloadReservationRequest reloadRequest,HttpHeaders headers ) {



		StringBuilder buff = new StringBuilder();

		buff.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?> ");
		buff.append("<soapenv:Envelope "
				+ "xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" "
				+ "xmlns:soap=\"http://telecomitalia.it/SOA/SOAP/SOAPHeader\" "
				+ "xmlns:ns=\"http://telecomitalia.it/SOA/PrepaidMobileTopUpMgmt/2015-05-11\" "
				+ "xmlns:ns1=\"http://telecomitalia.it/SOA/PrepaidMobileTopUpMgmtCustomTypes/2015-05-11\"> ");
		buff.append("<soapenv:Header> ");
		buff.append("<soap:Header> ");
		buff.append("<soap:sourceSystem>").append(headers.getFirst("sourceSystem")).append("</soap:sourceSystem> ");
		buff.append("<soap:interactionDate> ");
		buff.append("<soap:Date>").append(reloadRequest.getInteractionDate().substring(0, 10)).append("</soap:Date> ");
		buff.append("<soap:Time>").append(reloadRequest.getInteractionDate().substring(11)).append("</soap:Time> ");
		buff.append("</soap:interactionDate> ");
		buff.append("<soap:businessID>").append(headers.getFirst("businessID")).append("</soap:businessID> ");
		buff.append("<soap:messageID>").append(headers.getFirst("messageID")).append("</soap:messageID> ");
		buff.append("<soap:transactionID>").append(headers.getFirst("transactionID")).append("</soap:transactionID> ");
		buff.append("</soap:Header> ");
		buff.append("</soapenv:Header> ");
		buff.append("<soapenv:Body> ");

		// reloadRequest BODY
		buff.append("<ns:reloadRequest> ");
		buff.append("<ns:ProductOrder> ");

		buff.append("<ns1:CustomerOrderItem>");
		//ProductBundle
		buff.append("<ns1:ProductBundle>");
		buff.append("<ns1:ProductCharacteristicValue>");
		buff.append("<ns1:value>").append(reloadRequest.getMsisdn()).append("</ns1:value> ");
		buff.append("<ns1:ProductSpecCharacteristic>");
		buff.append("<ns1:name>ServiceNumber</ns1:name>");
		buff.append("</ns1:ProductSpecCharacteristic>");
		buff.append("</ns1:ProductCharacteristicValue>");
		buff.append("</ns1:ProductBundle>");
		// CharacteristicValue 1
		buff.append("<ns1:CharacteristicValue>");
		buff.append("<ns1:value>").append(reloadRequest.getSubSys()).append("</ns1:value> ");
		buff.append("<ns1:CharacteristicSpecification>");
		buff.append("<ns1:name>Subsys</ns1:name>");
		buff.append("</ns1:CharacteristicSpecification>");
		buff.append("</ns1:CharacteristicValue>");
		// CharacteristicValue 2
		buff.append("<ns1:CharacteristicValue>");
		buff.append("<ns1:value>reload</ns1:value>");
		buff.append("<ns1:CharacteristicSpecification>");
		buff.append("<ns1:name>operationType</ns1:name>");
		buff.append("</ns1:CharacteristicSpecification>");
		buff.append("</ns1:CharacteristicValue>");
		// 
		buff.append("<ns1:PhysicalResource>");
		buff.append("<ns1:ResourceCharacteristicValue>");
		buff.append("<ns1:value>");
		buff.append("</ns1:value>");
		buff.append("<ns1:ResourceSpecCharacteristic>");
		buff.append("<ns1:name>IMSI</ns1:name>");
		buff.append("</ns1:ResourceSpecCharacteristic>");

		buff.append("</ns1:ResourceCharacteristicValue>");
		buff.append("</ns1:PhysicalResource>");

		// Product
		buff.append("<ns1:Product>");
		buff.append("<ns1:CompositeProdPrice>");
		buff.append("<ns1:ComponentProdPrice>");
		buff.append("<ns1:price>").append(reloadRequest.getBasketValue()).append("</ns1:price>");
		buff.append("</ns1:ComponentProdPrice>");
		buff.append("</ns1:CompositeProdPrice>");

		buff.append("<ns1:ProductInvolvementRole>");
		buff.append("<ns1:Customer>");

		buff.append("<ns1:PaymentPlan>");
		buff.append("<ns1:CharacteristicValue>");
		buff.append("<ns1:value>");
		buff.append("</ns1:value>");


		buff.append("<ns1:CharacteristicSpecification>");
		buff.append("<ns1:name>");
		buff.append("</ns1:name>");
		buff.append("</ns1:CharacteristicSpecification>");
		buff.append("</ns1:CharacteristicValue>");
		buff.append("</ns1:PaymentPlan>");
		buff.append("</ns1:Customer>");


		buff.append("<ns1:involvementRole>");
		buff.append("</ns1:involvementRole>");
		buff.append("</ns1:ProductInvolvementRole>");

		//Active_Time ProductCharacteristicValue
		buff.append("<ns1:ProductCharacteristicValue>");
		buff.append("<ns1:value>365</ns1:value>");
		buff.append("<ns1:ProductSpecCharacteristic>");
		buff.append("<ns1:name>COActiveTime</ns1:name>");
		buff.append("</ns1:ProductSpecCharacteristic>");
		buff.append("</ns1:ProductCharacteristicValue>");
		
		//Life_Time ProductCharacteristicValue
		buff.append("<ns1:ProductCharacteristicValue>");
		buff.append("<ns1:value>395</ns1:value>");
		buff.append("<ns1:ProductSpecCharacteristic>");
		buff.append("<ns1:name>COLifeTime</ns1:name>");
		buff.append("</ns1:ProductSpecCharacteristic>");
		buff.append("</ns1:ProductCharacteristicValue>");

				
		buff.append("</ns1:Product>");

		buff.append("</ns1:CustomerOrderItem> ");

		buff.append("<ns1:CharacteristicValue>");
		buff.append("<ns1:value></ns1:value>");
		buff.append("<ns1:CharacteristicSpecification>");
		buff.append("<ns1:name></ns1:name>");
		buff.append("</ns1:CharacteristicSpecification>");
		buff.append("</ns1:CharacteristicValue>");

		buff.append("<ns1:interactionDate>").append(reloadRequest.getInteractionDate().substring(0, 10));
		buff.append("</ns1:interactionDate>");


		buff.append("<ns1:PartyInteractionRole>");
		buff.append("<ns1:interactionRole></ns1:interactionRole>");
		buff.append("<ns1:Employee>");
		buff.append("<ns1:PartyRoleOrganizationAssoc>");
		buff.append("<ns1:associationType></ns1:associationType>");

		buff.append("<ns1:OrganizationPost>");
		buff.append("<ns1:PartyRoleOrganizationAssoc>");
		buff.append("<ns1:associationType></ns1:associationType>");
		buff.append("<ns1:Partner>"); 
		buff.append("<ns1:partyRoleId></ns1:partyRoleId>");
		buff.append("</ns1:Partner>"); 
		buff.append("</ns1:PartyRoleOrganizationAssoc>");
		buff.append("</ns1:OrganizationPost>");
		buff.append("</ns1:PartyRoleOrganizationAssoc>");
		buff.append("</ns1:Employee>");
		buff.append("</ns1:PartyInteractionRole>");

		buff.append("</ns:ProductOrder> ");
		// ============ ns:ProcessData============
		buff.append("<ns:ProcessData> ");
		buff.append("<ns:returnCode> ");
		buff.append("</ns:returnCode> ");
		buff.append("<ns:returnDescription> ");
		buff.append("</ns:returnDescription> ");
		buff.append("<ns:Parameters> ");
		//requestType
		buff.append("<ns:Parameter> ");
		buff.append("<ns:name>").append("requestType").append("</ns:name> ");
		buff.append("<ns:value>").append("OneStep").append("</ns:value> ");
		buff.append("</ns:Parameter> ");
		// appDep_1
		buff.append("<ns:Parameter> ");
		buff.append("<ns:name>").append("appDep_1").append("</ns:name> ");
		buff.append("<ns:value>").append("1").append("</ns:value> ");
		buff.append("</ns:Parameter> ");
		// basketName
		buff.append("<ns:Parameter> ");
		buff.append("<ns:name>").append("basketName").append("</ns:name> ");
		buff.append("<ns:value>").append("DEBIT").append("</ns:value> ");
		buff.append("</ns:Parameter> ");
		// basketMode
		buff.append("<ns:Parameter> ");
		buff.append("<ns:name>").append("basketMode").append("</ns:name> ");
		buff.append("<ns:value>").append("0").append("</ns:value> ");
		buff.append("</ns:Parameter> ");
		// baskeZerotMode
		buff.append("<ns:Parameter> ");
		buff.append("<ns:name>").append("basketZeroMode").append("</ns:name> ");
		buff.append("<ns:value>").append("2").append("</ns:value> ");
		buff.append("</ns:Parameter> ");
		
		buff.append("</ns:Parameters> ");
		buff.append("</ns:ProcessData> ");

		buff.append("</ns:reloadRequest> ");
		buff.append("</soapenv:Body> ");
		buff.append("</soapenv:Envelope> ");

		return buff.toString();
	}




	private static String getTagValue(String resp, String tag, String defaultVal ) {
		String tagValue = defaultVal;

		String tag1 = "<"+tag+">";
		String tag2 = "</"+tag+">";

		int idx1 = resp.indexOf(tag1);
		int idx2 = resp.indexOf(tag2);

		if(idx1>0 && idx2>0) {
			tagValue = resp.substring(idx1 + tag1.length(),idx2).trim();
		}

		return tagValue;
	}



	public static void main(String[] args) {
		String anno = "2021";
		String year = anno.substring(2);
		System.out.println("year = " + year);

		String objResp = "<TransactionType>PAGAM</TransactionType><TransactionResult>KO</TransactionResult><ShopTransactionID>1234</ShopTransactionID><BankTransactionID>5678900000</BankTransactionID><AuthorizationCode>"
				+ "</AuthorizationCode><Currency></Currency><Amount></Amount><Country></Country><Buyer><BuyerName></BuyerName><BuyerEmail></BuyerEmail></Buyer><CustomInfo></CustomInfo><ErrorCode>1125</ErrorCode><ErrorDescription>Anno di scadenza non valido</ErrorDescription><AlertCode></AlertCode><AlertDescription></AlertDescription><TransactionKey>196704321</TransactionKey><VbV><VbVFlag></VbVFlag><VbVBuyer>KO</VbVBuyer><VbVRisp></VbVRisp></VbV><TOKEN></TOKEN><TokenExpiryMonth></TokenExpiryMonth><TokenExpiryYear></TokenExpiryYear></GestPayS2S></callPagamS2SResult></callPagamS2SResponse></S:Body></S:Envelope>";


		String erroCode = getTagValue(objResp, "ErrorCode" , "-1");
		System.out.println("erroCode = " + erroCode);

		String shopTransactionID = getTagValue(objResp, "ShopTransactionID" , "");
		System.out.println("shopTransactionID = " + shopTransactionID);

		String bankTransactionID = getTagValue(objResp, "BankTransactionID" , "");
		System.out.println("bankTransactionID = " + bankTransactionID);

		LocalDateTime bankAuthDate = LocalDateTime.now();
		String authDate = bankAuthDate.format(AUTH_DATE_FORMATTER);

		System.out.println("authDate = " + authDate);



		String debit = "25.00000000000";
		int idx = debit.indexOf(".");
		String db1 = debit.substring(0,idx+3);
		System.out.println("db1= " + db1);


		UUID transaction = UUID.randomUUID();
		String tid = transaction.toString();
		System.out.println("tid="+tid);

		SecureRandom random = new SecureRandom();
		byte bytes[] = new byte[16];
		random.nextBytes(bytes);
		Encoder encoder = Base64.getUrlEncoder().withoutPadding();
		String token = encoder.encodeToString(bytes);
		System.out.println(token);

	}

}
