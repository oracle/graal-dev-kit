package cloud.graal.gcn.rocker

import com.fizzed.rocker.model.PlainText
import com.fizzed.rocker.model.PostProcessorException
import com.fizzed.rocker.model.TemplateModel
import com.fizzed.rocker.model.TemplateModelPostProcessor
import com.fizzed.rocker.model.TemplateUnit
import groovy.transform.CompileStatic

/**
 * Based on io.micronaut.starter.rocker.WhitespaceProcessor.
 */
@CompileStatic
class WhitespaceProcessor implements TemplateModelPostProcessor {

    @Override
    TemplateModel process(TemplateModel templateModel, int ppIndex) throws PostProcessorException {
        List<TemplateUnit> units = templateModel.units
        int length = units.size()
        PlainText lastPlainText = null
        for (int i = 0; i < length; i++) {
            TemplateUnit tu = units[i]
            if (tu instanceof PlainText) {
                PlainText pt = (PlainText) tu
                if ((lastPlainText == null || lastPlainText.text.endsWith('\n')) && pt.text.startsWith('\n')) {
                    PlainText replacementPt = new PlainText(pt.sourceRef, pt.text.substring(1))
                    units.add(i, replacementPt)
                    units.remove(i + 1)
                }
                lastPlainText = pt
            }
        }
        return templateModel
    }
}
