/*
 * Copyright (c) 2024 Database Metadata Extractor
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://opensource.org/licenses/MIT
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 *     bsingh - Initial implementation
 */
package dev.bsingh.dbmetadata.extractor.controller;

import dev.bsingh.dbmetadata.extractor.model.DatabaseExtractRequest;
import dev.bsingh.dbmetadata.extractor.model.DatabaseExtractResponse;
import dev.bsingh.dbmetadata.extractor.service.DatabaseMetadataService;
import dev.bsingh.dbmetadata.extractor.service.format.FormatService;
import dev.bsingh.dbmetadata.extractor.service.format.FormatServiceFactory;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;

@RestController
@RequestMapping("/api")
@Log
public class DatabaseMetadataController {
  private static final int BUFFER_SIZE = 20;

  @Autowired
  private DatabaseMetadataService databaseService;

  @Autowired
  private FormatServiceFactory formatServiceFactory;


  @PostMapping("/extract/download")
  public Mono<ResponseEntity<byte[]>> extractObjects(@RequestBody DatabaseExtractRequest request) {
    FormatService formatService = formatServiceFactory.getFormatService(request.outputFormat());
    return databaseService.extract(request)
      .collectList()
      .flatMap(objects -> {
        byte[] content = formatService.format(objects);
        HttpHeaders headers = new HttpHeaders();
        formatService.setHeaders(headers);
        headers.setContentDispositionFormData("attachment", formatService.getFilename());
        return Mono.just(ResponseEntity.ok().headers(headers).body(content));
      })
      .onErrorResume(e -> Mono.just(ResponseEntity.internalServerError().body(e.getMessage().getBytes())));
  }

  @PostMapping(value = "/extract", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  public Flux<ServerSentEvent<DatabaseExtractResponse>> extractObjectsReactive(@RequestBody DatabaseExtractRequest request) {
    FormatService formatService = formatServiceFactory.getFormatService(request.outputFormat());
    return databaseService.extract(request)
      .bufferTimeout(BUFFER_SIZE, Duration.ofMillis(100))
      .flatMap(formatService::formatAsString)
      .map(data -> ServerSentEvent.<DatabaseExtractResponse>builder().data(data).build())
      .onErrorResume(e -> Flux.just(ServerSentEvent.<DatabaseExtractResponse>builder()
        .event("error")
        .data(new DatabaseExtractResponse(null, "", e.getMessage(), 0))
        .build()))
      .doOnCancel(() -> log.info("Client cancelled the request"))
      .subscribeOn(Schedulers.boundedElastic());
  }
}