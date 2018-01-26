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
     * Returns a string representation of the title of the 
     * Bug report web page
     */
    public String getTitle() {
    	return (String) browser.evaluate("if (typeof findBRGaze == 'function') {"
        		+ "try {"
    			+ 	"var title = document.getElementsByTagName('title');"
        		+ 	"return title[0].textContent;"
        		+ "} catch(err) {"
        		+ 	"return err.message;"
        		+ "}"
        		+ "}");
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
        String bre = (String) browser.evaluate( "if (typeof findBRGaze == 'function') {"
        		+ "return findBRGaze(" + relativeX + "," + relativeY +");"
        		+ "}");
        
        
        //create the bre based on the returned string bre
        if (bre != null) {
        	if (bre.contains("question info")) {
        		entity.part = BREPart.QUESTION;
        		entity.type = BREType.INFO;
        		entity.partNum = 1;
        		entity.typeNum = Integer.parseInt(bre.split("-")[1]); //1 is left col and 2 is right col
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
        		entity.partNum = Integer.parseInt(bre.split("-")[1]);
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
		    			"function foundBRGaze(x, y, bounds) {"
		    			+ 	"return (y > bounds.bottom+10 || y < bounds.top-10 || x < bounds.left-10 || x > bounds.right+10) ? false:true;"
		    			+ "}"
		    			+ "function findBRGaze(x,y) {"
						+ "try {"
		    			+ "var i;"
		    			
		    			+ "var question1 = document.getElementById('bz_show_bug_column_1');"
		    			+ "if (question1 == null) return null;"
		    			+ "var found = foundBRGaze(x, y, question1.getBoundingClientRect());"
		    			+ "if (found == true) return 'question info' + '-' + 1;"
	
		    			+ "var question2 = document.getElementById('bz_show_bug_column_2');"
		    			+ "if (question2 == null) return null;"
		    			+ "found = foundBRGaze(x, y, question2.getBoundingClientRect());"
		    			+ "if (found == true) return 'question info' + '-' + 2;"
		    
						+ "var attachment = document.getElementById('attachment_table');"
						+ "if (attachment == null) return null;"
						+ "var hiddenAttachments = attachment.getElementsByClassName('bz_default_hidden');"
						+ "for (i = 0; i < hiddenAttachments.length; i++) {"
						+ 	"hiddenAttachments[i].parentNode.removeChild(hiddenAttachments[i]);"
						+ "}"
		    			+ "var attInfo = attachment.getElementsByTagName('tr');"
		    			+ "for (i = 1; i < attInfo.length; i++) {"
		    			+ 	"var found = foundBRGaze(x, y, attInfo[i].getBoundingClientRect());"
		    			+ 	"var num = i-1;"
		    			+ 	"if (found == true) return 'attachment info' + '-' + num;"
		    			+ "}"
		    			+ "var answersText = document.getElementsByClassName('bz_comment');"
		    			+ "if (answersText == null) return null;"
		    			+ "for (i=1; i < answersText.length; i++){"
		    			+ 	"var found = foundBRGaze(x, y, answersText[i].getBoundingClientRect());" 
		    			+	"if (found == true) return 'answer info' + '-' + i;"
		    			+ "}"
		    			
		    			/*+ "var qTitle = document.getElementsByClassName('bz_alias_short_desc_container');"
		    			+ "if (qTitle[0] == null) return null;"
		    			+ "found = foundBRGaze(x, y, qTitle[0].getBoundingClientRect());"
		    			+ "if (found == true) return 'question title';"
		    			*/
						+ "} catch(err) {"
		    			+ 	"return err.message;"
		    			+ "}"
		    			+ "}");
			}
		});
    }
}
