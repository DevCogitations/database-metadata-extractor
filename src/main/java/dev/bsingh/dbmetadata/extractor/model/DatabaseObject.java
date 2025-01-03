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

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class DatabaseObject {
  private String             name;
  private DatabaseObjectType type;
  private String             schema;
  private String       definition;

  @Override
  public String toString() {
    return switch (type) {
      case INDEX, CONSTRAINT, FOREIGN_KEY -> this.definition;
      default -> """
          ---------------------------------------------
          --      %s:  %s.%s
          ---------------------------------------------
          %s
          """.formatted(
          getType(),
          getSchema(),
          getName(),
          getDefinition()
      );
    };
  }
}
