
package org.biouno.unochoice;

import hudson.Extension;
import org.biouno.unochoice.model.Script;
import org.kohsuke.stapler.DataBoundConstructor;

import java.util.List;


public class ActiveChoiceProvider extends ChoiceListProvider {
    private static final long serialVersionUID = -4449319038169585222L;


    private String choiceType;


    private Boolean filterable;

    private Integer filterLength;

    private Script script;


    @DataBoundConstructor
    public ActiveChoiceProvider(Script script, String choiceType, Boolean filterable, Integer filterLength) {
        this.script = script;
        this.choiceType = choiceType;
        this.filterable = filterable;
        this.filterLength = filterLength;
    }


    public String getChoiceType() {
        return this.choiceType;
    }


    public Boolean getFilterable() {
        return filterable;
    }


    public Integer getFilterLength() {
        return filterLength == null ? (Integer) 1 : filterLength;
    }

    public Script getScript() {
        return script;
    }

    @Extension
    public static final class DescriptImpl extends ChoiceListProviderDescriptor {

        @Override
        public String getDisplayName() {
            return "Active Choices Parameter";
        }

    }

}
