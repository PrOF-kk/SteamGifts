package iconics;

import androidx.annotation.NonNull;

import com.mikepenz.fontawesome_typeface_library.FontAwesome;

import org.junit.Test;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import ru.ztrap.iconics.IconicsStringGenerator;

public class TextStringGeneratorTest extends IconicsStringGenerator {

    @Test
    public void generateFontAwesomeStrings() throws ParserConfigurationException, TransformerException {
        generateIconsFrom(new FontAwesome());
    }

    @NonNull
    @Override
    protected FileCreationStrategy getFileCreationStrategy() {
        return FileCreationStrategy.SAVE_ONLY_CURRENT;
    }
}
