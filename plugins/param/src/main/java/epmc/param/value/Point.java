package epmc.param.value;

import epmc.value.TypeArrayReal;
import epmc.value.TypeReal;
import epmc.value.UtilValue;
import epmc.value.Value;
import epmc.value.ValueArray;

public final class Point {
	private final TypeFunction typeFunction;
    private final ValueArray content;
    
    public Point(TypeFunction typeFunction) {
    	assert typeFunction != null;
    	this.typeFunction = typeFunction;
    	TypeReal typeReal = TypeReal.get(typeFunction.getContext());
    	assert typeReal != null;
    	TypeArrayReal typeArrayReal = typeReal.getTypeArray();
    	this.content = UtilValue.newArray(typeArrayReal, typeFunction.getNumParameters());
    	assert this.content != null;
    }
    
    public ValueArray getContent() {
        return content;
    }
    
    public void getDimension(Value to, int dimensionNumber) {
    	adjustNumParameters();
    	assert to != null;
    	assert dimensionNumber >= 0;
    	assert dimensionNumber < content.size();
    	content.get(to, dimensionNumber);
    }
    
    public void setDimension(Value to, int dimensionNumber) {
    	adjustNumParameters();
    	assert to != null;
    	assert dimensionNumber >= 0;
    	assert dimensionNumber < content.size();
    	content.set(to, dimensionNumber);
    }

	public void adjustNumParameters() {
		int oldLength = content.size();
		if (oldLength == typeFunction.getNumParameters()) {
			return;
		}
		this.content.resize(typeFunction.getNumParameters());		
	}
	
	@Override
	public String toString() {
		adjustNumParameters();
		StringBuilder result = new StringBuilder();
		result.append("point(");
		result.append(content);
		result.append(")");
		return result.toString();
	}
}
