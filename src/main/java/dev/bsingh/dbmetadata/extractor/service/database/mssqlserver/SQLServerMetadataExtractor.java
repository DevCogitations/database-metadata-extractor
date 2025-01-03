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
package dev.bsingh.dbmetadata.extractor.service.database.mssqlserver;

import dev.bsingh.dbmetadata.extractor.model.DatabaseObject;
import dev.bsingh.dbmetadata.extractor.model.DatabaseObjectType;
import dev.bsingh.dbmetadata.extractor.model.DatabaseTableObject;
import dev.bsingh.dbmetadata.extractor.model.DatabaseExtractRequest;
import dev.bsingh.dbmetadata.extractor.service.database.DatabaseMetadataExtractor;
import io.r2dbc.spi.Connection;
import io.r2dbc.spi.Row;
import io.r2dbc.spi.Statement;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

@Log4j2
@Component
public class SQLServerMetadataExtractor implements DatabaseMetadataExtractor {

  private final SQLServerConnectionProvider sqlServerConnectionProvider;
  private final SQLServerQueryProvider      sqlServerQueryProvider;

  public SQLServerMetadataExtractor(@Autowired SQLServerConnectionProvider sqlServerConnectionProvider,
                                    @Autowired SQLServerQueryProvider sqlServerQueryProvider) {
    this.sqlServerConnectionProvider = sqlServerConnectionProvider;
    this.sqlServerQueryProvider = sqlServerQueryProvider;
  }


  @Override
  public Flux<DatabaseObject> extract(DatabaseExtractRequest request) {
    return sqlServerConnectionProvider.getConnection(request)
            .flatMapMany(connection -> Flux.fromIterable(request.getObjectTypes())
                 .flatMapSequential(type -> switch (type) {
                   case TABLE -> extractTables(connection, request);
                   case VIEW -> extractViews(connection, request);
                   case PROCEDURE ->
                       extractProcedures(connection, request);
                   case FUNCTION ->
                       extractFunctions(connection, request);
                   case TRIGGER ->
                       extractTriggers(connection, request);
                   default -> Flux.empty();
                 }));
  }

  public Flux<DatabaseObject> extractTables(Connection connection, DatabaseExtractRequest extractRequest) {
    return extractObjectsPaginated(statementWithSchemaBindingSupplier(connection,
                                                                      sqlServerQueryProvider.getTableCountQuery(extractRequest.tablePatterns()),
                                                                      extractRequest.schema(), extractRequest.tablePatterns()),
                                   statementWithSchemaBindingSupplier(connection,
                                                                      sqlServerQueryProvider.getTablesQuery(extractRequest.tablePatterns()),
                                                                      extractRequest.schema(), extractRequest.tablePatterns()),
                                   row -> mapToDBObject(row, DatabaseObjectType.TABLE, DatabaseTableObject.class))

        .flatMap(table -> {
          Flux<DatabaseObject> indexes     = extractIndexes(connection, extractRequest, table.getName());
          Flux<DatabaseObject> foreignKeys = extractForeignKeys(connection, extractRequest, table.getName());
          Flux<DatabaseObject> constraints = extractUniqueConstraints(connection, extractRequest, table.getName());
          return Mono.zip(indexes.collectList(), foreignKeys.collectList(), constraints.collectList())
                     .map(tuple -> {
                       table.setIndexes(tuple.getT1());
                       table.setForeignKeys(tuple.getT2());
                       table.setConstraints(tuple.getT3());
                       return table;
                     });
        });
  }


  private Flux<DatabaseObject> extractIndexes(Connection connection, DatabaseExtractRequest extractRequest, String tableName) {
    return extractObjectsByTable(
        statementWithSchemaBindingSupplier(connection, extractRequest, sqlServerQueryProvider.getIndexesQuery()),
        row -> mapToDBObject(row, DatabaseObjectType.INDEX, DatabaseObject.class), tableName);
  }

  private Flux<DatabaseObject> extractForeignKeys(Connection connection, DatabaseExtractRequest extractRequest, String tableName) {
    return extractObjectsByTable(
        statementWithSchemaBindingSupplier(connection, extractRequest, sqlServerQueryProvider.getForeignKeysQuery()),
        row -> mapToDBObject(row, DatabaseObjectType.FOREIGN_KEY, DatabaseObject.class), tableName);

  }

  public Flux<DatabaseObject> extractUniqueConstraints(Connection connection, DatabaseExtractRequest extractRequest,
                                                       String tableName) {
    return extractObjectsByTable(
        statementWithSchemaBindingSupplier(connection, extractRequest, sqlServerQueryProvider.getConstraintsQuery()),
        row -> mapToDBObject(row, DatabaseObjectType.CONSTRAINT, DatabaseObject.class), tableName);
  }

  public Flux<DatabaseObject> extractViews(Connection connection, DatabaseExtractRequest extractRequest) {
    return extractObjectsPaginated(statementWithSchemaBindingSupplier(connection,
                                                                      sqlServerQueryProvider.getViewCountQuery(extractRequest.viewPatterns())
                                   , extractRequest.schema(), extractRequest.viewPatterns()),
                                   statementWithSchemaBindingSupplier(connection,
                                                                      sqlServerQueryProvider.getViewsQuery(extractRequest.viewPatterns()),
                                                                      extractRequest.schema(), extractRequest.viewPatterns()),
                                   row -> mapToDBObject(row, DatabaseObjectType.VIEW, DatabaseObject.class));

  }

