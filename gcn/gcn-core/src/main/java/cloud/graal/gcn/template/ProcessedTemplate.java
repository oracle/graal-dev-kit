/*
 * Copyright 2023 Oracle and/or its affiliates
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cloud.graal.gcn.template;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.starter.template.Template;

import java.io.IOException;
import java.io.OutputStream;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Used in GcnTemplateRenderer; replaces the original template to write the
 * processed output from one or more <code>TemplatePostProcessor</code>s.
 *
 * @since 1.0.0
 */
public class ProcessedTemplate implements Template {

    private final Template template;
    private final String content;

    public ProcessedTemplate(Template template,
                             String content) {
        this.template = template;
        this.content = content;
    }

    @Override
    public String getPath() {
        return template.getPath();
    }

    @Override
    public void setUseModule(boolean useModule) {
        throw new IllegalStateException("Cannot update after construction");
    }

    @NonNull
    @Override
    public String getModule() {
        return template.getModule();
    }

    @Override
    public boolean isBinary() {
        return template.isBinary();
    }

    @Override
    public boolean isExecutable() {
        return template.isExecutable();
    }

    @Override
    public void write(OutputStream outputStream) throws IOException {
        outputStream.write(content.getBytes(UTF_8));
    }
}
