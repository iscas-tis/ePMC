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

package epmc.jani.extensions.functions;

import java.util.Map;

import epmc.expression.evaluatorexplicit.EvaluatorExplicit;
import epmc.expression.standard.OptionsExpressionBasic;
import epmc.jani.explorer.ExplorerExtension;
import epmc.jani.explorer.OptionsJANIExplorer;
import epmc.jani.model.ModelExtension;
import epmc.jani.model.OptionsJANIModel;
import epmc.options.Options;
import epmc.plugin.AfterOptionsCreation;
import epmc.util.OrderedMap;

public class AfterOptionsCreationJANIFunctions implements AfterOptionsCreation {
    private final static String IDENTIFIER = "after-options-creation-jani-functions";

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public void process(Options options) {
        Map<String,Class<? extends ModelExtension>> modelExtensions =
                options.get(OptionsJANIModel.JANI_MODEL_EXTENSION_CLASS);
        if (modelExtensions == null) {
            modelExtensions = new OrderedMap<>();
            options.set(OptionsJANIModel.JANI_MODEL_EXTENSION_CLASS, modelExtensions);
        }
        modelExtensions.put(ModelExtensionFunctions.IDENTIFIER,
                ModelExtensionFunctions.class);
        
        Map<String,Class<? extends ExplorerExtension>> explorerExtensions = options.get(OptionsJANIExplorer.JANI_EXPLORER_EXTENSION_CLASS);
        if (explorerExtensions == null) {
            explorerExtensions = new OrderedMap<>();
        }
        explorerExtensions.put(ExplorerExtensionFunctions.IDENTIFIER, ExplorerExtensionFunctions.class);
        options.set(OptionsJANIExplorer.JANI_EXPLORER_EXTENSION_CLASS, explorerExtensions);
        
        Map<String,Class<? extends EvaluatorExplicit.Builder>> evaluatorsExplicit = options.get(OptionsExpressionBasic.EXPRESSION_EVALUTOR_EXPLICIT_CLASS);
        if (evaluatorsExplicit == null) {
            evaluatorsExplicit = new OrderedMap<>(true);            
        }
        evaluatorsExplicit.put(EvaluatorExplicitCall.IDENTIFIER, EvaluatorExplicitCall.Builder.class);

        options.set(OptionsExpressionBasic.EXPRESSION_EVALUTOR_EXPLICIT_CLASS, evaluatorsExplicit);

    }
}
