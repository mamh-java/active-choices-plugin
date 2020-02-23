/*
 * The MIT License (MIT)
 *
 * Copyright (c) <2014-2015> <Ioannis Moutsatsos, Bruno P. Kinoshita>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.biouno.unochoice;

import hudson.DescriptorExtensionList;
import hudson.Extension;
import hudson.Util;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.model.DescriptorVisibilityFilter;
import hudson.model.ParameterDefinition;
import hudson.model.ParameterValue;
import hudson.model.SimpleParameterDefinition;
import hudson.model.StringParameterValue;
import hudson.util.FormValidation;
import hudson.util.VariableResolver;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.jvnet.localizer.Localizable;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;



public class ActiveChoiceParameterDefinition extends SimpleParameterDefinition {
    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = Logger.getLogger(ActiveChoiceParameterDefinition.class.getName());

    private String randomName;



    @Extension
    public static class DescriptorImpl extends ParameterDescriptor {
        private Map<String, Boolean> choiceListEnabledMap;

        public DescriptorImpl() {
            setChoiceListEnabledMap(Collections.<String, Boolean>emptyMap());
            load();
        }

        protected void setChoiceListEnabledMap(Map<String, Boolean> choiceListEnabledMap) {
            this.choiceListEnabledMap = choiceListEnabledMap;
        }

        protected Map<String, Boolean> getChoiceListEnabledMap() {
            return choiceListEnabledMap;
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject json) throws FormException {
            return super.configure(req, json);
        }


        @Override
        public ActiveChoiceParameterDefinition newInstance(StaplerRequest req, JSONObject formData) throws FormException {
            return (ActiveChoiceParameterDefinition) super.newInstance(req, formData);
        }

        private <T extends Describable<?>> T bindJSONWithDescriptor(
                StaplerRequest req,
                JSONObject formData,
                String fieldName,
                Class<T> clazz
        ) throws FormException {
            formData = formData.getJSONObject(fieldName);
            if (formData == null || formData.isNullObject()) {
                return null;
            }
            String staplerClazzName = formData.optString("$class", null);
            if (staplerClazzName == null) {
                // Fall back on the legacy stapler-class attribute.
                staplerClazzName = formData.optString("stapler-class", null);
            }
            if (staplerClazzName == null) {
                throw new FormException("No $stapler nor stapler-class is specified", fieldName);
            }
            Jenkins jenkins = Jenkins.getInstance();
            if (jenkins == null) {
                throw new IllegalStateException("Jenkins instance is unavailable.");
            }
            try {
                @SuppressWarnings("unchecked")
                Class<? extends T> staplerClass = (Class<? extends T>) jenkins.getPluginManager().uberClassLoader.loadClass(staplerClazzName);
                Descriptor<?> d = jenkins.getDescriptorOrDie(staplerClass);

                @SuppressWarnings("unchecked")
                T instance = (T) d.newInstance(req, formData);

                return instance;
            } catch (ClassNotFoundException e) {
                throw new FormException(
                        String.format("Failed to instantiate %s", staplerClazzName),
                        e,
                        fieldName
                );
            }
        }

        /**
         * Returns the string to be shown in a job configuration page, in the dropdown of &quot;Add Parameter&quot;.
         *
         * @return a name of this parameter type.
         * @see hudson.model.ParameterDefinition.ParameterDescriptor#getDisplayName()
         */
        @Override
        public String getDisplayName() {
            return "ActiveChoiceParameterDefinition";
        }


        public DescriptorExtensionList<ChoiceListProvider, Descriptor<ChoiceListProvider>> getChoiceListProviderList() {
            return ChoiceListProvider.all();
        }


        public List<Descriptor<ChoiceListProvider>> getEnabledChoiceListProviderList() {
            return DescriptorVisibilityFilter.apply(this, ChoiceListProvider.all());
        }

        public FormValidation doCheckName(@QueryParameter String name) {
            return FormValidation.ok();
        }
    }


    private ChoiceListProvider choiceListProvider = null;


    public ChoiceListProvider getChoiceListProvider() {
        return choiceListProvider;
    }

    public String getRandomName() {
        return randomName;
    }

    public ChoiceListProvider getEnabledChoiceListProvider() {
        ChoiceListProvider p = getChoiceListProvider();
        if (p == null) {
            return null;
        }

        // filter providers.
        List<Descriptor<ChoiceListProvider>> testList = DescriptorVisibilityFilter.apply(
                getDescriptor(),
                Arrays.asList(p.getDescriptor())
        );
        if (testList.isEmpty()) {
            LOGGER.log(Level.WARNING, "{0} is configured but disabled in the system configuration.", p.getDescriptor().getDisplayName());
            return null;
        }
        return p;
    }


    public List<String> getChoiceList() {
        return null;
    }


    @DataBoundConstructor
    public ActiveChoiceParameterDefinition(String name, String description, String randomName, ChoiceListProvider choiceListProvider) {
        super(name, description);
        this.randomName = randomName;
        this.choiceListProvider = choiceListProvider;
    }



    protected ParameterValue createValueCommon(StringParameterValue value) {
        return value;
    }


    @Override
    public ParameterValue createValue(StaplerRequest request, JSONObject jo) {
        StringParameterValue value = request.bindJSON(StringParameterValue.class, jo);
        value.setDescription(getDescription());
        return createValueCommon(value);
    }


    @Override
    public ParameterValue createValue(String value) throws IllegalArgumentException {
        return createValueCommon(new StringParameterValue(getName(), value, getDescription()));
    }


    @Override
    public ParameterValue getDefaultParameterValue() {
        return createValue("");
    }
}
