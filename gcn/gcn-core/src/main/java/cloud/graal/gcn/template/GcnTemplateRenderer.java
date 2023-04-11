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

import cloud.graal.gcn.model.GcnCloud;
import io.micronaut.starter.application.Project;
import io.micronaut.starter.io.OutputHandler;
import io.micronaut.starter.template.DefaultTemplateRenderer;
import io.micronaut.starter.template.RenderResult;
import io.micronaut.starter.template.Template;
import io.micronaut.starter.util.NameUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static cloud.graal.gcn.GcnUtils.LIB_MODULE;
import static cloud.graal.gcn.model.GcnCloud.NONE;
import static io.micronaut.starter.template.Template.ROOT;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Replaces the default rendered with logic to use <code>TemplatePostProcessor</code>s.
 *
 * @since 1.0.0
 */
public class GcnTemplateRenderer extends DefaultTemplateRenderer {

    private final Map<String, Set<TemplatePostProcessor>> postProcessors;
    private final Map<Pattern, Set<TemplatePostProcessor>> regexPostProcessors;
    private final Map<String, Map<String, String>> replacementsByModule = new HashMap<>();

    public GcnTemplateRenderer(Project libProject,
                               OutputHandler outputHandler,
                               Map<String, Set<TemplatePostProcessor>> postProcessors,
                               Map<Pattern, Set<TemplatePostProcessor>> regexPostProcessors,
                               Set<GcnCloud> clouds) {
        super(Collections.emptyMap(), outputHandler);
        this.postProcessors = postProcessors;
        this.regexPostProcessors = regexPostProcessors;

        for (GcnCloud cloud : clouds) {
            if (cloud == NONE) {
                continue;
            }
            Project cloudProject = NameUtils.parse(libProject.getPackageName() + '.' + cloud.getModuleName());
            replacementsByModule.put(cloud.getModuleName(), cloudProject.getProperties());
        }

        replacementsByModule.put(ROOT, libProject.getProperties());
        replacementsByModule.put(LIB_MODULE, libProject.getProperties());
    }

    public RenderResult render(Template template, String templateKey) {

        Map<String, String> replacements = replacementsByModule.get(template.getModule());
        String path = replaceVariables(template.getPath(), replacements);

        if (getOutputHandler().exists(path)) {
            return RenderResult.skipped(path);
        }

        try {
            if (!template.isBinary()) {
                template = process(template, templateKey);
            }

            getOutputHandler().write(path, template);
            return RenderResult.success(path);
        } catch (Exception e) {
            return RenderResult.error(path, e);
        }
    }

    private Template process(Template template,
                             String templateKey) throws IOException {

        Set<TemplatePostProcessor> regexProcessors = regexPostProcessors.entrySet()
                .stream()
                .filter(e -> e.getKey().matcher(template.getPath()).find())
                .map(Map.Entry::getValue)
                .flatMap(Set::stream)
                .collect(Collectors.toSet());

        Set<TemplatePostProcessor> processors = postProcessors.get(templateKey);
        if (processors == null && regexProcessors.isEmpty()) {
            return template;
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        template.write(out);

        String rendered = out.toString(UTF_8);

        if (processors != null) {
            for (TemplatePostProcessor processor : processors) {
                rendered = processor.process(rendered);
            }
        }

        for (TemplatePostProcessor processor : regexProcessors) {
            rendered = processor.process(rendered);
        }

        return new ProcessedTemplate(template, rendered);
    }
}
