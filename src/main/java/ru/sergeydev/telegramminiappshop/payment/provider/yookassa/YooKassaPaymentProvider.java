package ru.sergeydev.telegramminiappshop.payment.provider.yookassa;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import ru.sergeydev.telegramminiappshop.payment.config.YooKassaProperties;
import ru.sergeydev.telegramminiappshop.payment.entity.PaymentStatus;
import ru.sergeydev.telegramminiappshop.payment.provider.InvoiceProvider;
import ru.sergeydev.telegramminiappshop.payment.provider.PaymentProvider;
import ru.sergeydev.telegramminiappshop.payment.provider.yookassa.dto.*;
import java.net.http.HttpClient;
import java.time.Duration;
import org.springframework.http.client.JdkClientHttpRequestFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class YooKassaPaymentProvider implements PaymentProvider, InvoiceProvider {

    private final YooKassaProperties properties;
    private final RestClient restClient;

    public YooKassaPaymentProvider(
            YooKassaProperties properties,
            RestClient.Builder restClientBuilder
    ) {
        this.properties = properties;
//создаем таймаут запроса к провайдеру
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
/**Мы передаём фабрике уже настроенный httpClient. Она соединяет интерфейсы Spring с реализацией HTTP-клиента Java.
 *  Поэтому её можно понимать и как адаптер между ними.*/
        JdkClientHttpRequestFactory requestFactory =
                new JdkClientHttpRequestFactory(httpClient);
//дополнительная настройка таймаута на запрос-ответ после подключения к серверу
        requestFactory.setReadTimeout(Duration.ofSeconds(15));

        this.restClient = restClientBuilder
                .requestFactory(requestFactory)
                .baseUrl("https://api.yookassa.ru/v3")
                .defaultHeaders(headers ->
                        headers.setBasicAuth(
                                properties.shopId(),
                                properties.secretKey()
                        )
                )
                .build();
    }

    @Override
    public CreatedPayment createPayment(
            Long orderId,
            BigDecimal amount,
            UUID idempotencyKey
    ) {

        YooKassaCreatePaymentRequest request =
                new YooKassaCreatePaymentRequest(
                        new YooKassaCreatePaymentRequest.Amount(
                                amount.setScale(2, RoundingMode.HALF_UP)
                                        .toPlainString(),
                                "RUB"
                        ),
                        true,
                        new YooKassaCreatePaymentRequest.Confirmation(
                                "redirect",
                                properties.returnUrl()
                        ),
                        "Оплата заказа №" + orderId,
                        Map.of("order_id", orderId.toString())
                );

        YooKassaCreatePaymentResponse response = restClient
                .post()
                .uri("/payments")
                .header(
                        "Idempotence-Key",
                        idempotencyKey.toString()
                )
                .body(request)
                .retrieve()//выполнение запроса
                .body(YooKassaCreatePaymentResponse.class);

        if (response == null
                || response.confirmation() == null
                || response.confirmation().confirmationUrl() == null) {

            throw new IllegalStateException(
                    "ЮKassa не вернула ссылку на оплату"
            );
        }

        return new CreatedPayment(
                response.id(),
                response.confirmation().confirmationUrl()
        );
    }
    @Override
    public PaymentInfo getPayment(String providerPaymentId) {

        YooKassaPaymentResponse response = restClient
                .get()
                .uri("/payments/{paymentId}", providerPaymentId)
                .retrieve()
                .body(YooKassaPaymentResponse.class);

        if (response == null
                || !providerPaymentId.equals(response.id())
                || response.status() == null
                || response.amount() == null) {

            throw new IllegalStateException(
                    "ЮKassa вернула некорректные данные платежа"
            );
        }

        String providerInvoiceId = response.invoiceDetails() == null
                ? null
                : response.invoiceDetails().id();

        return new PaymentInfo(
                response.id(),
                mapStatus(response.status()),
                new BigDecimal(response.amount().value()),
                response.amount().currency(),
                providerInvoiceId
        );
    }
//мапаем yooKassaStatus в enum которые знает PaymentService
    private PaymentStatus mapStatus(String yooKassaStatus) {

        return switch (yooKassaStatus) {
            case "pending" -> PaymentStatus.PENDING;
            case "succeeded" -> PaymentStatus.PAID;
            case "canceled" -> PaymentStatus.CANCELLED;
            default -> throw new IllegalStateException(
                    "Неизвестный статус платежа ЮKassa: " + yooKassaStatus
            );
        };
    }

    @Override
    public CreatedInvoice createInvoice(
            Long orderId,
            BigDecimal amount,
            List<InvoiceItem> items,
            OffsetDateTime expiresAt,
            UUID idempotencyKey
    ) {
        if (expiresAt == null
                || !expiresAt.isAfter(OffsetDateTime.now())) {
            throw new IllegalArgumentException(
                    "Срок оплаты заказа уже истёк"
            );
        }
        //собираем корзину для инвойса
        var cart = items.stream()
                .map(item -> new YooKassaCreateInvoiceRequest.CartItem(
                        item.name(),
                        new YooKassaCreateInvoiceRequest.Amount(
                                item.price()
                                        .setScale(2, RoundingMode.UNNECESSARY)
                                        .toPlainString(),
                                "RUB"
                        ),
                        BigDecimal.valueOf(item.quantity())
                ))
                .toList();

        var request = new YooKassaCreateInvoiceRequest(
                new YooKassaCreateInvoiceRequest.PaymentData(
                        new YooKassaCreateInvoiceRequest.Amount(
                                amount.setScale(2, RoundingMode.UNNECESSARY)
                                        .toPlainString(),
                                "RUB"
                        ),
                        true,
                        "Оплата заказа №" + orderId,
                        Map.of("order_id", orderId.toString())
                ),
                cart,
                new YooKassaCreateInvoiceRequest.DeliveryMethodData(
                        "self"
                ),
                expiresAt.toInstant().toString(),
                "Счёт на оплату заказа №" + orderId,
                Map.of("order_id", orderId.toString())
        );

        YooKassaInvoiceResponse response = restClient
                .post()
                .uri("/invoices")
                .header("Idempotence-Key", idempotencyKey.toString())
                .body(request)
                .retrieve()
                .body(YooKassaInvoiceResponse.class);

        if (response == null
                || response.id() == null
                || response.id().isBlank()
                || response.deliveryMethod() == null
                || response.deliveryMethod().url() == null
                || response.deliveryMethod().url().isBlank()) {

            throw new IllegalStateException(
                    "ЮKassa не вернула идентификатор или ссылку счёта"
            );
        }

        return new CreatedInvoice(
                response.id(),
                response.deliveryMethod().url()
        );
    }

    @Override
    public InvoiceInfo getInvoice(String providerInvoiceId) {

        YooKassaInvoiceResponse response = restClient
                .get()
                .uri("/invoices/{invoiceId}", providerInvoiceId)
                .retrieve()
                .body(YooKassaInvoiceResponse.class);

        if (response == null
                || !providerInvoiceId.equals(response.id())
                || response.status() == null) {

            throw new IllegalStateException(
                    "ЮKassa вернула некорректные данные счёта"
            );
        }

        InvoiceStatus status = switch (response.status()) {
            case "pending" -> InvoiceStatus.PENDING;
            case "succeeded" -> InvoiceStatus.SUCCEEDED;
            case "canceled" -> InvoiceStatus.CANCELLED;
            default -> throw new IllegalStateException(
                    "Неизвестный статус счёта: " + response.status()
            );
        };

        String providerPaymentId =
                response.paymentDetails() == null
                        ? null
                        : response.paymentDetails().id();

        return new InvoiceInfo(
                response.id(),
                status,
                providerPaymentId
        );
    }

}