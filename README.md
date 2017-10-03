# Sonar Ruby Plugin
SonarQube plugin for analyzing Ruby files

## Requirements
* Rubocop (https://github.com/bbatsov/rubocop)
* Simplecov (https://github.com/colszowka/simplecov)
* Tested with v6.5

## Overview
This is plugin for SonarQube 5.6+ for analysing projects with Ruby content that supports:

* Rubocop for code quality information
* Simplecov for unit test coverage information
* NCLOC metric generation

## Configuration

### Example project configuration
This is an example of what a project configuration file (`sonar-project.properties`) could look like:
```
sonar.projectKey=company:my-application
sonar.projectName=My Application
sonar.projectVersion=1.0
sonar.sourceEncoding=UTF-8
sonar.sources=src/app
sonar.exclusions=**/node_modules/**,**/*.spec.ts
sonar.tests=src/app
sonar.test.inclusions=**/*.spec.ts

sonar.ruby.file.suffixes=rb,ruby
sonar.ruby.coverage.reportPath=coverage/.resultset.json
sonar.ruby.rubocopConfig=.rubocop.yml
sonar.ruby.rubocop=/usr/bin/rubocop
sonar.ruby.rubocop.reportPath=rubocop-result.json
sonar.ruby.rubocop.filePath=.
```

