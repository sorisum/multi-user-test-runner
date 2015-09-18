Multi User Test Runner
======================

# 0.3.0

## Changes

* First public release
* Better configuration support
* Support for creating users in `@Before` methods
* Improved documentation

## Breaking changes

* `SpringMultiUserTestClassRunner` is in its own artefact: spring-test-class-runner
* Default TestUsers runner is changed from `SpringMultiUserTestClassRunner` to `BlockMultiUserTestClassRunner`
* Runner configuration is done via `MultiUserTestConfig` annotation. Annotation can be added to a base class to reduce boilerplage code.
* Creator is logged after `@Before` methods but just before calling the test method. Previously 
  it was called in `AbstractUserRoleIT`class's `@Before` method which made impossible to create custom users
  which could be used as creator user.

# 0.2 - internal release

## Changes

* Add new advanced assertions. Java 8 syntactic sugar.

# 0.1 - internal release

## Changes/Features

* First release for internal use
* Support for one role per user and existing users
* Simple assertions