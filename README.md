# Sonar Ruby Plugin
SonarQube plugin for analyzing Ruby files

![SonarCloud](https://sonarcloud.io/api/project_badges/measure?project=com.fortitudetec.sonar%3Asonar-ruby-plugin&metric=alert_status)

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
sonar.ruby.coverage.framework=RSpec
sonar.ruby.rubocopConfig=.rubocop.yml
sonar.ruby.rubocop=/usr/bin/rubocop
sonar.ruby.rubocop.reportPath=rubocop-result.json
sonar.ruby.rubocop.filePath=.
```

## Installation

### Manual Installation
Here are the steps to manually install this plugin for use in SonarQube:

 1. Download the plugin jar
 2. Copy the jar into the SonarQube plugin directory (e.g. /opt/sonarqube/extensions/plugins)
 3. Restart SonarQube
 
### Update Center Installation
TODO: Need to get this plugin added to SonarQube's update center
 
## Running the analysis
In order to run the analysis for Ruby you will need to utilize the [sonar-scanner](https://docs.sonarqube.org/display/SCAN/Analyzing+with+SonarQube+Scanner) application.

* Run rubocop making sure that the json results file is created   
    e.g. `rubocop --format=json --out=rubocop-result.json`  
    
* Run your specs (make sure that your sonarqube config points to the right coverage directory )  
    e.g. `bundle exec rspec spec`  
    *Make sure that your sonarqube config points to the right coverage directory e.g. `spec/coverage/.resultset.json`*
    
* Make sure you have a sonar-project.properties file in the root of your project directory  
* run `sonar-scanner` 

## Contributing
#### 1. GitHub issue
To request a new feature, [create a GitHub issue](https://github.com/fortitudetec/sonar-ruby-plugin/issues/new). Even if you plan to implement it yourself and submit it back to the community, please create an issue to be sure that we can follow up on it.

#### 2. Pull Request
To submit a contribution, create a pull request for this repository.

## License

Copyright 2017-2018 Fortitude Technologies.

Licensed under the [MIT License](https://en.wikipedia.org/wiki/MIT_License)