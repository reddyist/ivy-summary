/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jenkinsci.plugins.ivy_summary;

import hudson.model.Action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

/**
 *
 * @author vareddy
 */
@ExportedBean(defaultVisibility=2)
public class IvySummaryAction implements Action {
    
    private final HashMap<String, ArrayList> sum;
    public static final String REGEX = "found .*#(.*);(.*) in";
    private final String iconPath = "warning.gif";
    
    @Exported public String getIconPath() { return iconPath; }
    
    public IvySummaryAction(HashMap<String, ArrayList> ivySummary) {
        this.sum = ivySummary;
    }
    
    public String getUrlName() {
        return "description";
    }
    
    public String getDisplayName() {
         return "Description";
    }
    
    public String getIconFileName() {
        return null;
    }
    
    @Exported public String getText() {
        StringBuilder textBuilder = new StringBuilder();
        textBuilder.append("<ul>");
        for (Map.Entry<String, ArrayList> entry: sum.entrySet()) {
            for (String val: (ArrayList<String>)entry.getValue()) {
                textBuilder.append(String.format("<li><b>%s</b>: %s</li>", entry.getKey(), val));
            }
            
        }
        textBuilder.append("<ul>");
        return textBuilder.toString();
    }
    
}
