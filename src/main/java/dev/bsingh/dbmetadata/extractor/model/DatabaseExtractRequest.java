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
package dev.bsingh.dbmetadata.extractor.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record DatabaseExtractRequest(
    String server,
    String database,
    String username,
    String schema,
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) String password,
    String outputFormat,
    DatabaseType dbType,
    List<DatabaseObjectType> objectTypes,
    List<String> tablePatterns,
    List<String> viewPatterns,
    List<String> procedurePatterns,
    List<String> functionPatterns,
    List<String> triggerPatterns

) {
  private static final String DEFAULT_SCHEMA = "dbo";

  public DatabaseExtractRequest {
    if (schema == null) {
      schema = DEFAULT_SCHEMA;
    }
  }

  public List<DatabaseObjectType> getObjectTypes() {
    if (objectTypes.isEmpty()) {
      return List.of(DatabaseObjectType.TABLE, DatabaseObjectType.VIEW, DatabaseObjectType.PROCEDURE,
                     DatabaseObjectType.FUNCTION, DatabaseObjectType.TRIGGER);
    } else {
      return objectTypes;
    }
  }
}