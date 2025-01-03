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

import dev.bsingh.dbmetadata.extractor.service.database.QueryProvider;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SQLServerQueryProvider implements QueryProvider {
  public static final String PARAMETER_PLACE_HOLDER = "P";

  @Override
  public String getTableCountQuery(List<String> patterns) {
    return """
        SELECT COUNT(*) as count FROM INFORMATION_SCHEMA.TABLES
        WHERE TABLE_TYPE = 'BASE TABLE' 
        AND TABLE_SCHEMA = @schemaName
        """ + buildPatternFilter("TABLE_NAME", patterns);
  }

  @Override
  public String getViewCountQuery(List<String> patterns){
    return """
        SELECT COUNT(*) as count FROM sys.objects
        WHERE type = 'V' AND is_ms_shipped = 0
        AND SCHEMA_NAME(schema_id) = @schemaName
        """ + buildPatternFilter("name", patterns);
  }

  @Override
  public String getFunctionCountQuery(List<String> patterns){
    return """
        
        SELECT COUNT(*) as count FROM sys.objects
        WHERE type IN ('FN', 'IF', 'TF')
        AND is_ms_shipped = 0
        AND SCHEMA_NAME(schema_id) = @schemaName
        """ + buildPatternFilter("name", patterns);
  }

  @Override
  public String getProcedureCountQuery(List<String> patterns){
    return """
        SELECT COUNT(*) as count FROM sys.objects
        WHERE type = 'P' AND is_ms_shipped = 0
        AND SCHEMA_NAME(schema_id) = @schemaName
        """ + buildPatternFilter("name", patterns);
  }

  @Override
  public String getTriggerCountQuery(List<String> patterns){
    return """
        SELECT COUNT(*) as count
         FROM sys.triggers t INNER JOIN sys.objects o ON t.parent_id = o.object_id
         WHERE t.is_ms_shipped = 0 AND t.parent_id > 0
         AND SCHEMA_NAME(o.schema_id) = @schemaName
        """ + buildPatternFilter("t.name", patterns);
  }


  @Override
  public String getTablesQuery(List<String> patterns) {
    return """
        SELECT
         TABLE_NAME AS name,
         TABLE_SCHEMA AS [schema],
         'CREATE TABLE [' + t.TABLE_SCHEMA + '].[' + t.TABLE_NAME + '] (' +
         CHAR(13) + CHAR(10) +
         STUFF((
             SELECT
                 ',' + CHAR(13) + CHAR(10) +
                 ' [' + c.COLUMN_NAME + '] ' +
                 c.DATA_TYPE +
                 CASE
                     WHEN c.CHARACTER_MAXIMUM_LENGTH IS NOT NULL
                     THEN '(' + CASE
                         WHEN c.CHARACTER_MAXIMUM_LENGTH = -1 THEN 'MAX'
                         ELSE CAST(c.CHARACTER_MAXIMUM_LENGTH AS VARCHAR)
                     END + ')'
                     ELSE ''
                 END +
                 CASE WHEN c.IS_NULLABLE = 'NO' THEN ' NOT NULL' ELSE ' NULL' END +
                 CASE WHEN tc.CONSTRAINT_TYPE = 'PRIMARY KEY' THEN ' PRIMARY KEY' ELSE '' END
             FROM INFORMATION_SCHEMA.COLUMNS c
             LEFT JOIN INFORMATION_SCHEMA.KEY_COLUMN_USAGE kcu
                 ON c.TABLE_SCHEMA = kcu.TABLE_SCHEMA
                 AND c.TABLE_NAME = kcu.TABLE_NAME
                 AND c.COLUMN_NAME = kcu.COLUMN_NAME
             LEFT JOIN INFORMATION_SCHEMA.TABLE_CONSTRAINTS tc
                 ON kcu.TABLE_SCHEMA = tc.TABLE_SCHEMA
                 AND kcu.TABLE_NAME = tc.TABLE_NAME
                 AND kcu.CONSTRAINT_NAME = tc.CONSTRAINT_NAME
             WHERE c.TABLE_SCHEMA = t.TABLE_SCHEMA
             AND c.TABLE_NAME = t.TABLE_NAME
             FOR XML PATH(''), TYPE
         ).value('.', 'NVARCHAR(MAX)'), 1, 2, '') +
         CHAR(13) + CHAR(10) +
         ')' AS definition
     FROM INFORMATION_SCHEMA.TABLES t
     WHERE t.TABLE_TYPE = 'BASE TABLE'
     AND TABLE_SCHEMA = @schemaName
     """ + buildPatternFilter("TABLE_NAME", patterns) + """
     ORDER BY t.TABLE_NAME
OFFSET @offset ROWS FETCH NEXT @pageSize ROWS ONLY
            """;
  }

  @Override
  public String getForeignKeysQuery() {
    return """
        SELECT
          fk.name AS name,
          OBJECT_SCHEMA_NAME(fk.parent_object_id) AS [schema],
          'ALTER TABLE [' + OBJECT_SCHEMA_NAME(fk.parent_object_id) + '].[' +
          OBJECT_NAME(fk.parent_object_id) +
          '] ADD CONSTRAINT [' + fk.name + '] FOREIGN KEY (' +
          STUFF((
              SELECT\
                  ',' + COL_NAME(fkc.parent_object_id, fkc.parent_column_id)
              FROM sys.foreign_key_columns fkc
              WHERE fkc.constraint_object_id = fk.object_id
              FOR XML PATH(''), TYPE
          ).value('.', 'NVARCHAR(MAX)'), 1, 1, '') +
          ') REFERENCES [' + OBJECT_SCHEMA_NAME(fk.referenced_object_id) + '].[' +
          OBJECT_NAME(fk.referenced_object_id) + '] (' +
          STUFF((
              SELECT
                  ',' + COL_NAME(fkc.referenced_object_id, fkc.referenced_column_id)
              FROM sys.foreign_key_columns fkc
              WHERE fkc.constraint_object_id = fk.object_id
              FOR XML PATH(''), TYPE
          ).value('.', 'NVARCHAR(MAX)'), 1, 1, '') +
          ');' AS definition
        FROM sys.foreign_keys fk
        WHERE OBJECT_SCHEMA_NAME(fk.parent_object_id) = @schemaName
            AND OBJECT_NAME(fk.parent_object_id) = @tableName
        ORDER BY fk.name;
        """;
  }

  @Override
  public String getIndexesQuery() {
    return """
        SELECT
            i.name AS name,
            t.TABLE_SCHEMA AS [schema],
            STUFF((
                SELECT
                    ',' + COL_NAME(ic.object_id, ic.column_id) +
                    CASE
                        WHEN ic.is_descending_key = 1 THEN ' DESC'
                        ELSE ' ASC'
                    END
                FROM sys.index_columns ic
                WHERE ic.object_id = i.object_id
                    AND ic.index_id = i.index_id
                FOR XML PATH(''), TYPE
            ).value('.', 'NVARCHAR(MAX)'), 1, 1, '') AS Column_Names,
            'CREATE ' +
            CASE
                WHEN i.is_unique = 1 THEN 'UNIQUE '
                ELSE ''
            END +
            'INDEX [' + i.name + '] ON [' + t.TABLE_SCHEMA + '].[' + t.TABLE_NAME + '] (' +
            STUFF((
                SELECT
                    ',' + COL_NAME(ic.object_id, ic.column_id) +
                    CASE
                        WHEN ic.is_descending_key = 1 THEN ' DESC'
                        ELSE ' ASC'
                    END
                FROM sys.index_columns ic
                WHERE ic.object_id = i.object_id
                    AND ic.index_id = i.index_id
                FOR XML PATH(''), TYPE
            ).value('.', 'NVARCHAR(MAX)'), 1, 1, '') +
            ');' AS definition
        FROM sys.indexes i
        INNER JOIN INFORMATION_SCHEMA.TABLES t
            ON OBJECT_SCHEMA_NAME(i.object_id) = t.TABLE_SCHEMA
            AND OBJECT_NAME(i.object_id) = t.TABLE_NAME
        WHERE t.TABLE_TYPE = 'BASE TABLE'
            AND t.TABLE_SCHEMA = @schemaName
            AND t.TABLE_NAME = @tableName
            AND i.type IN (1, 2)
            AND i.is_primary_key = 0
            AND i.is_unique_constraint = 0
            AND i.is_hypothetical = 0
        ORDER BY i.name;
    """;
  }

  @Override
  public String getConstraintsQuery() {
    return """
       SELECT
         i.name AS name,
         OBJECT_SCHEMA_NAME(i.object_id) AS [schema],
         STUFF((
             SELECT
                 ',' + COL_NAME(ic.object_id, ic.column_id)
             FROM sys.index_columns ic
             WHERE ic.object_id = i.object_id
                 AND ic.index_id = i.index_id
             FOR XML PATH(''), TYPE
         ).value('.', 'NVARCHAR(MAX)'), 1, 1, '') AS Column_Names,
         'ALTER TABLE [' + OBJECT_SCHEMA_NAME(i.object_id) + '].[' +
         OBJECT_NAME(i.object_id) +
         '] ADD CONSTRAINT [' + i.name + '] UNIQUE (' +
         STUFF((
             SELECT
                 ',' + COL_NAME(ic.object_id, ic.column_id)
             FROM sys.index_columns ic
             WHERE ic.object_id = i.object_id
                 AND ic.index_id = i.index_id
             FOR XML PATH(''), TYPE
         ).value('.', 'NVARCHAR(MAX)'), 1, 1, '') +
         ');' AS definition
     FROM sys.indexes i
     WHERE i.is_unique_constraint = 1
         AND OBJECT_SCHEMA_NAME(i.object_id) = @schemaName
         AND OBJECT_NAME(i.object_id) = @tableName
     ORDER BY i.name;
    """;
  }

  @Override
  public String getViewsQuery(List<String> patterns){
    return """
       SELECT
           o.name AS name,
           SCHEMA_NAME(o.schema_id) AS [schema],
           sm.definition AS definition
       FROM sys.objects o
       INNER JOIN sys.sql_modules sm
           ON o.object_id = sm.object_id
       WHERE o.type = 'V'
         AND o.is_ms_shipped = 0
         AND SCHEMA_NAME(o.schema_id) = @schemaName
       """ + buildPatternFilter("o.name", patterns) + """
       ORDER BY o.name
       OFFSET @offset ROWS FETCH NEXT @pageSize ROWS ONLY
    """;
  }

  @Override
  public String getFunctionsQuery(List<String> patterns){
    return """
       SELECT
           o.name AS Name,
            SCHEMA_NAME(o.schema_id) AS [schema],
           sm.definition AS definition
       FROM sys.objects o
       INNER JOIN sys.sql_modules sm
           ON o.object_id = sm.object_id
       WHERE o.type IN ('FN', 'IF', 'TF')
         AND o.is_ms_shipped = 0
         AND SCHEMA_NAME(o.schema_id) = @schemaName
       """ + buildPatternFilter("o.name", patterns) + """
       ORDER BY o.name
        OFFSET @offset ROWS FETCH NEXT @pageSize ROWS ONLY
    """;
  }

  @Override
  public String getProceduresQuery(List<String> patterns){
    return """
      SELECT
            o.name AS name,
            SCHEMA_NAME(o.schema_id) AS [schema],
            sm.definition AS definition
        FROM sys.objects o
        INNER JOIN sys.sql_modules sm
            ON o.object_id = sm.object_id
        WHERE o.type = 'P'
            AND o.is_ms_shipped = 0
            AND SCHEMA_NAME(o.schema_id) = @schemaName
       """ + buildPatternFilter("o.name", patterns) + """
        ORDER BY o.name
      OFFSET @offset ROWS FETCH NEXT @pageSize ROWS ONLY
    """;
  }

  @Override
  public String getTriggersQuery(List<String> patterns){
    return """
     SELECT
        t.name AS name,
        SCHEMA_NAME(o.schema_id) AS [schema],
        sm.definition AS definition
    FROM sys.triggers t
    INNER JOIN sys.sql_modules sm
        ON t.object_id = sm.object_id
    INNER JOIN sys.objects o
        ON t.parent_id = o.object_id
    WHERE t.is_ms_shipped = 0
        AND t.parent_id > 0
        AND SCHEMA_NAME(o.schema_id) = @schemaName
    """ + buildPatternFilter("t.name", patterns) + """
    ORDER BY t.name
    OFFSET @offset ROWS FETCH NEXT @pageSize ROWS ONLY
    """;
  }

  private String buildPatternFilter(String paramName, List<String> patterns) {
    if (patterns == null || patterns.isEmpty()) {
      return "";
    }
    boolean foundValidPattern = false;
    StringBuilder filter = new StringBuilder(" AND (");
    for (int i = 0; i < patterns.size(); i++) {
      if(patterns.get(i) == null || patterns.get(i).isEmpty()){
        continue;
      }
      foundValidPattern = true;
      if (i > 0) {
        filter.append(" OR ");
      }
      filter.append(paramName)
            .append(" LIKE @")
            .append(PARAMETER_PLACE_HOLDER)
            .append(i);
    }
    filter.append(" )");
    if (!foundValidPattern) {
      return "";
    }
    return filter.toString();
  }
}
