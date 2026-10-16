#!/usr/bin/env sh
# Uso: ./enviar-erro.sh [review.product|support.request]
RK="${1:-review.product}"
curl -s -u admin:admin123 -H "content-type: application/json" \
  -X POST "http://localhost:15672/api/exchanges/%2F/messages.topic/publish" \
  -d "{\"properties\":{},\"routing_key\":\"$RK\",\"payload\":\"ISTO NAO E UM JSON VALIDO\",\"payload_encoding\":\"string\"}"
echo