  public Flux<DatabaseObject> extractProcedures(Connection connection, DatabaseExtractRequest extractRequest) {
    return extractObjectsPaginated(statementWithSchemaBindingSupplier(connection,
                                                                      sqlServerQueryProvider.getProcedureCountQuery(extractRequest.procedurePatterns()),
                                                                      extractRequest.schema(),
                                                                      extractRequest.procedurePatterns()),
                                   statementWithSchemaBindingSupplier(connection,
                                                                      sqlServerQueryProvider.getProceduresQuery(extractRequest.procedurePatterns()),
                                                                      extractRequest.schema(), extractRequest.procedurePatterns()),
                                   row -> mapToDBObject(row, DatabaseObjectType.PROCEDURE, DatabaseObject.class));
  }

  public Flux<DatabaseObject> extractFunctions(Connection connection, DatabaseExtractRequest extractRequest) {
    return extractObjectsPaginated(statementWithSchemaBindingSupplier(connection,
                                                                      sqlServerQueryProvider.getFunctionCountQuery(extractRequest.viewPatterns()),
                                                                      extractRequest.schema(), extractRequest.viewPatterns()),
                                   statementWithSchemaBindingSupplier(connection,
                                                                      sqlServerQueryProvider.getFunctionsQuery(extractRequest.functionPatterns()),
                                                                      extractRequest.schema(), extractRequest.functionPatterns()),
                                   row -> mapToDBObject(row, DatabaseObjectType.FUNCTION, DatabaseObject.class));
  }

  public Flux<DatabaseObject> extractTriggers(Connection connection, DatabaseExtractRequest extractRequest) {
    return extractObjectsPaginated(statementWithSchemaBindingSupplier(connection,
                                                                      sqlServerQueryProvider.getTriggerCountQuery(extractRequest.triggerPatterns()),
                                                                      extractRequest.schema(), extractRequest.triggerPatterns()),
                                   statementWithSchemaBindingSupplier(connection,
                                                                      sqlServerQueryProvider.getTriggersQuery(extractRequest.triggerPatterns()),
                                                                      extractRequest.schema(), extractRequest.triggerPatterns()),
                                   row -> mapToDBObject(row, DatabaseObjectType.TRIGGER, DatabaseObject.class));
  }

  private Supplier<Statement> statementWithSchemaBindingSupplier(Connection connection,
                                                                 DatabaseExtractRequest extractRequest,
                                                                 String query) {
    return () -> connection.createStatement(query)
                           .bind("schemaName", extractRequest.schema());
  }

  private Supplier<Statement> statementWithSchemaBindingSupplier(Connection connection,
                                                                 String query,
                                                                 String schema,
                                                                 List<String> patterns) {
    return () -> {
      Statement statement = connection.createStatement(query)
                                      .bind("schemaName", schema);
      for (int i = 0; i < patterns.size(); i++) {
        String pattern = normalizePattern(patterns.get(i));
        if(pattern.isEmpty()) {
          continue;
        }
        statement.bind(SQLServerQueryProvider.PARAMETER_PLACE_HOLDER + i, pattern);
      }
      return statement;
    };
  }

  private String normalizePattern(String pattern) {
    if(pattern == null && pattern.isEmpty()) {
      return "";
    }
    return pattern.replaceAll("\\*", "%");
  }

  private <T extends DatabaseObject> T mapToDBObject(Row row, DatabaseObjectType objectType, Class<T> type) {
    String name       = row.get("name", String.class);
    String schema     = row.get("schema", String.class);
    String definition = row.get("definition", String.class);
    try {
      return type.getConstructor(String.class, DatabaseObjectType.class, String.class, String.class)
                 .newInstance(name, objectType, schema, definition);
    } catch (Exception e) {
      throw new RuntimeException("Failed to create instance of " + type.getName(), e);
    }
  }

  private <T extends DatabaseObject> Flux<T> extractObjectsPaginated(Supplier<Statement> countQuerySupplier,
                                                                     Supplier<Statement> pageQuerySupplier,
                                                                     Function<Row, T> mapper) {
    return Mono.from(countQuerySupplier.get().execute())
               .flatMap(result -> Mono.from(result.map((row, meta) -> row.get("count", Integer.class))))
               .flatMapMany(totalRecords -> {
                 int totalPages = (int) Math.ceil((double) totalRecords / PAGE_SIZE);
                 return Flux.range(0, totalPages)
                            .flatMapSequential(page -> Flux.from(pageQuerySupplier.get()
                                                                                  .bind("offset", page * PAGE_SIZE)
                                                                                  .bind("pageSize", PAGE_SIZE)
                                                                                  .execute()), 1)
                            .flatMap(result -> result.map((row, meta) -> mapper.apply(row)));

               });
  }

  private <T extends DatabaseObject> Flux<T> extractObjectsByTable(Supplier<Statement> schemaBoundedQuerySupplier,
                                                                   Function<Row, T> mapper,
                                                                   String tableName) {
    return Flux.from(schemaBoundedQuerySupplier.get()
                                               .bind("tableName", tableName)
                                               .execute())
               .flatMap(result -> result.map((row, meta) -> mapper.apply(row)));
  }


}
