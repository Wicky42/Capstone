package org.example.backend.common.service;

import org.example.backend.common.model.SequenceDocument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.time.Year;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderNumberServiceTest {

    @Mock
    private MongoTemplate mongoTemplate;

    @InjectMocks
    private OrderNumberService orderNumberService;

    private final int currentYear = Year.now(ZoneOffset.UTC).getValue();

    @BeforeEach
    void setUp() {
        // lenient: manche Tests überschreiben diesen Stub mit eigenen Werten
        lenient().when(mongoTemplate.findAndModify(
                any(Query.class),
                any(Update.class),
                any(FindAndModifyOptions.class),
                eq(SequenceDocument.class)
        )).thenReturn(new SequenceDocument("order_number", 1L));
    }

    // ═══════════════════ generateOrderNumber ══════════════════════════════════

    @Test
    void generateOrderNumber_returnsCorrectFormat() {
        String result = orderNumberService.generateOrderNumber();

        assertThat(result).matches("ORD-\\d{4}-\\d{6}");
    }

    @Test
    void generateOrderNumber_containsCurrentYear() {
        String result = orderNumberService.generateOrderNumber();

        assertThat(result).startsWith("ORD-" + currentYear + "-");
    }

    @Test
    void generateOrderNumber_formatsCounterWithLeadingZeros() {
        String result = orderNumberService.generateOrderNumber();

        assertThat(result).isEqualTo("ORD-" + currentYear + "-000001");
    }

    // ═══════════════════ generateSellerOrderNumber ════════════════════════════

    @Test
    void generateSellerOrderNumber_returnsCorrectFormat() {
        String result = orderNumberService.generateSellerOrderNumber();

        assertThat(result).matches("SO-\\d{4}-\\d{6}");
    }

    @Test
    void generateSellerOrderNumber_containsCurrentYear() {
        String result = orderNumberService.generateSellerOrderNumber();

        assertThat(result).startsWith("SO-" + currentYear + "-");
    }

    // ═══════════════════ generateInvoiceNumber ════════════════════════════════

    @Test
    void generateInvoiceNumber_returnsCorrectFormat() {
        String result = orderNumberService.generateInvoiceNumber();

        assertThat(result).matches("INV-\\d{4}-\\d{6}");
    }

    @Test
    void generateInvoiceNumber_containsCurrentYear() {
        String result = orderNumberService.generateInvoiceNumber();

        assertThat(result).startsWith("INV-" + currentYear + "-");
    }

    // ═══════════════════ Increment-Verhalten ══════════════════════════════════

    @Test
    void generateOrderNumber_incrementsCounterOnEachCall() {
        when(mongoTemplate.findAndModify(any(), any(), any(), eq(SequenceDocument.class)))
                .thenReturn(new SequenceDocument("order_number", 1L))
                .thenReturn(new SequenceDocument("order_number", 2L));

        String first  = orderNumberService.generateOrderNumber();
        String second = orderNumberService.generateOrderNumber();

        assertThat(first).endsWith("-000001");
        assertThat(second).endsWith("-000002");
    }

    @Test
    void generateOrderNumber_usesOrderSequenceKey() {
        ArgumentCaptor<Query> queryCaptor = ArgumentCaptor.forClass(Query.class);

        orderNumberService.generateOrderNumber();

        verify(mongoTemplate).findAndModify(
                queryCaptor.capture(),
                any(Update.class),
                any(FindAndModifyOptions.class),
                eq(SequenceDocument.class)
        );
        // Query muss nach _id = "order_number" suchen
        assertThat(queryCaptor.getValue().getQueryObject().get("_id")).isEqualTo("order_number");
    }

    @Test
    void generateSellerOrderNumber_usesSellerSequenceKey() {
        ArgumentCaptor<Query> queryCaptor = ArgumentCaptor.forClass(Query.class);

        orderNumberService.generateSellerOrderNumber();

        verify(mongoTemplate).findAndModify(
                queryCaptor.capture(),
                any(Update.class),
                any(FindAndModifyOptions.class),
                eq(SequenceDocument.class)
        );
        assertThat(queryCaptor.getValue().getQueryObject().get("_id")).isEqualTo("seller_order_number");
    }

    @Test
    void generateInvoiceNumber_usesInvoiceSequenceKey() {
        ArgumentCaptor<Query> queryCaptor = ArgumentCaptor.forClass(Query.class);

        orderNumberService.generateInvoiceNumber();

        verify(mongoTemplate).findAndModify(
                queryCaptor.capture(),
                any(Update.class),
                any(FindAndModifyOptions.class),
                eq(SequenceDocument.class)
        );
        assertThat(queryCaptor.getValue().getQueryObject().get("_id")).isEqualTo("invoice_number");
    }

    @Test
    void generateOrderNumber_usesUpsertAndReturnNewOptions() {
        ArgumentCaptor<FindAndModifyOptions> optionsCaptor =
                ArgumentCaptor.forClass(FindAndModifyOptions.class);

        orderNumberService.generateOrderNumber();

        verify(mongoTemplate).findAndModify(
                any(Query.class),
                any(Update.class),
                optionsCaptor.capture(),
                eq(SequenceDocument.class)
        );
        FindAndModifyOptions opts = optionsCaptor.getValue();
        assertThat(opts.isUpsert()).isTrue();
        assertThat(opts.isReturnNew()).isTrue();
    }

    @Test
    void generateOrderNumber_usesFiveDigitPaddingForLargeCounters() {
        when(mongoTemplate.findAndModify(any(), any(), any(), eq(SequenceDocument.class)))
                .thenReturn(new SequenceDocument("order_number", 42L));

        String result = orderNumberService.generateOrderNumber();

        assertThat(result).endsWith("-000042");
    }
}

