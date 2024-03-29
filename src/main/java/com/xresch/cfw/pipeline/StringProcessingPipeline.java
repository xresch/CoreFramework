package com.xresch.cfw.pipeline;

public class StringProcessingPipeline extends Pipeline<String, String> {
	/*******************************************************************************
	 * Constructor
	 *******************************************************************************/
	public StringProcessingPipeline() {
		super();
		
	}
	
	/*********************************************************************
	 * 
	 * @return
	 *********************************************************************/
	public StringProcessingPipeline removeBlankLines() {
		
		this.add(new PipelineAction<String, String>(){
			@Override
			public void execute(PipelineActionContext context) throws Exception {

				while(!inQueue.isEmpty()) {
					String line = inQueue.poll();
					if(!line.trim().isEmpty()) {
						outQueue.add(line);
					}
				}
				this.setDoneIfPreviousDone();
			}
			
		});
		return this;
	}
	
	/*********************************************************************
	 * 
	 * @return
	 *********************************************************************/
	public StringProcessingPipeline grep(String searchTerm, boolean inverse) {
		
		this.add(new PipelineAction<String, String>(){
			@Override
			public void execute(PipelineActionContext context) throws Exception {

				while(!inQueue.isEmpty()) {
					String line = inQueue.poll();
					if( !inverse) {
						if(line.contains(searchTerm)) {
							outQueue.add(line);
						}
					}else {
						if(!line.contains(searchTerm)) {
							outQueue.add(line);
						}
					}
				}
				this.setDoneIfPreviousDone();
			}
			
		});
		return this;
	}
	
	/*********************************************************************
	 * 
	 * @return
	 *********************************************************************/
	public StringProcessingPipeline countLines() {
		
		this.add(new PipelineAction<String, String>(){
			int counter = 0;
			@Override
			public void execute(PipelineActionContext context) throws Exception {
				
				PipelineAction<?, String> previousAction = this.getPreviousAction();
				while(true) {
					if(previousAction == null || previousAction.isDone()) {
						while(!inQueue.isEmpty()) {
							inQueue.poll();
							counter++;
						}
						break;
					}
					this.waitForInput(50);
				}
				outQueue.add(counter+"");
				this.setDoneIfPreviousDone();
			}
			
		});
		return this;
	}
	
	/*********************************************************************
	 * 
	 * @return
	 *********************************************************************/
	public StringProcessingPipeline trim() {
		
		this.add(new PipelineAction<String, String>(){
			@Override
			public void execute(PipelineActionContext context) throws Exception {

				while(!inQueue.isEmpty()) {
					outQueue.add(inQueue.poll().trim());
				}
				this.setDoneIfPreviousDone();
			}
			
		});
		return this;
	}
	
	public StringProcessingPipeline removeComments() {
		this.add(new RemoveCommentsAction());
		return this;
	}

}
