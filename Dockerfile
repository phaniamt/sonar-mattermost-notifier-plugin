FROM maven:3.6.3-adoptopenjdk-8 as build
RUN cd /tmp && git clone https://github.com/jufab/sonar-mattermost-notifier-plugin.git
RUN cd /tmp/sonar-mattermost-notifier-plugin && mvn clean package
RUN cd /tmp/sonar-mattermost-notifier-plugin/target/ && mv *.jar sonar-mattermost-notifier-plugin.jar
FROM sonarqube:8.3.1-community
COPY --from=build /tmp/sonar-mattermost-notifier-plugin/target/sonar-mattermost-notifier-plugin.jar /opt/sonarqube/extensions/plugins/
RUN cd /opt/sonarqube/extensions/plugins/ && chown sonarqube:sonarqube sonar-mattermost-notifier-plugin.jar && chmod u+x sonar-mattermost-notifier-plugin.jar
