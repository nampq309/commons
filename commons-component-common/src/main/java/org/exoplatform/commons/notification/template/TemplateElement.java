/*
 * Copyright (C) 2003-2013 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
* modify it under the terms of the GNU Affero General Public License
* as published by the Free Software Foundation; either version 3
* of the License, or (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.commons.notification.template;

import java.util.HashMap;
import java.util.Map;

public class TemplateElement {
  private String                language;

  private String                template;

  private String                templateText;

  private String                resouceLocal;

  private TemplateResouceBundle resouceBundle;

  private TemplateContext       context;

  private Map<String, Object>   valueables = new HashMap<String, Object>();
  private Map<String, String>   resouceBunldMappingKey = new HashMap<String, String>();

  public TemplateElement(String language, String resouceLocal) {
    this.language = language;
    this.resouceLocal = resouceLocal;
  }

  public TemplateContext accept(TemplateContext context) {
    this.context = context;
    this.context.put("_ctx", this);
    this.context.putAll(valueables);
    valueables.clear();
    return this.context;
  }

  /**
   * @return the template
   */
  public String getTemplate() {
    return template;
  }

  /**
   * @param content the content to set
   */
  public void setTemplate(String template) {
    this.template = template;
  }

  /**
   * @return the templateText
   */
  public String getTemplateText() {
    return templateText;
  }

  /**
   * @param templateText the templateText to set
   */
  public void setTemplateText(String templateText) {
    this.templateText = templateText;
  }

  /**
   * @return the resouceLocal
   */
  public String getResouceLocal() {
    return resouceLocal;
  }

  /**
   * @param resouceLocal the resouceLocal to set
   */
  public void setResouceLocal(String resouceLocal) {
    this.resouceLocal = resouceLocal;
  }

  public TemplateContext getContext() {
    return context;
  }

  /**
   * @return the language
   */
  public String getLanguage() {
    return language;
  }

  /**
   * @param language the language to set
   */
  public void setLanguage(String language) {
    this.language = language;
  }

  /**
   * @return the resouceBundle
   */
  public TemplateResouceBundle getResouceBundle() {
    return resouceBundle;
  }

  /**
   * @param resouceBundle the resouceBundle to set
   */
  public void setResouceBundle(TemplateResouceBundle resouceBundle) {
    this.resouceBundle = resouceBundle;
  }

  /**
   * @return the resouceBunldMappingKey
   */
  public Map<String, String> getResouceBunldMappingKey() {
    return resouceBunldMappingKey;
  }

  /**
   * @param resouceBunldMappingKey the resouceBunldMappingKey to set
   */
  public void setResouceBunldMappingKey(Map<String, String> resouceBunldMappingKey) {
    this.resouceBunldMappingKey = resouceBunldMappingKey;
  }

  public void addResouceBunldMappingKey(String key, String value) {
    this.resouceBunldMappingKey.put(key, value);
  }
  
  /**
   * @return the valueables
   */
  public Map<String, Object> getValueables() {
    return valueables;
  }

  /**
   * @param valueables2 the valueables to set
   */
  public TemplateElement putAllValueables(Map<String, String> valueables) {
    this.valueables.putAll(valueables);
    return this;
  }

  private String getBundleKey(String key) {
    String value = resouceBunldMappingKey.get(key);
    if (value != null) {
      return value;
    }
    return key;
  }

  public String appRes(String key) {
    return resouceBundle.appRes(getBundleKey(key));
  }

  public String appRes(String key, String... strs) {
    return resouceBundle.appRes(getBundleKey(key), strs);
  }

  public TemplateElement include(String elementLocal) throws Exception {

    TemplateElement element = new TemplateElement(language, elementLocal);
    element.setResouceBundle(resouceBundle);
    //
    context.visit(element);
    return this;
  }

}
