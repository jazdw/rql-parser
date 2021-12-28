# Changelog

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