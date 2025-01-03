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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;

@Service
public class TextFormatService implements FormatService {

  private TokenService tokenService;

  public TextFormatService( @Autowired TokenService tokenService) {
    this.tokenService = tokenService;
  }
  @Override
  public byte[] format(List<DatabaseObject> objects) {
    StringBuilder sb = new StringBuilder();
    for (DatabaseObject obj : objects) {
      sb.append(obj.toString()).append("\n\n");
    }
    return sb.toString().getBytes();
  }

  @Override
  public Flux<DatabaseExtractResponse> formatAsString(List<DatabaseObject> objects) {
    return Flux.fromIterable(objects)
               .map(obj -> {
                  System.out.println(obj.getName());
                  String extract = obj + "";
                  int token = tokenService.getTokens(extract);
                  return new DatabaseExtractResponse(obj.getType(), obj.getName(), extract, token);
               }).onErrorResume(e -> Flux.error(
                   new RuntimeException("Error formatting as text", e)
               ));
  }

  @Override
  public MediaType getMediaType() {
    return MediaType.TEXT_PLAIN;
  }

  @Override
  public String getFilename() {
    return "output.txt";
  }

  @Override
  public void setHeaders(HttpHeaders headers) {
    headers.setContentType(getMediaType());
  }
}