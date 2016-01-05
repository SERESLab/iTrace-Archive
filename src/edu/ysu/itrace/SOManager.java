package edu.ysu.itrace;

import java.util.regex.Pattern;

import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.ProgressEvent;
import org.eclipse.swt.browser.ProgressListener;

/**
 * Keeps updated information about the stack overflow document viewed in one Browser.
 */
public class SOManager {
    /**
     * Types of stack overflow entities.
     */
    public enum SOEType {
        TEXT,
        CODE,
        COMMENT,
        TITLE, //for Question part only
        TAG, //for Question part only
        VOTE,
        IMAGE
    }
    
    public enum SOEPart {
    	ANSWER,
    	QUESTION
    }

    /**
     * Information extracted about a stack overflow entity.
     */
    public class StackOverflowEntity {
        public SOEPart part;
        public SOEType type;
        public int partNum; //number of part (ie. Answer number 1, Answer number 2), Question part is always 1
        public int typeNum; //number of type (ie. Comment number 1, Comment number 2), Vote, and Title will always be 1
        //numbering is by the first element to be seen in the html code
    }
    
    private Browser browser;
    

    /**
     * Constructor. Sets up the Browser with a progress listener.
     * @param browser Browser to which this SO DOM pertains.
     */
    public SOManager(Browser browser) {
        this.browser = browser;
        
        /*
         * add the progress listener with a JavaScript function
         * to find the SOE at a specified location
         */
        addSOEFinder();
    }

    /**
     * Returns a string representation of the URL to the
     * Stack Overflow question page associated with the current browser.
     */
    public String getURL() {
        return browser.getUrl();
    }

    /**
     * Gets the stack overflow entity found at a location on the page.
     * @param relativeX x gaze coordinate relative to the control browser.
     * @param relativeY y gaze coordinate relative to the control browser.
     * @return the stack overflow entity
     */
    public StackOverflowEntity getSOE(int relativeX, int relativeY) {
        StackOverflowEntity entity = new StackOverflowEntity();
        
        //call JavaScript with relativeX and relativeY to map the x,y position to its SOE
        String soe = (String) browser.evaluate( "if (typeof findGaze == 'function') {"
        		+ "return findGaze(" + relativeX + "," + relativeY +");"
        		+ "}");
        //create the soe based on the returned string soe
        if (soe != null) {
        	if (soe.contains("question image")) {
        		entity.part = SOEPart.QUESTION;
        		entity.type = SOEType.IMAGE;
        		entity.partNum = 1;
        		entity.typeNum = Integer.parseInt(soe.split("-")[1])+1;
        		return entity;
        	}
        	if (soe.contains("question text")) {
        		entity.part = SOEPart.QUESTION;
        		entity.type = SOEType.TEXT;
        		entity.partNum = 1;
        		entity.typeNum = Integer.parseInt(soe.split("-")[1])+1;
        		return entity;
        	}
        	if (soe.contains("question code")) {
        		entity.part = SOEPart.QUESTION;
        		entity.type = SOEType.CODE;
        		entity.partNum = 1;
        		entity.typeNum = Integer.parseInt(soe.split("-")[1])+1;
        		return entity;
        	}
        	if (soe.contains("question title")) {
        		entity.part = SOEPart.QUESTION;
        		entity.type = SOEType.TITLE;
        		entity.partNum = 1;
        		entity.typeNum = 1;
        		return entity;
        	} 
        	if (soe.contains("question tag")) {
        		entity.part = SOEPart.QUESTION;
        		entity.type = SOEType.TAG;
        		entity.partNum = 1;
        		entity.typeNum = Integer.parseInt(soe.split("-")[1])+1;
        		return entity;
        	}
        	if (soe.contains("question vote")) {
        		entity.part = SOEPart.QUESTION;
        		entity.type = SOEType.VOTE;
        		entity.partNum = 1;
        		entity.typeNum = 1;
        		return entity;
        	}
        	if (soe.contains("question comment")) {
        		entity.part = SOEPart.QUESTION;
        		entity.type = SOEType.COMMENT;
        		entity.partNum = 1;
        		entity.typeNum = Integer.parseInt(soe.split("-")[1])+1;
        		return entity;
        	}
        	if (soe.contains("answer image")) {
        		entity.part = SOEPart.ANSWER;
        		entity.type = SOEType.IMAGE;
        		entity.partNum = Integer.parseInt(soe.split("-")[1])+1;
        		entity.typeNum = Integer.parseInt(soe.split("-")[2])+1;
        		return entity;
        	}
        	if (soe.contains("answer text")) {
        		entity.part = SOEPart.ANSWER;
        		entity.type = SOEType.TEXT;
        		entity.partNum = Integer.parseInt(soe.split("-")[1])+1;
        		entity.typeNum = Integer.parseInt(soe.split("-")[2])+1;
        		return entity;
        	}
        	if (soe.contains("answer code")) {
        		entity.part = SOEPart.ANSWER;
        		entity.type = SOEType.CODE;
        		entity.partNum = Integer.parseInt(soe.split("-")[1])+1;
        		entity.typeNum = Integer.parseInt(soe.split("-")[2])+1;
        		return entity;
        	}
        	if (soe.contains("answer comment")) {
        		entity.part = SOEPart.ANSWER;
        		entity.type = SOEType.COMMENT;
        		entity.partNum = Integer.parseInt(soe.split("-")[1])+1;
        		entity.typeNum = Integer.parseInt(soe.split("-")[2])+1;
        		return entity;
        	}
        	if (soe.contains("answer vote")) {
        		entity.part = SOEPart.ANSWER;
        		entity.type = SOEType.VOTE;
        		entity.partNum = Integer.parseInt(soe.split("-")[1])+1;
        		entity.typeNum = 1;
        		return entity;
        	}
        }
    	return null;
    }
    
