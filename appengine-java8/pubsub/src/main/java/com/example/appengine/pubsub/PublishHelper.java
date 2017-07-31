/*
 * Copyright 2017 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.appengine.pubsub;

import com.google.cloud.ServiceOptions;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.PubsubMessage;
import com.google.pubsub.v1.TopicName;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

public class PublishHelper implements Runnable {

  private static String projectId = ServiceOptions.getDefaultProjectId();
  private String message;
  private String topicId;

  private final ByteArrayOutputStream resultBytes;
  private final PrintStream resultStream;

  PublishHelper(String topicId, String message) throws Exception {
    this.message = message;
    this.topicId = topicId;
    this.resultBytes = new ByteArrayOutputStream();
    this.resultStream = new PrintStream(this.resultBytes);
  }

  @Override
  public void run() {
    synchronized (resultStream) {
      resultBytes.reset();
    }
    try {
      resultStream.append(projectId);
      resultStream.append(topicId);
      publish();
    } catch (Exception e) {
      resultStream.append("ERROR : " + e.getMessage());
    }
  }

  void publish() throws Exception {
    TopicName topicName = TopicName.create(projectId, topicId);
    Publisher publisher = Publisher.defaultBuilder(topicName).build();
    PubsubMessage pubsubMessage =
        PubsubMessage.newBuilder().setData(ByteString.copyFromUtf8(message)).build();
    String messageId = publisher.publish(pubsubMessage).get();
    resultStream.append(messageId);
    publisher.shutdown();
  }

  String getOutput() {
    //works because PrintStream is thread safe synchronizing on "this".
    synchronized (resultStream) {
      try {
        return resultBytes.toString("UTF-8");
      } catch (UnsupportedEncodingException e) {
        return null;
      }
    }
  }
}
