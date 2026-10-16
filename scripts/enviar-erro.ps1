# Publica manualmente uma mensagem INVÁLIDA (não é JSON) na fila de avaliações.
# O consumidor 1 falha ao processar => NACK sem requeue => mensagem vai para a DLQ (dlq.reviews).
param([string]$RoutingKey = "review.product")   # use "support.request" para testar o consumidor 2

$cred = [Convert]::ToBase64String([Text.Encoding]::ASCII.GetBytes("admin:admin123"))
$body = @{
    properties       = @{}
    routing_key      = $RoutingKey
    payload          = "ISTO NAO E UM JSON VALIDO - mensagem com erro de proposito"
    payload_encoding = "string"
} | ConvertTo-Json

Invoke-RestMethod -Method Post `
    -Uri "http://localhost:15672/api/exchanges/%2F/messages.topic/publish" `
    -Headers @{ Authorization = "Basic $cred" } `
    -ContentType "application/json" -Body $body
Write-Host "Mensagem com erro enviada com a routing key $RoutingKey"
