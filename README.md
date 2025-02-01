# Table Relation Manager

## Introduction

The Table Relation Manager is a powerful tool designed to help database administrators and developers easily manage and visualize relationships between database tables. Built using Spring Boot and supporting databases like MySQL and SQLite, this tool provides an intuitive web interface to define, modify, and explore table relationships.

## Features

1. **Database Connection Management**: Supports connections to multiple databases, allowing users to switch between different database configurations effortlessly.
2. **Table Relationship Definition**: Users can define and modify relationships between tables using a simple and intuitive interface.
3. **Visualization**: The tool provides visualization of table relationships, making it easier to understand complex database structures.
4. **Query Execution**: Execute custom SQL queries and view results directly within the tool.

## Getting Started

### Prerequisites

- Java 21 installed on your system.
- MySQL or SQLite database server.

### Installation

1. Clone the repository:

```shell
git clone https://github.com/yourusername/table-relation.git
```

2. Navigate to the project directory:
```shell
cd table-relation
```


3. Build the project using Maven:
```shell
./mvnw clean install
```

### Configuration

Edit the `application.properties` file located in `src/main/resources` to configure your database connection:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/table_manager?useUnicode=true&characterEncoding=utf-8&useSSL=false
spring.datasource.username=root
spring.datasource.password=password
pring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
```

For SQLite, the configuration is simpler:
```properties
spring.datasource.url=jdbc:sqlite:table_database.db
spring.datasource.driver-class-name=org.sqlite.JDBC
```

### Running the Application

Run the application using Maven:
```shell
./mvnw spring-boot:run
```

Or run the generated JAR file:
```shell
java -jar target/table-relation-0.0.1-SNAPSHOT.jar
```


## Usage

1. **Database Connection**: Connect to your configured database using the provided interface.
2. **Define Relationships**: Use the web interface to define relationships between tables.
3. **Visualize**: Generate and view visual representations of your table relationships.
4. **Query**: Execute SQL queries and analyze the results.

## Conclusion

The Table Relation Manager simplifies the process of managing and understanding complex database relationships. Whether you're a developer or a database administrator, this tool can help you save time and effort in your daily tasks.


