package com.koant.sonar.slacknotifier.extension.task;

import com.github.seratch.jslack.Slack;
import com.github.seratch.jslack.api.model.Attachment;
import com.github.seratch.jslack.api.model.Field;
import com.github.seratch.jslack.api.webhook.Payload;
import com.github.seratch.jslack.api.webhook.WebhookResponse;
import com.koant.sonar.slacknotifier.common.SlackNotifierProp;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.sonar.api.ce.posttask.*;
import org.sonar.api.config.MapSettings;
import org.sonar.api.config.Settings;

import java.util.*;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

/**
 * Created by 616286 on 3.6.2016.
 */
public class SlackPostProjectAnalysisTaskTest {

    SlackPostProjectAnalysisTask t;
    private Slack slackClient;
    private Settings settings;
    private String hook;

    @Before
    public void before(){
        settings = new MapSettings();
        settings.setProperty(SlackNotifierProp.ENABLED.property(), "true");
        hook = "hook";
        settings.setProperty(SlackNotifierProp.HOOK.property(), hook);
        settings.setProperty(SlackNotifierProp.CHANNEL.property(), "channel");
        settings.setProperty(SlackNotifierProp.USER.property(), "user");
        settings.setProperty(SlackNotifierProp.CHANNELS.property(), "com.koant.sonar.slack:sonar-slack-notifier-plugin");
        settings.setProperty(SlackNotifierProp.CHANNELS.property()+".com.koant.sonar.slack:sonar-slack-notifier-plugin."+SlackNotifierProp.PROJECT.property(), "com.koant.sonar.slack:sonar-slack-notifier-plugin");
        settings.setProperty(SlackNotifierProp.CHANNELS.property()+".com.koant.sonar.slack:sonar-slack-notifier-plugin."+SlackNotifierProp.CHANNEL.property(), "#random");
        settings.setProperty("sonar.core.serverBaseURL","http://your.sonar.com/");
        slackClient =Mockito.mock(Slack.class);
        t = new SlackPostProjectAnalysisTask(slackClient, settings);
    }

