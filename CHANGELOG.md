# Changelog

## [1.0.0-beta.1](https://github.com/jazdw/rql-parser/tree/v1.0.0-beta.1) (2022-01-03)

* Use [ANTLR 4](https://www.antlr.org/) to generate a lexer and parser from a grammar file
* Remove legacy regex based parser
* Provide a visitor that generates a `StreamFilter` for filtering streams and lists
* Provide a visitor that generates a `ASTNode` for backwards compatibility
* RQL parsing is now stricter
* Parsing exceptions contain more helpful messages about what when wrong and the position in the query
* [Fix #3](https://github.com/jazdw/rql-parser/issues/3) &ndash; Always set parent on child nodes
* [Fix #5](https://github.com/jazdw/rql-parser/issues/5) &ndash; Slashes in values are no longer interpreted as arrays

## [0.4.1](https://github.com/jazdw/rql-parser/tree/v0.4.0) (2022-01-03)

* Fix conversion of dates to `ZonedDateTime`
* Always convert to `ZonedDateTime` instead of `OffsetDateTime`

## [0.4.0](https://github.com/jazdw/rql-parser/tree/v0.4.0) (2021-12-27)

* Require Java 11
* Remove Joda time dependency
* Remove `isodate` converter
* `date` converter now returns any of
    * `ZonedDateTime`
    * `OffsetDateTime`
    * `LocalDateTime`
    * `LocalDate`
* `epoch` converter new returns `Instant` instead of `Date`
* Update org.apache.commons:commons-lang3 to version 3.12.0
* Update commons-beanutils:commons-beanutils to version 1.9.4
* Update commons-collections:commons-collections to version 3.2.2
* Update junit:junit to version 4.13.2