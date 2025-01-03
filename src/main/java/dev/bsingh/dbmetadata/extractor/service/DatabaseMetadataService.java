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
package dev.bsingh.dbmetadata.extractor.service;

import dev.bsingh.dbmetadata.extractor.model.DatabaseExtractRequest;
import dev.bsingh.dbmetadata.extractor.model.DatabaseObject;
import dev.bsingh.dbmetadata.extractor.service.database.DatabaseExtractorFactory;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@Setter
public class DatabaseMetadataService {

  private DatabaseExtractorFactory databaseExtractorFactory;

  public DatabaseMetadataService(@Autowired DatabaseExtractorFactory databaseExtractorFactory) {
    this.databaseExtractorFactory = databaseExtractorFactory;
  }

  public Flux<DatabaseObject> extract(DatabaseExtractRequest request) {
    return Mono.fromSupplier(() -> databaseExtractorFactory.getExtractor(request.dbType()))
               .flatMapMany(extractor -> extractor.extract(request));
  }

}