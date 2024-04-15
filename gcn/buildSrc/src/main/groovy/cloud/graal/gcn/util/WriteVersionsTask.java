package cloud.graal.gcn.util;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

public abstract class WriteVersionsTask extends DefaultTask {
    @Input
    public abstract MapProperty<String, String> getVersions();

    @OutputDirectory
    public abstract DirectoryProperty getOutputDirectory();

    @TaskAction
    public void generateVersionFile() throws IOException {
        var versions = getVersions().get();
        for (Map.Entry<String, String> entry : versions.entrySet()) {
            var fileName = entry.getKey();
            var version = entry.getValue();
            var outputFile = getOutputDirectory()
                .getAsFile()
                .get()
                .toPath()
                .resolve(fileName);
            Files.write(outputFile, List.of(version));
        }

    }
}
