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


import dev.bsingh.dbmetadata.extractor.service.database.QueryProvider;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PostgreSQLQueryProvider implements QueryProvider {

  @Override
  public String getTableCountQuery(List<String> patterns) {
    return "";
  }

  @Override
  public String getViewCountQuery(List<String> patterns) {
    return "";
  }

  @Override
  public String getProcedureCountQuery(List<String> patterns) {
    return "";
  }

  @Override
  public String getFunctionCountQuery(List<String> patterns) {
    return "";
  }

  @Override
  public String getTriggerCountQuery(List<String> patterns) {
    return "";
  }

  @Override
  public String getTablesQuery(List<String> patterns) {
    return "";
  }

  @Override
  public String getViewsQuery(List<String> patterns) {
    return "";
  }

  @Override
  public String getIndexesQuery() {
    return "";
  }

  @Override
  public String getFunctionsQuery(List<String> patterns) {
    return "";
  }

  @Override
  public String getTriggersQuery(List<String> patterns) {
    return "";
  }

  @Override
  public String getProceduresQuery(List<String> patterns) {
    return "";
  }

  @Override
  public String getConstraintsQuery() {
    return "";
  }

  @Override
  public String getForeignKeysQuery() {
    return "";
  }
}
