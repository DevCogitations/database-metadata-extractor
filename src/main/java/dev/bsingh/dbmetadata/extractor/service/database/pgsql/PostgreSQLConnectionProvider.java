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
package dev.bsingh.dbmetadata.extractor.service.database.pgsql;

import dev.bsingh.dbmetadata.extractor.model.DatabaseExtractRequest;
import dev.bsingh.dbmetadata.extractor.service.database.ConnectionProvider;
import io.r2dbc.postgresql.PostgresqlConnectionConfiguration;
import io.r2dbc.postgresql.PostgresqlConnectionFactory;
import io.r2dbc.postgresql.api.PostgresqlConnection;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class PostgreSQLConnectionProvider implements ConnectionProvider {
  public Mono<PostgresqlConnection> getConnection(DatabaseExtractRequest request) {
    PostgresqlConnectionConfiguration config = PostgresqlConnectionConfiguration.builder()
                                                                                .host(request.server())
                                                                                .database(request.database())
                                                                                .username(request.username())
                                                                                .password(request.password())
                                                                                .build();

    PostgresqlConnectionFactory factory = new PostgresqlConnectionFactory(config);
    return factory.create();
  }
}
