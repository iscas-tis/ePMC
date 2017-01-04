package epmc.webserver.backend.worker;

import epmc.webserver.backend.DataStore;
import epmc.webserver.backend.worker.task.towork.BuildModelTask;

/**
 * Worker that parse the prism model and generate the corresponding {@linkplain ModelPRISM}, that is just drop
 * @author ori
 */
public class PrismModelBuilder extends Worker {
	private final DataStore dataStore = DataStore.getDataStore();
	private final BuildModelTask model;
	
	/**
	 * Instantiates a new PrismModelBuilder with a model to be built
	 * @param model The task containing model to build
	 */
	public PrismModelBuilder(BuildModelTask model) {
		this.model = model;
	}

	@Override
	public void run() {
		// TODO!
//		PrismParser pp = new PrismParser(new StringReader(model.getModel()));
	//	ModelPRISM resultingModel;
//		try {
		//	resultingModel = pp.parseModel();
			//dataStore.addWorkedTask(this, new CompletedBuildModelTask(model.getUserId(), model.getTaskId(), TaskOperation.build, resultingModel));
			// TODO Moritz CHECK
			//		} catch (EPMCParseException impe) {
//			dataStore.addWorkedTask(this, new FailedParseTask(model.getUserId(), model.getTaskId(), TaskOperation.build, impe.getErrorIdentifier(), impe.getErrorLine(), impe.getErrorColumn(), impe.getErrorIdentifier()));
//	     } catch (EPMCRuntimeException imre) {
//	            dataStore.addWorkedTask(this, new FailedRuntimeTask(model.getUserId(), model.getTaskId(), TaskOperation.build, imre.getKey(), imre));
		//} catch (EPMCException ime) {
	//		dataStore.addWorkedTask(this, new FailedAnalysisTask(model.getUserId(), model.getTaskId(), TaskOperation.build, ime.getProblemString(), ime.getArguments(), ime.getLocalizedMessage()));
	//	} catch (Throwable thr) {
	//		dataStore.addWorkedTask(this, new FailedRuntimeTask(model.getUserId(), model.getTaskId(), TaskOperation.build, ProblemsWebserver.WORKER_MODEL_BUILDER_GENERAL_ERROR.toString(), thr));
	//	}
	}
}
