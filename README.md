[![Maven Central](https://img.shields.io/maven-central/v/net.jazdw/rql-parser)](https://search.maven.org/artifact/net.jazdw/rql-parser)
[![Snyk Vulnerabilities for GitHub Repo](https://img.shields.io/snyk/vulnerabilities/github/jazdw/rql-parser)](https://snyk.io/vuln/maven%3Anet.jazdw%3Arql-parser)


# rql-parser

A Java [Resource Query Language](https://github.com/persvr/rql) (RQL) parser using [ANTLR 4](https://www.antlr.org/).

## Requirements

* Java 11 or higher

## Grammar

The ANTLR grammar file can
be [viewed here](https://github.com/jazdw/rql-parser/blob/main/src/main/antlr4/net/jazdw/rql/Rql.g4).

## Usage

### Creating a parser

`RqlLexer` and `RqlParser` are generated by ANTLR and extend the ANTLR `Lexer` and `Parser` class respectively. For more
information see the [ANTLR documentation](https://www.antlr.org/api/Java/).

```java
public class RQLParserExample {
    public RqlParser createParser(String rql) {
        // create a lexer and set its input stream from an RQL string
        CharStream inputStream = CharStreams.fromString(rql);
        RqlLexer lexer = new RqlLexer(inputStream);
        // throw on lexer errors
        lexer.addErrorListener(new ThrowWithDetailsErrorListener());

        // get a token stream for the lexer
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);

        // create a parser
        RqlParser parser = new RqlParser(tokenStream);
        // throw on parser errors
        parser.addErrorListener(new ThrowWithDetailsErrorListener());

        return parser;
    }
}
```

### Implement a visitor

`RqlBaseVisitor` implements the ANTLR interface `ParseTreeVisitor`. For more information see
the [ANTLR documentation](https://www.antlr.org/api/Java/).

```java
public class RQLParserExample {
    public void visit(RqlParser parser) {
        RqlBaseVisitor<DesiredType> visitor = new RqlBaseVisitor<>() {
            // extend me
        };

        DesiredType result = visitor.visit(parser.query());
    }
}
```

### Filter a list

See also
[ListFilterTest](https://github.com/jazdw/rql-parser/blob/main/src/test/java/net/jazdw/rql/parser/ListFilterTest.java)

```java
public class RQLParserExample {

    final QueryVisitor<Person> visitor = new QueryVisitor<>(this::getProperty);

    // you could use Apache BeanUtils for this
    Object getProperty(Person item, String propertyName) {
        switch (propertyName) {
            case "name":
                return item.getName();
            default:
                throw new IllegalArgumentException("Unknown property");
        }
    }

    public List<Person> filterList(List<Person> input) {
        RqlParser parser = createParser("name=John");
        StreamFilter<Person> filter = visitor.visit(parser.query());

        return filter.apply(input.stream())
                .collect(Collectors.toList());
    }
}
```

## Legacy usage

Note: The `RQLParser` class is still available in version 1.x, you can still use it to generate a tree of `ASTNode`
.

The parser generates an `ASTNode` object representing the root node of an Abstract Syntax Tree (AST). Create a class
that implements `ASTVisitor<R,A>` in order to traverse the tree.

````java
public class RQLParserExample {
    public void parse() {
        RQLParser parser = new RQLParser();
        MyVisitor visitor = new MyVisitor();
        ASTNode node = parser.parse("(name=jack|name=jill)&age>30");
        Object result = node.accept(visitor);
    }
}
````

## Maven Central Repository coordinates

Find the latest version on [Maven Central](https://search.maven.org/artifact/net.jazdw/rql-parser).

````xml

<dependency>
    <groupId>net.jazdw</groupId>
    <artifactId>rql-parser</artifactId>
    <version>${rql.version}</version>
</dependency>
````
