package epmc.jani.model;

import epmc.prism.exporter.processor.JANI2PRISMProcessorStrict;
import epmc.prism.exporter.processor.ProcessorRegistrar;

public class TimeProgressProcessor implements JANI2PRISMProcessorStrict {

	private TimeProgress timeProgress = null;
	private String prefix = null;
	
	@Override
	public JANI2PRISMProcessorStrict setElement(Object obj) {
		assert obj != null;
		assert obj instanceof TimeProgress; 
		
		timeProgress = (TimeProgress) obj;
		return this;
	}

	@Override
	public JANI2PRISMProcessorStrict setPrefix(String prefix) {
		this.prefix = prefix;
		return this;
	}

	@Override
	public String toPRISM() {
		assert timeProgress != null;
		
		StringBuilder prism = new StringBuilder();
		JANI2PRISMProcessorStrict processor; 
		
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
		processor = ProcessorRegistrar.getProcessor(timeProgress.getExp());
		if (prefix != null)	{
			processor.setPrefix(ModelJANIProcessor.INDENT + prefix);
		} else {
			processor.setPrefix(ModelJANIProcessor.INDENT);			
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
		
		ProcessorRegistrar.getProcessor(timeProgress.getExp())
						  .validateTransientVariables();
	}

	@Override
	public boolean usesTransientVariables() {
		assert timeProgress != null;
		
		return ProcessorRegistrar.getProcessor(timeProgress.getExp())
								 .usesTransientVariables();
	}	
}
