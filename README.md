# Database Metadata Extractor
[![Java Version](https://img.shields.io/badge/Java-17%2B-blue.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

Database Metadata Extractor is a Spring Boot application that extracts database metadata, making it easier to integrate database schemas with AI tools like vector databases, LLMs, and GitHub Copilot.

![Database Extractor Screenshot](https://github.com/DevCogitations/database-metadata-extractor/main/docs/screenshot.png)

## 🚀 Features

- **Multi-Database Support**
  - Microsoft SQL Server
  - PostgreSQL (more coming soon)

- **Comprehensive Object Extraction**
  - Tables
  - Views
  - Stored Procedures
  - Functions
  - Triggers

- **Smart Filtering**
  - Pattern-based filtering for each object type
  - Schema-level filtering
  - Custom object selection

- **AI-Ready Output**
  - Token count estimation
  - JSON/Text output formats
  - Structured metadata export

- **Metadata Analysis**
  - Object count tracking
  - Interactive tree view

## 🛠️ Prerequisites

- Java 17 or higher
- Maven
- MSSQL Server
- Modern web browser

## 📦 Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/DevCogitations/database-metadata-extractor.git
   cd database-metadata-extractor
   ```

2. **Build the project**
   ```bash
   mvn clean install
   ```

3. **Run the application**
   ```bash
   java -jar target/database-metadata-extractor.jar
   ```

4. **Access the application**
   - Open your browser
   - Navigate to `http://localhost:8080`

## 💻 Usage

### Basic Configuration

1. Select your database type (MSSQL/PostgreSQL)
2. Enter connection details:
   ```properties
   Server: your-server-address
   Database: your-database-name
   Username: your-username
   Password: your-password
   Schema: dbo (default)
   ```

### Extracting Metadata

1. **Select Object Types**
   - Choose from Tables, Views, Procedures, Functions, Triggers
   - Use "ALL" to select everything

2. **Apply Filters (Optional)**
   - Add patterns for specific object types
   - Example: `Customer*` for all customer-related objects

3. **Choose Output Format**
   - JSON: Structured output for programmatic use
   - Text: Human-readable format

4. **Extract or Download**
   - Click "Extract" for immediate view
   - Click "Download" to save output

### Sample Output

#### JSON Format
```json
{
  "type": "TABLE",
  "name": "CustomerOrders",
  "extract": "<table definition>",
  "tokens": 150
}
```

#### Text Format
```sql
-- TABLE: CustomerOrders
-- Tokens: 150
<table definition>
```

## 🤖 AI Integration

### Token Count Estimation
- Provides token counts for:
  - GPT4 models
  - Vector databases
  - Other LLMs

## 🐛 Troubleshooting

### Common Issues

1. **Connection Failed**
   ```
   Solution: Verify credentials and network access
   ```

2. **Pattern Syntax Error**
   ```
   Solution: Use correct wildcard syntax (*)
   ```

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---
Made with ❤️ by Bhoopendra Singh
