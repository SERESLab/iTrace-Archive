package edu.ysu.itrace;

import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.ProgressEvent;
import org.eclipse.swt.browser.ProgressListener;

/**
 * Keeps updated information about the Bug Report document viewed in one Browser.
 */
public class BRManager {
    /**
     * Types of Bug Report entities.
     */
    public enum BREType {
        INFO,
        TITLE,
    }
    
    public enum BREPart {
    	ANSWER,
		ATTACHMENT,
    	QUESTION
    }

    /**
     * Information extracted about a bug report entity.
     */
    public class BugReportEntity {
        public BREPart part;
        public BREType type;
        public int partNum;
        public int typeNum;
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
     * Bug report page associated with the current browser.
     */
    public String getURL() {
        return browser.getUrl();
    }

    /**
     * Gets the Bug Report entity found at a location on the page.
     * @param relativeX x gaze coordinate relative to the control browser.
     * @param relativeY y gaze coordinate relative to the control browser.
     * @return the Bug Report entity
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
        		entity.typeNum = Integer.parseInt(bre.split("-")[1])+1;
        		return entity;
        	}
			if (bre.contains("question title")) {
        		entity.part = BREPart.QUESTION;
        		entity.type = BREType.TITLE;
        		entity.partNum = 1;
        		entity.typeNum = 1;
        		return entity;
        	} 
        	if (bre.contains("attachment info")) {
        		entity.part = BREPart.ATTACHMENT;
        		entity.type = BREType.INFO;
        		entity.partNum = 1;
        		entity.typeNum = Integer.parseInt(bre.split("-")[1])+1;
        		return entity;
        	}
			
        	if (bre.contains("answer info")) {
        		entity.part = BREPart.ANSWER;
        		entity.type = BREType.INFO;
        		entity.partNum = Integer.parseInt(bre.split("-")[1])+1;
        		entity.typeNum = 1; //comment info/text is in <pre> format so we cannot determine multiple type nums accurately
        		return entity;
        	}
			
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
		    			+ "var i;"
		    			
		    			+ "var question1 = document.getElementById('bz_show_bug_column_1');"
		    			+ "if (question1 == null) return null;"
		    			+ "var blankSpaces1 = question1.getElementsByClassName('bz_section_spacer');"
						+ "for (i = 0; i < blankSpaces1.length; i++) {"
						+ 	"var parent = blankSpaces1[i].parentNode;"
						+ 	"parent.parentNode.removeChild(parent);"
						+ "}"
		    			+ "var q1Info = question1.getElementsByTagName('tr');"
		    			+ "for (i = 0; i < q1Info.length; i++) {"
		    			+ 	"var found = foundGaze(x, y, q1Info[i].getBoundingClientRect());"
		    			+ 	"if (found == true) return 'question info' + '-' + i;"
		    			+ "}"
		    			
		    			+ "var question2 = document.getElementById('bz_show_bug_column_2');"
		    			+ "if (question2 == null) return null;"
		    			+ "var blankSpaces2 = question2.getElementsByClassName('bz_section_spacer');"
						+ "for (i = 0; i < blankSpaces2.length; i++) {"
						+ 	"var parent = blankSpaces2[i].parentNode;"
						+ 	"parent.parentNode.removeChild(parent);"
						+ "}"
		    			+ "var q2Info = question2.getElementsByTagName('tr');"
		    			+ "for (i = 0; i < q2Info.length; i++) {"
		    			+ 	"var found = foundGaze(x, y, q2Info[i].getBoundingClientRect());"
		    			+ 	"var num = i + q1Info.length;"
		    			+ 	"if (found == true) return 'question info' + '-' + num;"
		    			+ "}"
		    			
						+ "var attachment = document.getElementById('attachment_table');"
						+ "if (attachment == null) return null;"
						+ "var hiddenAttachments = attachment.getElementsByClassName('bz_default_hidden');"
						+ "for (i = 0; i < hiddenAttachments.length; i++) {"
						+ 	"hiddenAttachments[i].parentNode.removeChild(hiddenAttachments[i]);"
						+ "}"
		    			+ "var attInfo = attachment.getElementsByTagName('tr');"
		    			+ "for (i = 1; i < attInfo.length; i++) {"
		    			+ 	"var found = foundGaze(x, y, attInfo[i].getBoundingClientRect());"
		    			+ 	"var num = i-1;"
		    			+ 	"if (found == true) return 'attachment info' + '-' + num;"
		    			+ "}"
		    			
		    			+ "var qTitle = document.getElementsByClassName('bz_alias_short_desc_container');"
		    			+ "if (qTitle[0] == null) return null;"
		    			+ "found = foundGaze(x, y, qTitle[0].getBoundingClientRect());"
		    			+ "if (found == true) return 'question title';"
		    			
		    			+ "var answersText = document.getElementsByClassName('bz_comment_text');"
		    			+ "for (i = 0; i < answersText.length; i++) {"
		    			+	"var found = foundGaze(x, y, answersText[i].getBoundingClientRect());"
		    			+ 	"if (found == true) return 'answer info' + '-' + i;"
		    			+ "}"
		    			
		    			+ "} catch(err) {"
		    			+ 	"return err.message;"
		    			+ "}"
		    			+ "}");
			}
		});
    }
}
