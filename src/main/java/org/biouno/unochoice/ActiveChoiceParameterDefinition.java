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

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.util.JSONUtils;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.biouno.unochoice.model.Script;
import org.biouno.unochoice.util.ScriptCallback;
import org.biouno.unochoice.util.Utils;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.DoNotUse;
import org.kohsuke.stapler.Ancestor;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.bind.JavaScriptMethod;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.json.JsonHttpResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;

import hudson.DescriptorExtensionList;
import hudson.Extension;
import hudson.model.AbstractBuild;
import hudson.model.AbstractItem;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.model.DescriptorVisibilityFilter;
import hudson.model.FileParameterValue;
import hudson.model.Item;
import hudson.model.Job;
import hudson.model.ParameterValue;
import hudson.model.Project;
import hudson.model.SimpleParameterDefinition;
import hudson.model.StringParameterValue;
import hudson.util.FormValidation;
import jenkins.model.Jenkins;


public class ActiveChoiceParameterDefinition extends SimpleParameterDefinition {
    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = Logger.getLogger(ActiveChoiceParameterDefinition.class.getName());

    /*
     * Constants.
     * from class AbstractUnoChoiceParameter 第 1 父类
     * 这个AbstractUnoChoiceParameter是之前的 3个 parameter 的 父类，继承SimpleParameterDefinition的。
     * 现在是要统一把之前的 3个 parameter 的 类 内容 放到 1个 parameter中，也就是放到
     * public class ActiveChoiceParameterDefinition extends SimpleParameterDefinition  中
     */
    public static final String PARAMETER_TYPE_SINGLE_SELECT = "PT_SINGLE_SELECT"; // default choice type 单选下拉列表
    public static final String PARAMETER_TYPE_MULTI_SELECT = "PT_MULTI_SELECT"; //多选 下拉列表
    public static final String PARAMETER_TYPE_CHECK_BOX = "PT_CHECKBOX"; //方块多选框
    public static final String PARAMETER_TYPE_RADIO = "PT_RADIO"; //圆形单选框

    public static final String ELEMENT_TYPE_TEXT_BOX = "ET_TEXT_BOX"; // default choice type
    public static final String ELEMENT_TYPE_ORDERED_LIST = "ET_ORDERED_LIST";
    public static final String ELEMENT_TYPE_UNORDERED_LIST = "ET_UNORDERED_LIST";
    public static final String ELEMENT_TYPE_FORMATTED_HTML = "ET_FORMATTED_HTML";
    public static final String ELEMENT_TYPE_FORMATTED_HIDDEN_HTML = "ET_FORMATTED_HIDDEN_HTML";

    private static final int DEFAULT_MAX_VISIBLE_ITEM_COUNT = 10;

    /**
     * Used to split values that come from the UI via Ajax POST's
     * from class AbstractScriptableParameter  第 2 父类
     */
    private static final String SEPARATOR = "__LESEP__";
    /**
     * Used to split values when scripts return values like A=2, B=3.
     * from class AbstractScriptableParameter 第 2 父类
     */
    private static final String EQUALS = "=";
    /**
     * Constant used to add the project in the environment variables map.
     * from class AbstractScriptableParameter 第 2 父类
     */
    private static final String JENKINS_PROJECT_VARIABLE_NAME = "jenkinsProject";
    /**
     * Constant used to add the build in the environment variables map.
     * from class AbstractScriptableParameter 第 2 父类
     */
    private static final String JENKINS_BUILD_VARIABLE_NAME = "jenkinsBuild";
    /**
     * Constant used to add the parameter name in the environment variables map.
     * from class AbstractScriptableParameter 第 2 父类
     */
    private static final String JENKINS_PARAMETER_VARIABLE_NAME = "jenkinsParameter";

    /**
     * public class ActiveChoiceParameterDefinition extends SimpleParameterDefinition
     * public class SimpleParameterDefinition extends ParameterDefinition
     * from public abstract class ParameterDefinition
     */
    private String type;

    /**
     * from class AbstractUnoChoiceParameter 第 1 父类
     */
    private String randomName;

    /**
     * Number of visible items on the screen.
     * from class AbstractScriptableParameter 第 2 父类
     */
    private int visibleItemCount = 1;
    /**
     * Script used to render the parameter.
     * from class AbstractScriptableParameter 第 2 父类
     */
    private Script script;

    /**
     * The project name.
     * from class AbstractScriptableParameter 第 2 父类
     */
    private String projectName;
    /**
     * The project Full Name (including folder).
     * from class AbstractScriptableParameter 第 2 父类
     */
    private String projectFullName;

