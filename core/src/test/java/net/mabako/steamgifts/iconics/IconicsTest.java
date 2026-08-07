package net.mabako.steamgifts.iconics;

import static org.assertj.core.api.Assertions.assertThat;

import com.mikepenz.iconics.typeface.ITypeface;
import com.mikepenz.iconics.typeface.library.fontawesome.FontAwesome;
import com.mikepenz.iconics.typeface.library.fontawesome.FontAwesomeBrand;
import com.mikepenz.iconics.typeface.library.fontawesome.FontAwesomeRegular;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class IconicsTest {

    private static final List<ITypeface> TYPEFACES = List.of(FontAwesome.INSTANCE, FontAwesomeBrand.INSTANCE, FontAwesomeRegular.INSTANCE);
    // (faw-[\w-]+)|(fab-[\w-]+)|(far-[\w-]+)
    private static final Pattern PATTERN = Pattern.compile(TYPEFACES.stream()
            .map(tf -> "(" + tf.getMappingPrefix() + "-[\\w-]+)")
            .collect(Collectors.joining("|")));
    private static final Set<String> ICONS = TYPEFACES.stream()
            .map(ITypeface::getIcons)
            .flatMap(Collection::stream)
            .collect(Collectors.toSet());

    static Stream<Path> allJavaFiles() throws IOException {
        return Files.find(Path.of("./src/main/java"), 999, (path, basicFileAttributes) -> basicFileAttributes.isRegularFile());
    }
    static Stream<Path> allXmlFiles() throws IOException {
        return Files.find(Path.of("./src/main/res"), 999, (path, basicFileAttributes) -> basicFileAttributes.isRegularFile() && path.toString().endsWith(".xml"));
    }

    /// Inspect Java source for invalid icons
    @SuppressWarnings("java:S3415") // false positive
    @ParameterizedTest
    @MethodSource("allJavaFiles")
    void findInvalidIconsInJava(Path path) throws IOException {
        try (var lines = Files.lines(path)) {
            lines.forEach(line -> {
                // Find all icons in line (likely only one)
                Matcher matcher = PATTERN.matcher(line);
                while (matcher.find()) {
                    // Enums are in snake_case, string constants are in kebab-case
                    assertThat(ICONS).contains(matcher.group().replace("-", "_"));
                }
            });
        }
    }

    /// Inspect XML source for invalid icons
    @SuppressWarnings("java:S3415") // false positive
    @ParameterizedTest
    @MethodSource("allXmlFiles")
    void findInvalidIconsInXml(Path path) throws IOException {
        try (var lines = Files.lines(path)) {
            lines.forEach(line -> {
                // Find all icons in line (likely only one)
                Matcher matcher = PATTERN.matcher(line);
                while (matcher.find()) {
                    // Enums are in snake_case, string constants are in kebab-case
                    assertThat(ICONS).contains(matcher.group().replace("-", "_"));
                }
            });
        }
    }
}
