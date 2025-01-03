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

import lombok.Getter;
import lombok.Setter;
import org.springframework.util.StringUtils;

import java.util.List;

@Setter
@Getter
public class DatabaseTableObject extends DatabaseObject {
  private List<DatabaseObject> indexes;
  private List<DatabaseObject> foreignKeys;
  private List<DatabaseObject> constraints;

  public DatabaseTableObject(String name, DatabaseObjectType type, String schema, String definition) {
    super(name, type, schema, definition);
  }
  public DatabaseTableObject(String name, String schema, String definition) {
    super(name, DatabaseObjectType.TABLE, schema, definition);
  }

  @Override
  public String toString() {
    return
        """
        ---------------------------------------------
        --            %s: %s.%s
        ---------------------------------------------
        %s
        
        %s
        %s
        %s
        """.formatted(
            getType(),
            getSchema(),
            getName(),
            getDefinition(),
            StringUtils.collectionToDelimitedString(indexes, "\n"),
            StringUtils.collectionToDelimitedString(foreignKeys, "\n"),
            StringUtils.collectionToDelimitedString(constraints, "\n")
        );
  }
}