    /**
     * Map with parameters in the UI.
     * Map is not serializable, but LinkedHashMap is. Ignore static analysis errors
     * from class AbstractCascadableParameter 第 3 父类  联动参数
     */
    private Map<Object, Object> parameters = new LinkedHashMap<Object, Object>();
    /**
     * Referenced parameters.联动参数名称
     * from class AbstractCascadableParameter  第 3 父类  联动参数
     */
    private String referencedParameters;

    /**
     * Choice type.
     * from 子类
     * CascadeChoiceParameter       继承 第 3 父类, 联动可选的参数类型
     * DynamicReferenceParameter    继承 第 3 父类,
     * ChoiceParameter              继承 第 2 父类,普通的可选参数类型
     */
    private String choiceType;

    /**
     * Filter flag.
     * from 子类
     * CascadeChoiceParameter       继承 第 3 父类, 联动可选的参数类型
     * ChoiceParameter              继承 第 2 父类,普通的可选参数类型
     */
    private Boolean filterable;

    /**
     * Filter length. Defines a minimum number of characters that must be entered before the filter
     * is activated.
     * CascadeChoiceParameter       继承 第 3 父类, 联动可选的参数类型
     * ChoiceParameter              继承 第 2 父类,普通的可选参数类型
     */
    private int filterLength;

    /**
     * from 子类
     * DynamicReferenceParameter    继承 第 3 父类,
     */
    private Boolean omitValueField;


