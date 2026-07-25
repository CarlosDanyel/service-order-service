package com.fiap.tech_challenge_fase2.infrastructure.email.adapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiap.tech_challenge_fase2.application.port.out.EmailNotificationGateway;
import com.fiap.tech_challenge_fase2.domain.entity.ServiceOrder;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class ResendEmailAdapter implements EmailNotificationGateway {

    private static final Logger log = LoggerFactory.getLogger(ResendEmailAdapter.class);

    private static final String RESEND_URL = "https://api.resend.com/emails";
    private static final MediaType JSON     = MediaType.get("application/json; charset=utf-8");

    private final OkHttpClient  httpClient;
    private final ObjectMapper  objectMapper;
    private final String        apiKey;
    private final String        fromEmail;
    private final String        appBaseUrl;

    public ResendEmailAdapter(OkHttpClient httpClient, ObjectMapper objectMapper,
                               String apiKey, String fromEmail, String appBaseUrl) {
        this.httpClient   = httpClient;
        this.objectMapper = objectMapper;
        this.apiKey       = apiKey;
        this.fromEmail    = fromEmail;
        this.appBaseUrl   = appBaseUrl;
    }

    @Override
    public void sendQuotationApprovalEmail(ServiceOrder serviceOrder) {
        String approveUrl = buildApprovalUrl(serviceOrder, true);
        String refuseUrl  = buildApprovalUrl(serviceOrder, false);
        String html       = quotationEmailHtml(serviceOrder, approveUrl, refuseUrl);

        dispatch(serviceOrder.getCustomer().getEmail(),
                "Aprovação de Orçamento — OS " + serviceOrder.getOrderNumber(),
                html);
    }

    @Override
    public void sendStatusUpdateEmail(ServiceOrder serviceOrder) {
        String html = statusUpdateEmailHtml(serviceOrder);

        dispatch(serviceOrder.getCustomer().getEmail(),
                "Atualização da OS " + serviceOrder.getOrderNumber(),
                html);
    }

    private void dispatch(String to, String subject, String html) {
        try {
            String body = objectMapper.writeValueAsString(
                    Map.of("from", fromEmail, "to", to, "subject", subject, "html", html));

            Request request = new Request.Builder()
                    .url(RESEND_URL)
                    .header("Authorization", "Bearer " + apiKey)
                    .post(RequestBody.create(body, JSON))
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    log.info("E-mail enviado para {} — assunto: {}", to, subject);
                } else {
                    String responseBody = response.body() != null ? response.body().string() : "vazio";
                    log.error("Falha ao enviar e-mail. Status: {} | Body: {}", response.code(), responseBody);
                }
            }
        } catch (Exception e) {
            log.error("Erro ao chamar Resend API: {}", e.getMessage(), e);
        }
    }

    private String buildApprovalUrl(ServiceOrder so, boolean approved) {
        return String.format("%s/api/quotations/%s?token=%s&approved=%s",
                appBaseUrl, so.getId(), so.getApprovalToken(), approved);
    }

    private String quotationEmailHtml(ServiceOrder so, String approveUrl, String refuseUrl) {
        return """
                <!DOCTYPE html>
                <html lang="pt-BR">
                <body style="font-family:Arial,sans-serif;max-width:600px;margin:0 auto;padding:24px;color:#2c3e50;">
                  <h2 style="color:#2c3e50;">🔧 Orçamento Disponível para Aprovação</h2>
                  <p>Olá, <strong>%s</strong>!</p>
                  <p>O orçamento da sua OS <strong>%s</strong> está pronto.</p>
                  <table style="width:100%%;border-collapse:collapse;margin:16px 0;">
                    <tr><td style="padding:6px;color:#7f8c8d;">Veículo</td>
                        <td style="padding:6px;"><strong>%s %s (%d)</strong> — Placa: %s</td></tr>
                    <tr><td style="padding:6px;color:#7f8c8d;">Valor Total</td>
                        <td style="padding:6px;"><strong>R$ %.2f</strong></td></tr>
                  </table>
                  <div style="margin:32px 0;display:flex;gap:16px;">
                    <a href="%s" style="background:#27ae60;color:#fff;padding:12px 28px;
                       text-decoration:none;border-radius:6px;font-weight:bold;">
                       ✅ Aprovar Orçamento
                    </a>
                    &nbsp;&nbsp;
                    <a href="%s" style="background:#e74c3c;color:#fff;padding:12px 28px;
                       text-decoration:none;border-radius:6px;font-weight:bold;">
                       ❌ Recusar Orçamento
                    </a>
                  </div>
                  <p style="font-size:12px;color:#95a5a6;">
                    Este link é válido para uso único. Em caso de dúvidas, entre em contato conosco.
                  </p>
                </body>
                </html>
                """.formatted(
                so.getCustomer().getName(), so.getOrderNumber(),
                so.getVehicle().getBrand(), so.getVehicle().getModel(),
                so.getVehicle().getYear(),  so.getVehicle().getLicensePlate(),
                so.calculateTotalAmount().doubleValue(),
                approveUrl, refuseUrl);
    }

    private String statusUpdateEmailHtml(ServiceOrder so) {
        return """
                <!DOCTYPE html>
                <html lang="pt-BR">
                <body style="font-family:Arial,sans-serif;max-width:600px;margin:0 auto;padding:24px;color:#2c3e50;">
                  <h2 style="color:#2c3e50;">🔧 Atualização da sua OS</h2>
                  <p>Olá, <strong>%s</strong>!</p>
                  <p>Sua Ordem de Serviço <strong>%s</strong> foi atualizada.</p>
                  <p>Status atual:
                    <span style="background:#2980b9;color:#fff;padding:4px 12px;
                          border-radius:4px;font-weight:bold;">%s</span>
                  </p>
                  <p><strong>Veículo:</strong> %s %s (%d) — Placa: %s</p>
                  <p style="font-size:12px;color:#95a5a6;">
                    Entre em contato caso tenha dúvidas.
                  </p>
                </body>
                </html>
                """.formatted(
                so.getCustomer().getName(), so.getOrderNumber(),
                so.getStatus().getDescription(),
                so.getVehicle().getBrand(), so.getVehicle().getModel(),
                so.getVehicle().getYear(),  so.getVehicle().getLicensePlate());
    }
}
