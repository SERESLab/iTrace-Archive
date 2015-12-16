package edu.ysu.itrace;

import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.ProgressEvent;
import org.eclipse.swt.browser.ProgressListener;

/**
 * Keeps updated information about the stack overflow document viewed in one Browser.
 */
public class BRManager {
    /**
     * Types of stack overflow entities.
     */
    public enum BREType {
        INFO,
		BUGNUM, // for Question part only
        TITLE, //for Question part only
    }
    
    public enum BREPart {
    	ANSWER,
		ATTACHMENT,
    	QUESTION
    }

    /**
     * Information extracted about a stack overflow entity.
     */
    public class BugReportEntity {
        public BREPart part;
        public BREType type;
        public int partNum; //number of part (ie. Answer number 1, Answer number 2), Question part is always 1
        public int typeNum; //number of type (ie. Comment number 1, Comment number 2), Vote, and Title will always be 1
        //numbering is by the first element to be seen in the html code
    }
    
    private Browser browser;
    

    /**
     * Constructor. Sets up the Browser with a progress listener.
     * @param browser Browser to which this BR DOM pertains.
     */
    public BRManager(Browser browser) {
        this.browser = browser;
        
        /*
         * add the progress listener with a JavaScript function
         * to find the bre at a specified location
         */
        addBREFinder();
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
    public BugReportEntity getBRE(int relativeX, int relativeY) {
        BugReportEntity entity = new BugReportEntity();
        
        //call JavaScript with relativeX and relativeY to map the x,y position to its bre
        String bre = (String) browser.evaluate( "if (typeof findGaze == 'function') {"
        		+ "return findGaze(" + relativeX + "," + relativeY +");"
        		+ "}");
        System.out.println(bre);
        System.out.println(relativeX + " " + relativeY);
        //create the bre based on the returned string bre
        if (bre != null) {
        	if (bre.contains("question info")) {
        		entity.part = BREPart.QUESTION;
        		entity.type = BREType.INFO;
        		entity.partNum = 1;
        		entity.typeNum = Character.getNumericValue(bre.charAt(bre.length()-1))+1;
        		return entity;
        	}
			if (bre.contains("question title")) {
        		entity.part = BREPart.QUESTION;
        		entity.type = BREType.TITLE;
        		entity.partNum = 1;
        		entity.typeNum = 1;
        		return entity;
        	} 
        	if (bre.contains("question bugnum")) {
        		entity.part = BREPart.QUESTION;
        		entity.type = BREType.BUGNUM;
        		entity.partNum = 1;
        		entity.typeNum = Character.getNumericValue(bre.charAt(bre.length()-1))+1;
        		return entity;
        	}
        	if (bre.contains("attachment info")) {
        		entity.part = BREPart.ATTACHMENT;
        		entity.type = BREType.INFO;
        		entity.partNum = 1;
        		entity.typeNum = Character.getNumericValue(bre.charAt(bre.length()-1))+1;
        		return entity;
        	}

			
			/*
        	if (bre.contains("question vote")) {
        		entity.part = BREPart.QUESTION;
        		entity.type = BREType.VOTE;
        		entity.partNum = 1;
        		entity.typeNum = 1;
        		return entity;
        	}
			if (bre.contains("question comment")) {
        		entity.part = BREPart.QUESTION;
        		entity.type = BREType.COMMENT;
        		entity.partNum = 1;
        		entity.typeNum = Character.getNumericValue(bre.charAt(bre.length()-1))+1;
        		return entity;
        	}
			*/
			
        	if (bre.contains("answer info")) {
        		entity.part = BREPart.ANSWER;
        		entity.type = BREType.INFO;
        		entity.partNum = Character.getNumericValue(bre.charAt(bre.length()-2))+1;
        		entity.typeNum = Character.getNumericValue(bre.charAt(bre.length()-1))+1;
        		return entity;
        	}
			
			/*
        	if (bre.contains("answer code")) {
        		entity.part = BREPart.ANSWER;
        		entity.type = BREType.CODE;
        		entity.partNum = Character.getNumericValue(bre.charAt(bre.length()-2))+1;
        		entity.typeNum = Character.getNumericValue(bre.charAt(bre.length()-1))+1;
        		return entity;
        	}
        	if (bre.contains("answer comment")) {
        		entity.part = BREPart.ANSWER;
        		entity.type = BREType.COMMENT;
        		entity.partNum = Character.getNumericValue(bre.charAt(bre.length()-2))+1;
        		entity.typeNum = Character.getNumericValue(bre.charAt(bre.length()-1))+1;
        		return entity;
        	}
        	if (bre.contains("answer vote")) {
        		entity.part = BREPart.ANSWER;
        		entity.type = BREType.VOTE;
        		entity.partNum = Character.getNumericValue(bre.charAt(bre.length()-1))+1;
        		entity.typeNum = 1;
        		return entity;
        	}
			*/
			
        }
    	return null;
    }
    
    /**
     * Set up a page load listener to declare a JavaScript function for finding the bre
     * at a specified position on every new page load
     */
    private void addBREFinder() {
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
		    			+ "var question = document.getElementById('bz_show_bug_column_1')+document.getElementById('bz_show_bug_column_2');"
		    			+ "var qBzShowBugColumn = question.getElementsByClassName('bz_show_bug_column');"
		    			
		    			+ "var qInfo = qBzShowBugColumn[0].getElementsByTagName('a');"
		    			+ "var i;"
		    			+ "for (i = 0; i < qBzShowBugColumn.length; i++) {"
		    			+ 	"var found = foundGaze(x, y, qBzShowBugColumn[i].getBoundingClientRect());"
		    			+ 	"if (found == true) return 'question info' + i;"
		    			+ "}"
		    			
						/*
		    			+ "var qCode = qPostText[0].getElementsByTagName('code');"
		    			+ "for (i = 0; i < qCode.length; i++) {"
		    			+ 	"var found = foundGaze(x, y, qCode[i].getBoundingClientRect());"
		    			+ 	"if (found == true) return 'question code' + i;"
		    			+ "}"
						*/
						+ "var attachment = document.getElementById('attachment_table');"
		    			+ "var attInfo = attachment.getElementsByClassName('bz_contenttype_text_html');"
		    			+ "for (i = 0; i < attInfo.length; i++) {"
		    			+ 	"var found = foundGaze(x, y, attInfo[i].getBoundingClientRect());"
		    			+ 	"if (found == true) return 'attachment info' + i;"
		    			+ "}"
		    			
		    			+ "var qTitle = document.getElementsById('short_desc_nonedit_display');"
		    			+ "found = foundGaze(x, y, qTitle[0].getBoundingClientRect());"
		    			+ "if (found == true) return 'question title';"
		    			
		    			/*+ "var qComment = question.getElementsByClassName('comment-text');"
		    			+ "for (i = 0; i < qComment.length; i++) {"
		    			+ 	"var found = foundGaze(x, y, qComment[i].getBoundingClientRect());"
		    			+ 	"if (found == true) return 'question comment' + i;"
		    			+ "}" */
		    			
		    			+ "var answers = document.getElementById('comments');"
		    			+ "if (answers == null) return null;"
								/*
		    			+ "var aVotes = answers.getElementsByClassName('vote');"
		    			+ "for (i = 0; i < aVotes.length; i++) {"
		    			+ 	"var found = foundGaze(x, y, aVotes[i].getBoundingClientRect());"
		    			+ 	"if (found == true) return 'answer vote' + i;"
		    			+ "}" */
		    			
		    			+ "var aInfo = answers.getElementsByClassName('bz_comment_table');"
		    			+ "for (i = 0; i < aInfo.length; i++) {"
		    			+ 	"var aText = aInfo[i].getElementsByTagName('pre');"
		    			//+ 	"var aCode = aInfo[i].getElementsByTagName('code');"
		    			+	"var j;"
		    			+ 	"for (j = 0; j < aText.length; j++) {"
		    			+		"var found = foundGaze(x, y, aText[j].getBoundingClientRect());"
		    			+ 		"if (found == true) return 'answer info' + i + j;"
		    			+ 	"}" /*
		    			+	/*"for (j = 0; j < aCode.length; j++) {"
		    			+		"var found = foundGaze(x, y, aCode[j].getBoundingClientRect());"
		    			+ 		"if (found == true) return 'answer code' + i + j;"
		    			+ 	"}"*/
		    			+ "}"
		    			/*
		    			+ "var answerCells = answers.getElementsByClassName('answercell');"
		    			+ "for (i = 0; i < answerCells.length; i++) {"
		    			+ 	"var aComments = answerCells[i].getElementsByClassName('comment-text');"
		    			+	"for (var j = 0; j < aComments.length; j++) {"
		    			+ 		"var found = foundGaze(x, y, aComments[j].getBoundingClientRect());"
		    			+ 		"if (found == true) return 'answer comment' + i + j;"
		    			+ 	"}"
		    			+ "}"*/
		    			+  "}catch(err) {"
		    			+ "return err.message;"
		    			+ "}"
		    			+ "}");
			}
		});
    }
}
