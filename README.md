# rql-parser
A Java Resource Query Language (RQL) parser. For an explanation of RQL please see https://github.com/persvr/rql

## Requirements

* Java 11 or higher.

## Usage
The parser generates an ASTNode object representing the root node of an Abstract Syntax Tree (AST). Create a class that implements ASTVisitor<R,A> in order to traverse the tree.
````java
RQLParser parser = new RQLParser();
MyVisitor visitor = new MyVisitor();
ASTNode node = parser.parse("(name=jack|name=jill)&age>30");
Object result = node.accept(visitor);
````

## Examples
See the [ListFilter](https://github.com/jazdw/rql-parser/blob/master/src/test/java/net/jazdw/rql/parser/listfilter/ListFilter.java) class, which is an ASTVisitor which filters a list of bean objects.

## Maven Central Repository coordinates
````xml
<dependency>
  <groupId>net.jazdw</groupId>
  <artifactId>rql-parser</artifactId>
  <version>0.3.1</version>
</dependency>
````
