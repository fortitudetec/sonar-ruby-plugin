FROM sonarqube:7.1

COPY target/sonar-ruby-plugin-1.1.0.jar /opt/sonarqube/extensions/plugins/


