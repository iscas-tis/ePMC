package epmc.jani.model;

import epmc.error.EPMCException;
import epmc.expression.Expression;
import epmc.prism.exporter.processor.JANI2PRISMProcessorStrict;
import epmc.prism.exporter.processor.ProcessorRegistrar;

public class TimeProgressProcessor implements JANI2PRISMProcessorStrict {

	private TimeProgress timeProgress = null;
	private String prefix = null;
	
	@Override
	public void setElement(Object obj) throws EPMCException {
		assert obj != null;
		assert obj instanceof TimeProgress; 
		
		timeProgress = (TimeProgress) obj;
	}

	@Override
	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	@Override
	public StringBuilder toPRISM() throws EPMCException {
		assert timeProgress != null;
		
		StringBuilder prism = new StringBuilder();
		JANI2PRISMProcessorStrict processor; 
		
		String comment = timeProgress.getComment();
		if (comment != null) {
			if (prefix != null) {
				prism.append(prefix);
			}
			prism.append("// ").append(comment).append("\n");
		}
		
		Expression expression = timeProgress.getExp();
		processor = ProcessorRegistrar.getProcessor(expression);
		if (prefix != null)	{
			prism.append(prefix);
		}
		prism.append("invariant\n");
		if (prefix != null)	{
			processor.setPrefix(ModelJANIProcessor.INDENT + prefix);
		} else {
			processor.setPrefix(ModelJANIProcessor.INDENT);			
		}
		prism.append(processor.toPRISM().toString());
		if (prefix != null)	{
			prism.append(prefix);
		}
		prism.append("endinvariant\n");
		
		return prism;
	}
}