    /**
     * provider
     *参考extensible_choice_parameter/ExtensibleChoiceParameterDefinition.java
     *
     */
    private ChoiceListProvider choiceListProvider = null;

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
            String name = formData.getString("name");
            String randomName = formData.getString("randomName");
            String description = formData.getString("description");
            ChoiceListProvider choiceListProvider = bindJSONWithDescriptor(req, formData, "choiceListProvider", ChoiceListProvider.class);
            ActiveChoiceParameterDefinition def = new ActiveChoiceParameterDefinition(name, description, randomName, choiceListProvider);
            return def;
        }

        private <T extends Describable<?>> T bindJSONWithDescriptor(StaplerRequest req, JSONObject formData, String fieldName, Class<T> clazz) throws FormException {
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

    @Override
    public String getType() {
        return type;
    }

    public String getRandomName() {
        return randomName;
    }

    /**
     * Get the number of visible items in the select.
     * from class AbstractScriptableParameter 第 2 父类
     * @return the number of choices or, if it is higher than the default, then it returns the default maximum value
     */
    public int getVisibleItemCount() {
        if (visibleItemCount <= 0) {
            visibleItemCount = 1;
        }
        return Math.min(visibleItemCount, DEFAULT_MAX_VISIBLE_ITEM_COUNT);
    }

    public Script getScript() {
        return script;
    }

    public String getProjectName() {
        return projectName;
    }

    public String getProjectFullName() {
        return projectFullName;
    }

    public Map<Object, Object> getParameters() {
        return parameters;
    }

    public String getReferencedParameters() {
        return referencedParameters;
    }

    public String getChoiceType() {
        return choiceType;
    }

    public Boolean getFilterable() {
        return filterable;
    }

    public int getFilterLength() {
        return filterLength;
    }

    public Boolean getOmitValueField() {
        return omitValueField;
    }

    public ChoiceListProvider getChoiceListProvider() {
        return choiceListProvider;
    }

    public ChoiceListProvider getEnabledChoiceListProvider() {
        ChoiceListProvider p = getChoiceListProvider();
        if (p == null) {
            return null;
        }

        // filter providers.
        List<Descriptor<ChoiceListProvider>> testList = DescriptorVisibilityFilter
                .apply(getDescriptor(), Arrays.asList(p.getDescriptor()));
        if (testList.isEmpty()) {
            LOGGER.log(Level.WARNING, "{0} is configured but disabled in the system configuration.", p.getDescriptor().getDisplayName());
            return null;
        }
        return p;
    }

    /**
     * Return choices available for this parameter.
     *
     * TODO from src/main/java/jp/ikedam/jenkins/plugins/extensible_choice_parameter/ExtensibleChoiceParameterDefinition.java
     *暂时不知道是干什么用的
     *
     * @return list of choices. never null.
     */
    public List<String> getChoiceList() {
        ChoiceListProvider provider = getEnabledChoiceListProvider();
        List<String> choiceList = (provider !=  null)?provider.getChoiceList():null;
        return (choiceList !=  null)?choiceList:new ArrayList<String>(0);
    }

    /**
     * Expose choices to REST web API
     *
     * Expose choices to trigger builds from programs,
     * in the same way to built-in choice parameters.
     * Only users with the Item/BUILD permission can access it.
     *
     * TODO from src/main/java/jp/ikedam/jenkins/plugins/extensible_choice_parameter/ExtensibleChoiceParameterDefinition.java
     *暂时不知道是干什么用的
     *
     * @return the list of choices. {@code null} if no Item/Build permission.
     * @since 1.7.0
     */
    @Restricted(DoNotUse.class)
    @Exported(name="choices")
    public List<String> getChoicesForRestApi() {
        StaplerRequest req = Stapler.getCurrentRequest();
        if (req == null) {
            return null;
        }
        Job<?, ?> job = req.findAncestorObject(Job.class);
        if (job == null) {
            return null;
        }
        if (!job.hasPermission(Item.BUILD)) {
            return Collections.emptyList();
        }
        return getChoiceList();
    }


    public String getChoicesAsString() {
        return getChoicesAsString(getParameters());
    }

    public String getChoicesAsString(Map<Object, Object> parameters) {
        final Object value = eval(parameters);
        if (value != null)
            return value.toString();
        return "";
    }

    @DataBoundConstructor
    public ActiveChoiceParameterDefinition(String name, String description, String randomName, ChoiceListProvider choiceListProvider) {
        super(name, description);

        if (StringUtils.isBlank(randomName)) {
            this.randomName = Utils.createRandomParameterName("choice-parameter", "");
        } else {
            this.randomName = randomName;
        }

        this.choiceListProvider = choiceListProvider;

        if (choiceListProvider instanceof ActiveChoiceProvider) {
            ActiveChoiceProvider provider = (ActiveChoiceProvider) choiceListProvider;
            this.type = "PT_ACTIVE";
            this.choiceType = provider.getChoiceType();
            this.script = provider.getScript();
            this.filterable = provider.getFilterable();
            this.filterLength = provider.getFilterLength();
        } else if (choiceListProvider instanceof CascadeChoiceProvider) {
            CascadeChoiceProvider provider = (CascadeChoiceProvider) choiceListProvider;
            this.type = "PT_CASCADE";
            this.choiceType = provider.getChoiceType();
            this.script = provider.getScript();
            this.filterable = provider.getFilterable();
            this.filterLength = provider.getFilterLength();
            this.referencedParameters = provider.getReferencedParameters();
        } else if (choiceListProvider instanceof DynamicReferenceProvider) {
            DynamicReferenceProvider provider = (DynamicReferenceProvider) choiceListProvider;
            this.type = "PT_REFERENCE";
            this.choiceType = provider.getChoiceType();
            this.script = provider.getScript();
            this.referencedParameters = provider.getReferencedParameters();
            this.omitValueField = provider.getOmitValueField();
        }

        final StaplerRequest currentRequest = Stapler.getCurrentRequest();
        String projectName = null;
        String projectFullName = null;
        if (currentRequest != null) {
            final Ancestor ancestor = currentRequest.findAncestor(AbstractItem.class);
            if (ancestor != null) {
                final Object o = ancestor.getObject();
                if (o instanceof AbstractItem) {
                    final AbstractItem parentItem = (AbstractItem) o;
                    projectName = parentItem.getName();
                    projectFullName = parentItem.getFullName();
                }
            }
        }
        this.projectName = projectName;
        this.projectFullName = projectFullName;
    }


    protected ParameterValue createValueCommon(StringParameterValue value) {
        return value;
    }

    private Map<Object, Object> getHelperParameters() {
        // map with parameters
        final Map<Object, Object> helperParameters = new LinkedHashMap<Object, Object>();

        // First, if the project name is set, we then find the project by its name, and inject into the map
        Project<?, ?> project = null;
        if (StringUtils.isNotBlank(this.projectFullName)) {
            // First try full name if exists
            project = Jenkins.get().getItemByFullName(this.projectFullName, Project.class);
        } else if (StringUtils.isNotBlank(this.projectName)) {
            // next we try to get the item given its name, which is more efficient
            project = Utils.getProjectByName(this.projectName);
        }
        // Last chance, if we were unable to get project from name and full name, try uuid
        if (project == null) {
            // otherwise, in case we don't have the item name, we iterate looking for a job that uses this UUID
            project = Utils.findProjectByParameterUUID(this.getRandomName());
        }
        if (project != null) {
            helperParameters.put(JENKINS_PROJECT_VARIABLE_NAME, project);
            AbstractBuild<?, ?> build = project.getLastBuild();
            if (build != null && build.getHasArtifacts()) {
                helperParameters.put(JENKINS_BUILD_VARIABLE_NAME, build);
            }
        }

        // Here we set the parameter name
        helperParameters.put(JENKINS_PARAMETER_VARIABLE_NAME, this);

        // Here we inject the global node properties
        final Map<String, Object> globalNodeProperties = Utils.getGlobalNodeProperties();
        helperParameters.putAll(globalNodeProperties);
        return helperParameters;
    }

    private Object eval(Map<Object, Object> parameters) {
        try {
            Map<Object, Object> scriptParameters = getHelperParameters();
            scriptParameters.putAll(parameters);
            final ScriptCallback<Exception> callback = new ScriptCallback(getName(), script, scriptParameters);
            return callback.call();
        } catch (Exception e) {
            return Collections.emptyMap();
        }
    }

    public Map<Object, Object> getChoices(Map<Object, Object> parameters) {
        final Object value = eval(parameters);
        if (value instanceof Map) {
            Map<Object, Object> map = (Map<Object, Object>) value;
            visibleItemCount = map.size();
            return map;
        }
        if (value instanceof List) {
            final Map<Object, Object> map = new LinkedHashMap<Object, Object>();
            for (Object o : (List<Object>) value) {
                map.put(o, o);
            }
            visibleItemCount = map.size();
            return map;
        }

        return Collections.emptyMap();
    }

    public Map<Object, Object> getChoices() {
        Map<Object, Object> map = Collections.emptyMap();
        Map<Object, Object> choices = getChoices(map);
        visibleItemCount = choices.size();

        return choices;
    }

    @JavaScriptMethod
    public void doUpdate(String parameters) {
        Map<Object, Object> parameters1 = getParameters();
        final String[] params = parameters.split(SEPARATOR);
        for (String param : params) {
            final String[] nameValue = param.split(EQUALS);
            if (nameValue.length == 1) {
                final String name = nameValue[0].trim();
                if (name.length() > 0)
                    getParameters().put(name, "");
            } else if (nameValue.length == 2) {
                final String name = nameValue[0];
                final String value = nameValue[1];
                getParameters().put(name, value);
            } else if (nameValue.length > 2) {
                // TBD: we can eliminate this branch by splitting only on the first EQUALS
                final String name = nameValue[0];
                final StringBuilder sb = new StringBuilder();
                // rebuild the rest of the string, skipping the first value
                for (int i = 1; i < nameValue.length; ++i) {
                    sb.append(nameValue[i]);
                    if (i+1 < nameValue.length) {
                        sb.append(EQUALS);
                    }
                }
                final String value = sb.toString();
                getParameters().put(name, value);
            }
        }
        throw new JsonHttpResponse(null);
    }

    @JavaScriptMethod
    public List<Object> getChoicesForUI() {
        Map<Object, Object> mapResult = getChoices(getParameters());
        return Arrays.<Object>asList(mapResult.values(), mapResult.keySet());
    }

    public String[] getReferencedParametersAsArray() {
        String referencedParameters = this.getReferencedParameters();
        if (StringUtils.isNotBlank(referencedParameters)) {
            String[] array = referencedParameters.split(",");
            List<String> list = new ArrayList<String>();
            for (String value : array) {
                value = value.trim();
                if (StringUtils.isNotBlank(value)) {
                    list.add(value);
                }
            }
            return list.toArray(new String[0]);
        }
        return new String[]{};
    }

    @Override
    public ParameterValue createValue(StaplerRequest request, JSONObject json) {
        if (json.containsKey("file")) {
            // copied from FileParameterDefinition
            FileItem src;
            try {
                src = request.getFileItem(json.getString("file"));
            } catch (ServletException e) {
                return null;
            } catch (IOException e) {
                return null;
            }
            if (src == null) {
                // the requested file parameter wasn't uploaded
                return null;
            }
            FileParameterValue p = new FileParameterValue(getName(), src);
            p.setDescription(getDescription());
            return p;
        } else {
            final JSONObject parameterJsonModel = new JSONObject(false);
            final Object value = json.get("value");
            final Object name = json.get("name");
            final String valueAsText;

            if (JSONUtils.isArray(value)) {
                valueAsText = ((JSONArray) value).join(",", true);
            } else {
                valueAsText = (value == null) ? "" : String.valueOf(value);
            }

            parameterJsonModel.put("name", name);
            parameterJsonModel.put("value", valueAsText);

            StringParameterValue parameterValue = request.bindJSON(StringParameterValue.class, parameterJsonModel);
            parameterValue.setDescription(getDescription());
            return parameterValue;
        }
    }

    @Override
    public ParameterValue createValue(String value) {
        final String description = getDescription();
        final String name = getName();
        final StringParameterValue parameterValue = new StringParameterValue(name, value, description);
        return parameterValue;
    }

    @Override
    public ParameterValue getDefaultParameterValue() {
        Object firstElement = "";
        final Map<Object, Object> choices = getChoices(getParameters());
        if (choices != null && !choices.isEmpty()) {
            firstElement = choices.entrySet().iterator().next().getValue();
        }
        final String name = getName();
        final String value = ObjectUtils.toString(firstElement, ""); // Jenkins doesn't like null parameter values
        final StringParameterValue stringParameterValue = new StringParameterValue(name, value);
        return stringParameterValue;
    }
}
