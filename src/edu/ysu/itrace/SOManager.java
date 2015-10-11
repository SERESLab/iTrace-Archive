package edu.ysu.itrace;

import org.eclipse.swt.browser.Browser;

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
        VOTE
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
     * Constructor. Loads the SO DOM and sets up the Browser to automatically
     * reload after certain events.
     * @param browser Browser to which this SO DOM pertains.
     */
    public SOManager(Browser browser) {
        this.browser = browser;
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
        String soe = (String) browser.evaluate(
    			"if (document.readyState === 'interactive') {"
    			+ "function foundGaze(x, y, bounds) {"
    			+ 	"return (y < bounds.bottom || y > bounds.top || x < bounds.left || x > bounds.right) ? false:true;"
    			+ "}"
    			
    			+ "var question = document.getElementById('question');"
    			+ "var qPostText = question.getElementsByClassName('post-text');"
    			
    			+ "var qText = qPostText[0].getElementsByTagName('p');"
    			+ "var i;"
    			+ "for (i = 0; i < qText.length; i++) {"
    			+ 	"var found = foundGaze(" + relativeX + "," + relativeY + ", qText[i].getBoundingClientRect());"
    			+ 	"if (found == true) return 'question text' + i;"
    			+ "}"
    			
    			+ "var qCode = qPostText[0].getElementsByTagName('code');"
    			+ "for (i = 0; i < qCode.length; i++) {"
    			+ 	"var found = foundGaze(" + relativeX + "," + relativeY + ", qCode[i].getBoundingClientRect());"
    			+ 	"if (found == true) return 'question code' + i;"
    			+ "}"
    			
    			+ "var qTags = question.getElementsByClassName('post-tag');"
    			+ "for (i = 0; i < qTags.length; i++) {"
    			+ 	"var found = foundGaze(" + relativeX + "," + relativeY + ", qTags[i].getBoundingClientRect());"
    			+ 	"if (found == true) return 'question tag' + i;"
    			+ "}"
    			
    			+ "var qVote = question.getElementsByClassName('vote');"
    			+ "found = foundGaze(" + relativeX + "," + relativeY + ", qVote[0].getBoundingClientRect());"
    			+ "if (found == true) return 'question vote';"
    			
    			+ "var qTitle = document.getElementsByTagName('title');"
    			+ "found = foundGaze(" + relativeX + "," + relativeY + ", qTitle[0].getBoundingClientRect());"
    			+ "if (found == true) return 'question title';"
    			
    			+ "var qComment = question.getElementsByClassName('comment-text');"
    			+ "for (i = 0; i < qComment.length; i++) {"
    			+ 	"var found = foundGaze(" + relativeX + "," + relativeY + ", qComment[i].getBoundingClientRect());"
    			+ 	"if (found == true) return 'question comment' + i;"
    			+ "}"
    			
    			+ "var answers = document.getElementById('answers');"
    			+ "if (answers == null) return null;"
    			+ "var aVotes = answers.getElementsByClassName('vote');"
    			+ "for (i = 0; i < aVotes.length; i++) {"
    			+ 	"var found = foundGaze(" + relativeX + "," + relativeY + ", aVotes[i].getBoundingClientRect());"
    			+ 	"if (found == true) return 'answer vote' + i;"
    			+ "}"
    			
    			+ "var aPostText = answers.getElementsByClassName('post-text');"
    			+ "for (i = 0; i < aPostText.length; i++) {"
    			+ 	"var aText = aPostText[i].getElementsByTagName('p');"
    			+ 	"var aCode = aPostText[i].getElementsByTagName('code');"
    			+	"var j;"
    			+ 	"for (j = 0; j < aText.length; j++) {"
    			+		"var found = foundGaze(" + relativeX + "," + relativeY + ", aText[j].getBoundingClientRect());"
    			+ 		"if (found == true) return 'answer text' + i + j;"
    			+ 	"}"
    			+	"for (j = 0; j < aCode.length; j++) {"
    			+		"var found = foundGaze(" + relativeX + "," + relativeY + ", aCode[j].getBoundingClientRect());"
    			+ 		"if (found == true) return 'answer code' + i + j;"
    			+ 	"}"
    			+ "}"
    			
    			+ "var answerCells = answers.getElementsByClassName('answercell');"
    			+ "for (i = 0; i < answerCells.length; i++) {"
    			+ 	"var aComments = answerCells[i].getElementsByClassName('comment-text');"
    			+	"for (var j = 0; j < aComments.length; j++) {"
    			+ 		"var found = foundGaze(" + relativeX + "," + relativeY + ", aComments[j].getBoundingClientRect());"
    			+ 		"if (found == true) return 'answer comment' + i + j;"
    			+ 	"}"
    			+ "}"
    			
    			+ "} else {"
    			+ "return null;"
    			+ "}");
        
        //create the soe based on the returned string soe
        if (soe != null) {
        	if (soe.contains("question text")) {
        		entity.part = SOEPart.QUESTION;
        		entity.type = SOEType.TEXT;
        		entity.partNum = 1;
        		entity.typeNum = Character.getNumericValue(soe.charAt(soe.length()-1));
        	} else if (soe.contains("question code")) {
        		entity.part = SOEPart.QUESTION;
        		entity.type = SOEType.CODE;
        		entity.partNum = 1;
        		entity.typeNum = Character.getNumericValue(soe.charAt(soe.length()-1));
        	} else if (soe.contains("question title")) {
        		entity.part = SOEPart.QUESTION;
        		entity.type = SOEType.TITLE;
        		entity.partNum = 1;
        		entity.typeNum = 1;
        	} else if (soe.contains("question tag")) {
        		entity.part = SOEPart.QUESTION;
        		entity.type = SOEType.TAG;
        		entity.partNum = 1;
        		entity.typeNum = Character.getNumericValue(soe.charAt(soe.length()-1));
        	} else if (soe.contains("question vote")) {
        		entity.part = SOEPart.QUESTION;
        		entity.type = SOEType.VOTE;
        		entity.partNum = 1;
        		entity.typeNum = 1;
        	} else if (soe.contains("question comment")) {
        		entity.part = SOEPart.QUESTION;
        		entity.type = SOEType.COMMENT;
        		entity.partNum = 1;
        		entity.typeNum = Character.getNumericValue(soe.charAt(soe.length()-1));
        	} else if (soe.contains("answer text")) {
        		entity.part = SOEPart.ANSWER;
        		entity.type = SOEType.TEXT;
        		entity.partNum = Character.getNumericValue(soe.charAt(soe.length()-2));
        		entity.typeNum = Character.getNumericValue(soe.charAt(soe.length()-1));
        	} else if (soe.contains("answer code")) {
        		entity.part = SOEPart.ANSWER;
        		entity.type = SOEType.CODE;
        		entity.partNum = Character.getNumericValue(soe.charAt(soe.length()-2));
        		entity.typeNum = Character.getNumericValue(soe.charAt(soe.length()-1));
        	} else if (soe.contains("answer comment")) {
        		entity.part = SOEPart.ANSWER;
        		entity.type = SOEType.COMMENT;
        		entity.partNum = Character.getNumericValue(soe.charAt(soe.length()-2));
        		entity.typeNum = Character.getNumericValue(soe.charAt(soe.length()-1));
        	} else if (soe.contains("answer vote")) {
        		entity.part = SOEPart.ANSWER;
        		entity.type = SOEType.VOTE;
        		entity.partNum = Character.getNumericValue(soe.charAt(soe.length()-1));
        		entity.typeNum = 1;
        	} else { 
        		//ignore
        	}
        }
    	return entity;
    }
}
