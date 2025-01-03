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

import java.util.List;

public interface QueryProvider {

  String getTableCountQuery(List<String> patterns);
  String getViewCountQuery(List<String> patterns);
  String getProcedureCountQuery(List<String> patterns);
  String getFunctionCountQuery(List<String> patterns);
  String getTriggerCountQuery(List<String> patterns);

  String getTablesQuery(List<String> patterns);
  String getViewsQuery(List<String> patterns);
  String getFunctionsQuery(List<String> patterns);
  String getTriggersQuery(List<String> patterns);
  String getProceduresQuery(List<String> patterns);
  String getConstraintsQuery();
  String getForeignKeysQuery();
  String getIndexesQuery();

}
