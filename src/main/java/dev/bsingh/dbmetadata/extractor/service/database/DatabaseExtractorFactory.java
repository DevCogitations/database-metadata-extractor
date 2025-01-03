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
package dev.bsingh.dbmetadata.extractor.service.database;

import dev.bsingh.dbmetadata.extractor.exception.UnsupportedDatabaseException;
import dev.bsingh.dbmetadata.extractor.model.DatabaseType;
import dev.bsingh.dbmetadata.extractor.service.database.mssqlserver.SQLServerMetadataExtractor;
import dev.bsingh.dbmetadata.extractor.service.database.pgsql.PostgreSQLMetadataExtractor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class DatabaseExtractorFactory {


  private SQLServerMetadataExtractor  sqlServerExtractor;
  private PostgreSQLMetadataExtractor postgreSQLExtractor;

  DatabaseExtractorFactory(@Autowired SQLServerMetadataExtractor sqlServerExtractor,
                           @Autowired PostgreSQLMetadataExtractor postgreSQLExtractor) {
    this.sqlServerExtractor = sqlServerExtractor;
    this.postgreSQLExtractor = postgreSQLExtractor;
  }


  public DatabaseMetadataExtractor getExtractor(DatabaseType dbType) {
    return switch (dbType) {
      case MSSQL_SERVER -> sqlServerExtractor;
      case POSTGRESQL -> postgreSQLExtractor;
      default -> throw new UnsupportedDatabaseException("Unsupported database type: " + dbType);
    };
  }
}
