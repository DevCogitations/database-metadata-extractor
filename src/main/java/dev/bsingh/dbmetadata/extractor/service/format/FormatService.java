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
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import reactor.core.publisher.Flux;

import java.util.List;

public interface FormatService {
  byte[] format(List<DatabaseObject> objects);
  Flux<DatabaseExtractResponse> formatAsString(List<DatabaseObject> objects);
  MediaType getMediaType();
  String getFilename();
  void setHeaders(HttpHeaders headers);
}
