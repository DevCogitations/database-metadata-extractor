package dev.bsingh.dbmetadata.extractor.controller;

import dev.bsingh.dbmetadata.extractor.model.DatabaseExtractRequest;
import dev.bsingh.dbmetadata.extractor.model.DatabaseExtractResponse;
import dev.bsingh.dbmetadata.extractor.model.DatabaseObject;
import dev.bsingh.dbmetadata.extractor.model.DatabaseObjectType;
import dev.bsingh.dbmetadata.extractor.model.DatabaseType;
import dev.bsingh.dbmetadata.extractor.service.DatabaseMetadataService;
import dev.bsingh.dbmetadata.extractor.service.format.FormatService;
import dev.bsingh.dbmetadata.extractor.service.format.FormatServiceFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class DatabaseExtractControllerTest {

    @Mock
    private DatabaseMetadataService databaseService;

    @Mock
    private FormatServiceFactory formatServiceFactory;

    @Mock
    private FormatService formatService;

    @InjectMocks
    private DatabaseMetadataController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    //@Test
    void extractObjects_shouldReturnFormattedContent() {
        DatabaseExtractRequest request = getDatabaseExtractRequest();
        byte[] formattedContent = "formatted content".getBytes();
        when(formatServiceFactory.getFormatService(any())).thenReturn(formatService);
        when(databaseService.extract(any())).thenReturn(Flux.just(new DatabaseObject("table1", DatabaseObjectType.TABLE,"extract1", "")));
        when(formatService.format(any())).thenReturn(formattedContent);
        when(formatService.getFilename()).thenReturn("attachment");

        Mono<ResponseEntity<byte[]>> response = controller.extractObjects(request);

        StepVerifier.create(response)
                .assertNext(entity -> {
                    assertEquals(formattedContent, entity.getBody());
                    assertEquals("attachment; filename=" + formatService.getFilename(), entity.getHeaders().getContentDisposition().toString());
                })
                .verifyComplete();
    }

    @Test
    void extractObjects_shouldHandleErrorGracefully() {
        DatabaseExtractRequest request = getDatabaseExtractRequest();
        when(formatServiceFactory.getFormatService(any())).thenReturn(formatService);
        when(databaseService.extract(any())).thenReturn(Flux.error(new RuntimeException("error")));

        Mono<ResponseEntity<byte[]>> response = controller.extractObjects(request);

        StepVerifier.create(response)
                .assertNext(entity -> {
                    assertEquals(500, entity.getStatusCodeValue());
                    assertEquals("error", new String(entity.getBody()));
                })
                .verifyComplete();
    }


    //@Test
    void extractObjectsReactive_shouldReturnServerSentEvents() {
        DatabaseExtractRequest request = getDatabaseExtractRequest();
        DatabaseExtractResponse response =getDatabaseExtractResponse();
        when(formatServiceFactory.getFormatService(any())).thenReturn(formatService);
        when(databaseService.extract(any())).thenReturn(Flux.just(new DatabaseObject("table1", DatabaseObjectType.TABLE,"extract1", "")));
        when(formatService.formatAsString(any())).thenReturn(Flux.just(response));

        Flux<ServerSentEvent<DatabaseExtractResponse>> result = controller.extractObjectsReactive(request);

        StepVerifier.create(result)
                .assertNext(event -> assertEquals("formatted response", event.data()))
                .verifyComplete();
    }

    @Test
    void extractObjectsReactive_shouldHandleErrorGracefully() {
        DatabaseExtractRequest request = getDatabaseExtractRequest();
        when(formatServiceFactory.getFormatService(any())).thenReturn(formatService);
        when(databaseService.extract(any())).thenReturn(Flux.error(new RuntimeException("error")));

        Flux<ServerSentEvent<DatabaseExtractResponse>> result = controller.extractObjectsReactive(request);

        StepVerifier.create(result)
                .assertNext(event -> {
                    assertEquals("error", event.event());
                    assertEquals("error", event.data().extract());
                })
                .verifyComplete();
    }

    public DatabaseExtractRequest getDatabaseExtractRequest() {
      return new DatabaseExtractRequest(
          "localhost",
          "SALES_US",
          "sa",
          "dbo",
          "fipdev",
          "json", // Assuming outputFormat is "json"
          DatabaseType.MSSQL_SERVER,
          List.of(DatabaseObjectType.TABLE),
          List.of(".*"),
          List.of(".*"),
          List.of(".*"),
          List.of(".*"),
          List.of(".*"));
    }

    public DatabaseExtractResponse getDatabaseExtractResponse() {
      return new DatabaseExtractResponse(DatabaseObjectType.TABLE, "table1", "extract1", 1);
    }
}