    /**
     * Set up a page load listener to declare a JavaScript function for finding the SOE
     * at a specified position on every new page load
     */
    private void addSOEFinder() {
    	browser.addProgressListener(new ProgressListener() {
			@Override
			public void changed(ProgressEvent event) {
			}
			@Override
			public void completed(ProgressEvent event) {
				browser.execute(
		    			"function foundGaze(x, y, bounds) {"
		    			+ 	"return (y > bounds.bottom+10 || y < bounds.top-10 || x < bounds.left-10 || x > bounds.right+10) ? false:true;"
		    			+ "}"
		    			+ "function findGaze(x,y) {"
		    			+ "try {"
		    			+ "var question = document.getElementById('question');"
		    			+ "var qPostText = question.getElementsByClassName('post-text');"
		    			
		    			+ "var i;"
		    			+ "var qCode = qPostText[0].getElementsByTagName('code');"
		    			+ "for (i = 0; i < qCode.length; i++) {"
		    			+ 	"var found = foundGaze(x, y, qCode[i].getBoundingClientRect());"
		    			+ 	"if (found == true) return 'question code' + '-' + i;"
		    			+ "}"
		    			
		    			+ "var qImage = qPostText[0].getElementsByTagName('img');"
		    			+ "for (i = 0; i < qImage.length; i++) {"
		    			+ 	"var found = foundGaze(x, y, qImage[i].getBoundingClientRect());"
		    			+ 	"if (found == true) return 'question image' + '-' + i;"
		    			+ "}"
		    			
		    			+ "var qText = qPostText[0].querySelectorAll('p, ol, ul, dl, h1, h2, h3, h4, h5, h6');"
		    			+ "for (i = 0; i < qText.length; i++) {"
		    			+ 	"var found = foundGaze(x, y, qText[i].getBoundingClientRect());"
		    			+ 	"if (found == true) return 'question text' + '-' + i;"
		    			+ "}"
		    			
		    			+ "var qTags = question.getElementsByClassName('post-tag');"
		    			+ "for (i = 0; i < qTags.length; i++) {"
		    			+ 	"var found = foundGaze(x, y, qTags[i].getBoundingClientRect());"
		    			+ 	"if (found == true) return 'question tag' + '-' + i;"
		    			+ "}"
		    			
		    			+ "var qVote = question.getElementsByClassName('vote');"
		    			+ "found = foundGaze(x, y, qVote[0].getBoundingClientRect());"
		    			+ "if (found == true) return 'question vote';"
		    			
		    			+ "var qHeader = document.getElementById('question-header');"
		    			+ "var qTitle = qHeader.getElementsByTagName('h1');"
		    			+ "found = foundGaze(x, y, qTitle[0].getBoundingClientRect());"
		    			+ "if (found == true) return 'question title';"
		    			
		    			+ "var qComment = question.getElementsByClassName('comment-text');"
		    			+ "for (i = 0; i < qComment.length; i++) {"
		    			+ 	"var found = foundGaze(x, y, qComment[i].getBoundingClientRect());"
		    			+ 	"if (found == true) return 'question comment' + '-' + i;"
		    			+ "}"
		    			
		    			+ "var answers = document.getElementById('answers');"
		    			+ "if (answers == null) return null;"
		    			+ "var aVotes = answers.getElementsByClassName('vote');"
		    			+ "for (i = 0; i < aVotes.length; i++) {"
		    			+ 	"var found = foundGaze(x, y, aVotes[i].getBoundingClientRect());"
		    			+ 	"if (found == true) return 'answer vote' + '-' + i;"
		    			+ "}"
		    			
		    			+ "var aPostText = answers.getElementsByClassName('post-text');"
		    			+ "for (i = 0; i < aPostText.length; i++) {"
		    			+ 	"var aImage = aPostText[i].getElementsByTagName('img');"
		    			+ 	"var aText = aPostText[i].querySelectorAll('p, ol, ul, dl, h1, h2, h3, h4, h5, h6');"
		    			+ 	"var aCode = aPostText[i].getElementsByTagName('code');"
		    			+	"var j;"
		    			+	"for (j = 0; j < aCode.length; j++) {"
		    			+		"var found = foundGaze(x, y, aCode[j].getBoundingClientRect());"
		    			+ 		"if (found == true) return 'answer code' + '-' + i + '-' + j;"
		    			+ 	"}"
		    			+	"for (j = 0; j < aImage.length; j++) {"
		    			+		"var found = foundGaze(x, y, aImage[j].getBoundingClientRect());"
		    			+ 		"if (found == true) return 'answer image' + '-' + i + '-' + j;"
		    			+ 	"}"
		    			+ 	"for (j = 0; j < aText.length; j++) {"
		    			+		"var found = foundGaze(x, y, aText[j].getBoundingClientRect());"
		    			+ 		"if (found == true) return 'answer text' + '-' + i + '-' + j;"
		    			+ 	"}"
		    			+ "}"
		    			
		    			+ "var answerComments = answers.getElementsByClassName('comments');"
		    			+ "for (i = 0; i < answerComments.length; i++) {"
		    			+ 	"var aComments = answerComments[i].getElementsByClassName('comment-text');"
		    			+	"for (var j = 0; j < aComments.length; j++) {"
		    			+ 		"var found = foundGaze(x, y, aComments[j].getBoundingClientRect());"
		    			+ 		"if (found == true) return 'answer comment' + '-' + i + '-' + j;"
		    			+ 	"}"
		    			+ "}"
		    			+  "} catch(err) {"
		    			+ "return err.message;"
		    			+ "}"
		    			+ "}");
			}
		});
    }
}