    @Test
    public void finished() throws Exception {
        PostProjectAnalysisTask.ProjectAnalysis analysis = new PostProjectAnalysisTask.ProjectAnalysis() {
            @Override
            public CeTask getCeTask() {
                return new CeTask() {
                    @Override
                    public String getId() {
                        return "id";
                    }

                    @Override
                    public Status getStatus() {
                        return Status.SUCCESS;
                    }
                };
            }

            @Override
            public Project getProject() {
                return new Project() {
                    @Override
                    public String getUuid() {
                        return "uuid";
                    }

                    @Override
                    public String getKey() {
                        return "com.koant.sonar.slack:sonar-slack-notifier-plugin";
                    }

                    @Override
                    public String getName() {
                        return "Foobar";
                    }
                };
            }

            @Override
            public QualityGate getQualityGate() {
                return new QualityGate() {
                    @Override
                    public String getId() {
                        return "qgid";
                    }

                    @Override
                    public String getName() {
                        return "qualityqate";
                    }

                    @Override
                    public Status getStatus() {
                        return Status.OK;
                    }

                    @Override
                    public Collection<Condition> getConditions() {
                        List<Condition> list = new ArrayList<>();
                        list.add(new Condition() {
                            @Override
                            public EvaluationStatus getStatus() {
                                return EvaluationStatus.OK;
                            }

                            @Override
                            public String getMetricKey() {
                                return "metric_key_1";
                            }

                            @Override
                            public Operator getOperator() {
                                return Operator.EQUALS;
                            }

                            @Override
                            public String getErrorThreshold() {
                                return "1";
                            }

                            @Override
                            public String getWarningThreshold() {
                                return "-1";
                            }

                            @Override
                            public boolean isOnLeakPeriod() {
                                return false;
                            }

                            @Override
                            public String getValue() {
                                return "0";
                            }
                        });
                        list.add(new Condition() {
                            @Override
                            public EvaluationStatus getStatus() {
                                return EvaluationStatus.ERROR;
                            }

                            @Override
                            public String getMetricKey() {
                                return "metric_key_2";
                            }

                            @Override
                            public Operator getOperator() {
                                return Operator.GREATER_THAN;
                            }

                            @Override
                            public String getErrorThreshold() {
                                return "100";
                            }

                            @Override
                            public String getWarningThreshold() {
                                return "50";
                            }

                            @Override
                            public boolean isOnLeakPeriod() {
                                return false;
                            }

                            @Override
                            public String getValue() {
                                return "101";
                            }
                        });
                        list.add(new Condition() {
                            @Override
                            public EvaluationStatus getStatus() {
                                return EvaluationStatus.NO_VALUE;
                            }

                            @Override
                            public String getMetricKey() {
                                return "metric_key_3";
                            }

                            @Override
                            public Operator getOperator() {
                                return Operator.GREATER_THAN;
                            }

                            @Override
                            public String getErrorThreshold() {
                                return "100";
                            }

                            @Override
                            public String getWarningThreshold() {
                                return "50";
                            }

                            @Override
                            public boolean isOnLeakPeriod() {
                                return false;
                            }

                            @Override
                            public String getValue() {
                                return "101";
                            }
                        });
                        return list;
                    }
                };
            }

            @Override
            public Date getDate() {
                return new Date();
            }

            @Override
            public Optional<Date> getAnalysisDate() {
                return Optional.ofNullable(new Date());
            }

            @Override
            public ScannerContext getScannerContext() {
                ScannerContext scannerContext = new ScannerContext() {
                    @Override
                    public Map<String, String> getProperties() {
                        return Collections.emptyMap();
                    }
                };
                return scannerContext;

            }
        };

        WebhookResponse webhookResponse =  WebhookResponse.builder().code(200).build();
        when(slackClient.send(anyString(), any(Payload.class))).thenReturn(webhookResponse);

        t.finished(analysis);

        Mockito.verify(slackClient, times(1)).send(eq(hook), any(Payload.class));

        // Payload(text=Project [Foobar] analyzed. See http://your.sonar.com/overview?id=com.koant.sonar.slack:sonar-slack-notifier-plugin. Quality gate status is OK,
        // channel=#random, username=user, iconUrl=null, iconEmoji=null,
        // attachments=[
        //  Attachment(fallback=null, color=null, pretext=null, authorName=null, authorLink=null, authorIcon=null, title=null, titleLink=null, text=null,
        //      fields=[Field(title=metric_key_1: OK, value=Value [0], operator [EQUALS], warning threshold [-1], error threshold [1], on leak period [false], valueShortEnough=false),
        // Field(title=metric_key_2: ERROR, value=Value [101], operator [GREATER_THAN], warning threshold [50], error threshold [100], on leak period [false], valueShortEnough=false)],
        // imageUrl=null, thumbUrl=null, footer=null, footerIcon=null, ts=null, mrkdwnIn=null)])
        List<Attachment> attachments = new ArrayList<>();
        List<Field> fields = new ArrayList<>();
        fields.add(Field.builder()
                .title("metric_key_1: OK")
                .value("Value [0], operator [EQUALS], warning threshold [-1], error threshold [1], on leak period [false]")
                .valueShortEnough(false)
            .build());
        fields.add(Field.builder()
                .title("metric_key_2: ERROR")
                .value("Value [101], operator [GREATER_THAN], warning threshold [50], error threshold [100], on leak period [false]")
                .valueShortEnough(false)
                .build());
        fields.add(Field.builder()
                .title("metric_key_3")
                .value("NO_VALUE")
                .valueShortEnough(true)
                .build());
        attachments.add(Attachment.builder()
        .fields(fields)
        .build());
        Payload payload = Payload.builder()
                .text("Project [Foobar] analyzed. See http://your.sonar.com/overview?id=com.koant.sonar.slack:sonar-slack-notifier-plugin. Quality gate status is OK")
                .channel("#random")
                .username("user")
                .attachments(attachments)
                .build();
        Mockito.verify(slackClient, times(1)).send(eq(hook), eq(payload));

        /*
        String actual = EntityUtils.toString(actualRequest.getEntity());
        String expected = "payload={\"channel\":\"#random\",\"username\":\"user\",\"text\":\"Project [Foobar] analyzed. See http://your.sonar.com/overview?id=com.koant.sonar.slack:sonar-slack-notifier-plugin. Quality gate status is OK\",\"attachments\":[{\"text\":\"- metric_key_1 OK Value [0], operator [EQUALS], warning threshold [-1], error threshold [1], on leak period [false]\\n- metric_key_2 ERROR Value [101], operator [GREATER_THAN], warning threshold [50], error threshold [100], on leak period [false]\\n\",\"fallback\": \"Reasons: metric_key_1 OK Value [0], operator [EQUALS], warning threshold [-1], error threshold [1], on leak period [false], metric_key_2 ERROR Value [101], operator [GREATER_THAN], warning threshold [50], error threshold [100], on leak period [false], \"}]}";
        assertEquals(expected, URLDecoder.decode(actual,"UTF-8"));
        */

    }

}