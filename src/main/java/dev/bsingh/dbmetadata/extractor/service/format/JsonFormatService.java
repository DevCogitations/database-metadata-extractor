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
package dev.bsingh.dbmetadata.extractor.service.format;

import dev.bsingh.dbmetadata.extractor.model.DatabaseExtractResponse;
import dev.bsingh.dbmetadata.extractor.model.DatabaseObject;
import dev.bsingh.dbmetadata.extractor.service.token.TokenService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;

@Service
public class JsonFormatService implements FormatService {
  public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  private final TokenService tokenService;

  @Autowired
  public JsonFormatService(TokenService tokenService) {
    this.tokenService = tokenService;
  }

  @Override
  public byte[] format(List<DatabaseObject> objects) {
    try {
      return OBJECT_MAPPER.writeValueAsBytes(objects);
    } catch (Exception e) {
      throw new RuntimeException("Error formatting as JSON", e);
    }
  }

  @Override
  public Flux<DatabaseExtractResponse> formatAsString(List<DatabaseObject> objects) {
    try {
      return Flux.fromIterable(objects)
                 .<DatabaseExtractResponse>handle((obj, sink) -> {
                   try {
                     String json = OBJECT_MAPPER.writeValueAsString(obj);
                     int token = tokenService.getTokens(json);
                     sink.next(new DatabaseExtractResponse(obj.getType(), obj.getName(), json, token));
                   } catch (Exception e) {
                     sink.error(new RuntimeException("Error formatting as JSON", e));
                   }
                 })
                 .onErrorResume(e -> Flux.error(
                     new RuntimeException("Error formatting as JSON", e)
                 ));
    } catch (Exception e) {
      throw new RuntimeException("Error formatting as JSON", e);
    }
  }
  @Override
  public MediaType getMediaType() {
    return MediaType.APPLICATION_JSON;
  }

  @Override
  public String getFilename() {
    return "output.json";
  }

  @Override
  public void setHeaders(HttpHeaders headers) {
    headers.setContentType(getMediaType());
  }
}
