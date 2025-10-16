package app.pet_pode_back.service;
//import com.sendgrid.*;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
//import java.io.IOException;


@Service
public class EmailService {

//    @Value("${sendgrid.api.key}")
//    private String sendgridApiKey;

//   public void enviarEmail(String para, String assunto, String texto) {
//        Email from = new Email("petpodeoficial@gmail.com");
//        Email to = new Email(para);
//        Content content = new Content("text/plain", texto);
//        Mail mail = new Mail(from, assunto, to, content);
//
//        SendGrid sg = new SendGrid(sendgridApiKey);
//        Request request = new Request();
//
//        try {
//            request.setMethod(Method.POST);
//            request.setEndpoint("mail/send");
//            request.setBody(mail.build());
//            Response response = sg.api(request);
//
//            System.out.println("STATUS: " + response.getStatusCode());
//            System.out.println("BODY: " + response.getBody());
//            System.out.println("HEADERS: " + response.getHeaders());
//        } catch (IOException ex) {
//            System.err.println("Erro ao enviar e-mail com SendGrid:");
//            ex.printStackTrace();
//        }
//    }
}
