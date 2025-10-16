package app.pet_pode_back.service;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class EmailService {

    public void enviarEmail(String para, String assunto, String texto) {
        // Pega a chave da variável de ambiente
        String sendgridApiKey = System.getenv("SENDGRID_API_KEY");
        if (sendgridApiKey == null || sendgridApiKey.isBlank()) {
            throw new IllegalStateException("SENDGRID_API_KEY não está definida no ambiente.");
        }

        String url = "https://api.sendgrid.com/v3/mail/send";

        // Montar o corpo do e-mail em formato JSON conforme a API do SendGrid
        Map<String, Object> body = new HashMap<>();
        body.put("personalizations", new Object[]{
                Map.of("to", new Object[]{Map.of("email", para)})
        });
        body.put("from", Map.of("email", "petpodeoficial@gmail.com"));
        body.put("subject", assunto);
        body.put("content", new Object[]{
                Map.of("type", "text/plain", "value", texto)
        });

        // Criar headers com autorização
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(sendgridApiKey);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        // Enviar usando RestTemplate
        RestTemplate restTemplate = new RestTemplate();
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

            System.out.println("STATUS: " + response.getStatusCodeValue());
            System.out.println("BODY: " + response.getBody());

        } catch (Exception e) {
            System.err.println("Erro ao enviar e-mail com SendGrid:");
            e.printStackTrace();
        }
    }
}
