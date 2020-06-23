package com.pengtoolbox.cfw.pipeline;

public class RemoveCommentsAction extends PipelineAction<String, String>{

	private boolean isBlockCommentOpen = false;
	
	@Override
	void execute() throws Exception {
		boolean isBlockComment = false;
		while(!inQueue.isEmpty()) {
			String line = getInQueue().poll();
			
			//=================================================
			// Handle Single Line Comments
			//=================================================
			if(line.trim().startsWith("//")) {
				continue;
			}
			
			//=================================================
			// Handle Open Block comments
			//=================================================
			if(isBlockCommentOpen) {
				
				while(!inQueue.isEmpty() && !line.contains("*/")) {
					line = getInQueue().poll();
				}
				
				if(line.contains("*/")) {
					isBlockCommentOpen = false;
					line = line.substring(line.indexOf("*/")+2);
					if(line.trim().isEmpty()) {
						continue;
					}
				}
			}
			
			//=================================================
			// Handle Block comments
			//=================================================
			if(line.contains("/*")) {
				
				//----------------------------
				//remove all inline block comments
				while(line.contains("*/")) {
					line = line.substring(0, line.indexOf("/*")) + " " + line.substring(line.indexOf("*/")+2);
				}
				
				if(line.trim().isEmpty()) {
					continue;
				}
				
				//----------------------------
				//remove starting inline block comments
				if(line.contains("/*")) {
					
					//------------------------
					// Check is in Quotes

					line = line.substring(0, line.indexOf("/*"));
					if(!line.trim().isEmpty()) {
						outQueue.add(line);
					}
					isBlockCommentOpen = true;
					continue;
				}
				
			}
			//=================================================
			// Handle lines without comments
			//=================================================
			outQueue.add(line);
		}
		this.setDoneIfPreviousDone();
		
	}
	
}
