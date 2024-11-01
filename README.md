**Sistema de Notificación de Pagos**
=====================================

**Descripción General**
------------------------

Este documento describe la configuración de dos tópicos de Kafka, los productores y consumidores en el Sistema de Notificación de Pagos. El sistema está diseñado para gestionar transacciones de pagos y actualizar automáticamente los saldos según los eventos generados en estos tópicos de Kafka.

**Definición de Tópicos**
-------------------------

### Tópico 1: payment_completed_notifications

* **Propósito**: Notificar a otros servicios cuando el estado de un pago cambia a COMPLETED (completado).
* **Acción Activada**: Al completar un pago, se actualiza el saldo del receptor (payee), sumando el monto de la transacción a su cuenta.
* **Factor de Replicación**: 1 (para desarrollo; se puede ajustar para producción).
* **Particiones**: 1 (ajustable según necesidades de escalabilidad).

**Formato de Mensaje Ejemplo**
```json
{
  "customerId": "789",
  "amount": 100.0
}
```

### Tópico 2: payment_canceled_notifications

* **Propósito**: Notificar a otros servicios cuando el estado de un pago cambia a CANCELED (cancelado).
* **Acción Activada**: Al cancelar un pago, se actualiza el saldo del pagador (payer), devolviendo el monto de la transacción a su cuenta.
* **Factor de Replicación**: 1 (para desarrollo; se puede ajustar para producción).
* **Particiones**: 1 (ajustable según necesidades de escalabilidad).

**Formato de Mensaje Ejemplo**
```json
{
  "customerId": "456",
  "amount": 100.0
}
```

**Definición del Productor**
---------------------------

### Componente del Productor: PaymentService

* **Descripción**: El productor en PaymentService envía mensajes a los tópicos payment_completed_notifications o payment_canceled_notifications dependiendo del nuevo estado del pago. Esto permite notificar a otros servicios para que actualicen los saldos de los usuarios correspondientes.

**Detalles de Implementación**

* **Formato del Mensaje**: Cadena JSON que contiene detalles del pago (como se muestra en los ejemplos anteriores).
* **Disparador**: Activado automáticamente cuando el estado de un pago cambia a COMPLETED o CANCELED.
* **Plantilla de Kafka**: Usa KafkaTemplate<String, String> para enviar mensajes.

**Flujo de Mensajes**

* **Capa de Controlador**: PaymentController maneja solicitudes HTTP para actualizar el estado del pago.
* **Capa de Servicio**: PaymentService procesa la actualización de estado del pago.
* **Productor**: Envía un mensaje al tópico de Kafka correspondiente, según el nuevo estado del pago.

**Ejemplo de Código para el Productor**
```java
if (newStatus == PaymentStatus.COMPLETED) {
    kafkaTemplate.send("payment_completed_notifications", getString(payment.getPayee(), payment.getAmount()));
} else if (newStatus == PaymentStatus.CANCELED) {
    kafkaTemplate.send("payment_canceled_notifications", getString(payment.getPayer(), payment.getAmount()));
}
```

**Definición de los Consumidores**
---------------------------------

### Consumidor 1: PaymentCompletedConsumer

* **Tópico**: payment_completed_notifications
* **Descripción**: El PaymentCompletedConsumer escucha el tópico payment_completed_notifications. Cuando recibe un evento con estado COMPLETED, actualiza el saldo del receptor (payee) sumando el monto del pago a su cuenta.

**Detalles de Implementación**

* **Disparador**: Activado cuando se envía un mensaje a payment_completed_notifications.
* **ID del Grupo**: payment-completed-group
* **Lógica de Actualización de Saldo**:
+ Extrae customerId (ID del receptor) y amount (monto) del mensaje.
+ Llama a BalanceService para sumar el monto al saldo del receptor.

**Ejemplo de Código**
```java
@KafkaListener(topics = "payment_completed_notifications", groupId = "payment-completed-group")
public void handleCompletedPayment(String message) {
  PaymentMessage paymentMessage = parseMessage(message);
  if (paymentMessage != null) {
    balanceService.updateBalance(paymentMessage.getCustomerId(), paymentMessage.getAmount());
  }
}
```

### Consumidor 2: PaymentCanceledConsumer

* **Tópico**: payment_canceled_notifications
* **Descripción**: El PaymentCanceledConsumer escucha el tópico payment_canceled_notifications. Cuando recibe un evento con estado CANCELED, devuelve el monto del pago al pagador (payer) sumándolo a su saldo.

**Detalles de Implementación**

* **Disparador**: Activado cuando se envía un mensaje a payment_canceled_notifications.
* **ID del Grupo**: payment-canceled-group
* **Lógica de Actualización de Saldo**:
+ Extrae customerId (ID del pagador) y amount (monto) del mensaje.
+ Llama a BalanceService para sumar el monto al saldo del pagador.

**Ejemplo de Código**
```java
@KafkaListener(topics = "payment_canceled_notifications", groupId = "payment-canceled-group")
public void handleCanceledPayment(String message) {
  PaymentMessage paymentMessage = parseMessage(message);
  if (paymentMessage != null) {
    balanceService.updateBalance(paymentMessage.getCustomerId(), paymentMessage.getAmount());
  }
}
```

**Servicio de Saldos**
---------------------------------

El BalanceService proporciona métodos para actualizar el saldo de los usuarios (tanto receptores como pagadores). Este servicio maneja tanto el incremento como la devolución de saldos.
Detalles de Implementación

+ El método updateBalance recibe un customerId (ID del usuario) y un amount (monto) como parámetros.
+ Actualiza el saldo del usuario especificado.

Ejemplo de Código:
```java
public void updateBalance(Long payeeId, BigDecimal amount) {
    Customer customer = customerRepository.findById(payeeId).orElseThrow(() -> new CustomerNotFoundException
        ("Cliente no encontrado"));

    customer.setBalance(customer.getBalance().add(amount));
    log.info("Balance actualizado: {}", customer.getBalance());
    customerRepository.save(customer);
}
```
