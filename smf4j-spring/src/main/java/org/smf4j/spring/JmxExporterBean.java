/*
 * Copyright 2013 Russell Morris (wrussellmorris@gmail.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.smf4j.spring;

import org.smf4j.to.jmx.JmxRegistrarPublisher;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

/**
 *
 * @author Russell Morris (wrussellmorris@gmail.com)
 */
public class JmxExporterBean implements InitializingBean, DisposableBean {

    private JmxRegistrarPublisher publisher;
    private boolean autoPublish = true;

    public boolean getAutoPublish() {
        return autoPublish;
    }

    public void setAutoPublish(boolean autoPublish) {
        this.autoPublish = autoPublish;
    }

    public void afterPropertiesSet() throws Exception {
        if(autoPublish) {
            publish();
        }
    }

    public void destroy() throws Exception {
        unpublish();
    }

    public void publish() {
        if(publisher == null) {
            publisher = new JmxRegistrarPublisher();
            publisher.publish();
        }
    }

    public void unpublish() {
        if(publisher != null) {
            publisher.unpublish();
            publisher = null;
        }
    }

    public boolean isPublished() {
        return publisher != null;
    }
}
