/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jenkinsci.plugins.ivy_summary;

import hudson.AbortException;
import hudson.Extension;
import hudson.Launcher;
import hudson.matrix.MatrixAggregatable;
import hudson.matrix.MatrixAggregator;
import hudson.matrix.MatrixBuild;
import hudson.matrix.MatrixRun;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.Build;
import hudson.model.BuildListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import org.kohsuke.stapler.DataBoundConstructor;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author vareddy
 */
public class IvySummaryRecorder extends Recorder implements MatrixAggregatable {
    
    private static boolean setMatrixSummary = false;
    
    /**
    * Required constructor. Our Recorder, which extends Publisher, which implements Describable
    * must have specified a DataBoundConstuctor. The DataBoundConstructor is invoked whenever 
    * the job cofiguration is saved, and is used to bind form input variables to constructor 
    * parameters.
    */
    @DataBoundConstructor
    public IvySummaryRecorder() {
        
    }
    

             /**
    * Required class with a concrete implementation of the descriptor.
    */
    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {

                 /**
        * Same as with the {@link FirstBuilder}
        * @param project The class name of the selected project type.
        * @return a boolean value indicating whether this BuildStep can be used with the selected Project Type.
        */
        @Override
        public boolean isApplicable(Class<? extends AbstractProject> project) {
            return true;
        }

                /**
        * Required method. Needs to be overridden.
        *
        * @return the name to be display in the describable list
        */
        @Override
        public String getDisplayName() {
            return "Ivy Packages Info Displayer";
        }
        
    }
    
    //Just copy paste this. No reason to worry about this, do it this way and it will work with 
    //modern Jenkin installation
    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }
    
    /**
    * Performs the required operations for this build step. The method should generally return 
    * true. If some critical error arises such as  not being able to open a required file, it 
    * is much better to abort the pipeline by throwing an {@link AbortException}. This very simple
    * reference implementation contains code that checks if the action contains items with the 
    * text, you specify when configuring the {@link FirstRecorder} project.
    *
    * @param build
    * @param launcher
    * @param listener
    * @return a boolean value indicating proper execution, if true, the next item in build step
    *         is picked up for execution
    * @throws InterruptedException
    * @throws IOException
    */
    @Override
    public boolean perform(Build<?, ?> build, Launcher launcher, BuildListener listener) 
            throws InterruptedException, IOException {
        IvySummaryAction sumAction = new IvySummaryAction(parseLog(build.getLogFile(), 
                                                          IvySummaryAction.REGEX));
        build.addAction(sumAction);
        listener.getLogger().println("Build Summary set: " + sumAction);
        return true;
    }
    
    private HashMap<String, ArrayList> parseLog(File logFile, String regexp) throws IOException, InterruptedException {
        HashMap<String, ArrayList> deps = new HashMap<String, ArrayList>();
        if (regexp == null) {
            return deps;
        }
        // Assume default encoding and text files
        String line;
        Pattern pattern = Pattern.compile(regexp);
        BufferedReader reader = new BufferedReader(new FileReader(logFile));
        while ((line = reader.readLine()) != null) {
                Matcher match = pattern.matcher(line);
                if (match.find()) {
                    ArrayList<String> val = new ArrayList<String>();
                    val.add(match.group(2));
                    deps.put(match.group(1), val);                    
                }
        }
        return deps;
    }
    
    public MatrixAggregator createAggregator(final MatrixBuild build,
                Launcher launcher, final BuildListener listener) {

        return new MatrixAggregator(build, launcher, listener) {
                @Override
                public boolean endRun(MatrixRun run) throws InterruptedException,
                                IOException {
                        if (isSetForMatrix()) {
                            return true;
                        }
                        setMatrixSummary = true;
                        if (run.getAction(IvySummaryAction.class) != null) {
                            build.addAction(run.getAction(IvySummaryAction.class));
                        }
                        
                        return true;
                }
        };
    }
    
    public boolean isSetForMatrix() {
        return setMatrixSummary;
    }

}
