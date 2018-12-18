/****************************************************************************

    ePMC - an extensible probabilistic model checker
    Copyright (C) 2017

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

 *****************************************************************************/

package epmc.jani.exporter.expressionprocessor;

import static epmc.error.UtilError.ensure;

import java.util.HashMap;
import java.util.Map;

import epmc.expression.Expression;
import epmc.jani.exporter.error.ProblemsJANIExporter;
import epmc.jani.model.ModelJANI;
import epmc.util.Util;

/**
 * Class that is responsible for registering the JANI components and their corresponding JANI processors.
 * 
 * @author Andrea Turrini
 *
 */
public class ExpressionProcessorRegistrar {
    private static Map<Class<? extends Expression>, Class<? extends ExpressionProcessor>> expressionProcessors = registerExpressionProcessors();
    
    private static ModelJANI model = null;
    
    public void setModel(ModelJANI model) {
        assert model != null;
        
        ExpressionProcessorRegistrar.model = model;
    }
    
    public ModelJANI getModel() {
        assert model != null;
        
        return model;
    }

    /**
     * Add a new processor for an expression in the set of known processors.
     * 
     * @param expression the expression to which associate the processor
     * @param processor the corresponding processor
     */
    public static void registerExpressionProcessor(Class<? extends Expression> expression, Class<? extends ExpressionProcessor> processor) {
        expressionProcessors.put(expression, processor);
    }

    /**
     * Return the processor associated to the given expression.
     * 
     * @param expression the expression for which obtain the processor
     * @return the corresponding processor
     */
    public static ExpressionProcessor getExpressionProcessor(Expression expression) {
        assert model != null;
        assert expression != null;

        ExpressionProcessor processor = null;
        Class<? extends ExpressionProcessor> processorClass = expressionProcessors.get(expression.getClass());
        if (processorClass != null) {
            processor = Util.getInstance(processorClass)
                    .setElement(model, expression);
        } else {
            ensure(false, 
                    ProblemsJANIExporter.JANI_EXPORTER_ERROR_UNKNOWN_PROCESSOR, 
                    expression.getClass().getSimpleName());
        }

        return processor;
    }

    private static Map<Class<? extends Expression>, Class<? extends ExpressionProcessor>> registerExpressionProcessors() {
        Map<Class<? extends Expression>, Class<? extends ExpressionProcessor>> processors = new HashMap<>();
        
        return processors;
    }
}
