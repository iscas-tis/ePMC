package epmc.webserver.frontend.dbms;

/**
 *
 * @author ori
 */
public class Model {
	private int modelId;
	private int userId;
	private String modelName;
	private String modelContent;
	private String modelComment;
	private String modelOptions;
	private String modelCreationTimestamp;
	private String modelUpdateTimestamp;

	/**
	 *
	 * @return
	 */
	public int getUserId() {
		return userId;
	}

	/**
	 *
	 * @param userId
	 */
	public void setUserId(int userId) {
		this.userId = userId;
	}

	/**
	 *
	 * @return
	 */
	public int getModelId() {
		return modelId;
	}

	/**
	 *
	 * @param modelId
	 */
	public void setModelId(int modelId) {
		this.modelId = modelId;
	}

	/**
	 *
	 * @return
	 */
	public String getModelName() {
		return modelName;
	}

	/**
	 *
	 * @param modelName
	 */
	public void setModelName(String modelName) {
		this.modelName = modelName;
	}

	/**
	 *
	 * @return
	 */
	public String getModelContent() {
		return modelContent;
	}

	/**
	 *
	 * @param modelContent
	 */
	public void setModelContent(String modelContent) {
		this.modelContent = modelContent;
	}

	/**
	 *
	 * @return
	 */
	public String getModelComment() {
		return modelComment;
	}

	/**
	 *
	 * @param modelComment
	 */
	public void setModelComment(String modelComment) {
		this.modelComment = modelComment;
	}

	/**
	 *
	 * @return
	 */
	public String getModelOptions() {
		return modelOptions;
	}

	/**
	 *
	 * @param modelOptions
	 */
	public void setModelOptions(String modelOptions) {
		this.modelOptions = modelOptions;
	}

	/**
	 *
	 * @return
	 */
	public String getModelCreationTimestamp() {
		return modelCreationTimestamp;
	}

	/**
	 *
	 * @param modelCreationTimestamp
	 */
	public void setModelCreationTimestamp(String modelCreationTimestamp) {
		this.modelCreationTimestamp = modelCreationTimestamp;
	}

	/**
	 *
	 * @return
	 */
	public String getModelUpdateTimestamp() {
		return modelUpdateTimestamp;
	}

	/**
	 *
	 * @param modelUpdateTimestamp
	 */
	public void setModelUpdateTimestamp(String modelUpdateTimestamp) {
		this.modelUpdateTimestamp = modelUpdateTimestamp;
	}
}
