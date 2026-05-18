package org.example.backend.common.service;

import lombok.RequiredArgsConstructor;
import org.example.backend.common.model.SequenceDocument;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.time.Year;
import java.time.ZoneOffset;

/**
 * Generiert lesbare, kollisionssichere Nummern für Bestellungen, Teilbestellungen und Rechnungen.
 *
 * Format:
 *   FulfillmentOrder  → ORD-2026-000001
 *   SellerOrder       → SO-2026-000001
 *   CustomerInvoice   → INV-2026-000001
 *
 * Der Zähler läuft jahresübergreifend durch (kein jährlicher Reset).
 * Der Jahres-Präfix wird aus dem aktuellen UTC-Jahr generiert.
 * Das atomare Increment via findAndModify garantiert keine Kollisionen bei parallelen Requests.
 */
@Service
@RequiredArgsConstructor
public class OrderNumberService {

    private static final String SEQ_ORDER   = "order_number";
    private static final String SEQ_SELLER  = "seller_order_number";
    private static final String SEQ_INVOICE = "invoice_number";

    private final MongoTemplate mongoTemplate;

    public String generateOrderNumber() {
        return format("ORD", nextValue(SEQ_ORDER));
    }

    public String generateSellerOrderNumber() {
        return format("SO", nextValue(SEQ_SELLER));
    }

    public String generateInvoiceNumber() {
        return format("INV", nextValue(SEQ_INVOICE));
    }

    // ── Private Helpers ───────────────────────────────────────────────────────

    /**
     * Inkrementiert den Zähler für den gegebenen Sequence-Key atomar und gibt den neuen Wert zurück.
     * Wenn das Dokument noch nicht existiert, wird es mit value=1 angelegt (upsert).
     */
    private long nextValue(String sequenceName) {
        Query query = new Query(Criteria.where("_id").is(sequenceName));
        Update update = new Update().inc("value", 1);
        FindAndModifyOptions options = FindAndModifyOptions.options()
                .returnNew(true)
                .upsert(true);

        SequenceDocument result = mongoTemplate.findAndModify(
                query, update, options, SequenceDocument.class
        );

        return result != null ? result.getValue() : 1L;
    }

    /**
     * Formatiert eine Nummer als "PREFIX-YYYY-000001".
     */
    private String format(String prefix, long value) {
        int year = Year.now(ZoneOffset.UTC).getValue();
        return String.format("%s-%d-%06d", prefix, year, value);
    }
}

