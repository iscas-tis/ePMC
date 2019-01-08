package epmc.jani.model;

import epmc.prism.exporter.processor.PRISMExporter_ProcessorStrict;
import epmc.prism.exporter.processor.PRISMExporter_ProcessorRegistrar;

public class PRISMExporter_TimeProgressProcessor implements PRISMExporter_ProcessorStrict {

    private TimeProgress timeProgress = null;
    private String prefix = null;

    @Override
    public PRISMExporter_ProcessorStrict setElement(Object obj) {
        assert obj != null;
        assert obj instanceof TimeProgress; 

        timeProgress = (TimeProgress) obj;
        return this;
    }

    @Override
    public PRISMExporter_ProcessorStrict setPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    @Override
    public String toPRISM() {
        assert timeProgress != null;

        StringBuilder prism = new StringBuilder();
        PRISMExporter_ProcessorStrict processor; 

        String comment = timeProgress.getComment();
        if (comment != null) {
            if (prefix != null) {
                prism.append(prefix);
            }
            prism.append("// ")
                .append(comment)
                .append("\n");
        }

        if (prefix != null)	{
            prism.append(prefix);
        }
        prism.append("invariant\n");
        processor = PRISMExporter_ProcessorRegistrar.getProcessor(timeProgress.getExp());
        if (prefix != null)	{
            processor.setPrefix(PRISMExporter_ModelJANIProcessor.INDENT + prefix);
        } else {
            processor.setPrefix(PRISMExporter_ModelJANIProcessor.INDENT);			
        }
        prism.append(processor.toPRISM())
            .append("\n");
        if (prefix != null)	{
            prism.append(prefix);
        }
        prism.append("endinvariant\n");

        return prism.toString();
    }

    @Override
    public void validateTransientVariables() {
        assert timeProgress != null;

        PRISMExporter_ProcessorRegistrar.getProcessor(timeProgress.getExp())
            .validateTransientVariables();
    }

    @Override
    public boolean usesTransientVariables() {
        assert timeProgress != null;

        return PRISMExporter_ProcessorRegistrar.getProcessor(timeProgress.getExp())
                .usesTransientVariables();
    }	
}
