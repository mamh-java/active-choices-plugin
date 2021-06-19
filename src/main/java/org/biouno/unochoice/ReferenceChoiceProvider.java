
package org.biouno.unochoice;

import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.util.FormValidation;
import net.sf.json.JSONObject;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.biouno.unochoice.model.Script;
import org.kohsuke.stapler.Ancestor;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.bind.JavaScriptMethod;

import java.util.List;


public class ReferenceChoiceProvider extends ChoiceListProvider {


    private static final long serialVersionUID = 8261526672604361397L;


    private String choiceType;

    private Boolean omitValueField;

    private Script script;
    private String referencedParameters;

    @DataBoundConstructor
    public ReferenceChoiceProvider(Script script, String choiceType, String referencedParameters, Boolean omitValueField) {
        this.script = script;
        this.referencedParameters = referencedParameters;
        this.choiceType = choiceType;
        this.omitValueField = BooleanUtils.toBooleanDefaultIfNull(omitValueField, Boolean.FALSE);
    }


    public String getChoiceType() {
        return this.choiceType;
    }

    public Boolean getOmitValueField() {
        return omitValueField;
    }


    public Script getScript() {
        return script;
    }

    public String getReferencedParameters() {
        return referencedParameters;
    }

    @Extension
    public static final class DescriptorImpl extends ChoiceListProviderDescriptor {


        @Override
        public String getDisplayName() {
            return Messages.ReferenceChoiceProvider_DisplayName();
        }


    }

}
